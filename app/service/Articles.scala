package service

import scala.concurrent.Future
import scala.language.postfixOps

import models._, Article._

case class ArticleComponent(
	dbEc: EC.DatabaseEC
) {
  import anorm._, SqlParser.get
  import utils.Anorm._

	implicit val ec = dbEc.ec

  val articleP =
    (
      get[UUID]("uuid") ~
      get[Description]("description") ~
      get[Content]("content")
    ).map { u => Article.gen.from(ToHlist(u)) }

  def find(uuid: UUID): Future[Option[Article]] = DB { implicit c =>
    SQL"""
      SELECT * FROM article where uuid = ${uuid.value}
    """.as(articleP*).headOption
  }

  def delete(uuid: UUID): Future[Boolean] = DB { implicit c =>
    SQL"""
      DELETE FROM article where uuid = ${uuid.value}
    """.execute
  }

  def all(): Future[Seq[Article]] = DB { implicit c =>
    SQL"""
      SELECT * FROM article
    """.as(articleP *)
  }

}