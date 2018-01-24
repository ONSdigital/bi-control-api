package utils

import java.io.File

import org.apache.spark.sql.SparkSession
import org.apache.hadoop.hbase.{ HBaseConfiguration, KeyValue }
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{ HFileOutputFormat2, LoadIncrementalHFiles }
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.fs.{ Path, FileSystem }

class Bulkloader() extends java.io.Serializable {

  def loadHbase() {

    val csvFile = new File(s"conf/sample/businessIndex.csv").toURI.toURL.toExternalForm
    val ss = SparkSession.builder().master("local").appName("appName").getOrCreate()

    val path = "conf/sample/hfile"
    val id = "companyname"

    val period = "201706"
    val tableName = "june"
    val colFamily = "d"

    val df = ss.read
      .option("header", true)
      .csv(csvFile)
      .sort(id)

    val sortedCol = df.columns.sorted

    val pairs = df.rdd.flatMap(line => {
      val rowKey = s"${line.getAs(id)}~$period"
      for (i <- sortedCol) yield {
        val value = if (line.getAs[String](i) == null) "" else line.getAs[String](i)
        (new ImmutableBytesWritable(rowKey.getBytes()), new KeyValue(rowKey.getBytes(), colFamily.getBytes(), i.getBytes(), value.getBytes()))
      }
    })

    val conf = HBaseConfiguration.create()
    val table = new HTable(conf, tableName)
    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    conf.setInt("hbase.mapreduce.bulkload.max.hfiles.perRegion.perFamily", 500)
    val job = Job.getInstance(conf)
    job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setMapOutputValueClass(classOf[KeyValue])
    HFileOutputFormat2.configureIncrementalLoadMap(job, table)
    val fs = FileSystem.get(conf)

    pairs.saveAsNewAPIHadoopFile(path, classOf[ImmutableBytesWritable], classOf[KeyValue], classOf[HFileOutputFormat2])
    val bulkLoader = new LoadIncrementalHFiles(conf)
    bulkLoader.doBulkLoad(new Path(path), table)
    fs.delete(new Path(path))
    ss.stop()
  }
}