import example.Metrics
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.Encoders
import java.util.Base64

object KafkaSparkProtobufCSV  {


  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("kafka-spark-kafka-protobuf-csv")
      .master("local[*]")
      //.config("spark.driver.bindAddress", "127.0.0.1")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Important for using DataFrame operations like select
    import spark.implicits._


    // Kafka configuration
    val kafkaTopicInput = "ServerHealthCheckProto"
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
    val deserializedData = df
      .selectExpr("CAST(value AS STRING) as encoded_value")
      .as[String]
      .flatMap { encodedValue =>
        val bytes = Base64.getDecoder.decode(encodedValue)
        val metric = Metrics.ServerHealthCheck.parseFrom(bytes)
        try {
          Some((
            metric.getHost,
            metric.getMetricName,
            metric.getRegion,
            metric.getTimestamp,
            metric.getValue
          ))
        } catch {
          case e: Exception => None
        }
      }(Encoders.tuple(Encoders.STRING, Encoders.STRING, Encoders.STRING, Encoders.STRING, Encoders.STRING))
      .toDF("host", "metricName", "region", "timestamp", "value")


    // Start the streaming query, writing the result to the given location in append mode
    val query = deserializedData
      .writeStream
      .outputMode("append")
      .format("csv")
      .option("path", "/Users/purvigupta/test/datasets/data/ServerHealthCheckPointCSV")
      .option("checkpointLocation", "/Users/purvigupta/test/datasets/checkpoint/ServerHealthCheckPointCSV")
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .partitionBy("metricName")
      .start()


    /*val query = deserializedData.writeStream
      .format("console")
      .outputMode("append")
      .start()*/

    query.awaitTermination()
  }

}


