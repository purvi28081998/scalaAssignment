package daos

import models.{User, UserTable}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[MySQLProfile]
  import dbConfig._
  import profile.api._

  val users = TableQuery[UserTable]

  def create(user: User): Future[User] = {
    val insertQuery = (users returning users.map(_.id)
      into ((user, id) => user.copy(id = Some(id)))
      ) += user
    db.run(insertQuery)
  }


  def authenticate( email: String, password:String) = {
    db.run(users.filter(user => user.email === email && user.password === password).result.headOption)
  }

  def findById(id: Int): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def update(user: User): Future[Int] = {
    db.run(users.filter(_.id === user.id).update(user))
  }

  def delete(id: Int): Future[Int] = {
    db.run(users.filter(_.id === id).delete)
  }

  def findAll(): Future[Seq[User]] = {
    db.run(users.result)
  }
}
