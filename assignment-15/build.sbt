ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-submittable"
  )

// Define Spark version that has better compatibility with newer Java versions
val sparkVersion = "3.2.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "mysql" % "mysql-connector-java" % "8.0.27"
)


import sbtassembly.AssemblyPlugin.autoImport._

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => xs match {
    case "MANIFEST.MF" :: Nil => MergeStrategy.discard // Custom strategy as an example
    case "module-info.class" :: Nil => MergeStrategy.concat
    case _ => MergeStrategy.discard // Or use other strategies as necessary
  }
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
