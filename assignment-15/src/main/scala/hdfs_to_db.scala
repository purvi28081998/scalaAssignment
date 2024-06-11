import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
object hdfs_to_db {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("CSV to JSON Converter")
      .getOrCreate()

    Class.forName("com.mysql.cj.jdbc.Driver")

    val inputPath = "hdfs://0.0.0.0:9000/employee.csv"
    //val inputPath = "./src/main/scala/employee.csv"
    val outputPath = "hdfs://0.0.0.0:9000/json"
    //val outputPath = "./src/main/scala/json"

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(inputPath)

    val filteredDf = df.filter(col("id") > 1)

    filteredDf.write
      .mode("overwrite")
      .json(outputPath)

    filteredDf.write
      .format("jdbc")
      .option("url", "jdbc:mysql://35.244.19.:3306/manish")
      .option("dbtable", "employee_purvi_new")
      .option("user", "root")
      .option("password", "Password@")
      .mode(SaveMode.Overwrite) // Overwrite existing data
      .save()

    spark.stop()
  }
}

