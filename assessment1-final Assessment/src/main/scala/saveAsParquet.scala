
import org.apache.spark.sql.{DataFrame, SparkSession}
import software.amazon.awssdk.auth.credentials.{DefaultCredentialsProvider, AwsCredentialsProvider}


object saveAsParquet extends App {
  val credentialsProvider: AwsCredentialsProvider = DefaultCredentialsProvider.builder.build
  val awsCredentials = credentialsProvider.resolveCredentials()
  def createSparkSession(): SparkSession = {
    SparkSession.builder
      .appName("Writing data to AWS S3")
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
      .getOrCreate()
  }


  val spark: SparkSession = createSparkSession()

  // Define a case class corresponding to your schema

  val aqi = spark.read
    .format("org.apache.spark.sql.cassandra")
    .options(Map( "table" -> "AQI", "keyspace" -> "tutorialkeyspace"))
    .load()


  aqi.printSchema()
  aqi.write.format("parquet").mode("overwrite").save("s3a://akka-bucket-scala/athens_data_parquet")

  spark.stop()
}

