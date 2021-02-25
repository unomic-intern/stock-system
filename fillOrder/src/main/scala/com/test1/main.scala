package com.test1

import akka.http.scaladsl.Http
import akka.actor._
import akka.event.Logging
import scala.language.postfixOps
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}


object AkkaHttpJson extends App with RequestTimeout{

  implicit val system = ActorSystem()
  val ec = system.dispatcher
  val log =Logging(system.eventStream,"actorsystemlogging")
  val config = ConfigFactory.load()

  val otheractor = system.actorOf(CorporManager.props(Vector("Kakao","Samsung","Naver")),"CorporManager")

  val api = new OrderServiceApi(system, // OrderServiceApi가 루트를 반환함
    requestTimeout(config),
    otheractor).route

  //Http().newServerAt("192.168.0.78", 8001).bind(api)
  Http().newServerAt("192.168.0.83", 8001).bind(api)
}

trait RequestTimeout {
  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}