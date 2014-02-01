package data

import play.api.libs.ws._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Neo4j{

  val url = "http://localhost:7474/db/data/cypher"

  def fetch(name: String): Future[Seq[(JsValue, JsValue)]] = {
    val query = Json.obj(
      "query"  -> """
        MATCH (e)-[:TAGGED*]->(b:tag) WHERE b.name = {nodeName}
        MATCH (e)-[:TAGGED]->(t:tag)
        RETURN distinct t, e
      """,
      "params" -> Json.obj("nodeName" -> name )
    )

    WS.url(url)
      .withHeaders("Accept" -> "application/json")
      .post(query)
      .map { r =>
        ( r.json \ "data" ).as[JsArray].value.map { a =>
          a.as[JsArray].value.map{ e => ( e \ "data" ) } match {
            case Seq(t, e) => ( t, e )
            case _         => throw new RuntimeException("Invalid response")
          }
        }
      }
  }

}