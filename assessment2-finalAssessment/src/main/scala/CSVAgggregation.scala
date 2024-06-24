import org.apache.spark.sql.SparkSession
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import java.io.{File, PrintWriter}

object CSVAgggregation  {

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



      def getListOfSubDirectories(directoryName: String): Array[String] = {
        new File(directoryName)
          .listFiles
          .filter(_.isDirectory)
          .filter(dir => !dir.getName.contains("_spark_metadata"))
          .map(_.getPath)
      }

      def processFolder(folderPath: String): Unit = {
        println(s"Processing folder: $folderPath")

        val df = spark.read
          .option("header", "false") // Specifying that files do not contain headers
          .option("inferSchema", "true")
          .csv(s"$folderPath/*.csv")


        val recordCount = df.count()

        val writer = new PrintWriter(new File(s"$folderPath/count.txt"))
        try {
          println(s"Writing into $folderPath/count.txt")
          writer.write(s"Total records: $recordCount\n")
        } finally {
          writer.close()
        }
      }

      val baseDirectory = "/Users/purvigupta/test/datasets/data/ServerHealthCheckPointCSV"

      val subFolders = getListOfSubDirectories(baseDirectory)

      subFolders.foreach(processFolder)
      spark.stop()
  }
}