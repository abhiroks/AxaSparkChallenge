
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




import sbtassembly.MergeStrategy
assemblyMergeStrategy in assembly := { // this is the default plus one more for mime.types
  // See https://github.com/sbt/sbt-assembly#merge-strategy
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat

  case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename

  case PathList(ps@_*) if ps.last.startsWith("CHANGELOG.") =>
    MergeStrategy.discard

  case PathList("META-INF", xs@_*) =>
    xs map {
      _.toLowerCase
    } match {
      case "mime.types" :: _ =>
        MergeStrategy.filterDistinctLines

      case "manifest.mf" :: Nil | "index.list" :: Nil | "dependencies" :: Nil =>
        MergeStrategy.discard

      case ps@x :: _ if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard

      case "plexus" :: _ =>
        MergeStrategy.discard

      case "services" :: _ =>
        MergeStrategy.filterDistinctLines

      case "spring.schemas" :: Nil | "spring.handlers" :: Nil =>
        MergeStrategy.filterDistinctLines

      case _ => MergeStrategy.deduplicate
    }

  case _ => MergeStrategy.deduplicate

}


