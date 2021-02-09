package com.test1

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import java.util.UUID
//case class UserAdded(id:String, timestamp: Long)
//case class UserAdded(person:Person)
//case class DisplayUser(name:String, age:Int, id:String,timestamp:Long)

object otherActor{
  def props:Props = Props(new otherActor)
}

class otherActor extends Actor with ActorLogging {
  import com.test1._
  def receive:Receive={
    case UserAdded(person) => {
      sender() ! DisplayUser(person.name,person.age,UUID.randomUUID().toString, System.currentTimeMillis())
      print("I'm stockManager Test in here\n")
    }
  }
}
