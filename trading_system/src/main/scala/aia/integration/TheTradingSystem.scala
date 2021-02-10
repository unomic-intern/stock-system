package aia.integration

import aia.integration.OrderServiceApp.system
import akka.actor.Actor
import akka.event.Logging
import akka.http.javadsl.server.Directives._

class TheTradingSystem extends Actor{

  val log = Logging(system.eventStream, "order-service")

  override def receive: Receive = {
    case json: TrackingOrder  => {
      log.info(json.UserId)
      sender() ! "here"
    }
  }
}
