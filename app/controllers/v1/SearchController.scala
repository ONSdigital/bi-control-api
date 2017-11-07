package controllers.v1
import java.time.{ DateTimeException, Period }
import javax.inject.Inject
import play.api.mvc.{ Action, AnyContent, Result, Controller }
import scala.concurrent.Future
import com.typesafe.config.Config
import utils.Utilities._
import models._
import services._
import play.api.libs.json._

/**
 * Created by ChiuA on 23/10/2017.
 */
class SearchController @Inject() (data: DataAccess, val config: Config) extends Controller {

  def getBusiness(period: String, id: String): Action[AnyContent] = {
    Action.async { implicit request =>
      ResultsMatcher(period, id)
    }
  }

  def ResultsMatcher(period: String, id: String): Future[Result] = {
    val businessModel = Business.toJson(data.getOutput(period, id))
    Ok(Json.toJson(businessModel)).future
  }
}