package models
import java.util.Base64
import java.nio.charset.StandardCharsets
import scala.util.parsing.json.JSON._
import scala.collection.mutable.ListBuffer
import play.api.libs.json.{ JsValue, Json, OFormat }
import utils.Utilities._

case class Business(
  id: Option[String],
  businessName: Option[String],
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
    val bv: collection.mutable.Map[String, String] = hbaseMapper(jsonMap)
    Business(
      bv.get("id"),
      bv.get("vars:businessName"),
      bv.get("vars:industryCode"),
      bv.get("vars:legalStatus"),
      bv.get("vars:tradingStatus"),
      bv.get("vars:turnover"),
      bv.get("vars:employmentBands"),
      bv.get("vars:postCode"),
      bv.get("vars:vatRefs"),
      bv.get("vars:payeRefs"),
      bv.get("vars:companyNo")
    )
  }
}
