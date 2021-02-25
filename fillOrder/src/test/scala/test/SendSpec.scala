package test

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.test1.{Api, CorporManager}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class SendSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with SprayJsonSupport
  with Api{

  implicit val executionContext = system.dispatcher
  implicit val requestTimeout = akka.util.Timeout(20 second)
  val otheractor = system.actorOf(CorporManager.props(Vector("Kakao","Samsung","Naver")),"CorporManager")


  "The order service" should {
    // 1-1. 매수(BUY) 주문 테스트
    "return the tracking order for an BUY order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 1,
            "UserId" : "id1",
            "Corporname" : "Kakao",
            "Price" : 3000
        }
        """.stripMargin
      )

      Post("/calling/buy", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    // 1-2. 매도(SELL) 주문 테스트
    "return the tracking order for an SELL order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 1,
            "UserId" : "id2",
            "Corporname" : "Samsung",
            "Price" : 8000
        }
        """.stripMargin
      )

      Post("/calling/sell", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    //----------------------------------------------------------------------------------
    // 2-1. 매도(SELL) 체결X 테스트 - 매수가(6000)가 매도가(8000) 미만인 경우
    "return FAIL about BUY order that was posted for an SELL order" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 2,
            "UserId" : "id3",
            "Corporname" : "Samsung",
            "Price" : 6000
        }
        """.stripMargin
      )

      Post("/calling/buy", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    // 2-2. 매수(BUY) 체결X 테스트 - 매도가(4000)가 매수가(3000) 미만인 경우
    "return FAIL about SELL order that was posted for an BUY order" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 2,
            "UserId" : "id4",
            "Corporname" : "Kakao",
            "Price" : 4000
        }
        """.stripMargin
      )

      Post("/calling/sell", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    //----------------------------------------------------------------------------------
    // 3-1. 매수(BUY) 체결O 테스트
    "return SUCCESS about BUY order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 3,
            "UserId" : "id5",
            "Corporname" : "Naver",
            "Price" : 4000
        }
        """.stripMargin
      )

      Post("/calling/buy", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    // 3-2. 매도(SELL) 체결O 테스트
    "return SUCCESS about SELL order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 3,
            "UserId" : "id6",
            "Corporname" : "Naver",
            "Price" : 4000
        }
        """.stripMargin
      )

      Post("/calling/sell", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    //----------------------------------------------------------------------------------
    // 4-1. 매수(BUY) 뒤늦게 체결O 테스트
    "return later SUCCESS about BUY order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 4,
            "UserId" : "id7",
            "Corporname" : "Samsung",
            "Price" : 8000
        }
        """.stripMargin
      )

      Post("/calling/buy", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    // 4-2. 매도(SELL) 뒤늦게 체결O 테스트
    "return later SUCCESS about SELL order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 4,
            "UserId" : "id8",
            "Corporname" : "Kakao",
            "Price" : 3000
        }
        """.stripMargin
      )

      Post("/calling/sell", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    //----------------------------------------------------------------------------------
    // 5-1. 매수 주문 취소 테스트
    "return the cancel order for an BUY order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 2,
            "UserId" : "id3",
            "Corporname" : "Samsung",
            "Price" : 6000
        }
        """.stripMargin
      )

      Delete("/cancel/buy", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }

    // 5-2. 매도 주문 취소 테스트
    "return the cancel order for an SELL order that was posted" in {
      val jsonOrder = ByteString(
        s"""
        {
            "CallingId" : 2,
            "UserId" : "id4",
            "Corporname" : "Kakao",
            "Price" : 4000
        }
        """.stripMargin
      )

      Delete("/cancel/sell", HttpEntity(ContentTypes.`application/json`, jsonOrder)) ~> Route.seal(route) ~> check {
        system.log.info(responseAs[String])
        system.log.info(status.toString())
        status shouldEqual StatusCodes.OK
      }
    }
  }
}

