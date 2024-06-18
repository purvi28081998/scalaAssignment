import org.apache.spark.sql.SparkSession

object WriteToKeyspaces extends App {

  val spark = SparkSession.builder
      .appName("Writing data to Apache Cassandra")
      .master("local[*]")
      .config("spark.cassandra.connection.host", "cassandra.ap-south-1.amazonaws.com")
      .config("spark.cassandra.connection.port", "9142")
      .config("spark.cassandra.connection.ssl.enabled", "true")
      .config("spark.cassandra.auth.username", "purvi_iam-at-767397967709")
      .config("spark.cassandra.auth.password", "gjo2b6c7wK+xez0Y2l7UzDyZiGHaKyajrVsD73zsAWvU58IhyoP2IHyKpqM=")
      .config("spark.cassandra.input.consistency.level", "LOCAL_QUORUM")
      .config("spark.cassandra.connection.ssl.trustStore.path", "/Users/purvigupta/cassandra_truststore.jks")
      .config("spark.cassandra.connection.ssl.trustStore.password", "purvi@123")
      .getOrCreate()

  // Define a case class corresponding to your schema
  case class SalesCFamily(transaction_id: Int, product_id: Int, product_name: String, customer_name: String, sales_amount: Int)

  val products = spark.read
    .format("csv")
    .option("header", "true")
    .load("/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Products.csv")

  val customers = spark.read
    .format("csv")
    .option("header", "true")
    .load("/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Customers.csv")

  val sales = spark.read
    .format("csv")
    .option("header", "true")
    .load("/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Sales.csv")

  val productsRenamed = products.withColumnRenamed("product_id", "prod_id")
  val customersRenamed = customers.withColumnRenamed("customer_id", "cust_id").withColumnRenamed("name", "customer_name")

  val salesWithProducts = sales.join(productsRenamed, sales("product_id") === productsRenamed("prod_id"))

  val transactionsWithNames = salesWithProducts.join(customersRenamed, salesWithProducts("customer_id") === customersRenamed("cust_id"))

  val salesAmount = transactionsWithNames.select(
    transactionsWithNames("transaction_id"),
    transactionsWithNames("product_id"),
    productsRenamed("name").as("product_name"),
    customersRenamed("customer_name"),
    (transactionsWithNames("units") * productsRenamed("price")).as("sales_amount")
  )

  val salesAmountDF = salesAmount.toDF("transaction_id", "product_id", "product_name", "customer_name", "sales_amount")

  salesAmountDF.show()

  salesAmountDF.write
    .format("org.apache.spark.sql.cassandra")
    .options(Map("table" -> "SalesCFamily", "keyspace" -> "abhishek_keyspace"))
    .mode("append")
    .save()

  spark.stop()
}