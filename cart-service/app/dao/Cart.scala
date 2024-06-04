package daos
import models.{Cart}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import akka.stream.scaladsl.Source
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.actor.ActorSystem
import akka.stream.Materializer

@Singleton
class CartDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) {
  val dbConfig = dbConfigProvider.get[MySQLProfile]
  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("34.125.7.167:9092")
  import dbConfig._
  import profile.api._

  private class CartTable(tag: Tag) extends Table[Cart](tag, "cart") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)

    def userID = column[Long]("userID")

    def apparelID = column[Long]("apparelID")

    def quantity = column[Int]("quantity")

    def price = column[Int]("price")

    def * = (id, userID, apparelID, quantity, price) <> ((Cart.apply _).tupled, Cart.unapply)
  }

  //need to add filter to fetch cart only for existing user

  private val cart = TableQuery[CartTable]

  def all(userID: Long): Future[Seq[Cart]] = {
    db.run(cart.filter(_.userID === userID).result)
  }



  def changeStock(apparelID: Long, change: Int): Future[Unit] = {
    val message = Json.obj(
      "apparelID" -> apparelID,
      "change" -> change
    ).toString()

    val producerRecord = new ProducerRecord[String, String]("stock-updates", apparelID.toString, message)


    Source.single(producerRecord)
      .runWith(Producer.plainSink(producerSettings))
      .map(_ => ())
  }


  def insert(newItem: Cart): Future[Unit] = {
    // Step 1: Reduce stock by 1

    changeStock(newItem.apparelID, -1).flatMap { _ =>
      // Step 2: Add into cart
      getCartItemByApparelID(newItem.apparelID, newItem.userID).flatMap {
        case Some(cartItem) =>
          println(cartItem)
          updateCartItem(cartItem.id, newItem.quantity).map(_ => ())
        case None =>
          println("insert new cart item")
          db.run(cart += newItem).map(_ => ())
      }
    }
  }

  def delete(id: Long): Future[AnyVal] = {
    val query = cart.filter(cartItem => cartItem.id === id)
    db.run(query.delete).map(_ => ())
  }

  def updateCartItem(id: Option[Long], existingQuantity: Int): Future[AnyVal] = {
    println("update cart item")
    val query = for (cartItem <- cart if cartItem.id === id) yield cartItem.quantity
    db.run(query.result.headOption).flatMap {
      case Some(newQuantity) =>
        println(newQuantity, existingQuantity)
        val updatedQuantity = existingQuantity + newQuantity
        db.run(query.update(updatedQuantity)).map(_ => ())
      case None =>
        // Handle the case where the cart item with the given id doesn't exist
        Future.successful(())
    }
  }

  def getCartItemByApparelID(apparelID: Long, userID : Long): Future[Option[Cart]] = {
    println("getCartItemByApparelID: apparelID:",apparelID,"UserID:",userID)
    db.run(cart.filter(item => item.apparelID === apparelID && item.userID === userID).result.headOption)
  }

  def getCartItemByID(id: Long,userID :Long): Future[Option[Cart]] = {
    db.run(cart.filter(item => item.id === id && item.userID === userID).result.headOption)

  }

  def reduceCartItem(apparelID: Long,userID : Long): Future[AnyVal] = {
    getCartItemByApparelID(apparelID,userID).flatMap {
      case Some(cartItem) if cartItem.quantity > 1 => {
        //step 1 : increase apparel stock by 1
        changeStock(cartItem.apparelID,1)
        //step 2 : decrease cart quantity by 1
        updateCartItem(cartItem.id, -1).map(_ => ())
      }
      case Some(cartItem) => {
        //step 1 : increase apparel stock by 1
        changeStock(cartItem.apparelID,1)
        //step 2 : delete from cart
        delete(  cartItem.id.fold(0L)(identity)).map(_ => true)
      }
      case None =>
        Future.successful(false)
    }
  }

  def placeOrder(userID : Long): Future[Unit] = {
    db.run(cart.filter(item => item.userID == userID).delete).map(_ => ())
  }

  def modifyCart(newItem : Cart ,flag : Int) = {
    if(flag == -1){
      reduceCartItem(newItem.apparelID , newItem.userID)
    }
    else{
      insert(newItem)
    }
  }
}
