package config

import com.typesafe.config.Config
import play.api.Logger

object SbrConfigManager {
  def envConf(conf: Config): Config = {
    // Get the environment variable 'source' that we pass in
    val src = sys.props.get("source").getOrElse("default")
    Logger.info(s"Load config for [$src] src")
    val envConf = conf.getConfig(s"src.$src")
    println(envConf)
    Logger.debug(envConf.toString)
    envConf
  }
}
