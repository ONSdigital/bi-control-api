package models
import java.sql.ResultSet
import play.api.libs.json.{ JsValue, Json, Writes }
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
  implicit val writer = new Writes[Business] {
    def writes(b: Business): JsValue = {
      Json.obj(
        "UBRN" -> b.id,
        "Vars" -> Json.obj(
          "BusinessName" -> b.businessName,
          "UPRN" -> b.uprn,
          "PostCode" -> b.postCode,
          "IndustryCode" -> b.industryCode,
          "LegalStatus" -> b.legalStatus,
          "TradingStatus" -> b.tradingStatus,
          "Turnover" -> b.turnover,
          "EmploymentBands" -> b.employmentBands,
          "VatRefs" -> b.vatRefs,
          "PayeRefs" -> b.payeRefs,
          "CompanyNo" -> b.companyNo
        )
      )
    }
  }
  def toJson(business: List[Business]): JsValue = Json.toJson(business)
}
