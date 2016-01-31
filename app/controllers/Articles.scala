package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, JsArray}

import scala.concurrent.Future

import models._

class Articles(
  artComp: service.ArticleComponent,
  gEc:     EC.GlobalEC
) extends Controller {
  import Articless._
  import Article._

  implicit val ec = gEc.ec

  def create = Action.async(parse.json) { r =>
    ???
  }

  def get(uuid: UUID) = Action.async {
    artComp.find(uuid).map {
      case Some(a) => Ok(articleW.writes(a))
      case None    => NotFound
    }
  }

  def delete(uuid: UUID) = Action.async {
    artComp.delete(uuid).map {
      case true  => Ok
      case false => NotFound
    }
  }

  def all() = Action.async {
    artComp.all().map { articles =>
      Ok(articlesW.writes(articles))
    }
  }
}

import play.api.data.mapping._
object Articless extends DefaultWrites with GenericRules {
  import play.api.data.mapping.json.Rules._
  import utils.Validation.Rules._

  val articleR = Rule.gen[JsValue, Article]
  val articlesR = seqR(articleR)

  import play.api.data.mapping.json.Writes._
  import utils.Validation.Writes._

  val articleW = Write.gen[Article, JsObject]
  val articlesW = seqW(articleW).map(new JsArray(_))
}