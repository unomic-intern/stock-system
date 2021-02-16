package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import spray.json.DefaultJsonProtocol._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.pattern.{ask, pipe}
import spray.json._

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
      sender() ! Complete(calling.CallingId,calling.UserId,s"Panic ${calling.Corporname} Buy")
    }
    case Sell(calling) =>{
      sender() ! Complete(calling.CallingId,calling.UserId,s"Panic ${calling.Corporname} Sell")
    }
  }
}

class TradingResponse extends Actor with DefaultJsonProtocol {
  implicit val callingFormat = jsonFormat4(Calling)
  implicit val materializer = ActorMaterializer()
  import AkkaHttpJson.system.dispatcher

  def sendCalling(calling:Calling,uri:String) = {

    val source = """
        {
            "CallingId" : """ + calling.CallingId + """,
            "UserId" : """" + calling.UserId + """",
            "Corporname" : """" + calling.Corporname + """",
            "Price" : """ + calling.Price + """
        }
        """.stripMargin

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"http://192.168.0.83:5000/response/${uri}",
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
      sendCalling(calling,"buy").foreach(println)
    case Sell(calling)=>
      sendCalling(calling,"sell").foreach(println)
  }
}