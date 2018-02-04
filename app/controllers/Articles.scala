package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsObject, JsString}

import scala.concurrent.Future

import models._, Article._

case class Articles(
  artComp : service.ArticleComponent
)(
  implicit 
  val controllerComponents : play.api.mvc.ControllerComponents, 
  gEc                      : EC.GlobalEC
) extends BaseController {
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

import jto.validation._
object Articles extends DefaultWrites with GenericRules {
  import jto.validation.playjson._, Rules._
  import utils.Validation.Rules._
  import shapeless._

  val createR : Rule[JsValue, Article] = utils.Validation.Rules.From[JsValue] { __ =>
    UUID.gen() ::
    (__ \ "description").read[Description] ::
    (__ \ "content").read[Content] ::
    HNil
  }

  val articleR: Rule[JsValue, Article] = utils.Validation.Rules.From[JsValue] { __ =>
    (__ \ "uuid").read[UUID] ::
    (__ \ "description").read[Description] ::
    (__ \ "content").read[Content] ::
    HNil
  }

  val articlesR = seqR(articleR)

  import jto.validation.playjson.Writes._
  import utils.Validation.Writes._

  val articleW = To[JsObject] { __ =>
    ( (__ \ "uuid").write[UUID] ~
      (__ \ "description").write[Description] ~
      (__ \ "content").write[Content]
    ){ a: Article => a.tupled }
  }


  val articlesW: Write[Seq[Article], JsValue] = seqToJsArray(articleW)
}