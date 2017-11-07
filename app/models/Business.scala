package models
import java.util.Base64
import java.nio.charset.StandardCharsets
import scala.util.parsing.json.JSON._
import scala.collection.mutable.ListBuffer
import play.api.libs.json.{ JsValue, Json, OFormat }
import utils.Utilities._

case class Business(
  id: Long, //ubrn
  businessName: String,
  industryCode: Option[String],
  legalStatus: Option[String],
  tradingStatus: Option[String],
  turnover: Option[String],
  employmentBands: Option[String],
  postCode: Option[String],
  vatRefs: Option[String],
  payeRefs: Option[String],
  companyNo: Option[String]
)

object Business {
  implicit val unitFormat: OFormat[Business] = Json.format[Business]

  implicit def string2Option(s: String): Some[String] = Some(s)

  def toJson(business: String): Business = {
    val jsonMap = parseFull(business)
    val bv = hbaseMapper(jsonMap)
    Business(bv(0).toLong, bv(1), bv(2), bv(3), bv(4), bv(5), bv(6), bv(7), bv(8), bv(9), bv(10))
  }
}
