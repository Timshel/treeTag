package object models {
  import shapeless.{::, HNil }

  type NewArticle =
    Article.UUID :: 
    Article.Url ::
    Article.Title ::
    Option[Article.Description] ::
    Option[Article.Content] :: 
    HNil

  type Article = 
    Article.UUID :: 
    Article.Url ::
    Article.Title ::
    Option[Article.Description] ::
    Option[Article.Content] :: 
    Article.Created ::
    Article.Updated ::
    HNil
}

package models {

  object Article {
    case class UUID(value: String) extends AnyVal
    case class Url(value: java.net.URL) extends AnyVal
    case class Title(value: String) extends AnyVal
    case class Description(value: String) extends AnyVal
    case class Content(value: String) extends AnyVal
    case class Created(value: java.time.Instant) extends AnyVal
    case class Updated(value: java.time.Instant) extends AnyVal

    object UUID {
      def gen() = new UUID(java.util.UUID.randomUUID().toString)
    }
  }
}