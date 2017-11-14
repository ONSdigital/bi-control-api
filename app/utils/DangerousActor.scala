package utils

import scala.concurrent.duration._
import akka.pattern.CircuitBreaker
import akka.actor.{ Actor, ActorLogging }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

class DangerousActor extends Actor with ActorLogging {

  import context.dispatcher

  val breaker =
    new CircuitBreaker(
      context.system.scheduler,
      maxFailures = 5,
      callTimeout = 1.seconds,
      resetTimeout = 20.seconds
    ).
      onOpen(notifyMe("Open")).
      onClose(notifyMe("Closed")).
      onHalfOpen(notifyMe("Half-Open"))

  def notifyMe(state: String): Unit =
    log.warning(s"Circuitbreaker is $state")

  def dangerousCall: Unit = {
    println("This really isn't that dangerous of a call after all")
  }

  def receive = {
    case "Success" =>
      breaker.withCircuitBreaker(Future(dangerousCall))
    case _ =>
      breaker.fail()
  }
}