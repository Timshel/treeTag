package utils

import play.api.libs.json._
import scala.deriving.Mirror

trait CommonReads extends play.api.libs.json.DefaultReads {

  // Make Reads available for single-element products.
  implicit def unwrapReads[P <: Product, A](
    implicit  m: Mirror.ProductOf[P],
              i: m.MirroredElemTypes =:= (A *: EmptyTuple),
              r: Reads[A]
  ): Reads[P] = r.map(a => m.fromProduct(a *: EmptyTuple))

  implicit def urlWrapperReads: Reads[java.net.URL] = 
    StringReads.map(str => scala.util.Try(new java.net.URL(str)))
      .collect(JsonValidationError(Seq("invalid URL"))){
        case scala.util.Success(url) => url
      }

  implicit def instantWrapperReads: Reads[java.time.Instant] = 
    StringReads.map(str => scala.util.Try(java.time.ZonedDateTime.parse(str).toInstant))
     .collect(JsonValidationError(Seq("invalid instant"))){
        case scala.util.Success(i) => i
      }

}
  
trait CommonWrites extends play.api.libs.json.DefaultWrites {

  implicit def urlWrites: Writes[java.net.URL] = 
    StringWrites.contramap { url => url.toString }

  implicit def instantWrites: Writes[java.time.Instant] = 
    StringWrites.contramap { instant => instant.toString }

  // Make Writes available for single-element products.
  implicit def unwrapWrites[P <: Product, A](
    implicit  m: Mirror.ProductOf[P],
              i: m.MirroredElemTypes =:= (A *: EmptyTuple),
              w: Writes[A]
  ): Writes[P] = w.contramap(p => i(Tuple.fromProductTyped(p)).head)
}
