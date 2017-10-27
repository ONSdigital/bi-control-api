package controllers.v1

import java.time.{ DateTimeException, Period }
import javax.inject.Inject
import play.api.mvc.{ Action, AnyContent, Result, Controller }
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import com.typesafe.config.Config
import utils.Utilities._
import models._
import services._

/**
 * Created by ChiuA on 23/10/2017.
 */
class SearchController @Inject() (data: DataAccess, val config: Config) extends Controller {

  def getBusiness(period: String, id: String): Action[AnyContent] = {
    Action.async { implicit request =>
      proxyRequest(period, id)
    }
  }

  def proxyRequest(period: String, id: String): Future[Result] = {
    //data.loadHBaseData()
    val exitCode = data.getOutput(period: String, id: String)
    Ok(s"last, ${exitCode}").future
  }
}