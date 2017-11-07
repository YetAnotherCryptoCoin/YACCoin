package yaccoin.actors

import akka.actor.Terminated
import akka.util.Timeout
import yaccoin.actors.Protocol._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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

    /* Bootstrap. */
    case BootStrap(remotes) =>
      log.info(s"Bootstrapping for addresses: $remotes.")

      implicit val timeout: Timeout = Timeout(1.minute)
      implicit val ctx: ExecutionContext = context.system.dispatchers.lookup("resolve-dispatcher")

      remotes
        .map(_.toString + "/user/localCommunicator")
        .foreach(x => context.actorSelection(x) ! BootStrap(List(self.path.address)))

      remotes
        .map(_.toString + "/user/localMiner")
        .map(x => context.actorSelection(x).resolveOne)
        .foreach(_.map(ref => self ! DiscoverMiner(ref)))

      remotes
        .map(_.toString + "/user/localTransactor")
        .map(x => context.actorSelection(x).resolveOne)
        .foreach(_.map(ref => self ! DiscoverTransactor(ref)))

    /* A new transactor appeared. */
    case DiscoverTransactor(actor) =>
      if (!state.transactors.par.contains(actor)) {
        log.info(s"New transactor $actor recognised.")

        context.watch(actor)
        context become active((state.miners, state.transactors + actor))
      }

    /* A new miner appeared. */
    case DiscoverMiner(actor) =>
      if (!state.miners.par.contains(actor)) {
        log.info(s"New miner $actor recognised.")

        context.watch(actor)
        context become active((state.miners + actor, state.transactors))
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
