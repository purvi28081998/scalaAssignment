package controllers

import daos.UserDAO
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class UserController @Inject()(cc: ControllerComponents, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def createUser() = Action.async(parse.json) { implicit request =>
    println("Inside the controller method ")
    val name = (request.body \ "name").as[String]
    val email = (request.body \ "email").as[String]
    val password = (request.body \ "password").as[String]
    val phoneNumber = (request.body \ "phoneNumber").as[String]

    println("Name: " + name + "email: "+ email + " phonenumber: "+ phoneNumber );

    userService.createUser(name, email, password,  phoneNumber).map { user =>
//      Created(s"User created with ID: ${user.id.get}")
      Created(Json.toJson(user))
    }
  }

  def login() =  Action.async(parse.json){ implicit request =>
      println("Inside the controller method ")
      val email = (request.body \ "email").as[String]
      val password = (request.body \ "password").as[String]
      userService.authenticate( email, password).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound("User not found")
    }
  }


  def getUser(id: Int) = Action.async { implicit request =>
    userService.getUser(id).map {
//      case Some(user) => Ok(s"User found: ${user.name}, ${user.email}, ${user.phoneNumber}, ${user.role}")
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound("User not found")
    }
  }

  def updateUser(id: Int) = Action.async(parse.json) { implicit request =>
    val name = (request.body \ "name").as[String]
    val email = (request.body \ "email").as[String]
    val password = (request.body \ "password").as[String]
    val phoneNumber = (request.body \ "phoneNumber").as[String]

    userService.updateUser(id, name, email, password, phoneNumber).map { result =>
//      if (result > 0) Ok(s"User updated with ID: $id")
      if (result > 0) Ok(Json.toJson(result))
      else NotFound("User not found")
    }
  }

  def deleteUser(id: Int) = Action.async { implicit request =>
    userService.deleteUser(id).map { result =>
      if (result > 0) Ok(s"User deleted with ID: $id")
      else NotFound("User not found")
    }
  }

  def listUsers() = Action.async { implicit request =>
//    userService.listUsers().map { users =>
//      Ok(users.map(user => s"${user.name}, ${user.email}, ${user.phoneNumber}, ${user.role}").mkString("\n"))
//    }
    userService.listUsers().map { users =>
      Ok(Json.toJson(users))
    }
  }

//  def getUserTransaactions(id: Int) = Action.async { implicit request =>
//    userService.getUserTransactions(id).map {
//      case Some(user) => Ok(s"Trnsactions:  found: ${user.name}, ${user.email}, ${user.phoneNumber}, ${user.role}")
//      case None => NotFound("No transactions found for user")
//    }
//  }
}