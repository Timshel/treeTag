package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def tag(name: String) = Action.async {
    dao.Neo4j.fetch(name).map { d =>
      Ok( Json.toJson(
        d.map {
          case (t, e: Tag) => Json.obj("tag" -> t, "elt" -> e)
          case (t, e: Article) => Json.obj("tag" -> t, "elt" -> e)
        }
      ))
    }
  }

  def createTag(name: String) = Action.async {
    dao.Neo4j.create(Tag(name)).map { b => Ok(b.toString) }
  }

}