package models
import java.sql.ResultSet
import play.api.libs.json.{ JsValue, Json, OFormat }
import scala.util.parsing.json.JSON

import utils.RsIterator

case class Business(
  id: Long, //ubrn
  businessName: String,
  uprn: Option[Long],
  industryCode: Option[String],
  legalStatus: Option[String],
  tradingStatus: Option[String],
  turnover: Option[String],
  employmentBands: Option[String],
  postCode: Option[String],
  vatRefs: Option[Seq[Long]],
  payeRefs: Option[Seq[String]],
  companyNo: Option[String]
)

object Business {
  implicit val unitFormat: OFormat[Business] = Json.format[Business]
  def toJson(business: String): JsValue = Json.toJson(business)
}
