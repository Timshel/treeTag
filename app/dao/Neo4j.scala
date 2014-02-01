package dao

import models._

import play.api.libs.ws._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Neo4j{

  val url = "http://localhost:7474/db/data/cypher"

  def ws(query: JsValue): Future[Response] = {
    WS.url(url)
      .withHeaders("Accept" -> "application/json")
      .post(query)
  }

  def fetch(name: String): Future[Seq[(Tag, Tagged)]] = {
    val query = Json.obj(
      "query"  -> """
        MATCH (e)-[:TAGGED*]->(b:tag) WHERE b.name = {nodeName}
        MATCH (e)-[:TAGGED]->(t:tag)
        RETURN distinct t, e
      """,
      "params" -> Json.obj("nodeName" -> name )
    )

    ws(query).map { r =>
      ( r.json \ "data" ).as[JsArray].value.map { a =>
        a.as[JsArray].value.map{ e => ( e \ "data" ) } match {
          case Seq(t, e) => ( t, e )
          case _         => throw new RuntimeException("Invalid response")
        }
      }.flatMap { case (t, a) =>
        (a.asOpt[Tag], a.asOpt[Article]) match {
          case ( Some(tt), _ ) => Some( (t.as[Tag], tt) )
          case ( _, Some(a) ) => Some( (t.as[Tag], a) )
          case _              => None
        }
      }
    }
  }

  def create(tag: Tag): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MERGE (t:tag { name: {tagName} }) RETURN t
      """,
      "params" -> Json.obj("tagName" -> tag.name )
    )

    ws(query).map { r =>
      r.status match {
        case 201 => Right(r.status)
        case _   => Left(r.body)
      }
    }
  }

  def create(article: Article, tags: Seq[Tag]): Future[Either[String, Int]] = {

    val (query, params) = tags.zipWithIndex.foldLeft((
      """CREATE (a:article { uuid: {uuid}, description: {description}, content: {content} })""",
      Json.obj(
        "uuid"        -> java.util.UUID.randomUUID.toString,
        "description" -> article.description,
        "content"     -> article.content
      )
    )){ case ( (query, params) , (tag, index) ) =>
      (
        query + s", (a)-[:TAGGED]->(t$index:tag {name: {tagName$index} })",
        params + ( s"tagName$index", JsString(tag.name) )
      )
    }

    ws( Json.obj( "query"  -> query, "params" -> params ) ).map { r =>
      r.status match {
        case 200 | 201 => Right(r.status)
        case _         => Left(r.body)
      }
    }
  }

  def tag(tagged: Tag, tag: Tag) = {
    val query = Json.obj( "query"  ->
      """
        MATCH (td:tag { name: {taggedName} })
        MATCH (t:tag { name: {tagName} })
        MERGE (td)-[:TAGGED]->(t)
      """,
      "params" -> Json.obj("taggedName" -> tagged.name, "tagName" -> tag.name )
    )

    ws(query).map { r =>
      r.status match {
        case 200 | 201  => Right(r.status)
        case _          => Left(r.body)
      }
    }
  }

  def tag(uuid: String, tag: Tag) = {
    val query = Json.obj( "query"  ->
      """
        MATCH (a:article { uuic: {uuid} })
        MATCH (t:tag { name: {tagName} })
        MERGE (a)-[:TAGGED]->(t)
      """,
      "params" -> Json.obj("uuid" -> uuid, "tagName" -> tag.name )
    )

    ws(query).map { r =>
      r.status match {
        case 200 | 201  => Right(r.status)
        case _          => Left(r.body)
      }
    }
  }

}
