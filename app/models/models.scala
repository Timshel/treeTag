package models

case class Article(
  uuid:         Article.UUID,
  description:  Article.Description,
  content:      Article.Content
)

object Article {
  val gen = shapeless.Generic[Article]

  case class UUID(value: String) extends AnyVal
  case class Description(value: String) extends AnyVal
  case class Content(value: String) extends AnyVal

}