package utils

import jto.validation._
import jto.validation.v3.tagless.types.op
import play.api.libs.json.JsValue
import shapeless.{::, HNil, HList, Generic}

trait GrammarHelper {
	type op[F[_, _]] = jto.validation.v3.tagless.types.op[F]
}

trait CommonGrammar[I, K[_, _]] extends v3.tagless.Grammar[I, K] {
  implicit def hlistWrapper[T](implicit k: K[I, T]): K[I, T :: HNil] 
  implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: K[I, H]): K[I, N] 

  implicit def urlWrapper: K[I, java.net.URL]
  implicit def instantWrapper: K[I, java.time.Instant]
}

object CommonGrammar {

}

trait JsonGrammar[K[_, _]] extends CommonGrammar[JsValue, K] 

trait JsonRules extends v3.tagless.playjson.RulesGrammar 
    with JsonGrammar[Rule]
    with v3.tagless.RulesTypeclasses[JsValue] {

  implicit def hlistWrapper[T](implicit rule: Rule[JsValue, T]): Rule[JsValue, T :: HNil] = rule.map { t => t :: HNil }

  implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Rule[JsValue, H]): Rule[JsValue, N] =
    c.map(r => gen.from(r))

  implicit def urlWrapper: Rule[JsValue, java.net.URL] = string.andThen(
    Rule.fromMapping[String, java.net.URL] { str =>
      scala.util.Try(new java.net.URL(str)).fold(
        err => Invalid(Seq(ValidationError(err.getMessage))),
        url => Valid(url)
      )
    }
  )

  implicit def instantWrapper: Rule[JsValue, java.time.Instant] = string.andThen(
    Rule.fromMapping[String, java.time.Instant] { str =>
      Valid(java.time.ZonedDateTime.parse(str).toInstant)
    }
  )
}


  
trait JsonWrites extends v3.tagless.playjson.WritesGrammar 
    with JsonGrammar[op[Write]#λ]
    with v3.tagless.WritesTypeclasses[JsValue] {

  implicit def hlistWrapper[T](implicit w: Write[T, JsValue]): Write[T :: HNil, JsValue] = w.contramap { case t :: HNil => t }

  implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], w: Write[H, JsValue]): Write[N, JsValue]  = {
    w.contramap { h => gen.to(h) }
  }

  implicit def urlWrapper: Write[java.net.URL, JsValue] = 
    string.contramap { url => url.toString }

  implicit def instantWrapper: Write[java.time.Instant, JsValue] = 
    string.contramap { instant => instant.toString }
}