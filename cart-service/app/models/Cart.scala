package models

import play.api.libs.json._

case class Cart(id: Option[Long], userID: Long, apparelID: Long  , quantity : Int , price : Int)

object Cart {
  implicit val apparelFormat: OFormat[Cart] = Json.format[Cart]
}

