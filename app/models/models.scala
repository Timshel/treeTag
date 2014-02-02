package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait Tagged

case class Tag(name: String) extends Tagged

object Tag {
  implicit val reader = Json.reads[Tag]
  implicit val writer = Json.writes[Tag]
}

case class Article(uuid: String, description: String, content: String, tags: Seq[Tag]) extends Tagged

object Article {

  implicit val reader = Json.reads[Article]
  implicit val writer = Json.writes[Article]

  val createReader: Reads[Article] = (
    (__ \ 'description).read[String] and
    (__ \ 'content).read[String] and
    (__ \ 'tags).read[Seq[Tag]]
  )(Article.create _)

  def create(description: String, content: String, tags: Seq[Tag]): Article =
    Article( java.util.UUID.randomUUID.toString, description, content, tags)

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