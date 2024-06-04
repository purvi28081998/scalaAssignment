package models

import models.User
import slick.jdbc.MySQLProfile.api._

import java.sql.Date

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def password = column[String]("password")
  def phoneNumber = column[String]("phoneNumber")

  def * = (id.?, name, email, password, phoneNumber) <> ((User.apply _).tupled, User.unapply)
}


