package controllers

import models._

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action.async {
    dao.Nodes.fetch("root").map { n =>
      Ok(views.html.index(n))
    }
  }

  def indexTag(name: String) = Action.async {
    dao.Nodes.fetch(name).map { n =>
      Ok(views.html.index(n))
    }
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

  val articleReads = (
    (__ \ 'article).read[Article](Article.createReader) and
    (__ \ 'tags).read[Seq[Tag]]
  )(Leaf.apply _)

  def articleCreate = Action.async(parse.json) { r =>
    r.body.validate(articleReads).map { l =>
      dao.Neo4j.create(l).map { b => Ok(b.toString) }
    } recoverTotal {
      case e => Future.successful( BadRequest( Json.prettyPrint(JsError.toFlatJson(e)) ) )
    }
  }

  def articleUpdate(uuid: String) = Action.async(parse.json) { r =>
    r.body.validate[Leaf].map { l =>
      dao.Neo4j.update(l).map { b => Ok(b.toString) }
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
    dao.Nodes.fetch(name).map { n =>
      Ok( Json.toJson(n) )
    }
  }

}