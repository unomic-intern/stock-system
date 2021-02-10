package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import java.util.UUID
import akka.pattern.ask

//case class Calling(UserId:String, Corporation:String, Price:Int)
//case class Buy(Calling:Calling)
//case class Sell(Calling:Calling)
//case class Complete(UserId:String, Result:String)


object CorporManager{
  def props(CorpornameList:Vector[String]):Props = Props(new CorporManager(CorpornameList))

  case class Calling(UserId:String, Corporname:String, Price:Int)
  case class Buy(Calling:Calling)
  case class Sell(Calling:Calling)
  case class Complete(UserId:String, Result:String)
}

class CorporManager(CorpornameList:Vector[String]) extends Actor with ActorLogging {
  private var CorporNames = CorpornameList
  val corporActors:Map[String, ActorRef] = CorporNames.map{Corpor =>
    (Corpor,context.actorOf(Corporation.props,s"${Corpor}"))
  }.toMap
  import com.test1._
  def receive:Receive={
    case Buy(calling) => {
      println(corporActors)
      corporActors.getOrElse(calling.Corporname,context.actorOf(Failure.props,"Match")) ! Buy(calling)
      sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Buy")
    }
    case Sell(calling) =>{
      println(corporActors)
      corporActors.getOrElse(calling.Corporname,context.actorOf(Failure.props,"Match")) ! Sell(calling)
      sender() ! Complete(calling.UserId,s"Success ${calling.Corporname} Sell")
    }
  }

}
