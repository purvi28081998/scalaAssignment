package models

import play.api.libs.json._

case class Apparel(id: Option[Long], name: String, price: Int  , stock : Int)

object Apparel {
  implicit val apparelFormat: OFormat[Apparel] = Json.format[Apparel]
}

