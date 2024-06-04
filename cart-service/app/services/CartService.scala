package services

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import daos.CartDAO
import javax.inject._
import models.Cart
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.Json
import scala.concurrent.Future

@Singleton
class CartService @Inject()(cartDAO: CartDAO)(implicit system: ActorSystem, mat: Materializer) {
  private val kafkaConsumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("34.125.7.167:9092")
    .withGroupId("cart-service-group")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

  Consumer
    .plainSource(kafkaConsumerSettings, Subscriptions.topics("cart-update-topic"))
    .mapAsync(1) { msg =>
      val json = Json.parse(msg.value())
      val userID = (json \ "userID").as[Long]
      val apparelID = (json \ "apparelID").as[Long]
      val quantity = (json \ "quantity").as[Int]
      val price = (json \ "price").as[Int]
      val flag = (json \ "flag").as[Int]
      println("kafka msg:", userID, apparelID, quantity, price, flag)
      updateCart(userID, apparelID, quantity, price, flag)
    }
    .runWith(Sink.ignore)



  private def updateCart( userID : Long , apparelID : Long , quantity : Int , price: Int , flag : Int): Future[AnyVal] = {
    val newItem : Cart = Cart(None, userID, apparelID, quantity, price)
    cartDAO.modifyCart(newItem, flag)
  }

  def allCarts(userID : Long): Future[Seq[Cart]] = cartDAO.all(userID)

  def addCart(cart: Cart): Future[Unit] = cartDAO.insert(cart)

  def deleteCart(apparelID: Long,userID: Long): Future[AnyVal] = cartDAO.reduceCartItem(apparelID,userID )
}
