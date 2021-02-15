package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

case class success(calling:Calling)

object Corporation{
  def props:Props = Props(new Corporation)

}

class Corporation extends Actor with ActorLogging {
  //  val matcher = context.actorOf(Match.props,"Match")
  private var buyList = Vector[Calling]()
  private var sellList = Vector[Calling]()

  val tradingresponse = context.actorOf(TradingResponse.props,"tradingresponse")

  def buyTrading(calling:Calling,operator:Int=>Boolean ) : Unit = {
    sellList.find(x => {
      operator(x.Price)
    }) match {
      case Some(calling) => {
        sellList = sellList.diff(Seq(calling))
        tradingresponse ! Buy(calling)
        sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Buy")
        println(s"remove in selllist : ${sellList}")
      }
      case None => {
        buyList = (buyList :+ calling).sortBy(_.Price).reverse
        sender() ! Complete(calling.UserId,s"Not Yet ${calling.Corporname} Buy")
        println(s"add in buylist ${buyList}")
      }
    }
  }

  def sellTrading(calling:Calling,operator:Int=>Boolean ) : Unit = {
    buyList.find(x => {
      operator(x.Price)
    }) match {
      case Some(calling) => {
        buyList = buyList.diff(Seq(calling))
        tradingresponse ! Sell(calling)
        sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Sell")
        println(s"remove in buylist : ${buyList}")
      }
      case None => {
        sellList = (sellList :+ calling).sortBy(_.Price)
        sender() ! Complete(calling.UserId,s"Not Yet ${calling.Corporname} Sell")
        println(s"add in selllist ${sellList}")
      }
    }
  }
  //여기에 매칭 함수 따로 작성하고 이후에

  def receive:Receive={
    case Buy(calling) => {
      println(s"in ${calling.Corporname}")
      buyTrading(calling,calling.Price.>=)
    }
    case Sell(calling) =>{
      println(s"in ${calling.Corporname}")
      sellTrading(calling,calling.Price.<=)
    }
  }

}