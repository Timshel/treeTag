package service

/*

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Nodes {

  sealed trait Tagged
  case class Tag(name: String) extends Tagged
  case class Article(uuid: String, description: String, content: String)

  case class Leaf(article: Article, tags: Seq[Tag])
  case class Node(tag: Tag, childs: Seq[Node], articles: Seq[Leaf])

  case class CoreArticle(uui: String, description: String, content: String)

  def fetch(name: String): Future[Node] = ???

  def convert(root: Tag, elts: Seq[(Tag, Tagged)]): Node = {
    val articleTags: Map[Article, Seq[Tag]] = elts.flatMap {
      case (t, a: Article) => Some( ( t, a ) )
      case _               => None
    }.groupBy(_._2).mapValues { v => v.map(_._1).sortBy(_.name) }

    val tagMap: Map[Tag, Seq[Tagged]] = elts.groupBy(_._1).mapValues { v => v.map(_._2) }

    buildTree(root, tagMap, articleTags)
  }

  private def buildTree(root: Tag, tagMap: Map[Tag, Seq[Tagged]],
      articleTags: Map[Article, Seq[Tag]]): Node = {

    tagMap.get(root).map { c =>
      val (tags, articles) = c.foldLeft[(Seq[Tag], Seq[Article])]( (Nil, Nil) ) { (acc, t) =>
        t match {
          case a : Article => (acc._1, a +: acc._2)
          case t : Tag     => (t +: acc._1, acc._2)
        }
      }

      val nodes = tags.sortBy(_.name)
                      .map( t => buildTree(t, tagMap, articleTags) )

      val leafs = articles.flatMap { a => articleTags.get(a).map( Leaf(a, _ )) }
                          .sortBy(_.article.description)

      Node(root, nodes, leafs )
    }.getOrElse( Node(root, Nil, Nil) )
  }

}
*/