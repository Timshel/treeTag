package controllers

import play.api.mvc._
import play.api.libs.json.JsString
import scala.concurrent.Future
import models._, Article._

case class Articles(
  articleTable : service.db.ArticleTable
)(
  implicit 
  val controllerComponents : play.api.mvc.ControllerComponents, 
  gEc                      : EC.GlobalEC
) extends BaseController {
  implicit val ec = gEc.ec

  def create = Action.async(parse.json) { r =>
    Articles.create.reads(r.body).fold( 
      err => Future.successful(BadRequest),
      article => articleTable.upsert(article).map { _ =>
        Ok(new JsString(article.select[UUID].value))
      }
    )
  }

  def get(uuid: UUID) = Action.async {
    articleTable.find(uuid).map {
      case Some(a) => Ok(Articles.writer.writes(a))
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
      Ok(Articles.articles.writes(articles))
    }
  }
}

object Articles extends utils.CommonReads with utils.CommonWrites {
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import shapeless.syntax.std.tuple._

  val create : Reads[NewArticle] = (
    (JsPath \ "url").read[Url] and
    (JsPath \ "title").read[Title] and
    (JsPath \ "description").readNullable[Description] and
    (JsPath \ "content").readNullable[Content]
  ).tupled.map{ t => UUID.gen :: t.productElements }

  val reader: Reads[Article] = (
    (JsPath \ "uuid").read[UUID] and
    (JsPath \ "url").read[Url] and
    (JsPath \ "title").read[Title] and
    (JsPath \ "description").readNullable[Description] and
    (JsPath \ "content").readNullable[Content] and
    (JsPath \ "created").read[Created] and
    (JsPath \ "updated").read[Updated]
  ).tupled.map(_.productElements)

  implicit val writer: Writes[Article] = (
    (JsPath \ "uuid").write[UUID] and
    (JsPath \ "url").write[Url] and
    (JsPath \ "title").write[Title] and
    (JsPath \ "description").writeNullable[Description] and
    (JsPath \ "content").writeNullable[Content] and
    (JsPath \ "created").write[Created] and
    (JsPath \ "updated").write[Updated]
  ).tupled.contramap(_.tupled)

  val articles: Writes[List[Article]] = arrayWrites[Article].contramap(_.toArray)
}