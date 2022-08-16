package controllers

import cats.effect.IO
import play.api.libs.json.{JsString, JsValue}
import models._, Article._

import org.http4s._
import org.http4s.dsl.io._

case class ArticlesController(
  articleTable : service.db.ArticleTable
) {
  import controllers.HTTP4SJsParser._
  import utils.TupleSelector

  val routes = org.http4s.HttpRoutes.of[IO] {
    case _ @ GET -> Root =>
      articleTable.all().map { articles =>
        Response[IO](Status.Ok).withEntity(ArticlesController.articles.writes(articles))
      }

    case req @ POST -> Root =>
      req.as[JsValue].flatMap { body =>
        ArticlesController.create.reads(body).fold( 
          err     => IO.pure(Response[IO](Status.BadRequest).withEntity[JsValue](new JsString(err.toString()))),
          article => articleTable.upsert(article).map { _ =>
            Response[IO](Status.Ok).withEntity[JsValue](new JsString(article.select[UUID].value))
          }
        )
      }

    case _ @ GET -> Root / uuid =>
      articleTable.find(UUID(uuid)).map {
        case Some(a) => Response[IO](Status.Ok).withEntity(ArticlesController.writer.writes(a))
        case None    => Response[IO](Status.NotFound)
      }

    case _ @ DELETE -> Root / uuid =>
      articleTable.delete(UUID(uuid)).map {
        case true  => Response[IO](Status.Ok)
        case false => Response[IO](Status.NotFound)
      }
  }
}

object ArticlesController extends utils.CommonReads with utils.CommonWrites {
  import _root_.play.api.libs.json._
  import _root_.play.api.libs.functional.syntax._

  val create : Reads[NewArticle] = (
    (JsPath \ "url").read[Url] and
    (JsPath \ "title").read[Title] and
    (JsPath \ "description").readNullable[Description] and
    (JsPath \ "content").readNullable[Content]
  ).tupled.map { t => Tuple(UUID.gen()) ++ t }


  val reader: Reads[Article] = (
    (JsPath \ "uuid").read[UUID] and
    (JsPath \ "url").read[Url] and
    (JsPath \ "title").read[Title] and
    (JsPath \ "description").readNullable[Description] and
    (JsPath \ "content").readNullable[Content] and
    (JsPath \ "created").read[Created] and
    (JsPath \ "updated").read[Updated]
  ).tupled.map { _ => 
    // t.productElements(fullGeneric))
    val toto : Article = null
    toto
  }

  implicit val writer: Writes[Article] = (
    (JsPath \ "uuid").write[UUID] and
    (JsPath \ "url").write[Url] and
    (JsPath \ "title").write[Title] and
    (JsPath \ "description").writeNullable[Description] and
    (JsPath \ "content").writeNullable[Content] and
    (JsPath \ "created").write[Created] and
    (JsPath \ "updated").write[Updated]
  ).tupled

  implicit val articles: Writes[List[Article]] = arrayWrites[Article].contramap(_.toArray)
}

