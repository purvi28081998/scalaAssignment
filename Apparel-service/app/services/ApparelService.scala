package services

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import daos.ApparelDAO
import javax.inject._
import models.Apparel
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json

import scala.concurrent.Future

@Singleton
class ApparelService @Inject()(apparelDAO: ApparelDAO)(implicit system: ActorSystem, mat: Materializer) {
  private val kafkaConsumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("34.125.7.167:9092")
    .withGroupId("apparel-service-stock-updates-group")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  Consumer
    .plainSource(kafkaConsumerSettings, Subscriptions.topics("stock-updates"))
    .mapAsync(1) { msg =>
      val json = Json.parse(msg.value())
      val apparelId = (json \ "apparelID").as[Long]
      val change = (json \ "change").as[Int]
      updateApparelStock(apparelId, change)
    }
    .runWith(Sink.ignore)

  private def updateApparelStock(apparelId: Long, stock: Int): Future[Unit] = {
    apparelDAO.updateStockCount(apparelId, stock)
  }

  def allApparels(): Future[Seq[Apparel]] = apparelDAO.all()

  def addApparel(apparel: Apparel): Future[Unit] = apparelDAO.insert(apparel)

  def deleteApparel(id: Long): Future[Boolean] = apparelDAO.delete(id)

  def updateCart(userID: Long, apparelID: Long, quantity: Int, price: Int, flag:Int) = apparelDAO.updateCart(userID, apparelID, quantity, price, flag)
}
