
name := "AxaSpark"

version := "0.1"

scalaVersion := "2.12.4"
val sparkVersion = "2.4.1"

resolvers ++= Seq(
  "confluent" at "http://packages.confluent.io/maven/",
  "spark-packages" at "https://repos.spark-packages.org/"

)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

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




