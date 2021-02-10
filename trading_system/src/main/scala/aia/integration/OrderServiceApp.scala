package aia.integration

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future

// HTTP 서버 시작
object OrderServiceApp extends App with RequestTimeout {
  val config = ConfigFactory.load()
  val host = config.getString("http.host")    // 설정의 host를 가져옴
  val port = config.getInt("http.port")       //설정의 port를 가져옴

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val processOrders = system.actorOf(
    Props(new ProcessOrders), "process-orders"
  )

  val theTradingSystem = system.actorOf(
    Props(new TheTradingSystem), "trading-system"
  )

  val api = new OrderServiceApi(system, // OrderServiceApi가 루트를 반환함
    requestTimeout(config),
    processOrders).routes

  implicit val materializer = ActorMaterializer()
  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, host, port) // 루트를 HTTP서버에 연결

  val log = Logging(system.eventStream, "order-service")
  bindingFuture.map { serverBinding =>
    log.info(s"Bound to ${serverBinding.localAddress}") // 서비스가 성공적으로 시작했다는 로그를 남김
  }.failed.foreach {
    case ex: Exception => // host와 port에 대한 바인딩이 실패했다는 로그를 남김
      log.error(ex, "Failed to bind to {}:{}!", host, port)
      system.terminate()
  }
}


trait RequestTimeout {
  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}
