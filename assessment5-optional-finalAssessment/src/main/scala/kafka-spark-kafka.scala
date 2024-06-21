import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import org.apache.spark.sql.avro._

object KafkaSparkKafka  {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)
  val credentialsProvider: AwsCredentialsProvider = DefaultCredentialsProvider.builder.build
  val awsCredentials = credentialsProvider.resolveCredentials()

    def main(args: Array[String]): Unit = {
      val spark = SparkSession.builder
        .appName("Writing data to Apache Cassandra")
        .master("local[*]")
        .config("spark.cassandra.connection.host", "cassandra.eu-north-1.amazonaws.com")
        .config("spark.cassandra.connection.port", "9142")
        .config("spark.cassandra.connection.ssl.enabled", "true")
        .config("spark.cassandra.auth.username", "casssandra-user-at-058264511862")
        .config("spark.cassandra.auth.password", "w3Ya6f0gvWM2Wt3UBycR9uoT1MPCl4Rq776ua9DlpX4WDNVEbx/cfOhpRcA=")
        .config("spark.cassandra.input.consistency.level", "LOCAL_QUORUM")
        .config("spark.cassandra.connection.ssl.trustStore.path", "/Users/purvigupta/cassandra_truststore.jks")
        .config("spark.cassandra.connection.ssl.trustStore.password", "abc@123")
        .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
        .config("spark.hadoop.fs.s3a.access.key", awsCredentials.accessKeyId())
        .config("spark.hadoop.fs.s3a.secret.key", awsCredentials.secretAccessKey())
        .config("spark.hadoop.fs.s3a.endpoint", "s3.amazonaws.com")
        //.config("spark.driver.bindAddress", "127.0.0.1")
        .getOrCreate()

      // Important for using DataFrame operations like select
      import spark.implicits._

      // Define the schema of the JSON data
      val schema = new StructType()
        .add("process_time_ms", IntegerType)
        .add("threadname", StringType)
        .add("memory_used_mb", IntegerType)

      // Kafka configuration
      val kafkaTopicInput = "CPUprocessInput"
      val kafkaTopicOutput = "CPUprocessOutput"
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


      // Convert DataFrame to Avro format
      val avroDf = parsedData.select(to_avro(struct($"process_time_ms", $"threadname", $"memory_used_mb")).alias("value"))

      // Start the streaming query, writing the result to the given location in append mode

      val query = avroDf
        .writeStream
        .outputMode("append")
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaBootstrapServers)
        .option("topic", kafkaTopicOutput)
        .option("checkpointLocation", "/Users/purvigupta/test/datasets/CPUprocessOutputCheckpoint")
        .trigger(Trigger.ProcessingTime("30 seconds"))  // Trigger the write every 30 seconds
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

