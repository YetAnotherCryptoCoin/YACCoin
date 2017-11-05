package yaccoin.actors

import yaccoin.actors.Protocol._
import yaccoin.block.{Block, MemPool}
import yaccoin.utils.MiningUtils
import yaccoin.utils.future.CancellableFuture

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Miner(initMemPool: MemPool, mineInterval: FiniteDuration)
  extends AbstractActor[MinerState]((initMemPool, CancellableFuture.successful(Unit))) {

  override def active(state: MinerState): Receive = {

    case NewTransaction(signedTxn) => state.memPool.addTransaction(signedTxn) match {
      case Success(x) =>
        log.info(s"Adding new transaction ${signedTxn.txn.txnId} to MemPool.")

        context.become(active((x, state.forge)))

      case Failure(ex) =>
        log.error(ex.getMessage)
    }

    case ConfirmedTransactions(txns) =>
      context.become(active((state.memPool.removeTransactions(txns), state.forge)))

    case BeginMining(prevHash) => if (state.forge.future.isCompleted) {
      log.info(s"Beginning the mining of a new block.")

      implicit val ctx: ExecutionContext = context.system.dispatchers.lookup("mining-dispatcher")

      context.become(active(state.memPool, CancellableFuture {
        /* Start mining the block. */
        val block = MiningUtils.mineBlock(Block(state.memPool.transactions, prevHash))

        /* Send newly mined block to everyone. */
        localCommunicator ! NewBlock(block)
      }))
    } else {
      log.info(s"Already busy mining a block.")
    }

    case StopMining =>
      log.info("Force stopping mining operation.")

      if (!state.forge.future.isCompleted) {
        state.forge.cancel()
      }
      context.become(active((state.memPool, CancellableFuture.successful(Unit))))

  }

}
