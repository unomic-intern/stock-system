package aia.integration

//import aia.integration.OrderServiceApp.{system, theTradingSystem}
import aia.integration.OrderServiceApp.{ec, system}
import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import spray.json.DefaultJsonProtocol._

import collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

case class TrackingOrder(UserId: String, Corporname: String, Price: Int)
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
      val newOrder = new TrackingOrder(order.UserId, order.Corporname,  order.Price)
      BuyorderList += newOrder
      val uri = "http://localhost:8001/calling/buy"
      sendMessage(newOrder, uri).foreach(println)
      sender() ! newOrder
    }
    case order: SellOrder => {          // POST요청으로 새로운 Sell(매도) 주문을 전달받음
      val newOrder = new TrackingOrder(order.UserId, order.Corporname,  order.Price)
      SellorderList += newOrder
      val uri = "http://localhost:8001/calling/sell"
      sendMessage(newOrder, uri).foreach(println)
      sender() ! newOrder
    }
    case "reset" => {               // 테스트에 사용한 상태 재설정
      BuyorderList.clear()
      SellorderList.clear()
    }
  }

  def sendMessage(newOrder: TrackingOrder, Uri: String) = {
    //theTradingSystem.ask(newOrder)(20 seconds)

    /*
    Http().singleRequest(HttpRequest(uri = "https://thecocktaildb.com/api/json/v1/1/search.php?f=2")).onComplete{
      case Success(res) => println(res)
      case Failure(_)   => sys.error("something wrong")
    }(ec)
    */

    val source = """
        {
            "UserId" : """" + newOrder.UserId + """",
            "Corporname" : """" + newOrder.Corporname + """",
            "Price" : """ + newOrder.Price + """
        }
        """.stripMargin

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = Uri,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)

  }

/*
  val source =
    """
      |{
      |    "UserId" : "id1",
      |    "Corporname" : "Naver",
      |    "Price" : 7500
      |}
      |""".stripMargin
*/
/*
  def sendRequest(): Future[String] = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "http://localhost:8001/calling/buy",
      entity = HttpEntity(
        ContentTypes.`application/json`,
        source
      )
    )

    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    val entityFuture: Future[HttpEntity.Strict] = responseFuture.flatMap(response => response.entity.toStrict(2.seconds))
    entityFuture.map(entity => entity.data.utf8String)
  }
 */
}

