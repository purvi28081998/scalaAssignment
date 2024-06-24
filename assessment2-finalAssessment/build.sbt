ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"


lazy val root = (project in file("."))
  .settings(
    name := "akka-stream-http-kafka-producer"
  )
resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val akkaVersion = sys.props.getOrElse("akka.version", "2.8.5")
// Define Spark version that has better compatibility with newer Java versions
val sparkVersion = "3.2.1"
val jacksonVersion = "2.12.5"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.scalatest" %% "scalatest" % "3.2.18" % "test",
  "org.apache.hadoop" % "hadoop-aws" % "3.3.1",
  "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.563",
  "software.amazon.awssdk" % "aws-sdk-java" % "2.17.32",
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.2.18" % "test",
  "com.github.jnr" % "jnr-posix" % "3.1.19",
  "joda-time" % "joda-time" % "2.12.7",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.2.1",
  "org.apache.spark" %% "spark-avro" % "3.2.1",
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.google.protobuf" % "protobuf-java" % "4.27.1",
)

// Exclude any transitive dependencies of Jackson Databind that might conflict

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion

// Fork in run and set java options
fork in run := true

javaOptions ++= Seq(
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED"

)

