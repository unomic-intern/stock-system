package com.newTrading

import collection.mutable
import akka.actor.{Actor, Props}

object WaitingClient{
  def props = Props(new WaitingClient)
}

class WaitingClient extends Actor{
  //import AkkaHttp.log

  val BuyOrderList = new mutable.HashMap[Long, Calling]
  val SellOrderList = new mutable.HashMap[Long, Calling]
  val endpoint = context.actorOf(EndPoint.props, "endpoint")

  def DropOrder(targetList: mutable.HashMap[Long,Calling],callingId:Long) = {
    targetList.find(_._1 == callingId) match{
      case Some(result) => targetList.remove(result._1);
      case None => context.system.log.info("Response.buy.error : Calling Id doesn't exist")
    }
  }

  def receive :Receive ={
    case buy:Buy => {
      BuyOrderList += buy.Calling.CallingId -> buy.Calling
      endpoint forward buy
      context.system.log.info(s"buy calling /WaitingClient \n${BuyOrderList}")
    }
    case sell:Sell => {
      SellOrderList += sell.Calling.CallingId -> sell.Calling
      endpoint forward sell
      context.system.log.info(s"sell calling /WaitingClient \n${SellOrderList}")
    }
    case Cancel(action) => {endpoint forward Cancel(action);context.system.log.info("cancel /WaitingClient")}
    //응답 오는거 보고 삭제하자
    case Finish(Buy(calling)) => {
      context.system.log.info(s"finish buy calling /WaitingClients \n${calling}")
      val tmpbuy = BuyOrderList(calling.CallingId)
      DropOrder(BuyOrderList,calling.CallingId)
      sender() ! Complete(calling.CallingId, tmpbuy.UserId, "Success buy response")
      context.system.log.info(s"\n${BuyOrderList}\n${SellOrderList}")
    }
    case Finish(Sell(calling)) => {
      context.system.log.info(s"finish sell calling /WaitingClients \n${calling}")
      val tmpsell = SellOrderList(calling.CallingId)
      DropOrder(SellOrderList,calling.CallingId)
      sender() ! Complete(calling.CallingId, tmpsell.UserId, "Success buy response")
      context.system.log.info(s"\n${BuyOrderList}\n${SellOrderList}")
    }
    case Response(complete,dest) if complete.Result.contains("Sell") => {
      context.system.log.info(s"cancel sell res /WaitingClient \n${complete}\n${sender()}")
      DropOrder (SellOrderList, complete.CallingId)
      dest ! complete
      context.system.log.info(s"cancel sell res /WaitingClient \n${SellOrderList} ")
    }
    case Response(complete,dest) if complete.Result.contains("Buy") => {
      context.system.log.info(s"cancel buy res /WaitingClient \n${complete}\n${sender()}")
      DropOrder (BuyOrderList, complete.CallingId)
      dest ! complete
      context.system.log.info(s"cancel buy res /WaitingClient \n${BuyOrderList}")
    }
  }
}
