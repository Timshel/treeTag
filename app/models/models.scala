package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait Tagged

case class Tag(name: String) extends Tagged

object Tag {
  implicit val reader = Json.reads[Tag]
  implicit val writer = Json.writes[Tag]
}

case class Article(uuid: String, description: String, content: String) extends Tagged{

  override def equals(o: Any):Boolean = o match {
    case a: Article => uuid == a.uuid
    case _          => false
  }

  override def hashCode: Int = uuid.hashCode

}

object Article {

  implicit val reader = Json.reads[Article]
  implicit val writer = Json.writes[Article]

  val createReader: Reads[Article] = (
    (__ \ 'description).read[String] and
    (__ \ 'content).read[String]
  )(Article.create _)

  def create(description: String, content: String): Article =
    Article( java.util.UUID.randomUUID.toString, description, content)

}

case class Leaf(article: Article, tags: Seq[Tag])

object Leaf {
  implicit val reader = Json.reads[Leaf]
  implicit val writer = Json.writes[Leaf]
}

case class Node(tag: Tag, childs: Seq[Node], articles: Seq[Leaf])

object Node {
  implicit val reader = Json.reads[Node]
  implicit val writer = Json.writes[Node]
}