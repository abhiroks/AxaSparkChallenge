package com.axa.jobs

import com.axa.util.TransformUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

object job extends TransformUtil{

  val spark = SparkSession.builder
    .master("local[*]")
    .appName("AxaSpark")
    .getOrCreate()

  val input = "/data"

  import spark.implicits._

  def extract(): DataFrame = {
    val df = spark.read.format("csv").option("header", "true").load("/data/log20170211.csv")
    df.printSchema()
    val new_df = df.groupBy("ip","time").agg(sum("size"),count("size")).
      withColumnRenamed("sum(size)","doc_sum").withColumnRenamed("count(size)","doc_count")
    new_df.persist(StorageLevel.MEMORY_AND_DISK)


    new_df
  }

  def transload(df : DataFrame): DataFrame = {


    val sum_df = df.transform(deduplicate(Seq("ip"),"doc_sum","doc_count"))
    sum_df.show(20)
    val count_df = df.transform(deduplicate(Seq("ip"),"doc_count","doc_sum"))
    count_df.show(20)

    df

  }

  def main(args: Array[String]): Unit = {

    transload(extract())
    //load(transform(extract()))
  }


}

