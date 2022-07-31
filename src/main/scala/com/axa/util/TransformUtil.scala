package com.axa.util

import com.axa.jobs.job.spark
import org.apache.spark.sql.execution.SparkStrategies
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{count, min, sum}
import org.apache.spark.sql.{Column, DataFrame}

trait TransformUtil {

  def aggCal(uniqueIdCols: Seq[String],
             rankCol: String,
             deletedCol : String
                  )(dataFrame: DataFrame): DataFrame ={

    val df = dataFrame.drop(deletedCol)



    val windowSpec = Window.orderBy(dataFrame.col(rankCol).desc_nulls_last)

    val finalDF  = df.
      withColumn("_rank_", org.apache.spark.sql.functions.row_number().over(windowSpec))
      .filter("_rank_ <= 10")
      .drop("_rank_")

    finalDF
  }


}
