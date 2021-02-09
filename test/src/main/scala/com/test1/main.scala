package com.test1

//import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import scala.language.postfixOps

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._


case class Person(name:String, age: Int)
case class UserAdded(person:Person)
case class DisplayUser(name:String, age:Int, id:String,timestamp:Long)

trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat2(Person)
//  implicit val userAddedFormat = jsonFormat2(UserAdded)
  implicit val DisplayUserFormat = jsonFormat4(DisplayUser)
}


object AkkaHttpJson extends App with PersonJsonProtocol with SprayJsonSupport with Directives{
  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

//  implicit val system = ActorSystem(Behaviors.empty, "AkkaHttpJson")
  implicit val system = ActorSystem()
  val otheractor = system.actorOf(otherActor.props,"otherActor")

  val route: Route = (path("api"/"user") & post){
    import akka.actor.typed.scaladsl.AskPattern._
    import akka.util.Timeout
    //api/user 에 post로 정보가 들어온다면 userAdded이벤트를 otherActor에게 보내고 otheractor는 받고
    //display이벤트를 다시 돌려준다 그럼 그 정보를 반환한다
    entity(as[Person]) { person:Person =>
      onSuccess(otheractor.ask(UserAdded(person))(5 seconds).mapTo[DisplayUser]) {
        case DisplayUser(name,age,id,timestamp) => complete(DisplayUser(name,age,id,timestamp))
//        case _ => complete("something wrong!!!")
      }

    }
  }
//  def main(args: Array[String]): Unit = {
  Http().newServerAt("localhost", 8001).bind(route)
//  }
}

//post {
//// POST /events/:event
//  entity(as[EventDescription]) { ed =>
//    onSuccess(boxOffice.ask(CreateEvent(event, nrOfTickets)).mapTo[EventResponse]) {
//    case BoxOffice.EventCreated(event) => complete(Created, event)
//    case BoxOffice.EventExists =>
//      val err = Error(s"$event event exists already.")
//      complete(BadRequest, err)
//    }
//  }
//}

//post {
//  // POST /events/:event
//  entity(as[EventDescription]) { ed =>
//    onSuccess(createEvent(event, ed.tickets)) {
//      case BoxOffice.EventCreated(event) => complete(Created, event)
//      case BoxOffice.EventExists =>
//        val err = Error(s"$event event exists already.")
//        complete(BadRequest, err)
//    }
//  }
//}
//def createEvent(event: String, nrOfTickets: Int) =
//boxOffice.ask(CreateEvent(event, nrOfTickets))
//.mapTo[EventResponse]





//package com.test1
//
//import akka.actor.typed.ActorSystem
//import akka.actor.typed.scaladsl.Behaviors
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Directives
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import spray.json._
//import java.util.UUID
//
//
//case class Person(name:String, age: Int)
//case class UserAdded(id:String, timestamp: Long)
//
//trait PersonJsonProtocol extends DefaultJsonProtocol {
//  implicit val personFormat = jsonFormat2(Person)
//  implicit val userAddedFormat = jsonFormat2(UserAdded)
//}
//
//
//object AkkaHttpJson extends PersonJsonProtocol with SprayJsonSupport with Directives{
//
//  implicit val system = ActorSystem(Behaviors.empty, "AkkaHttpJson")
//
//  val route: Route = (path("api"/"user") & post){
//    entity(as[Person]) { person:Person =>
//      complete(UserAdded(UUID.randomUUID().toString, System.currentTimeMillis()))
//    }
//  }
//
//  def main(args: Array[String]):Unit={
//    Http().newServerAt("localhost",8001).bind(route)
//  }
//
//}