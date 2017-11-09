package models
import scala.util.parsing.json.JSON._
import play.api.libs.json.{ JsValue, Json, OFormat }
import utils.Utilities._

case class Business(
  id: Long,
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
  def toJson(business: String): JsValue = {
    val jsonMap = parseFull(business)
    val businessVars: Map[String, String] = hbaseMapper(jsonMap)
    Json.toJson(
      Business(
        businessVars.get("id").get.toLong,
        businessVars.get("vars:businessName").get,
        businessVars.get("vars:industryCode"),
        businessVars.get("vars:legalStatus"),
        businessVars.get("vars:tradingStatus"),
        businessVars.get("vars:turnover"),
        businessVars.get("vars:employmentBands"),
        businessVars.get("vars:postCode"),
        businessVars.get("vars:vatRefs"),
        businessVars.get("vars:payeRefs"),
        businessVars.get("vars:companyNo")
      )
    )
  }
}
