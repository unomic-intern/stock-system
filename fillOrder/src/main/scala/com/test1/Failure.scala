package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import spray.json.DefaultJsonProtocol
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

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

class TradingResponse extends Actor with DefaultJsonProtocol {
  implicit val callingFormat = jsonFormat3(Calling)
  implicit val materializer = ActorMaterializer()
  import AkkaHttpJson.system.dispatcher

  def sendCalling(calling:Calling) = {

    val source = """
        {
            "UserId" : """" + calling.UserId + """",
            "Corporname" : """" + calling.Corporname + """",
            "Price" : """" + calling.Price + """"
        }
        """.stripMargin

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:5000/calling/buy",
      //이거 uri 데이지가 endpoint 파시고 그 uri 넣으면 됩니다
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture: Future[HttpResponse] = Http()(AkkaHttpJson.system).singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)

  }

  def receive:Receive={
    case Buy(calling) =>
      sendCalling(calling).foreach(println)
    case Sell(calling)=>
      sendCalling(calling).foreach(println)
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