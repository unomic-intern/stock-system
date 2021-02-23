package com.newTrading
import akka.actor.{ActorRef}
import spray.json._

object Static{
  var long:Long = 0L
}

case class UserCalling(UserId:String, Corporname:String, Price:Int){
  def toCalling:Calling = {
    Static.long += 1
    Calling(Static.long,UserId,Corporname,Price)
  }
}

case class Calling(CallingId:Long, UserId:String, Corporname:String, Price:Int)
case class Result(end : String)

case class Cancel[T](Action:T)
case class Buy(Calling:Calling)
case class Sell(Calling:Calling)
case class Finish[T](Action:T)
case class Complete(CallingId:Long,UserId:String, Result:String)
case class Response(Complete:Complete, dest:ActorRef)

trait JsonProtocol extends DefaultJsonProtocol{
  //input
  implicit val UserCallingFormat = jsonFormat3(UserCalling)
  implicit val CompleteFormat = jsonFormat3(Complete)

  //output
  implicit val CallingFormat = jsonFormat4(Calling)
  implicit val ResultFormat = jsonFormat1(Result)
  //  implicit val userAddedFormat = jsonFormat2(UserAdded)
}
