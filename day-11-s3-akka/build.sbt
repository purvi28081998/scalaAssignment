ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "Day_11_Task"
  )
resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val akkaVersion = sys.props.getOrElse("akka.version", "2.9.3")

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.13",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.2.9"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.18"
libraryDependencies += "software.amazon.awssdk" % "s3" % "2.17.103"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "6.0.0",
  "com.typesafe.akka" %% "akka-http" % "10.6.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.6.3",
  "org.apache.kafka" %% "kafka" % "3.7.0" // Kafka client
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.9",
  "com.typesafe.akka" %% "akka-stream" % "2.6.19",
  "software.amazon.awssdk" % "s3" % "2.17.106",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9",
  "com.typesafe" % "config" % "1.4.1",
  "io.spray" %% "spray-json" % "1.3.6"
)