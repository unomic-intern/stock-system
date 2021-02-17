package aia.integration

import akka.actor._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

// 매도/매수 주문
final case class BuyOrder(UserId: String, Corporname: String, Price: Int)
final case class SellOrder(UserId: String, Corporname: String, Price: Int)
final case class Item(UserId: String, Corporname: String, Price: Int)
// 매도/매수 주문 취소
final case class CancelBuyOrder(CallingId: Long)
final case class CancelSellOrder(CallingId: Long)
final case class Num(CallingId: Long)
// 매도/매수 뒤늦게 체결
final case class CompletedBuyOrder(CallingId: Long)
final case class CompletedSellOrder(CallingId: Long)
final case class CompletedItem(CallingId: Long, UserId: String, Corporname: String, Price: Int)

// HTTP로부터 주문을 받고 그에 대한 응답을 내보내는 시스템
class OrderServiceApi (system: ActorSystem, timeout: Timeout, val processOrders: ActorRef)
  extends OrderService{
  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher
}

trait OrderService{
  val processOrders: ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  val routes =  postJSONBuy ~ postJSONSell ~ postJSONCancelBuy ~ postJSONCancelSell ~ postJSONResponseBuy ~ postJSONResponseSell// ~는 루트나 지시문을 합성함. 'getOrder 또는 postOrders' 라고 읽음
  // 루트에 맞지 않는 요청은 모두 HTTP 404 Not Found 응답을 받게됨
  // ex) order라는 경로를 사용하거나 DELETE방식을 사용한 요청이 들어올 때 404를 결과로 돌려줌

  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat3(Item)
  implicit val citemFormat = jsonFormat4(CompletedItem)
  implicit val numFormat = jsonFormat1(Num)


  // 매수
  def postJSONBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "buy") {              // 경로와 매치시킴
      entity(as[Item]) { item =>
        val order = toJSONBuyOrder(item)   // JSON -> BuyOrder로 언마샬링함

        onSuccess(processOrders.ask(order)) {
          case result: CallingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  // 매도
  def postJSONSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "sell") {              // 경로와 매치시킴
      entity(as[Item]) { item =>
        val order = toJSONSellOrder(item)   // JSON -> SellOrder로 언마샬링함

        onSuccess(processOrders.ask(order)) {
          case result: CallingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  //-----------------------------------------------------------------------------------------

  // 매수 취소
  def postJSONCancelBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("cancel" / "buy") { // 경로와 매치시킴
      entity(as[Num]) { num =>
          val order = toJSONCancelBuyOrder(num)

        onSuccess(processOrders.ask(order)) {
          case result: CallingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest) // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  // 매도 취소
  def postJSONCancelSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("cancel" / "sell") { // 경로와 매치시킴
      entity(as[Num]) { num =>
          val order = toJSONCancelSellOrder(num)

        onSuccess(processOrders.ask(order)) {
          case result: CallingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest) // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  //-----------------------------------------------------------------------------------------

  // 매수 뒤늦게 체결
  def postJSONResponseBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("response" / "buy") { // 경로와 매치시킴
      entity(as[CompletedItem]) { citem =>
        val order = toJSONResponseBuyOrder(citem)

        onSuccess(processOrders.ask(order)) {
          case result: String =>
            complete(result) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest) // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  // 매도 뒤늦게 체결
  def postJSONResponseSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("response" / "sell") { // 경로와 매치시킴
      entity(as[CompletedItem]) { citem =>
        val order = toJSONResponseSellOrder(citem)

        onSuccess(processOrders.ask(order)) {
          case result: String =>
            complete(result) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest) // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  //-----------------------------------------------------------------------------------------

  // 매수 JSON -> BuyOrder로 언마샬링함
  def toJSONBuyOrder(item: Item): BuyOrder = {
    val UserId = item.UserId
    val Corporname = item.Corporname
    val Price = item.Price
    new BuyOrder(UserId, Corporname, Price)
  }

  // 매도 JSON -> SellOrder로 언마샬링함
  def toJSONSellOrder(item: Item): SellOrder = {
    val UserId = item.UserId
    val Corporname = item.Corporname
    val Price = item.Price
    new SellOrder(UserId, Corporname, Price)
  }

  //-----------------------------------------------------------------------------------------

  // 매수 취소 JSON -> CancelBuyOrder로 언마샬링함
  def toJSONCancelBuyOrder(num: Num): CancelBuyOrder = {
    val CallingId = num.CallingId
    new CancelBuyOrder(CallingId)
  }

  // 매도 취소 JSON -> CancelSellOrder로 언마샬링함
  def toJSONCancelSellOrder(num: Num): CancelSellOrder = {
    val CallingId = num.CallingId
    new CancelSellOrder(CallingId)
  }


  //-----------------------------------------------------------------------------------------

  // 매수 뒤늦게 체결 JSON -> CompletedBuyOrder로 언마샬링함
  def toJSONResponseBuyOrder(citem: CompletedItem): CompletedBuyOrder = {
    val CallingId = citem.CallingId
    new CompletedBuyOrder(CallingId)
  }

  // 매도 뒤늦게 체결 JSON -> CompletedSellOrder로 언마샬링함
  def toJSONResponseSellOrder(citem: CompletedItem): CompletedSellOrder = {
    val CallingId = citem.CallingId
    new CompletedSellOrder(CallingId)
  }

  /*
  //-----------------------------------------------------------------------------------------

  // 매수 취소 JSON -> CancelBuyOrder로 언마샬링함
  def toJSONCancelBuyOrder(citem: CompletedItem): CancelBuyOrder = {
    val CallingId = citem.CallingId
    val CallingId = citem.UserId
    val CallingId = citem.CallingId
    val CallingId = citem.CallingId
    new CompletedItem(CallingId)
  }

  // 매도 취소 JSON -> CancelSellOrder로 언마샬링함
  def toJSONCancelSellOrder(citem: CompletedItem): CancelSellOrder = {
    val CallingId = citem.CallingId
    val CallingId = citem.CallingId
    val CallingId = citem.CallingId
    val CallingId = citem.CallingId
    new CompletedItem(CallingId)
  }
  */
}