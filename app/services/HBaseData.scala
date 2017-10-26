package services

import java.io.File
import java.time.YearMonth
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.Config

import org.apache.hadoop.util.ToolRunner
import play.api.Logger
import uk.gov.ons.sbr.data.controller.AdminDataController
import uk.gov.ons.sbr.data.hbase.HBaseConnector
import uk.gov.ons.sbr.data.hbase.load.BulkLoader
import uk.gov.ons.sbr.data.domain.UnitType
import models._

/**
 * Created by chiua on 24/10/2017.
 * When the application starts, this will be accessible in the controllers through the use of @Inject()
 */
@Singleton
class HBaseData @Inject() (val loadData: Boolean, val config: Config) extends DataAccess {

  private val adminController = new AdminDataController()
  HBaseConnector.getInstance().connect()

  if (loadData) loadHBaseData()

  def loadHBaseData(): Unit = {
    Logger.info("Loading local CSVs into In-Memory HBase...")
    val bulkLoader = new BulkLoader()
    val firstPeriod = "201706"
    val secondPeriod = "201708"

    List(
      List[String](UnitType.COMPANY_REGISTRATION.toString, firstPeriod, new File(s"conf/sample/businessIndex.csv").toURI.toURL.toExternalForm),
      List[String](UnitType.COMPANY_REGISTRATION.toString, secondPeriod, new File(s"conf/sample/businessIndex.csv").toURI.toURL.toExternalForm)
    ).foreach(arg => {
        Logger.info(s"Loading CSV [${arg(2)}] into HBase for period [${arg(1)}] and type [${arg(0)}]...")
        ToolRunner.run(HBaseConnector.getInstance().getConfiguration(), bulkLoader, arg.toArray)
      })
  }
}