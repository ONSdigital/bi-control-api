package services

import java.time.YearMonth
import models._

import scala.util.Try

/**
 * Created by chiua on 24/10/2017.
 */
trait DataAccess {
  def loadHBaseData(): Unit
}
