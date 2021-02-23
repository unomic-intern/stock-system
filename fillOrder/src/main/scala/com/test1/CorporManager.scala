package com.test1
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object CorporManager{
  def props(CorpornameList:Vector[String]):Props = Props(new CorporManager(CorpornameList))

  case class Calling(UserId:String, Corporname:String, Price:Int)
  case class Buy(Calling:Calling)
  case class Sell(Calling:Calling)
  case class Complete(UserId:String, Result:String)
}

class CorporManager(CorpornameList:Vector[String]) extends Actor with ActorLogging {
  import AkkaHttpJson.log

  private var CorporNames = CorpornameList
  val exceptionActor = context.actorOf(Panic.props,"Panic")
  val corporActors:Map[String, ActorRef] = CorporNames.map{Corpor =>
    (Corpor,context.actorOf(Corporation.props,s"${Corpor}"))
  }.toMap
  import com.test1._
  def receive:Receive={
    case Buy(calling) => {
      log.info(s"${corporActors} : CorporManager")
      corporActors.getOrElse(calling.Corporname,exceptionActor) forward Buy(calling)
      //      sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Buy")
    }
    case Sell(calling) =>{
      log.info(s"${corporActors} : CorporManager")
      corporActors.getOrElse(calling.Corporname,exceptionActor) forward Sell(calling)
      //      sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Sell")
    }
    case Cancle(action:Buy) =>{
      corporActors.getOrElse(action.Calling.Corporname,exceptionActor) forward Cancle(action)
    }
    case Cancle(action:Sell) =>{
      corporActors.getOrElse(action.Calling.Corporname,exceptionActor) forward Cancle(action)
    }
  }

}