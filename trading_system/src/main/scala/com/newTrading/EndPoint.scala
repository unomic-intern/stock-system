package com.newTrading

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpMethods, HttpRequest}

import scala.concurrent.{Await, Future}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.SystemMaterializer

import scala.util.{Failure, Success}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object EndPoint {
  def props = Props(new EndPoint)
}
class EndPoint extends Actor with JsonProtocol with SprayJsonSupport{
  implicit val executionContext = context.system.dispatcher
  implicit val mat = SystemMaterializer(context.system).materializer
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
      //uri = s"http://192.168.0.78:8001/${uri}",
      uri = s"http://192.168.0.83:8001/${uri}",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture = Http(context.system).singleRequest(request).flatMap({response =>
      Unmarshal(response.entity).to[Complete]// (Unmarshaller[ResponseEntity,Complete],AkkaHttp.executionContext,mat)
    })
    Await.result(responseFuture, 10 second)    //시스템이 동작될 때 너무 빨라서 Future(<not completed>)가 출력되기 때문에 일정 시간 이후에 completed된 결과를 보기 위함
    responseFuture
  }

  def resSender(dest:ActorRef,res:Future[Complete])={
    res.onComplete{
      case Success(response) => {
        context.system.log.info(s"resSender : ${response}")
        response match {
          case Complete(_,_,result) if result.contains("Success") => context.parent ! Response(response,dest);context.system.log.info("res match success");
          case _ => dest ! response;context.system.log.info("res match yet")
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
      context.system.log.info("buy calling /endpoint section")
    }
    case sell :Sell => {
      val res = sendmessage(sell.Calling,"calling/sell",HttpMethods.POST)
      resSender(sender(),res)
      context.system.log.info("sell calling /endpoint section")
    }
    case Cancel(buy:Buy) => {
      val res = sendmessage(buy.Calling,"cancel/buy",HttpMethods.DELETE)
      resSender(sender(),res)
      //context.system.log.info(s"buy cancel /endpoint section\n${sender()}")
      context.system.log.info(s"buy cancel /endpoint section")
    }
    case Cancel(sell:Sell) => {
      val res = sendmessage(sell.Calling,"cancel/sell",HttpMethods.DELETE)
      resSender(sender(),res)
      context.system.log.info("sell cancel /endpoint section")
    }
    case _ => println("In Endpoint fail")
  }

}
//      val resFuture = Unmarshal(sendmessage(sell.Calling,"calling/buy",HttpMethods.POST)).to[Complete]
//      val res = Await.result(resFuture, 1.second)
