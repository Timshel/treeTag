package controllers

import play.api.mvc._
import play.api.libs.json.{JsValue, JsString}
import scala.concurrent.Future
import scala.language.higherKinds
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

import jto.validation._
object Articles {
  import shapeless.{Path => _, _}

  trait CustomGrammer[I, K[_, _]] extends jto.validation.v3.tagless.Grammar[I, K] {
    implicit def hlistWrapper[T](implicit k: K[I, T]): K[I, T :: HNil] 
    implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: K[I, H]): K[I, N] 

    val article: K[Out, Article] = (
      at(Path \ "uuid").is(req[UUID]) ~:
      at(Path \ "description").is(req[Description]) ~:
      at(Path \ "content").is(req[Content]) ~:
      knil
    )

    val articles: K[I, Seq[Article]] = seq(article)
  }

  object Rules extends jto.validation.v3.tagless.playjson.RulesGrammar 
      with CustomGrammer[JsValue, Rule]
      with jto.validation.v3.tagless.RulesTypeclasses[JsValue] {

    implicit def hlistWrapper[T](implicit rule: Rule[JsValue, T]): Rule[JsValue, T :: HNil] = rule.map { t => t :: HNil }

    implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Rule[JsValue, H]): Rule[JsValue, N] =
      c.map(r => gen.from(r))

    val create : Rule[JsValue, Article] = (
      Rule.pure[JsValue, UUID](UUID.gen) ~:
      at(Path \ "description").is(req[Description]) ~:
      at(Path \ "content").is(req[Content]) ~:
      knil
    )
  }


  import jto.validation.v3.tagless.types.op
  object Writes extends jto.validation.v3.tagless.playjson.WritesGrammar 
      with CustomGrammer[JsValue, op[Write]#λ]
      with jto.validation.v3.tagless.WritesTypeclasses[JsValue] {

    implicit def hlistWrapper[T](implicit w: Write[T, JsValue]): Write[T :: HNil, JsValue] = w.contramap { case t :: HNil => t }

    implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], w: Write[H, JsValue]): Write[N, JsValue]  = {
      w.contramap { h => gen.to(h) }
    }
  }
}