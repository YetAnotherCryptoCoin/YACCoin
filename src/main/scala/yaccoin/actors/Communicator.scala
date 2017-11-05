package yaccoin.actors

import akka.actor.Terminated
import yaccoin.actors.Protocol._

class Communicator(initState: CommunicatorState) extends AbstractActor[CommunicatorState](initState) {

  override def active(state: CommunicatorState): Receive = {

    case DiscoverTransactor =>
      if (!state.transactors.par.contains(sender)) {
        log.info(s"New transactor $sender recognised.")

        context.watch(sender)
        context.become(active((state.miners, state.transactors + sender)))
      }

    case DiscoverMiner =>
      if (!state.miners.par.contains(sender)) {
        log.info(s"New miner $sender recognised.")

        context.watch(sender)
        context.become(active((state.miners + sender, state.transactors)))
      }

    case Terminated(actor) =>
      if (state.transactors.par.contains(actor)) {
        log.info(s"Transactor $actor died.")

        context.become(active((state.miners, state.transactors - actor)))
      }
      else if (state.miners.par.contains(actor)) {
        log.info(s"Miner $actor died.")

        context.become(active((state.miners - actor, state.transactors)))
      }

    case x: NewTransaction =>
      log.info(s"Sending new transaction ${x.signedTxn.txn.txnId} to everyone.")

      state.miners.par.foreach(_ ! x)
      localMiner ! x

    case x: NewBlock =>
      log.info(s"Sending new block ${x.block.blockId} to everyone.")

      state.transactors.par.foreach(_ ! x)
      localTransactor ! x

  }

}
