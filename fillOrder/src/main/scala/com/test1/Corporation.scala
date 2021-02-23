package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

case class success(calling:Calling)

object Corporation{
  def props:Props = Props(new Corporation)

}

class Corporation extends Actor with ActorLogging {
  //  val matcher = context.actorOf(Match.props,"Match")
  import AkkaHttpJson.log

  private var buyList = Vector[Calling]()
  private var sellList = Vector[Calling]()

  val tradingresponse = context.actorOf(TradingResponse.props,"tradingresponse")

  def buyTrading(targetcalling:Calling,operator:Int=>Boolean ) : Unit = {
    sellList.find(x => {
      operator(x.Price)
    }) match {
      case Some(matchcalling) => {
        sellList = sellList.diff(Seq(matchcalling))
        tradingresponse ! Buy(matchcalling)
        sender() ! Complete(targetcalling.CallingId,targetcalling.UserId,s"Success ${targetcalling.Corporname} Buy")
        log.info(s"remove in selllist : ${sellList}")
      }
      case None => {
        buyList = (buyList :+ targetcalling).sortBy(_.Price).reverse
        sender() ! Complete(targetcalling.CallingId,targetcalling.UserId,s"Not Yet ${targetcalling.Corporname} Buy")
        log.info(s"add in buylist ${buyList}")
      }
    }
  }

  def sellTrading(targetcalling:Calling,operator:Int=>Boolean ) : Unit = {
    buyList.find(x => {
      operator(x.Price)
    }) match {
      case Some(matchcalling) => {
        buyList = buyList.diff(Seq(matchcalling))
        tradingresponse ! Sell(matchcalling)
        sender() ! Complete(targetcalling.CallingId,targetcalling.UserId,s"Success ${targetcalling.Corporname} Sell")
        log.info(s"remove in buylist : ${buyList}")
      }
      case None => {
        sellList = (sellList :+ targetcalling).sortBy(_.Price)
        sender() ! Complete(targetcalling.CallingId,targetcalling.UserId,s"Not Yet ${targetcalling.Corporname} Sell")
        log.info(s"add in selllist ${sellList}")
      }
    }
  }
  //여기에 매칭 함수 따로 작성하고 이후에

  def receive:Receive={
    case Buy(calling) => {
      log.info(s"in ${calling.Corporname}")
      buyTrading(calling,calling.Price.>=)
    }
    case Sell(calling) =>{
      log.info(s"in ${calling.Corporname}")
      sellTrading(calling,calling.Price.<=)
    }
    case Cancle(action:Buy) =>{
      buyList = buyList.diff(Seq(action.Calling))
      sender() ! Complete(action.Calling.CallingId,action.Calling.UserId,s"success buy cancle")
    }
    case Cancle(action:Sell) =>{
      sellList = sellList.diff(Seq(action.Calling))
      sender() ! Complete(action.Calling.CallingId,action.Calling.UserId,s"success sell cancle")
    }
  }

}