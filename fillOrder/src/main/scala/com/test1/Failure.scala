package com.test1

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import scala.concurrent.duration._
import scala.concurrent.Future
import spray.json._

object Panic{
  def props:Props = Props(new Panic)
}

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
  //import AkkaHttpJson.system.dispatcher
  //import AkkaHttpJson.log

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
      //uri = s"http://192.168.0.83:5000/response/${uri}",
      uri = s"http://192.168.0.83:8002/response/${uri}",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture: Future[HttpResponse] = Http()(context.system).singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))(context.system.dispatcher)
    println(entityFuture.map(entity => entity.data.utf8String)(context.system.dispatcher))

  }

  def receive:Receive={
    case Buy(calling) =>
      sendCalling(calling,"buy")
    case Sell(calling)=>
      sendCalling(calling,"sell")
  }
}