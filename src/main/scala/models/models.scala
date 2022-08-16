package object models {
  type NewArticle = (
    Article.UUID,
    Article.Url,
    Article.Title,
    Option[Article.Description],
    Option[Article.Content]
  )

  type Article = (
    Article.UUID, 
    Article.Url,
    Article.Title,
    Option[Article.Description],
    Option[Article.Content],
    Article.Created,
    Article.Updated
  )
}

package models {

  object Article {
    case class UUID(value: String) 
    case class Url(value: java.net.URL) 
    case class Title(value: String) 
    case class Description(value: String) 
    case class Content(value: String) 
    case class Created(value: java.time.Instant) 
    case class Updated(value: java.time.Instant) 

    object UUID {
      def gen() = new UUID(java.util.UUID.randomUUID().toString)
    }
  }
}