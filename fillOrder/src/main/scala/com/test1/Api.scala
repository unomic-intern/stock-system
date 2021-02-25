package com.test1

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.{ExecutionContext}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

case class Calling(CallingId: Long, UserId:String, Corporname:String, Price:Int)
case class Buy(Calling:Calling)
case class Sell(Calling:Calling)
case class Cancel[A](Action:A)
case class Complete(CallingId:Long,UserId:String, Result:String)

class OrderServiceApi (system: ActorSystem, timeout: Timeout, val otheractor: ActorRef)
  extends Api{
  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher
}

trait Api {
  val otheractor: ActorRef
  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  implicit val CallingFormat = jsonFormat4(Calling)
  implicit val CompleteFormat = jsonFormat3(Complete)

  val route: Route = post {
    pathPrefix("calling" / "buy") {
      //api/user 에 post로 정보가 들어온다면 userAdded이벤트를 otherActor에게 보내고 otheractor는 받고
      //display이벤트를 다시 돌려준다 그럼 그 정보를 반환한다
      entity(as[Calling]) { calling: Calling =>
        println(s"user: ${calling.UserId} cor: ${calling.Corporname} price: ${calling.Price}")
        onSuccess(otheractor.ask(Buy(calling))(20 seconds).mapTo[Complete]) {
          case result:Complete => println(result);complete(result)
          //        case _ => complete("something wrong!!!")
        }
      }
    }
  } ~ post  {
    pathPrefix("calling" / "sell") {
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      //api/user 에 post로 정보가 들어온다면 userAdded이벤트를 otherActor에게 보내고 otheractor는 받고
      //display이벤트를 다시 돌려준다 그럼 그 정보를 반환한다
      entity(as[Calling]) { calling: Calling =>
        println(s"user: ${calling.UserId} cor: ${calling.Corporname} price: ${calling.Price}")
        onSuccess(otheractor.ask(Sell(calling))(5 seconds).mapTo[Complete]) {
          case result:Complete => println(result);complete(result)
          //        case _ => complete("something wrong!!!")
        }
      }
    }
  } ~ delete {
    pathPrefix("cancel"/"buy"){
      entity(as[Calling]){ calling: Calling =>{
        onSuccess(otheractor.ask(Cancel[Buy](Buy(calling)))(5 seconds).mapTo[Complete]){
          case result:Complete => println(result);complete(result)
        }
      }
      }
    }
  } ~ delete {
    path("cancel"/"sell"){
      entity(as[Calling]){ calling: Calling =>{
        onSuccess(otheractor.ask(Cancel[Sell](Sell(calling)))(5 seconds).mapTo[Complete]){
          case result:Complete => println(result);complete(result)
        }
      }
      }
    }
  }
  //Http().newServerAt("192.168.0.78", 8001).bind(route)
  //Http().newServerAt("172.17.0.3", 8001).bind(route)
}
