package services

import javax.inject.{ Inject, Singleton }

import sys.process._

/**
 * Created by chiua on 24/10/2017.
 * When the application starts, this will be accessible in the controllers through the use of @Inject()
 */
@Singleton
class HBaseData @Inject() {
  def getOutput(period: String, id: String): String = {
    Seq("curl", "-H", "Accept: application/json", s"http://localhost:8081/${period}/${id}").!!
  }
}