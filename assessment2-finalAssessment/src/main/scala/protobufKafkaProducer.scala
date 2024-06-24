
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors


import java.sql.Date
import java.time.LocalDateTime
import scala.util.Random
import java.util.Properties

object protobufKafkaProducer {
  val brokerList = "localhost:9092"
  val topic = "ServerHealthCheck"
  implicit val system = ActorSystem(Behaviors.empty, "MyActorSystem")

  def createProducer(brokerList: String): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", brokerList)
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    new KafkaProducer[String, String](props)
  }

  val producer = createProducer(brokerList)

  def generateRandom( end : Int)={
    val rnd = new scala.util.Random
    rnd.nextInt( end )
  }

  def callAPI() = {
    /*--------------------------
          Specifications:
        • Metric Names: Choose randomly from the following:
        1. cpu_usage_percentage (Range: 0 to 100)
        2. memory_usage_gb (Range: 0 to 64)
        3. disk_io_rate_mbps (Range: 0 to 500)
        4. network_throughput_mbps (Range: 0 to 1000)
        5. response_time_ms (Range: 0 to 2000)
        6. error_rate_percentage (Range: 0 to 100)
        • Hosts: Randomly selected from server01, server02, server03, server04,server05.
        • Regions: Randomly selected from us-east-1, us-west-2, eu-west-1, eu-central-1, ap-south-1.
        • Timestamp: current time stamp
       ------------------*/
    val metric_name : List[String] = List("cpu_usage_percentage", "memory_usage_gb", "disk_io_rate_mbps", "network_throughput_mbps" ,"response_time_ms" ,"error_rate_percentage")
    val range : Map[Int, Int] = Map(0 -> 100, 1 -> 64, 2 -> 500, 3 -> 1000, 4 -> 2000, 5 -> 100)
    val hosts : List[String] = List("server01", "server02", "server03", "server04" ,"server05")
    val regions : List[String] = List("us-east-1", "us-west-2", "eu-west-1", "eu-central-1" ,"ap-south-1")
    val currentTimestamp  = LocalDateTime. now()

    val index = generateRandom(metric_name.size)
    val metricName = metric_name(index)
    val metricValue = generateRandom(range(index))
    val host = hosts(generateRandom(hosts.size))
    val region = regions(generateRandom(regions.size))
    /*------------------------
    Example JSON Output:
    {"metricName": "cpu_usage_percentage", "value": 73.2, "timestamp": "2024-06-16T14:55:23Z", "host": "server03","region": "eu-west-1"}
    ------------------------*/
    val outputString :String = "{'metricName': '"+metricName+"', 'value': '"+metricValue+"', 'timestamp': '"+currentTimestamp+"', 'host': '"+host+"','region': '"+region+"'}"

    val record = new ProducerRecord[String, String](topic, outputString)
    producer.send(record)

    println(s"Sent to Kafka: $outputString")  // Debugging output
    complete(StatusCodes.OK, s"Message sent to Kafka: $outputString")

  }


  def main(args: Array[String]): Unit = {
    while(true) {
      callAPI()
      Thread.sleep(5000)
    }
  }

}



