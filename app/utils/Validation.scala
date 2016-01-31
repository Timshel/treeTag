package utils

import shapeless.Generic
import shapeless.{ ::, HList, HNil }

import play.api.data.mapping._

import scala.concurrent.{ExecutionContext, Future}

object Validation {

  object Writes {
    implicit def hlistWrite[H, O](implicit c: Write[H, O]): Write[H :: HNil, O] =
      Write[H :: HNil, O] { h : H :: HNil => c.writes(h.select[H]) }

    implicit def anyvalWriteDerivation[N <: AnyVal, H <: HList, O](implicit gen: Generic.Aux[N, H], c: Write[H, O]): Write[N, O] =
      Write[N, O] { n => c.writes(gen.to(n)) }
  }

  object Rules {
    implicit def hlistRule[I, H](implicit c: Rule[I, H]): Rule[I, H :: HNil] =
      c.fmap(_ ::HNil)

    implicit def anyvalRuleDerivation[I, N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Rule[I, H]): Rule[I, N] =
      c.fmap(r => gen.from(r))

    trait ToHlist[S] {
      type Out <: HList
      def apply(s: S): Out
    }
  }
}