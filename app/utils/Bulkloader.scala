package utils

import java.io.File
import java.util._
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext._
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.KeyValue
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.fs.Path

class Bulkloader() {

  def loadHbase() {

    val csvFile = new File(s"conf/sample/companyHouse.csv").toURI.toURL.toExternalForm
    val ss = SparkSession.builder().master("local").appName("appName").getOrCreate()

    val conf = HBaseConfiguration.create()
    val tableName = "august"
    val table = new HTable(conf, tableName)

    val companyNumber = "_c1"
    val period = "ref_period"
    val rowKey = s"$companyNumber~$period"
    val cf = "d"
    val savePath = "conf/sample/hfile/d"
    val loadPath = "conf/sample/hfile"

    //    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    //    val job = Job.getInstance(conf)
    //    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    //    job.setMapOutputValueClass(classOf[KeyValue])
    //    HFileOutputFormat.configureIncrementalLoad(job, table)

    val df = ss.read.csv(csvFile)
    val rd = df.rdd
    val headers = df.first()
    //val pairs = rd.map(line => (line.getAs[String]("_c1"), Array[String](cf, cn, value).mkString)).sortByKey(true)
    val pairs = rd.map(line => {
      for (x <- headers) {
        //println(s"line: ${line(1)}, cf: ${cf}, header: ${x}, value: ${line.getAs[String](x)}")
      }
    })
    //pairs.saveAsHadoopFile(savePath, classOf[String], classOf[String], classOf[MultipleTextOutputFormat[String, String]])
    //pairs.saveAsNewAPIHadoopFile(savePath, classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat])

    // bulk load
    //val bulkLoader = new LoadIncrementalHFiles(conf)
    //bulkLoader.doBulkLoad(new Path(loadPath), table)
    //pairs.collect()
    ss.stop()
  }
}