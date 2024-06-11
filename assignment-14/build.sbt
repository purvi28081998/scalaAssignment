ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-project"
  )



libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "mysql" % "mysql-connector-java" % "8.0.23",
  "org.apache.hadoop" % "hadoop-aws" % "3.3.1",
  "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.563",
   "software.amazon.awssdk" % "aws-sdk-java" % "2.17.32"
)



