package aia.integration

import akka.actor._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}


final case class BuyOrder(UserId: String, Corporname: String, Price: Int)
final case class SellOrder(UserId: String, Corporname: String, Price: Int)
final case class Item(UserId: String, Corporname: String, Price: Int)

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

  val routes =  postJSONBuy ~ postJSONSell // ~는 루트나 지시문을 합성함. 'getOrder 또는 postOrders' 라고 읽음
  // 루트에 맞지 않는 요청은 모두 HTTP 404 Not Found 응답을 받게됨
  // ex) order라는 경로를 사용하거나 DELETE방식을 사용한 요청이 들어올 때 404를 결과로 돌려줌

  // formats for unmarshalling and marshalling
  implicit val itemFormat = jsonFormat3(Item)


  def postJSONBuy = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "Buy") {              // /orders 경로와 매치시킴
      entity(as[Item]) { item =>
        val order = toJSONBuyOrder(item)   // JSON -> Order로 언마샬링함

        onSuccess(processOrders.ask(order)) {
          case result: TrackingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  def postJSONSell = post {     //GET 구현과 비슷하지만, URL에서 주문 ID를 가져올 필요가 없고, POST의 본문을 받아야 함
    path("calling" / "Sell") {              // /orders 경로와 매치시킴
      entity(as[Item]) { item =>
        val order = toJSONSellOrder(item)   // JSON -> Order로 언마샬링함

        onSuccess(processOrders.ask(order)) {
          case result: TrackingOrder =>
            complete(Item(result.UserId, result.Corporname, result.Price)) // JSON응답으로 요청 완료

          case result =>
            complete(StatusCodes.BadRequest)    // processActor가 다른 유형의 메시지를 돌려주면 BadReqeust상태 코드를 반환함
        }
      }
    }
  }

  def toJSONBuyOrder(item: Item): BuyOrder = {
    val UserId = item.UserId
    val Corporname = item.Corporname
    val Price = item.Price
    new BuyOrder(UserId, Corporname, Price)
  }

  def toJSONSellOrder(item: Item): SellOrder = {
    val UserId = item.UserId
    val Corporname = item.Corporname
    val Price = item.Price
    new SellOrder(UserId, Corporname, Price)
  }

}