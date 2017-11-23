package controllers.v1

import java.time.{ DateTimeException }
import javax.inject.Inject

import play.api.mvc.{ Action, AnyContent, Result, Controller }
import play.api.libs.json._
import play.api.cache._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

import akka.actor.{ ActorSystem, Props }

import com.typesafe.config.{ Config, ConfigFactory }
import utils.Utilities._
import utils.CircuitBreakerActor
import models._
import services._

/**
 * Created by ChiuA on 23/10/2017.
 */

class SearchController @Inject() (data: HBaseData, cache: CacheApi) extends Controller {

  private val config: Config = ConfigFactory.load()
  private val system = ActorSystem("bi-breaker")
  private val userActor = system.actorOf(Props[CircuitBreakerActor], name = "User")

  def getBusiness(period: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    val validParams: Either[String, Result] = validateParams(period, id)
    cache.get[JsValue](id) match {
      case Some(x: JsValue) => Ok(x).future
      case None => validParams match {
        case Right(error) => error.future
        case Left(validPeriod) => Try(data.getOutput(validPeriod, id)) match {
          case Success(results) => Try(Business.toJson(results)) match {
            case Success(results) => ResultsMatcher(results, id)
            case Failure(_) => NotFound(errAsJson(404, "Not Found", s"Could not find value ${id}")).future
          }
          case _ => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
        }
      }
    }
  }

  def ResultsMatcher(businessModel: JsValue, id: String): Future[Result] = {
    cache.set(id, businessModel, config.getInt("cache.reset.timeout").hours)
    Ok(businessModel).future
  }
}
