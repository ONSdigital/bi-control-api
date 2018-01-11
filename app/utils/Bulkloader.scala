package utils

import java.io.File

import org.apache.spark.sql.{ Row, SparkSession }
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{ HTable, Put }
import org.apache.hadoop.hbase.KeyValue
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{ HFileOutputFormat2, LoadIncrementalHFiles }
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql
import scala.collection.mutable.ListBuffer

class Bulkloader() extends java.io.Serializable {

  def loadHbase() {

    val csvFile = new File(s"conf/sample/companyHouse.csv").toURI.toURL.toExternalForm
    val ss = SparkSession.builder().master("local").appName("appName").getOrCreate()

    val savePath = "conf/sample/hfile/d"
    val loadPath = "conf/sample/hfile"

    val df = ss.read
      .option("header", true)
      .csv(csvFile)
    val columns = df.columns
    val rd = df.rdd

    val pairs = rd.map(line => convertToKeyValuePairs(line, columns))

    val conf = HBaseConfiguration.create()
    val tableName = "august"
    val table = new HTable(conf, tableName)

    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputValueClass(classOf[KeyValue])
    HFileOutputFormat2.configureIncrementalLoadMap(job, table)

    pairs.saveAsTextFile(savePath)

    //pairs.saveAsNewAPIHadoopFile(savePath, classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat2])

    val bulkLoader = new LoadIncrementalHFiles(conf)
    bulkLoader.doBulkLoad(new Path(loadPath), table)
    ss.stop()
  }

  def convertToKeyValuePairs(line: Row, columns: Array[String]): Put = {

    val cfDataBytes = Bytes.toBytes("d")
    val rowkey = Bytes.toBytes(line(1).toString)
    val put = new Put(rowkey)
    for (x <- columns) {
      put.addColumn(cfDataBytes, Bytes.toBytes(x), Bytes.toBytes("x"))
    }
    put
    //(new ImmutableBytesWritable(rowkey), put)
  }
}