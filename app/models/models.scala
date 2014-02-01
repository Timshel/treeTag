package models

import play.api.libs.json._

sealed trait Tagged

case class Tag(name: String) extends Tagged

object Tag {
  implicit val reader = Json.reads[Tag]
  implicit val writer = Json.writes[Tag]
}

case class Article(uuid: String, description: String, content: String) extends Tagged

object Article {
  implicit val reader = Json.reads[Article]
  implicit val writer = Json.writes[Article]
}

case class Node(tag: Tag, childs: Seq[Node], articles: Seq[Article])

object Node {

  private def buildTree(root: Tag, tagMap: Map[Tag, Seq[Tagged]]): Option[Node] = {
    tagMap.get(root).map { c =>
      val (tags, articles) = c.foldLeft[(Seq[Tag], Seq[Article])]( (Nil, Nil) ) { (acc, t) =>
        t match {
          case a : Article => (acc._1, a +: acc._2)
          case t : Tag     => (t +: acc._1, acc._2)
        }
      }
      val nodes = tags.flatMap( t => buildTree(t, tagMap) )
      Node(root, nodes, articles)
    }
  }

  def create(root: Tag, elts: Seq[(Tag, Tagged)]): Option[Node] = {
    val articleTags: Map[Article, Seq[Tag]] = elts.flatMap {
      case (t, a: Article) => Some( ( t, a ) )
      case _               => None
    }.groupBy(_._2).mapValues { v => v.map(_._1).sortBy(_.name) }

    val tagMap: Map[Tag, Seq[Tagged]] = elts.groupBy(_._1).mapValues { v => v.map(_._2) }

    buildTree(root, tagMap)
  }
}