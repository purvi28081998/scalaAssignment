package models

case class User(id: Option[Int], name: String, email: String, password: String, phoneNumber: String )

import play.api.libs.json._

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}

