package utils

import play.api.libs.json._
import shapeless.{::, HNil, HList, Generic}

trait CommonReads extends play.api.libs.json.DefaultReads {

  implicit def hlistWrapperReads[T](implicit rule: Reads[T]): Reads[T :: HNil] = rule.map { t => t :: HNil }

  implicit def anyvalDerivationReads[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Reads[H]): Reads[N] =
    c.map(r => gen.from(r))

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

  implicit def hlistWrapperWrites[T](implicit w: Writes[T]): Writes[T :: HNil] = w.contramap { case t :: HNil => t }

  implicit def anyvalDerivationWrites[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], w: Writes[H]): Writes[N]  = {
    w.contramap { h => gen.to(h) }
  }

  implicit def urlWrites: Writes[java.net.URL] = 
    StringWrites.contramap { url => url.toString }

  implicit def instantWrites: Writes[java.time.Instant] = 
    StringWrites.contramap { instant => instant.toString }
}