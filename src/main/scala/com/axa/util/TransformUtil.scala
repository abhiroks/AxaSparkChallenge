package com.axa.util

import org.apache.spark.sql.execution.SparkStrategies
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{Column, DataFrame}

trait TransformUtil {

  def deduplicate(uniqueIdCols: Seq[String],
                  updatedAtCol: String,
                  deletedCol : String
                  )(dataFrame: DataFrame): DataFrame ={

    val df = dataFrame.drop(deletedCol)

    val windowSpec =
      Window
        .partitionBy(uniqueIdCols.map(new Column(_)): _*)
        .orderBy(dataFrame.col(updatedAtCol).desc_nulls_last)
    println(windowSpec)

    val windowSpec1 = Window.orderBy(dataFrame.col(updatedAtCol).desc_nulls_last)

    val outputDF = df
      .withColumn("_rank_", org.apache.spark.sql.functions.row_number().over(windowSpec))
      .filter("_rank_ = 1")
      .drop("_rank_")
    outputDF.show(30)
    val finalDF  = outputDF.
      withColumn("_rank_", org.apache.spark.sql.functions.row_number().over(windowSpec1))
      .filter("_rank_ <= 10")
      .drop("_rank_")

    finalDF
  }

}
