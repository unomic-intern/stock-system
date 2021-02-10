package aia.integration

import aia.integration.OrderServiceApp.{system, theTradingSystem}
import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, path, post}
import akka.pattern.ask
import spray.json.DefaultJsonProtocol._

import collection.mutable
import scala.concurrent.duration.DurationInt

case class TrackingOrder(UserId: String, Corporation: String, Price: Int)
//case class TrackingOrder(UserId: String, Corporation: String, BuySell: String, Price: Int)
//case class BuyOrderId(id: Long)
//case class SellOrderId(id: Long)
//case class NoSuchOrder(id: Long)
case class NoSuchOrder(id: String)

// 전달받은 주문 처리하는 Actor
class ProcessOrders extends Actor {

  val BuyorderList = new mutable.ListBuffer[TrackingOrder]
  val SellorderList = new mutable.ListBuffer[TrackingOrder]

  // marshalling
  implicit val itemFormat = jsonFormat3(TrackingOrder)

  val log = Logging(system.eventStream, "order-service")

  def receive = {
    case order: BuyOrder => {          // POST요청으로 새로운 Buy(매수) 주문을 전달받음
      val newOrder = new TrackingOrder(order.UserId, order.Corporation,  order.Price)
      BuyorderList += newOrder
      sendMessage(newOrder)
      sender() ! newOrder
    }
    case order: SellOrder => {          // POST요청으로 새로운 Sell(매도) 주문을 전달받음
      val newOrder = new TrackingOrder(order.UserId, order.Corporation,  order.Price)
      SellorderList += newOrder
      sendMessage(newOrder)
      sender() ! newOrder
    }
    case "reset" => {               // 테스트에 사용한 상태 재설정
      BuyorderList.clear()
      SellorderList.clear()
    }
  }

  def sendMessage(newOrder: TrackingOrder) = {
    theTradingSystem.ask(newOrder)(20 seconds)
    /*
    theTradingSystem.ask("message")(20 seconds) = {
      case _ =>
        log.info("The other Actor System!")
    }
     */


  }
}

