package utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.Base64
import java.nio.charset.StandardCharsets

import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result

import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by chiua on 05/11/2017.
 */
object Utilities {

  def decodeHbase(hbaseData: Map[String, Any], column: String) = {
    val valueKey = Base64.getDecoder.decode(hbaseData.get(column).get.toString.getBytes(StandardCharsets.UTF_8))
    new String(valueKey)
  }

  def hbaseMapper(jsonMap: Option[Any]): Map[String, String] = {
    var businessVars = Map[String, String]()
    jsonMap match {
      //strips out some
      case Some(e: Map[String, List[Map[String, Any]]]) => {
        //loops over initial map
        for ((key, value) <- e) {
          //gets contents of first list of maps
          for (record <- value) {
            //gets key(ubrn)
            businessVars += ("id" -> decodeHbase(record, "key"))
            //gets second list of maps(columns)
            record.get("Cell") match {
              case Some(x: List[Map[String, Any]]) => {
                //loops through columns in second list of maps
                for (vars <- x)
                  businessVars += (decodeHbase(vars, "column") -> decodeHbase(vars, "$"))
              }
            }
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

  def periodToYearMonth(period: String): YearMonth = {
    YearMonth.parse(period.slice(0, 6), DateTimeFormatter.ofPattern("yyyyMM"))
  }

  def validateUbrn(id: String): Boolean = {
    if (id.length equals 12) true else false
  }
}