package spray.examples

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.actor.Actor
import akka.actor.ActorRef

object Main extends App {

  implicit val system = ActorSystem()

  // the handler actor replies to incoming HttpRequests
  val handler : ActorRef = system.actorOf(Props[MyServiceActor], name = "handler")

  IO(Http) ! Http.Bind(handler, "localhost", 8080, 100, Nil, None)
}