package daos

import models.Apparel
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.{Future, ExecutionContext}
import play.api.libs.json.Json
import akka.stream.scaladsl.Source

class ApparelDAO @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
  import dbConfig._
  import profile.api._

  private class ApparelTable(tag: Tag) extends Table[Apparel](tag, "apparel") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def price = column[Int]("price")
    def stock = column[Int]("stock")
    def * = (id, name,  price ,stock) <> ((Apparel.apply _).tupled, Apparel.unapply)
  }

  private val apparels = TableQuery[ApparelTable]

  def all(): Future[Seq[Apparel]] = db.run(apparels.result)

  def insert(apparel: Apparel): Future[Unit] = db.run(apparels += apparel).map(_ => ())

  def delete(id: Long): Future[Boolean] = {
    val query = apparels.filter(apparel => apparel.id === id && apparel.stock>0)
    db.run(query.delete).map(_ > 0)
  }
  //kafka producer



  def updateCart(userID: Long, apparelID: Long, quantity: Int, price: Int, flag:Int): Future[Unit] = {
    val message = Json.obj(
    "userID" -> userID,
    "apparelID" -> apparelID,
    "quantity" -> quantity,
    "price" -> price,
    "flag" -> flag
    ).toString()

    val producerRecord = new ProducerRecord[String, String]("cart-update-topic", userID.toString, message)


    Source.single(producerRecord)
      .runWith(Producer.plainSink(producerSettings))
      .map(_ => ())

  }

  //via kafka
  def updateStockCount(id: Long, change: Int): Future[Unit] = {
    val query = for (apparel <- apparels if apparel.id === id ) yield apparel.stock
    db.run(query.result.headOption).flatMap {
      case Some(newStock) =>
        println(newStock, change)
        val updatedQuantity = change + newStock
        db.run(query.update(updatedQuantity)).map(_ => ())
      case None =>
        // Handle the case where the cart item with the given id doesn't exist
        Future.successful(())

  }
}
  }