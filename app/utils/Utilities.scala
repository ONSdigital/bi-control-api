package utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.Base64
import java.nio.charset.StandardCharsets

import play.api.mvc.Controller
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result

import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by chiua on 05/11/2017.
 */
object Utilities extends Controller {

  def decodeHbase(hbaseData: Map[String, Any], column: String) = {
    val valueKey = Base64.getDecoder.decode(hbaseData.get(column).get.toString.getBytes(StandardCharsets.UTF_8))
    new String(valueKey)
  }

  def hbaseMapper(jsonMap: Option[Any]): Map[String, String] = {
    var businessVars = Map[String, String]()
    jsonMap match {
      case Some(e: Map[String, List[Map[String, Any]]]) => {
        for ((key, value) <- e)
          for (record <- value) {
            businessVars += ("id" -> decodeHbase(record, "key"))
            record.get("Cell") match {
              case Some(x: List[Map[String, Any]]) => x.map(vars => businessVars += (decodeHbase(vars, "column") -> decodeHbase(vars, "$")))
            }
          }
      }
    }
    businessVars
  }

  def errAsJson(status: Int, code: String, msg: String): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "message_en" -> msg
    )
  }

  /**
   * Run regex on a string to check for validity
   *
   *  @param toCheck String to check
   *  @param conditions Any number of regex strings
   *  @return boolean
   */
  def checkRegex(toCheck: String, conditions: String*): Boolean = conditions.toList
    .map(x => toCheck.matches(x))
    .foldLeft(false)(_ || _)

  /**
   * Method source: https://github.com/outworkers/util/blob/develop/util-play/src/main/scala/com/outworkers/util/play/package.scala#L98
   */
  implicit class ResultAugmenter(val res: Result) {
    def future: Future[Result] = {
      Future.successful(res)
    }
  }

  implicit class OptionalConversion[A](val o: Optional[A]) extends AnyVal {
    def toOption[B](implicit conv: A => B): Option[B] = if (o.isPresent) Some(o.get) else None
  }

  def validateUbrn(id: String): Boolean = {
    if (id.length equals 12) true else false
  }

  def validPeriod(p: String): Boolean = {
    Try(YearMonth.parse(p, DateTimeFormatter.ofPattern("yyyyMM"))) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def validateParams(period: String, id: String): Either[String, Result] = (period, id) match {
    case (_, i) if !validateUbrn(i) => Right(UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please Ensure that UBRN is 12 characters long")))
    case (p, _) if !validPeriod(p) => Right(UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")))
    case _ => Left(period)
  }
}