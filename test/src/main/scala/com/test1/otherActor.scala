package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import java.util.UUID

//case class Calling(UserId:String, Corporation:String, Price:Int)
//case class Buy(Calling:Calling)
//case class Sell(Calling:Calling)
//case class Complete(UserId:String, Result:String)

object otherActor{
  def props:Props = Props(new otherActor)
}

class otherActor extends Actor with ActorLogging {
  import com.test1._
  def receive:Receive={
    case Buy(calling) => {
      println("I'm buying now")
      sender() ! Complete(calling.UserId,s"Success ${calling.Corporation} Buy")
    }
    case Sell(calling) =>{
      println("I'm selling now")
      sender() ! Complete(calling.UserId,s"Success ${calling.Corporation} Sell")
    }
  }
}
