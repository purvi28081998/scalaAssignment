import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import java.util.Base64
import example.Metrics

object KafkaSparkProtobuf  {


  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("kafka-spark-kafka-protobuf")
      .master("local[*]")
      //.config("spark.driver.bindAddress", "127.0.0.1")
      .getOrCreate()

    // Important for using DataFrame operations like select
    import spark.implicits._

    // Define the schema of the JSON data
    val schema = new StructType()
      .add("metricName", StringType)
      .add("value", StringType)
      .add("timestamp", StringType)
      .add("host", StringType)
      .add("region", StringType)

    // Kafka configuration
    val kafkaTopicInput = "ServerHealthCheck"
    val kafkaTopicOutput = "ServerHealthCheckProto"
    val kafkaBootstrapServers = "localhost:9092"  // Update as necessary

    // Create DataFrame representing the stream of input lines from Kafka
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("failOnDataLoss","false")
      .option("subscribe", kafkaTopicInput)
      .option("startingOffsets", "earliest")  // From the beginning of the topic
      .load()

    // Convert the key and value from Kafka into string types, and then parse the JSON


    val parsedData = df
      .selectExpr("CAST(value AS STRING) as json_string")
      .select(from_json(col("json_string"), schema).as("data"))
      .select("data.*")


    // Convert DataFrame to proto format
    val protobufData = parsedData.map { row =>
    val metric = Metrics.ServerHealthCheck.newBuilder()
      .setHost(row.getAs[String]("host"))
      .setMetricName(row.getAs[String]("metricName"))
      .setRegion(row.getAs[String]("region"))
      .setTimestamp(row.getAs[String]("timestamp"))
      .setValue(row.getAs[String]("value"))
      .build()
    Base64.getEncoder.encodeToString(metric.toByteArray)
  }


    // Start the streaming query, writing the result to the given location in append mode
    val query = protobufData
      .writeStream
      .outputMode("append")
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("topic", kafkaTopicOutput)
      .option("checkpointLocation", "/Users/purvigupta/test/datasets/checkpoint/ServerHealthCheckPointProto")
      .trigger(Trigger.ProcessingTime("1 seconds"))
      .start()

    /*
    val query = avroDf.writeStream
      .format("console")
      .outputMode("append")
      .start()

     */

    query.awaitTermination()
  }

}


