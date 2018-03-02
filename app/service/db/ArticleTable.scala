package service.db

import doobie._
import scala.concurrent.Future
import models._, Article._

case class ArticleTable(
  dbContext : utils.DBContext
) extends utils.DBHelper {
  import ArticleTable._

  def upsert(a: Article): Future[Int] = 
    dbContext.run(upsertQuery.run(a))

  def find(uuid: UUID): Future[Option[Article]] = 
    dbContext.run(findQuery(uuid).option)

  def all(): Future[List[Article]] = 
    dbContext.run(allQuery.to[List])

  def delete(uuid: UUID): Future[Boolean] =
    dbContext.run(deleteQuery.run(uuid)).map(_ == 1)
}

object ArticleTable {  
  import doobie.implicits._

  val upsertQuery = Update[Article]("""
    INSERT INTO article (uuid, description, content)
      VALUES (?, ?, ?)
      ON CONFLICT DO UPDATE SET 
        description = EXCLUDED.description,
        content     = EXCLUDED.content
  """)

  def findQuery(uuid: UUID): Query0[Article] = sql"""
    SELECT uuid, description, content 
      FROM article WHERE uuid = $uuid
  """.query[Article]

  val allQuery: Query0[Article] = sql"""
    SELECT uuid, description, content  FROM article
  """.query[Article]

  val deleteQuery = Update[UUID]("""
    DELETE from article WHERE uuid = ?
  """)
}