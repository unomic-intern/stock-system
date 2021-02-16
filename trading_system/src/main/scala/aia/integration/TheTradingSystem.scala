package aia.integration

import aia.integration.OrderServiceApp.system
import akka.actor.{Actor}

import scala.concurrent.duration._

class TheTradingSystem extends Actor{

  //implicit val system = ActorSystem()
  //val log = Logging(system.eventStream, "order-service")

  override def receive: Receive = {
    case json: TrackingOrder  => {
      //log.info(json.UserId)
      system.log.info(json.UserId)
      sender() ! "here"
    }
  }

}

