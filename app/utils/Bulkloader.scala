package utils

import java.io.File

import org.apache.spark.sql.SparkSession
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.KeyValue
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{HFileOutputFormat, HFileOutputFormat2, LoadIncrementalHFiles}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.fs.Path

class Bulkloader() {

  def loadHbase() {

    val csvFile = new File(s"conf/sample/companyHouse.csv").toURI.toURL.toExternalForm
    val ss = SparkSession.builder().master("local").appName("appName").getOrCreate()

    val cf = "d"
    val savePath = "conf/sample/hfile/d"
    val loadPath = "conf/sample/hfile"

    val df = ss.read
      .option("header", true)
      .csv(csvFile)
    val columns = df.columns
    val rd = df.rdd

    val pairs = rd.map(line => {
      for (x <- columns) {
        println(s"line: ${line(1)}, cf: ${cf}, header: ${x}, value: ${line.getAs[String](x)}")
      }
    })

    val conf = HBaseConfiguration.create()
    val tableName = "august"
    val table = new HTable(conf, tableName)

    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputValueClass(classOf[KeyValue])
    HFileOutputFormat2.configureIncrementalLoadMap(job, table)

    pairs.saveAsNewAPIHadoopFile(savePath, classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat2])

    val bulkLoader = new LoadIncrementalHFiles(conf)
    bulkLoader.doBulkLoad(new Path(loadPath), table)

    pairs.collect()
    ss.stop()
  }
}