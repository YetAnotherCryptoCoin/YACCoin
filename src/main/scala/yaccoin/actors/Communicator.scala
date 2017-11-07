package yaccoin.actors

import akka.actor.Terminated
import yaccoin.actors.Protocol._

/** Communicator is responsible for keeping
  * track of all the miners and transactors in the
  * system and relaying them the goodies.
  *
  * For their state see [[CommunicatorState]].
  *
  * @param initState Initial state of Communicator.
  */
class Communicator(initState: CommunicatorState) extends AbstractActor[CommunicatorState](initState) {

  override def active(state: CommunicatorState): Receive = {

    /* A new transactor appeared. */
    case DiscoverTransactor =>
      if (!state.transactors.par.contains(sender)) {
        log.info(s"New transactor $sender recognised.")

        context.watch(sender)
        context become active((state.miners, state.transactors + sender))
      }

    /* A new miner appeared. */
    case DiscoverMiner =>
      if (!state.miners.par.contains(sender)) {
        log.info(s"New miner $sender recognised.")

        context.watch(sender)
        context become active((state.miners + sender, state.transactors))
      }

    /* A transactor/miner died. */
    case Terminated(actor) =>
      if (state.transactors.par.contains(actor)) {
        log.info(s"Transactor $actor died.")

        context become active((state.miners, state.transactors - actor))
      }
      else if (state.miners.par.contains(actor)) {
        log.info(s"Miner $actor died.")

        context become active((state.miners - actor, state.transactors))
      }

    /* The local transactor sent me a transaction. Send it to all miners. */
    case x: NewTransaction =>
      log.info(s"Sending new transaction ${x.signedTxn.txn.txnId} to everyone.")

      state.miners.par.foreach(_ ! x)
      localMiner ! x

    /* The local miner sent me a block. Send it to all transactors. */
    case x: NewBlock =>
      log.info(s"Sending new block ${x.block.blockId} to everyone.")

      state.transactors.par.foreach(_ ! x)
      localTransactor ! x

  }

}
