package controllers

import models._

import play.api.mvc._

class Application(
  gEc: EC.GlobalEC
) extends Controller {

  implicit val ec = gEc

  def index =  ???
}