package utils

import java.io.File

import org.apache.spark.sql.{ Row, SparkSession }
import org.apache.hadoop.hbase.{ HBaseConfiguration, KeyValue }
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{ HFileOutputFormat2, LoadIncrementalHFiles }
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.util.Bytes

class Bulkloader() extends java.io.Serializable {

  def loadHbase() {

    val csvFile = new File(s"conf/sample/sbr-2500-ent-ch-data.csv").toURI.toURL.toExternalForm
    val ss = SparkSession.builder().master("local").appName("appName").getOrCreate()

    val path = "conf/sample/hfile"
    val hbasePath = "/usr/local/Cellar/hbase/1.2.6/libexec/conf"
    val column = "companyname"
    val period = "201706"

    val df = ss.read
      .option("header", true)
      .csv(csvFile)
      .sort("companynumber")

    val pairs = df.rdd.map(line => convertToKeyValuePairs(line, column, period))

    val conf = HBaseConfiguration.create()
    val tableName = "june"
    val table = new HTable(conf, tableName)

    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    conf.addResource(new File(s"$hbasePath/hbase-site.xml").toURI.toURL)
    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputValueClass(classOf[KeyValue])
    HFileOutputFormat2.configureIncrementalLoadMap(job, table)

    pairs.saveAsNewAPIHadoopFile(path, classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat2])
    val bulkLoader = new LoadIncrementalHFiles(conf)
    bulkLoader.doBulkLoad(new Path(path), table)
    ss.stop()
  }

  def convertToKeyValuePairs(line: Row, column: String, period: String): (ImmutableBytesWritable, KeyValue) = {
    val rowkey = Bytes.toBytes(s"${line(1)}~$period")
    val cell = new KeyValue(
      rowkey,
      Bytes.toBytes("d"),
      Bytes.toBytes(column),
      Bytes.toBytes(line.getAs[String](column))
    )
    (new ImmutableBytesWritable(rowkey), cell)
  }
}