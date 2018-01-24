package controllers.v1

import javax.inject.Inject

import play.api.mvc.{ Action, Controller }
import play.api.Configuration
import utils.Bulkloader

/**
 * Created by ChiuA on 23/10/2017.
 */

class SearchController @Inject() (bl: Bulkloader, config: Configuration) extends Controller {

  def testLoad = Action {
    bl.loadHbase()
    Ok("Hello world!")
  }
}
