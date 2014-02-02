package dao

import models._

import play.api.libs.ws._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Neo4j{

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


  def create(article: Article): Future[Either[String, Int]] = {
    val (query, params) = (
      """CREATE (a:article { uuid: {uuid}, description: {description}, content: {content} })""",
      Json.obj(
        "uuid"        -> article.uuid,
        "description" -> article.description,
        "content"     -> article.content
      )
    )

    for {
      e <- ws( Json.obj( "query"  -> query, "params" -> params ) ).toEither
      t <- Future.sequence( article.tags.map( tag(article.uuid, _) ) )
    } yield e
  }

  def delete(uuid: String): Future[Either[String, Int]] = {
    val query = Json.obj( "query"  ->
      """
        MATCH (t:article { uuid: {uuid} })
        OPTIONAL MATCH ()-[r1]->(t)
        OPTIONAL MATCH (t)-[r2]->()
        DELETE t, r1, r2
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
        MERGE (td)-[:TAGGED]->(t)
      """,
      "params" -> Json.obj("taggedName" -> tagged.name, "tagName" -> tag.name )
    )

    for {
      e1 <- ws(query1).toEither
      e2 <- ws(query2).toEither
    } yield e2
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
        MERGE (a)-[:TAGGED]->(t)
      """,
      "params" -> Json.obj("uuid" -> uuid, "tagName" -> tag.name )
    )

    for {
      e1 <- ws(query1).toEither
      e2 <- ws(query2).toEither
    } yield e2
  }

}
