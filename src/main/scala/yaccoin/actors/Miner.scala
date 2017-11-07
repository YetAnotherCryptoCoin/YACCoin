package yaccoin.actors

import akka.actor.{ActorSelection, Timers}
import akka.pattern.ask
import akka.util.Timeout
import yaccoin.actors.Protocol._
import yaccoin.block.{Block, MemPool}
import yaccoin.utils.MiningUtils
import yaccoin.utils.future.CancellableFuture

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/** Miner will mine blocks, and send them to all transactors to update block chain.
  *
  * For their state, see [[MinerState]].
  *
  * @param initMemPool Initial mem pool.
  */
class Miner(initMemPool: MemPool)
  extends AbstractActor[MinerState]((initMemPool, CancellableFuture.successful(Unit))) with Timers {

  /* Schedule periodic mining messages. */
  timers.startPeriodicTimer("mining_timer", BeginMining, MiningUtils.mineInterval)

  override def active(state: MinerState): Receive = {

    /* New transaction arrived. Add it to MemPool. */
    case NewTransaction(signedTxn) => state.memPool.addTransaction(signedTxn) match {
      case Success(x) =>
        log.info(s"Adding new transaction ${signedTxn.txn.txnId} to MemPool.")

        context become active((x, state.forge))

      case Failure(ex) =>
        log.error(ex.getMessage)
    }

    /* Some transactions were confirmed. Remove them from MemPool. */
    case ConfirmedTransactions(txns) => if (txns.intersect(state.memPool.transactions).nonEmpty) {
      state.forge.cancel()
      context become active((state.memPool.removeTransactions(txns), CancellableFuture.successful(Unit)))
    }

    /* Start a mining operation. */
    case BeginMining => if (state.memPool.transactions.nonEmpty) {
      state.forge.cancel()

      log.info(s"Beginning the mining of a new block.")

      implicit val ctx: ExecutionContext = context.system.dispatchers.lookup("mining-dispatcher")
      implicit val timeout: Timeout = Timeout(MiningUtils.mineInterval)

      context become active((state.memPool, CancellableFuture {
        localTransactor ? GetHash map {
          case LatestBlockHash(prevHash) =>
            /* Start mining the block. */
            val block = MiningUtils.mineBlock(Block(state.memPool.transactions, prevHash))

            /* Send newly mined block to everyone. */
            localCommunicator ! NewBlock(block)
        }
      }))
    }

    /* Stop your mining. */
    case StopMining =>
      log.info("Force stopping mining operation.")

      state.forge.cancel()
      context become active((state.memPool, CancellableFuture.successful(Unit)))

  }

}