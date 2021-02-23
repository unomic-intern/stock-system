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

object AkkaHttp extends App with JsonProtocol with SprayJsonSupport with Directives{
  implicit val akkahttpsystem = ActorSystem()

  val log = Logging(akkahttpsystem.eventStream,"actorsystemlogging")
  var index:Long = 1
  val waitingclient = akkahttpsystem.actorOf(WaitingClient.props,"Waiting")

  val routes = UserCallingBuy ~ UserCallingSell ~ UserCancelBuy ~ UserCancelSell ~ KRXResponseSell ~ KRXResponseBuy

  def UserCallingBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "buy") {              // 경로와 매치시킴
      entity(as[UserCalling]) { usercalling =>
        onSuccess(waitingclient.ask(Buy(usercalling.toCalling))(5 seconds).mapTo[Complete]) {
          case cmp: Complete =>
            complete(Result("calling buy success")) // JSON응답으로 요청 완료
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  def UserCallingSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "sell") {              // 경로와 매치시킴
      entity(as[UserCalling]) { usercalling =>
        onSuccess(waitingclient.ask(Sell(usercalling.toCalling))(5 seconds).mapTo[Complete]) {
          case cmp: Complete =>
            complete(Result("calling sell success")) // JSON응답으로 요청 완료
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  def UserCancelBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("cancel" / "buy") {              // 경로와 매치시킴
      entity(as[Calling]) { calling =>
        onSuccess(waitingclient.ask(Cancel[Buy](Buy(calling)))(5 seconds).mapTo[Complete]) {
          case cmp: Complete =>
            complete(Result("cancel buy success")) // JSON응답으로 요청 완료
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  def UserCancelSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("cancel" / "sell") {              // 경로와 매치시킴
      entity(as[Calling]) { calling =>
        onSuccess(waitingclient.ask(Cancel[Sell](Sell(calling)))(5 seconds).mapTo[Complete]) {
          case cmp: Complete =>
            complete(Result("cancel sell success"))
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  def KRXResponseSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("response" / "buy") {              // 경로와 매치시킴
      entity(as[Calling]) { calling =>
        onSuccess(waitingclient.ask(Finish[Buy](Buy(calling)))(5 seconds).mapTo[Complete]) {
          case cmp:Complete =>
            complete(cmp) // JSON응답으로 요청 완료
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  def KRXResponseBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("response" / "sell") {              // 경로와 매치시킴
      entity(as[Calling]) { calling =>
        onSuccess(waitingclient.ask(Finish[Sell](Sell(calling)))(5 seconds).mapTo[Complete]) {
          case cmp:Complete =>
            complete(cmp) // JSON응답으로 요청 완료
          case _ =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }
  Http().newServerAt("localhost", 8002).bind(routes)
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