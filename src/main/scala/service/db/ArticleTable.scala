package service.db

import cats.effect.IO
import doobie._
import models._, Article._

case class ArticleTable(
  dbContext : utils.DBContext
) extends utils.DBHelper {
  import ArticleTable._

  def upsert(a: NewArticle): IO[Int] = 
    dbContext.run(upsertQuery.run(a))

  def find(uuid: UUID): IO[Option[Article]] = 
    dbContext.run(findQuery(uuid).option)

  def all(): IO[List[Article]] = 
    dbContext.run(allQuery.to[List])

  def delete(uuid: UUID): IO[Boolean] =
    dbContext.run(deleteQuery.run(uuid)).map(_ == 1)
}

object ArticleTable extends utils.CommonDoobie {  
  import doobie.implicits._

  implicit val articleM: Meta[Article] = Meta[Article]
  implicit val articleW: Write[models.NewArticle] = implicitly[Write[models.NewArticle]]
  implicit val articleUUIDW: Write[models.Article.UUID] = implicitly[Write[models.Article.UUID]]

  val upsertQuery: Update[NewArticle] = Update[NewArticle]("""
    INSERT INTO article (uuid, url, title, description, content, created, updated)
      VALUES (?, ?, ?, ?, ?, now(), now())
      ON CONFLICT (uuid) DO UPDATE SET 
        url         = EXCLUDED.url,
        title       = EXCLUDED.title,
        description = EXCLUDED.description,
        content     = EXCLUDED.content,
        updated     = EXCLUDED.updated
  """)

  def findQuery(uuid: UUID): Query0[Article] = sql"""
    SELECT uuid, url, title, description, content, created, updated
      FROM article WHERE uuid = $uuid
  """.query[Article]

  val allQuery: Query0[Article] = sql"""
    SELECT uuid, url, title, description, content, created, updated FROM article
  """.query[Article]

  val deleteQuery: Update[UUID] = Update[UUID]("""
    DELETE from article WHERE uuid = ?
  """)
}