
name := "AxaSpark"

version := "0.1"

scalaVersion := "2.11.12"
val sparkVersion = "2.2.1"

resolvers ++= Seq(
  "confluent" at "http://packages.confluent.io/maven/"

)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion

  //"com.rakuten.dps" %% "dps-base-batch" % "1.3",
  // to resolve conflicts
//  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.6",
//  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.6",
//  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6",
//  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.9.6",
//  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6",
//
//  "org.json4s" %% "json4s-native" % "3.5.1",
//  "org.json4s" %% "json4s-jackson" % "3.5.1",
//  "com.github.pureconfig" %% "pureconfig" % "0.10.2",
//  "com.outr" %% "hasher" % "1.2.1",
//  "com.github.scopt" %% "scopt" % "3.7.1",
//
//  "org.scalatest" %% "scalatest" % "3.0.5" % Test

)