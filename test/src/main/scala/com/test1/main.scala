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


//case class Person(name:String, age: Int)
//case class UserAdded(person:Person)
//case class DisplayUser(name:String, age:Int, id:String,timestamp:Long)

case class Calling(UserId:String, Corporname:String, Price:Int)
case class Buy(Calling:Calling)
case class Sell(Calling:Calling)
case class Complete(UserId:String, Result:String)

trait PersonJsonProtocol extends DefaultJsonProtocol {
  implicit val CallingFormat = jsonFormat3(Calling)
  //  implicit val userAddedFormat = jsonFormat2(UserAdded)
  implicit val CompleteFormat = jsonFormat2(Complete)
}


object AkkaHttpJson extends App with PersonJsonProtocol with SprayJsonSupport with Directives{
  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  //  implicit val system = ActorSystem(Behaviors.empty, "AkkaHttpJson")
  implicit val system = ActorSystem()
  val otheractor = system.actorOf(CorporManager.props(Vector("Kakao","Samsung","Naver")),"CorporManager")

  val route: Route = post {
    pathPrefix("calling" / "buy") {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      //api/user 에 post로 정보가 들어온다면 userAdded이벤트를 otherActor에게 보내고 otheractor는 받고
      //display이벤트를 다시 돌려준다 그럼 그 정보를 반환한다
      entity(as[Calling]) { calling: Calling =>
        println(s"user: ${calling.UserId} cor: ${calling.Corporname} price: ${calling.Price}")
        onSuccess(otheractor.ask(Buy(calling))(5 seconds).mapTo[Complete]) {
          case Complete(userid,result) => println(result);complete(Complete(userid,result))
          //        case _ => complete("something wrong!!!")
        }
      }
    }
  } ~ post  {
    path("calling" / "sell") {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      //api/user 에 post로 정보가 들어온다면 userAdded이벤트를 otherActor에게 보내고 otheractor는 받고
      //display이벤트를 다시 돌려준다 그럼 그 정보를 반환한다
      entity(as[Calling]) { calling: Calling =>
        println(s"user: ${calling.UserId} cor: ${calling.Corporname} price: ${calling.Price}")
        onSuccess(otheractor.ask(Sell(calling))(5 seconds).mapTo[Complete]) {
          case Complete(userid,result) => println(result);complete(Complete(userid,result))
          //        case _ => complete("something wrong!!!")
        }
      }
    }
  }
  //  def main(args: Array[String]): Unit = {
  Http().newServerAt("localhost", 8001).bind(route)
  //  }
}