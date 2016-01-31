package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, JsArray, JsString}

import scala.concurrent.Future

import models._, Article._

class Articles(
  artComp: service.ArticleComponent,
  gEc:     EC.GlobalEC
) extends Controller {
  import Articles._
  implicit val ec = gEc.ec

  def create = Action.async(parse.json) { r =>
    createR.validate(r.body).fold(
      err => Future.successful(BadRequest),
      article => artComp.insert(article).map { _ =>
        Ok(new JsString(article.select[UUID].value))
      }
    )
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
object Articles extends DefaultWrites with GenericRules {
  import play.api.data.mapping.json.Rules._
  import utils.Validation.Rules._
  import shapeless._, syntax.std.product._

  val createR : Rule[JsValue, Article] = From[JsValue] { __ =>
    ( (__ \ "description").read[Description] and
      (__ \ "content").read[Content]
    ).tupled
  }.fmap { t => UUID.gen() :: t.productElements }

  val articleR: Rule[JsValue, Article] = From[JsValue] { __ =>
    ( (__ \ "uuid").read[UUID] and
      (__ \ "description").read[Description] and
      (__ \ "content").read[Content]
    ).tupled
  }.fmap(_.productElements)

  val articlesR = seqR(articleR)

  import play.api.data.mapping.json.Writes._
  import utils.Validation.Writes._

  val articleW = To[JsObject] { __ =>
    ( (__ \ "uuid").write[UUID] and
      (__ \ "description").write[Description] and
      (__ \ "content").write[Content]
    ){ a: Article => a.tupled }
  }
  val articlesW = seqW(articleW).map(new JsArray(_))
}