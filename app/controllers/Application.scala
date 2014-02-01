package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def tag(name: String) = Action.async {
    _root_.data.Neo4j.fetch(name).map { d =>
      Ok( Json.toJson(
        d.map {
          case (t, e ) => Json.obj("tag" -> t, "elt" -> e)
        }
      ))
    }
  }

}