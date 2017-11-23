package controllers.v1

import java.time.{ DateTimeException }
import javax.inject.Inject

import play.api.mvc.{ Action, AnyContent, Result, Controller }
import play.api.libs.json._
import play.api.cache._
import play.api.Configuration

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

import akka.actor.{ ActorSystem, Props }

import utils.Utilities._
import utils.CircuitBreakerActor
import models._
import services._

/**
 * Created by ChiuA on 23/10/2017.
 */

class SearchController @Inject() (data: HBaseData, cache: CacheApi, config: Configuration) extends Controller {

  private val system = ActorSystem("bi-breaker")
  val userActor = system.actorOf(Props[CircuitBreakerActor], name = "User")

  def getBusiness(period: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val validParams: Either[String, Result] = validateParams(period, id)
    validParams match {
      case Right(error) => error.future
      case Left(validPeriod) => cache.get[JsValue](s"${validPeriod}:${id}") match {
        case Some(x: JsValue) => Ok(x).future
        case None => getOutput(validPeriod, id)
      }
    }
  }

  def getOutput(validPeriod: String, id: String): Future[Result] = {
    Try(data.getOutput(validPeriod, id)) match {
      case Success(results) => Try(Business.toJson(results)) match {
        case Success(results) => ResultsMatcher(results, id, validPeriod)
        case Failure(_) => NotFound(errAsJson(404, "Not Found", s"Could not find value ${id}")).future
      }
      case _ =>
        userActor ! "Error"
        InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
    }
  }

  def ResultsMatcher(businessModel: JsValue, id: String, period: String): Future[Result] = {
    userActor ! "Success"
    cache.set(s"${period}:${id}", businessModel, config.getInt("cache.reset.timeout").getOrElse(60) minutes)
    Ok(businessModel).future
  }
}
