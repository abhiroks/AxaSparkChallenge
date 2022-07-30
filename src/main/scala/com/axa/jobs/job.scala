package com.axa.jobs

import java.util.concurrent.Executors

import com.axa.util.TransformUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import org.slf4j.LoggerFactory

object job extends TransformUtil{

  val logger = LoggerFactory.getLogger(getClass())

  val spark = SparkSession.builder
    .master("local[*]")
    .appName("AxaSpark")
    .getOrCreate()

  val input = "/app/data/log20170201.csv"
  val output1 = "/app/data/output-sum"
  val output2 = "/app/data/output-count"

  import spark.implicits._
  implicit val context = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))

  def extract(): DataFrame = {
    val df = spark.read.format("csv").option("header", "true").load(input)
    df.printSchema()
    val new_df = df.groupBy("ip", "time").agg(sum("size"), count("size")).
      withColumnRenamed("sum(size)", "doc_sum").withColumnRenamed("count(size)", "doc_count")
    new_df.persist(StorageLevel.MEMORY_AND_DISK)


    val sumSnapshot: Future[String] = Future {

      new_df.transform(deduplicate(Seq("ip"), "doc_sum", "doc_count")).
        coalesce(1).write.option("header", "true").mode("overwrite").format("csv").save(output1)
      "sum  load completed"

    }

    val countSnapshot: Future[String] = Future {

      new_df.transform(deduplicate(Seq("ip"), "doc_count", "doc_sum")).
        coalesce(1).write.option("header", "true").mode("overwrite").format("csv").save(output2)
      "count  load completed"

    }

    val chainFutures: Future[Seq[String]] = for {
      sumData <- sumSnapshot
      countData <- countSnapshot
    } yield Seq(countData, sumData)

    /** this step gets the actual message out of the future;
      * to confirm if the execution of future threads are successful
      * */
    chainFutures onComplete {
      case Success(seqOfFutures) => seqOfFutures.map { f => logger.info(s"Calculation Done! ${f}") }
      case Failure(e) =>
        logger.error(s"Future Calculation Has Failed! ${e.getMessage}")
        spark.stop()
        context.shutdownNow()
    }

    /** we block the main thread to keep it
      * from shutting down the application until the future threads are completed;
      * because future threads are daemon threads and they get shut down when the main thread is shutdown
      * */
    while (!chainFutures.isCompleted) {
      Await.result(chainFutures, Duration.Inf)
    }

    new_df.unpersist()
    spark.stop()
    context.shutdown()


    new_df

  }



  def main(args: Array[String]): Unit = {

    extract()
    while (true) {
      Thread.sleep(30000)
    }

  }


}

