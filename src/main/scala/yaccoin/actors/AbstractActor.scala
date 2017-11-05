package yaccoin.actors

import akka.actor.{Actor, ActorLogging, ActorSelection}

/** AbstractActor for encapsulation some common logic across all YACC Actors.
  *
  * @param initialState Initial state.
  * @tparam S Type of State of this Actor.
  */
abstract class AbstractActor[S](initialState: S) extends Actor with ActorLogging {

  /** Local actor instances. */
  protected def localCommunicator: ActorSelection = context.actorSelection("../localCommunicator")
  protected def localMiner: ActorSelection = context.actorSelection("../localMiner")
  protected def localTransactor: ActorSelection = context.actorSelection("../localTransactor")

  override def receive: Receive = active(initialState)

  /** Implement message and state transition logic. */
  def active(state: S): Receive

}
