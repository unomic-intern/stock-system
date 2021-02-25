package com.newTrading

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives

import scala.language.postfixOps
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

object AkkaHttp extends App with RequestTimeout{

  implicit val akkahttpsystem = ActorSystem()
  val ec = akkahttpsystem.dispatcher
  val log = Logging(akkahttpsystem.eventStream,"actorsystemlogging")
  val config = ConfigFactory.load()
  var index:Long = 1

  val waitingclient = akkahttpsystem.actorOf(WaitingClient.props,"Waiting")

  val api = new ServiceApi(akkahttpsystem, // OrderServiceApi가 루트를 반환함
    requestTimeout(config),
    waitingclient).routes

  Http().newServerAt("localhost", 8002).bind(api)
}

trait RequestTimeout {
  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}

//val routes :Route = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("calling" / "buy") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  }~ post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("calling" / "sell") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  } ~ post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("cancel" / "buy") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  } ~ post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("cancel" / "sell") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  } ~ post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("response" / "buy") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  } ~ post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
//    pathPrefix("response" / "sell") {              // 경로와 매치시킴
//      entity(as[Calling]) { calling =>
//        onSuccess(tradingsystem.ask(calling)(5 seconds).mapTo[Result]) {
//          case result: Result =>
//            complete(result) // JSON응답으로 요청 완료
//          case _ =>
//            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
//        }
//      }
//    }
//  }