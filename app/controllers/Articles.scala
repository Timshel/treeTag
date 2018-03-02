package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsString}
import scala.concurrent.Future
import models._, Article._

case class Articles(
  articleTable : service.db.ArticleTable
)(
  implicit 
  val controllerComponents : play.api.mvc.ControllerComponents, 
  gEc                      : EC.GlobalEC
) extends BaseController {
  import Articles._
  implicit val ec = gEc.ec

  def create = Action.async(parse.json) { r =>
    Rules.create.validate(r.body).fold( 
      err => Future.successful(BadRequest),
      article => articleTable.upsert(article).map { _ =>
        Ok(new JsString(article.select[UUID].value))
      }
    )
  }

  def get(uuid: UUID) = Action.async {
    articleTable.find(uuid).map {
      case Some(a) => Ok(Writes.article.writes(a))
      case None    => NotFound
    }
  }

  def delete(uuid: UUID) = Action.async {
    articleTable.delete(uuid).map {
      case true  => Ok
      case false => NotFound
    }
  }

  def all() = Action.async {
    articleTable.all().map { articles =>
      Ok(Writes.articles.writes(articles))
    }
  }
}

object Articles extends utils.GrammarHelper {
  import jto.validation.{Rule, Write, Path}

  trait CustomGrammer[K[_, _]] extends utils.JsonGrammar[K] {
    val article: K[Out, Article] = (
      at(Path \ "uuid").is(req[UUID]) ~:
      at(Path \ "description").is(req[Description]) ~:
      at(Path \ "content").is(req[Content]) ~:
      knil
    )

    val articles: K[JsValue, Seq[Article]] = seq(article)
  }

  object Rules extends utils.JsonRules with CustomGrammer[Rule] {
    val create : Rule[JsValue, Article] = (
      Rule.pure[JsValue, UUID](UUID.gen) ~:
      at(Path \ "description").is(req[Description]) ~:
      at(Path \ "content").is(req[Content]) ~:
      knil
    )
  }
 
  object Writes extends utils.JsonWrites with CustomGrammer[op[Write]#λ] 
}