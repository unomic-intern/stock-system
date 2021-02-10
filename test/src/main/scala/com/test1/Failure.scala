package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Failure{
  def props:Props = Props(new Failure)
}

class Failure extends Actor with ActorLogging {

  def receive:Receive={
    case Buy(calling) => {
      println("I'm buying fail")
    }
    case Sell(calling) =>{
      println("I'm selling fail")
    }
  }

}