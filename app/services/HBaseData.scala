package services

import java.time.{ Period, YearMonth }
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.Config
import models._

import sys.process._

/**
 * Created by chiua on 24/10/2017.
 * When the application starts, this will be accessible in the controllers through the use of @Inject()
 */
@Singleton
class HBaseData @Inject() (val loadData: Boolean, val config: Config) extends DataAccess {
  def getOutput(period: String, id: String): String = {
    Seq("curl", "-H", "Accept: application/json", s"http://localhost:8081/${period}/${id}").!!
  }
}