package com.newTrading

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpMethods, HttpRequest}

import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshal

import akka.stream.{SystemMaterializer}

import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

object EndPoint {
  def props = Props(new EndPoint)
}
class EndPoint extends Actor with JsonProtocol with SprayJsonSupport{
  //  import AkkaHttp.system
  //  import AkkaHttp.executionContext
  //  import AkkaHttp.system.log
  import AkkaHttp.akkahttpsystem
  import AkkaHttp.log
  implicit val executionContext = akkahttpsystem.dispatcher
  implicit val mat = SystemMaterializer(akkahttpsystem).materializer
  //  implicit val CompleteFormat = jsonFormat3(Complete)

  def sendmessage(newOrder: Calling,uri :String, Hmethod: HttpMethod) = {
    val source = """
        {
            "CallingId" : """ + newOrder.CallingId + """,
            "UserId" : """" + newOrder.UserId + """",
            "Corporname" : """" + newOrder.Corporname + """",
            "Price" : """ + newOrder.Price + """
        }
        """.stripMargin

    val request = HttpRequest(
      method = Hmethod,
      //uri = Uri,
      uri = s"http://192.168.0.78:8001/${uri}",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture = Http().singleRequest(request).flatMap({response =>
      Unmarshal(response.entity).to[Complete]// (Unmarshaller[ResponseEntity,Complete],AkkaHttp.executionContext,mat)
    })
    responseFuture
  }

  def resSender(dest:ActorRef,res:Future[Complete])={
    res.onComplete{
      case Success(response) => {
        log.info(s"resSender : ${response}")
        response match {
          case Complete(_,_,result) if result.contains("Success") => context.parent ! Response(response,dest);log.info("res match success");
          case _ => dest ! response;log.info("res match yet")
        }
      }
      case Failure(_) =>{
        sys.error("something wrong in resSender")
      }
    }
  }

  def receive :Receive={
    case buy :Buy => {
      val res = sendmessage(buy.Calling,"calling/buy",HttpMethods.POST)
      resSender(sender(), res)
      log.info("buy calling /endpoint section")
    }
    case sell :Sell => {
      val res = sendmessage(sell.Calling,"calling/sell",HttpMethods.POST)
      resSender(sender(),res)
      log.info("sell calling /endpoint section")
    }
    case cancel :Cancel[Buy] => {
      val res = sendmessage(cancel.Action.Calling,"cancel/buy",HttpMethods.DELETE)
      resSender(sender(),res)
      log.info(s"buy cancel /endpoint section\n${sender()}")
    }
    case cancel:Cancel[Sell] => {
      val res = sendmessage(cancel.Action.Calling,"cancel/sell",HttpMethods.DELETE)
      resSender(sender(),res)
      log.info("sell cancel /endpoint section")
    }
    case _ => println("In Endpoint fail")
  }

}
//      val resFuture = Unmarshal(sendmessage(sell.Calling,"calling/buy",HttpMethods.POST)).to[Complete]
//      val res = Await.result(resFuture, 1.second)
