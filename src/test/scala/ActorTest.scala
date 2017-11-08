import akka.actor.{Actor, ActorSystem, Props}

object ActorTest extends App {

  class Hello extends Actor {

    override def receive: Receive = {
      case "hi" => println(self.path.)
    }

  }

  val system = ActorSystem("AS")

  val h = system.actorOf(Props[Hello])

  h ! "hi"

}
