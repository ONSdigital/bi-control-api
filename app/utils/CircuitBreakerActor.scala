package utils

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import akka.pattern.CircuitBreaker
import akka.actor.{ Actor, ActorLogging }

import com.typesafe.config.{ Config, ConfigFactory }

class CircuitBreakerActor extends Actor with ActorLogging {

  import context.dispatcher
  private val config: Config = ConfigFactory.load()

  val breaker =
    new CircuitBreaker(
      context.system.scheduler,
      maxFailures = config.getInt("circuit-breaker.failure.threshold"),
      callTimeout = config.getInt("circuit-breaker.failure.declaration.time").seconds,
      resetTimeout = config.getInt("circuit-breaker.reset.timeout").seconds
    ).
      onOpen(notifyMe("Open")).
      onClose(notifyMe("Closed")).
      onHalfOpen(notifyMe("Half-Open"))

  def notifyMe(state: String): Unit =
    log.warning(s"Circuitbreaker is $state")

  def receive = {
    case "Success" => breaker.succeed()
    case _ => breaker.fail()
  }
}