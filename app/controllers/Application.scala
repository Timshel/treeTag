package controllers

import play.api.mvc._
import models.EC

class Application(
  controllerComponent: play.api.mvc.ControllerComponents
)(implicit val gEc: models.EC.GlobalEC) extends AbstractController(controllerComponent) {

  implicit val ec: EC.GlobalEC = gEc

  def index(path: String): Action[AnyContent] =  Action { _ =>
  	Ok(views.html.index())
  }
}