package controllers.v1

import java.time.{ DateTimeException }
import javax.inject.Inject

import play.api.mvc.{ Action, AnyContent, Result, Controller }
import play.api.libs.json._

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

class SearchController @Inject() (data: HBaseData) extends Controller {

  val system = ActorSystem("bi-breaker")
  val userActor = system.actorOf(Props[CircuitBreakerActor], name = "User")

  def getBusiness(period: String, id: String): Action[AnyContent] = {
    Action.async { implicit request =>
      id match {
        case id if validateUbrn(id) => Try(periodToYearMonth(period)) match {
          case Success(validPeriod) => Try(data.getOutput(period, id)) match {
            case Success(results) => {
              userActor ! "Success"
              Try(Business.toJson(results)) match {
                case Success(results) => ResultsMatcher(results)
                case Failure(_) => NotFound(errAsJson(404, "Not Found", s"Could not find value ${id}")).future
              }
            }
            case _ => {
              userActor ! "Failure"
              InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
            }
          }
          case Failure(_: DateTimeException) => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")).future
        }
        case _ => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please Ensure that UBRN is 12 characters long")).future
      }
    }
  }

  def ResultsMatcher(businessModel: JsValue): Future[Result] = {
    Ok(businessModel).future
  }
}