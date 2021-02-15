package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Panic{
  def props:Props = Props(new Panic)
}
//object Match{
//  def props:Props = Props(new Match)
//}
object TradingResponse{
  def props:Props = Props(new TradingResponse)
}

class Panic extends Actor with ActorLogging {
  def receive:Receive={
    case Buy(calling) => {
      sender() ! Complete(calling.UserId,s"Panic ${calling.Corporname} Buy")
    }
    case Sell(calling) =>{
      sender() ! Complete(calling.UserId,s"Panic ${calling.Corporname} Sell")
    }
  }
}

class TradingResponse extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  implicit val executionContext = AkkaHttpJson.system.dispatcher

  //  override def preStart() = {
  //    http.singleRequest(HttpRequest(uri = "https://thecocktaildb.com/api/json/v1/1/search.php?f=2"))
  //      .pipeTo(self)
  //  }

  def receive:Receive={
    case Buy(calling) =>
      Http(AkkaHttpJson.system).singleRequest(HttpRequest(uri = "https://thecocktaildb.com/api/json/v1/1/search.php?f=2")).onComplete{
        case Success(res) => println(res)
        case Failure(_)   => sys.error("something wrong")
      }(executionContext)
    case Sell(calling)=>
      Http(AkkaHttpJson.system).singleRequest(HttpRequest(uri = "https://thecocktaildb.com/api/json/v1/1/search.php?f=4")).onComplete{
        case Success(res) => println(res)
        case Failure(_)   => sys.error("something wrong")
      }(executionContext)
  }
}

//class TradingResponse extends Actor with ActorLogging {
//  import akka.pattern.pipe
//  import context.dispatcher
//
//  implicit val system = context.system
//  val http = Http(system)
//  implicit val materializer = ActorMaterializer()
//
//  override def preStart() = {
//    http.singleRequest(HttpRequest(uri = "https://thecocktaildb.com/api/json/v1/1/search.php?f=2"))
//      .pipeTo(self)
//  }
//
//  def receive:Receive={
//    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
//      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
//        log.info("Got response, body: " + body.utf8String)
//      }
//    case resp @ HttpResponse(code, _, _, _) =>
//      log.info("Request failed, response code: " + code)
//      resp.discardEntityBytes()
//  }
//}


//package com.test1
//
//import akka.actor.{Actor, ActorLogging, ActorRef, Props}
//
//object Failure{
//  def props:Props = Props(new Failure)
//}
//
//class Failure extends Actor with ActorLogging {
//
//  def receive:Receive={
//    case Buy(calling) => {
//      println("I'm buying fail")
//    }
//    case Sell(calling) =>{
//      println("I'm selling fail")
//    }
//  }
//
//}