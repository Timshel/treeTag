package dao

import models._

import play.api.libs.ws._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Neo4j {

  val url = "http://localhost:7474/db/data/cypher"


  implicit class ResponseEnhancer(f: Future[Response]){
    def toEither: Future[Either[String, Int]] = f.map { r =>
      r.status match {
        case 200 | 201 => Right(r.status)
        case _         => Left(r.body)
      }
    }
  }

  def ws(query: JsValue): Future[Response] = {
    WS.url(url)
      .withHeaders("Accept" -> "application/json")
      .post(query)
  }

}

object Neo4j extends Neo4j {

  def create(tag: Tag): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MERGE (t:tag { name: {tagName} }) RETURN t
      """,
      "params" -> Json.obj("tagName" -> tag.name )
    )

    ws(query).toEither
  }

  def delete(tag: Tag): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MATCH (t:tag { name: {tagName} })
        OPTIONAL MATCH ()-[r1]->(t)
        OPTIONAL MATCH (t)-[r2]->()
        DELETE r1, r2, t
      """,
      "params" -> Json.obj("tagName" -> tag.name )
    )

    ws(query).toEither
  }

  def create(leaf: Leaf): Future[Either[String, Int]] = {

    val (query, params) = (
      """CREATE (a:article { uuid: {uuid}, description: {description}, content: {content} })""",
      Json.obj(
        "uuid"        -> leaf.article.uuid,
        "description" -> leaf.article.description,
        "content"     -> leaf.article.content
      )
    )

    for {
      e <- ws( Json.obj( "query"  -> query, "params" -> params ) ).toEither
      t <- Future.sequence( leaf.tags.map( tag(leaf.article.uuid, _) ) )
    } yield e
  }

  def update(leaf: Leaf): Future[Either[String, Int]] = {

    val (query, params) = (
      """MERGE (a:article { uuid: {uuid}, description: {description}, content: {content} })""",
      Json.obj(
        "uuid"        -> leaf.article.uuid,
        "description" -> leaf.article.description,
        "content"     -> leaf.article.content
      )
    )

    for {
      e <- ws( Json.obj( "query"  -> query, "params" -> params ) ).toEither
      t <- Future.sequence( leaf.tags.map( tag(leaf.article.uuid, _) ) )
    } yield e
  }

  def delete(uuid: String): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MATCH (a:article { uuid: {uuid} })
        OPTIONAL MATCH ()-[r1]->(a)
        OPTIONAL MATCH (t)-[r2]->()
        DELETE a, r1, r2
      """,
      "params" -> Json.obj("uuid" -> uuid )
    )

    ws(query).toEither
  }

  def tag(tagged: Tag, tag: Tag) = {
    val query1 = Json.obj( "query"  ->
      """
        MATCH (td:tag { name: {taggedName} })
        MERGE (t:tag { name: {tagName} })
      """,
      "params" -> Json.obj("taggedName" -> tagged.name, "tagName" -> tag.name )
    )

    val query2 = Json.obj( "query"  ->
      """
        MATCH (td:tag { name: {taggedName} })
        MATCH (t:tag { name: {tagName} })
        MERGE (t)-[:TAGGED]->(td)
      """,
      "params" -> Json.obj("taggedName" -> tagged.name, "tagName" -> tag.name )
    )

    for {
      e1 <- ws(query1).toEither
      e2 <- ws(query2).toEither
    } yield e2
  }

  def unTag(tagged: Tag, tag: Tag): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MATCH (:tag { name: {tagName} })-[r]->(:tag { name: {taggedName} })
        DELETE r
      """,
      "params" -> Json.obj(
        "taggedName" -> tagged.name,
        "tagName"    -> tag.name
      )
    )

    ws(query).toEither
  }


  def tag(uuid: String, tag: Tag): Future[Either[String, Int]] = {
    val query1 = Json.obj( "query"  ->
      """
        MATCH (a:article { uuid: {uuid} })
        MERGE (t:tag { name: {tagName} })
      """,
      "params" -> Json.obj("uuid" -> uuid, "tagName" -> tag.name )
    )

    val query2 = Json.obj( "query"  ->
      """
        MATCH (a:article { uuid: {uuid} })
        MATCH (t:tag { name: {tagName} })
        MERGE (t)-[:TAGGED]->(a)
      """,
      "params" -> Json.obj("uuid" -> uuid, "tagName" -> tag.name )
    )

    for {
      e1 <- ws(query1).toEither
      e2 <- ws(query2).toEither
    } yield e2
  }

  def unTag(uuid: String, tag: Tag): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MATCH (:tag { name: {tagName} })-[r]->(:article { uuid: {uuid} })
        DELETE r
      """,
      "params" -> Json.obj(
        "uuid" -> uuid,
        "tagName" -> tag.name
      )
    )

    ws(query).toEither
  }

}
