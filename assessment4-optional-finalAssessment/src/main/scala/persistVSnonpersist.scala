import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import org.apache.log4j.Logger
import org.apache.log4j.Level
object persistVSnonpersist extends App {


  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)
  val credentialsProvider: AwsCredentialsProvider = DefaultCredentialsProvider.builder.build
  val awsCredentials = credentialsProvider.resolveCredentials()
  def createSparkSession(): SparkSession = {
    SparkSession.builder
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
  }
  val absolute_path = "s3a://akka-bucket-scala/"

  val spark: SparkSession = createSparkSession()
  def readCSV(name : String) = {
    spark.read.format("csv").option("header", "true").option("delimiter", ";").load(absolute_path + name)
  }

  val transactions_df = readCSV( "transactions.csv")
  //idTransaction | idCustomer | dtTransaction | pointsTransaction - 95086
  val transactions_product_df = readCSV("transactions_product.csv")
  //idTransactionCart | idTransaction | NameProduct | QuantityProduct - 95442
  val customers_df = readCSV("customers.csv")
  //idCustomer | PointsCustomer | flEmail - 1463

  transactions_product_df.createOrReplaceTempView("transactions_product")
  transactions_df.createOrReplaceTempView("transactions")
  customers_df.createOrReplaceTempView("customers")



  var start_time = System.nanoTime
  spark.sql("""Select *
      from transactions_product tp JOIN  transactions t on t.idTransaction = tp.idTransaction""").collect()
  spark.sql("""Select *
      from customers c JOIN  transactions t on t.idCustomer = c.idCustomer""").collect()
  var end_time = System.nanoTime
  var timeWithoutPersist = end_time - start_time
  println("Time taken(in ns.) without persist : "+ timeWithoutPersist.toString)
  transactions_df.persist(StorageLevel.MEMORY_ONLY)
  //action to persist
  transactions_df.count()
  start_time = System.nanoTime
  spark.sql("""Select *
      from transactions_product tp JOIN  transactions t on t.idTransaction = tp.idTransaction""").collect()
  spark.sql("""Select *
      from customers c JOIN  transactions t on t.idCustomer = c.idCustomer""").collect()
  end_time = System.nanoTime
  var timeWithPersist = end_time - start_time

  println("Time taken(in ns.) with persist : "+ timeWithPersist.toString)
  println("Performance Improvement percentage : "+ ((timeWithoutPersist-timeWithPersist)*100/timeWithoutPersist).toString)
  Thread.sleep(20000)
  spark.stop()
}

