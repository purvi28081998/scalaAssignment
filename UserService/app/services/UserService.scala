package services

import daos.UserDAO
import models.User

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userDAO: UserDAO)(implicit ec: ExecutionContext) {

  def createUser(name: String, email: String, password: String, phoneNumber: String): Future[User] = {
    val user = User(None, name, email, password, phoneNumber )
    userDAO.create(user)
  }
  def authenticate( email: String, password:String) = {
    userDAO.authenticate( email, password)
  }
  def updateUser(id: Int, name: String, email: String, password: String, phoneNumber: String): Future[Int] = {
    val updatedUser = User(Some(id), name, email, password, phoneNumber)
    userDAO.update(updatedUser)
  }

  def deleteUser(id: Int): Future[Int] = {
    userDAO.delete(id)
  }

  def getUser(id: Int): Future[Option[User]] = {
    userDAO.findById(id)
  }

  def listUsers(): Future[Seq[User]] = {
    userDAO.findAll()
  }

//  def getUserTransactions(id: Int): Future[Seq[Transaction]] = {
//
//  }
}

