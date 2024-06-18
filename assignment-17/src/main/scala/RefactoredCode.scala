import org.apache.spark.sql.{DataFrame, SparkSession}

object RefactoredCode extends App {

  val spark: SparkSession = createSparkSession()

  val productsPath = "/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Products.csv"
  val customersPath = "/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Customers.csv"
  val salesPath = "/Users/purvigupta/IdeaProjects/Day17Task/src/main/scala/Sales.csv"

  val products = readCSV(spark, productsPath)
  val customers = readCSV(spark, customersPath)
  val sales = readCSV(spark, salesPath)

  val productsRenamed = renameProductColumns(products)
  val customersRenamed = renameCustomerColumns(customers)
  val salesWithProducts = joinSalesWithProducts(sales, productsRenamed)
  val transactionsWithNames = joinSalesWithCustomers(salesWithProducts, customersRenamed)
  val salesAmountDF = calculateSalesAmount(transactionsWithNames, productsRenamed, customersRenamed)

  salesAmountDF.show()

  writeToCassandra(salesAmountDF, "SalesCFamily", "purvi_keyspace")

  spark.stop()

  def createSparkSession(): SparkSession = {
    SparkSession.builder
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
  }

  def readCSV(spark: SparkSession, path: String): DataFrame = {
    spark.read
      .format("csv")
      .option("header", "true")
      .load(path)
  }

  def renameProductColumns(products: DataFrame): DataFrame = {
    products.withColumnRenamed("product_id", "prod_id")
  }

  def renameCustomerColumns(customers: DataFrame): DataFrame = {
    customers.withColumnRenamed("customer_id", "cust_id")
      .withColumnRenamed("name", "customer_name")
  }

  def joinSalesWithProducts(sales: DataFrame, products: DataFrame): DataFrame = {
    sales.join(products, sales("product_id") === products("prod_id"))
  }

  def joinSalesWithCustomers(salesWithProducts: DataFrame, customers: DataFrame): DataFrame = {
    salesWithProducts.join(customers, salesWithProducts("customer_id") === customers("cust_id"))
  }

  def calculateSalesAmount(transactionsWithNames: DataFrame, products: DataFrame, customers: DataFrame): DataFrame = {
    transactionsWithNames.select(
      transactionsWithNames("transaction_id"),
      transactionsWithNames("product_id"),
      products("name").as("product_name"),
      customers("customer_name"),
      (transactionsWithNames("units") * products("price")).as("sales_amount")
    ).toDF("transaction_id", "product_id", "product_name", "customer_name", "sales_amount")
  }

  def writeToCassandra(df: DataFrame, table: String, keyspace: String): Unit = {
    df.write
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> table, "keyspace" -> keyspace))
      .mode("append")
      .save()
  }
}
