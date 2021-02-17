package aia.integration

import aia.integration.OrderServiceApp.{ec, system}
import akka.actor.Actor
import akka.event.Logging
import akka.http.javadsl.model
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, _}
import spray.json.DefaultJsonProtocol._

import collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

//case class TrackingOrder(UserId: String, Corporname: String, Price: Int)
case class CallingOrder(CallingId: Long, UserId: String, Corporname: String, Price: Int)
case class TrackingCancelOrder(CallingId: Long)
case class NoSuchOrder(id: String)
case class NoSuchCacelOrder(CallingId: Long)

// 전달받은 주문 처리하는 Actor
class ProcessOrders extends Actor {

  //val BuyorderList = new mutable.ListBuffer[TrackingOrder]
  //val SellorderList = new mutable.ListBuffer[TrackingOrder]

  //val BuyorderList = new mutable.HashMap[Long, TrackingOrder]
  //val SellorderList = new mutable.HashMap[Long, TrackingOrder]

  val BuyorderList = new mutable.HashMap[Long, CallingOrder]
  val SellorderList = new mutable.HashMap[Long, CallingOrder]
  var BuyorderNum = 0L
  var SellorderNum = 0L

  // marshalling
  //implicit val itemFormat = jsonFormat3(TrackingOrder)
  implicit val itemFormat = jsonFormat4(CallingOrder)

  val log = Logging(system.eventStream, "order-service")

  def receive = {
    // 매수 주문 처리
    case order: BuyOrder => {          // POST요청으로 새로운 Buy(매수) 주문을 전달받음
      BuyorderNum += 1
      //val newOrder = new TrackingOrder(order.UserId, order.Corporname,  order.Price)
      val newSendOrder = new CallingOrder(BuyorderNum, order.UserId, order.Corporname,  order.Price)
      BuyorderList += BuyorderNum -> newSendOrder
      sendOrderMessage(newSendOrder, "calling/buy", HttpMethods.POST).foreach(println)
      // success이면 리스트에서 주문삭제
      sender() ! newSendOrder
      log.info(BuyorderList.toString)
    }
    // 매도 주문 처리
    case order: SellOrder => {          // POST요청으로 새로운 Sell(매도) 주문을 전달받음
      SellorderNum += 1
      //val newOrder = new TrackingOrder(order.UserId, order.Corporname,  order.Price)
      val newSendOrder = new CallingOrder(SellorderNum, order.UserId, order.Corporname,  order.Price)
      SellorderList += SellorderNum -> newSendOrder
      sendOrderMessage(newSendOrder, "calling/sell", HttpMethods.POST).foreach(println)
      // success이면 리스트에서 주문삭제
      sender() ! newSendOrder
      log.info(SellorderList.toString)
    }

    //-----------------------------------------------------------------------------------------

    // 매수 취소 주문 처리
    case order: CancelBuyOrder => {          // POST요청으로 새로운 Buy(매수) 주문을 전달받음
      BuyorderList.find(_._1 == order.CallingId) match{
        case Some(result) =>
          log.info(result._2.toString)
          sendOrderMessage(result._2, "cancel/buy", HttpMethods.DELETE).foreach(println)
          sender() ! result._2

          BuyorderList.remove(result._1)
          log.info(BuyorderList.toString)
        case None =>
          sender() ! NoSuchCacelOrder(order.CallingId)
      }
    }
    // 매도 취소 주문 처리
    case order: CancelSellOrder => {          // POST요청으로 새로운 Sell(매도) 주문을 전달받음
      SellorderList.find(_._1 == order.CallingId) match{
        case Some(result) =>
          log.info(result._2.toString)
          sendOrderMessage(result._2, "cancel/sell", HttpMethods.DELETE).foreach(println)
          sender() ! result._2

          SellorderList.remove(result._1)
          log.info(SellorderList.toString)
        case None =>
          sender() ! NoSuchCacelOrder(order.CallingId)
      }
    }

    //-----------------------------------------------------------------------------------------

    // 매수 뒤늦게 체결 처리
    case order: CompletedBuyOrder => {          // POST요청으로 새로운 Buy(매수) 주문을 전달받음
      BuyorderList.find(_._1 == order.CallingId) match{
        case Some(result) =>
          log.info(result._2.toString)
          sender() ! "BUY Completed!!!"

          BuyorderList.remove(result._1)
          log.info(BuyorderList.toString)
        case None =>
          sender() ! NoSuchCacelOrder(order.CallingId)
      }
    }
    // 매도 뒤늦게 체결 처리
    case order: CompletedSellOrder => {          // POST요청으로 새로운 Sell(매도) 주문을 전달받음
      SellorderList.find(_._1 == order.CallingId) match{
        case Some(result) =>
          log.info(result._2.toString)
          sender() ! "SEll Completed!!!"

          SellorderList.remove(result._1)
          log.info(SellorderList.toString)
        case None =>
          sender() ! NoSuchCacelOrder(order.CallingId)
      }
    }

    //-----------------------------------------------------------------------------------------

    // 리셋
    case "reset" => {               // 테스트에 사용한 상태 재설정
      BuyorderList.clear()
      SellorderList.clear()
      BuyorderNum = 0L
      SellorderNum = 0L
    }
  }

  //-----------------------------------------------------------------------------------------

  // 주문 전송 메시지
  def sendOrderMessage(newOrder: CallingOrder, Uri: String, Hmethod: HttpMethod) = {
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
      uri = s"http://192.168.0.78:8001/${Uri}",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)

  }
}

