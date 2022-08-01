package com.axa.jobs

import java.util.concurrent.Executors

import com.axa.util.TransformUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType
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


    /** Here we are sessionizing the data .
      * Main goal here to identify which rows belong to one session based on the 30 minutes rule
      * Then we persist the data in memory as it will re used twice to calculate Top 10 session based on size and number of count
      * */


    val df1 = df.withColumn("date_time",unix_timestamp(concat($"date",lit(" "),$"time"),"dd/MM/yyyy HH:mm:ss").cast(TimestampType)).
      select($"ip",$"date_time",$"size")
    //df1.show(50)
    df1.createOrReplaceTempView("source")
    val sessionized_df = spark.sql("select ip,date_time ,size, SUM(is_new_session) OVER (PARTITION BY ip ORDER BY date_time) AS user_session_id " +
      "from (select * , CASE WHEN (unix_timestamp(date_time) - unix_timestamp(last_event)) >= (60 * 30) OR last_event IS NULL THEN 1 ELSE 0 END AS is_new_session FROM " +
      "(SELECT *,LAG(date_time,1) OVER (PARTITION BY ip ORDER BY date_time) AS last_event FROM source) last ) final")


    val sessionized_grouped_df = sessionized_df.groupBy("ip", "user_session_id").agg(sum("size"), count("size"),min("date_time")).
      withColumnRenamed("sum(size)", "doc_sum").withColumnRenamed("count(size)", "doc_count").
      withColumnRenamed("min(date_time)", "session_start_time")

    sessionized_grouped_df.show(50)
    sessionized_grouped_df.persist(StorageLevel.MEMORY_AND_DISK)

    val sumSnapshot: Future[String] = Future {

      sessionized_grouped_df.transform(aggCal(Seq("ip","user_session_id","session_start_time"), "doc_sum", "doc_count")).
        coalesce(1).write.option("header", "true").mode("overwrite").format("csv").save(output1)
      "sum  load completed"
    }

    /** Here we are creating two separate function to calculate count and sum .
      * We are running both of them simultaneously in Thread as they do not depend on each other
      * */


    val countSnapshot: Future[String] = Future {


      sessionized_grouped_df.transform(aggCal(Seq("ip","user_session_id","session_start_time"), "doc_count", "doc_sum")).
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

    sessionized_grouped_df.unpersist()
    spark.stop()
    context.shutdown()



    df

  }



  def main(args: Array[String]): Unit = {

    extract()
    while (true) {
      Thread.sleep(30000)
    }

  }


}

