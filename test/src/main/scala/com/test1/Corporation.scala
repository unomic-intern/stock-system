package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Corporation{
  def props:Props = Props(new Corporation)
}

class Corporation extends Actor with ActorLogging {

  def receive:Receive={
    case Buy(calling) => {
      println("I'm buying now")
    }
    case Sell(calling) =>{
      println("I'm selling now")
    }
  }

}