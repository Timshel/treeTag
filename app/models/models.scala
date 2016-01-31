package object models {
  import shapeless.{ ::, HNil }

  type Article = Article.UUID :: Article.Description :: Article.Content :: HNil

}

package models {

  object Article {
    case class UUID(value: String) extends AnyVal
    case class Description(value: String) extends AnyVal
    case class Content(value: String) extends AnyVal

    object UUID {
      def gen() = new UUID(java.util.UUID.randomUUID().toString)
    }
  }
}