package controllers

import play.api.mvc._

class Application(
  controllerComponent: play.api.mvc.ControllerComponents
)(implicit val gEc: models.EC.GlobalEC) extends AbstractController(controllerComponent) {

  implicit val ec = gEc

  def index =  ???
}