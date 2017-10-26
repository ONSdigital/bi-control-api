package controllers.v1

import java.time.DateTimeException
import javax.inject.Inject

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import play.api.mvc.{ Action, AnyContent, Result, Controller }
import play.api.Logger
import com.typesafe.config.Config

import utils.Utilities._
import models._
import services._

/**
 * Created by ChiuA on 23/10/2017.
 */
class SearchController @Inject() (data: DataAccess, val config: Config) extends Controller {

  def getBusiness(period: String, ubrn: String): Action[AnyContent] = {
    Action.async { implicit request =>
      proxyRequest()
    }
  }

  def proxyRequest(): Future[Result] = {
    data.loadHBaseData()
    Ok("good").future
  }

  //  def getBusiness(period: String, ubrn: String): Action[AnyContent] = {
  //    Action.async { implicit request =>
  //      Try(periodToYearMonth(period)) match {
  //        case Success(validPeriod) => Try(data.getBusinessByIdForPeriod(validPeriod, ubrn)) match {
  //          case Success(results) => resultsMatcher(results, ubrn)
  //          case Failure(e) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator $e")).future
  //        }
  //        case Failure(_: DateTimeException) => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")).future
  //        case _ => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
  //      }
  //    }
  //  }

  //  def resultsMatcher(results: List[Business], ubrn: String): Future[Result] = results match {
  //    case Nil => NotFound(errAsJson(404, "Not Found", s"Could not find ubrn:  $ubrn")).future
  //    case x => Ok(Business.toJson(x)).future
  //  }
}