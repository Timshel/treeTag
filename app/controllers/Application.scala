package controllers

import models._

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def tagTag(name: String, tag: String) = Action.async {
    dao.Neo4j.tag(Tag(name), Tag(tag)).map { b => Ok(b.toString) }
  }

  def tagCreate(name: String) = Action.async {
    dao.Neo4j.create(Tag(name)).map { b => Ok(b.toString) }
  }

  def tagDelete(name: String) = Action.async {
    dao.Neo4j.delete(Tag(name)).map { b => Ok(b.toString) }
  }

  def articleCreate = Action.async(parse.json) { r =>
    r.body.validate(Article.createReader).map { a =>
      dao.Neo4j.create(a).map { b => Ok(b.toString) }
    } recoverTotal {
      case e => Future.successful( BadRequest( Json.prettyPrint(JsError.toFlatJson(e)) ) )
    }
  }

  def articleDelete(uuid: String) = Action.async {
    dao.Neo4j.delete(uuid).map { b => Ok(b.toString) }
  }

  def articleTag(uuid: String, tag: String) = Action.async {
    dao.Neo4j.tag(uuid: String, Tag(tag)).map { b => Ok(b.toString) }
  }

  def fetchTag(name: String) = Action.async {
    dao.Neo4j.fetch(name).map { d =>
      Ok( Json.toJson(
        d.map {
          case (t, e: Tag) => Json.obj("tag" -> t, "elt" -> e)
          case (t, e: Article) => Json.obj("tag" -> t, "elt" -> e)
        }
      ))
    }
  }

}