package dao

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import models._

object Nodes extends Neo4j {

  case class CoreArticle(uui: String, description: String, content: String)

  def fetch(name: String): Future[Seq[(Tag, Tagged)]] = {
    val query = Json.obj(
      "query"  -> """
        MATCH (b:tag)-[:TAGGED*]->(e) WHERE b.name = {nodeName}
        MATCH (t:tag)-[:TAGGED]->(e)
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

  private def buildTree(root: Tag, tagMap: Map[Tag, Seq[Tagged]],
      articleTags: Map[Article, Seq[Tag]]): Option[Node] = {

    tagMap.get(root).map { c =>
      val (tags, articles) = c.foldLeft[(Seq[Tag], Seq[Article])]( (Nil, Nil) ) { (acc, t) =>
        t match {
          case a : Article => (acc._1, a +: acc._2)
          case t : Tag     => (t +: acc._1, acc._2)
        }
      }

      val nodes = tags.flatMap( t => buildTree(t, tagMap, articleTags) )
      val leafs = articles.flatMap { a => articleTags.get(a).map( Leaf(a, _ )) }

      Node(root, nodes, leafs )
    }
  }

  def create(root: Tag, elts: Seq[(Tag, Tagged)]): Option[Node] = {
    val articleTags: Map[Article, Seq[Tag]] = elts.flatMap {
      case (t, a: Article) => Some( ( t, a ) )
      case _               => None
    }.groupBy(_._2).mapValues { v => v.map(_._1).sortBy(_.name) }

    val tagMap: Map[Tag, Seq[Tagged]] = elts.groupBy(_._1).mapValues { v => v.map(_._2) }

    buildTree(root, tagMap, articleTags)
  }

}