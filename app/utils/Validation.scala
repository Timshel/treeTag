package utils

import shapeless.Generic
import shapeless.{ ::, HList, HNil }

import jto.validation._
import scala.concurrent.{ExecutionContext, Future}

import com.mandubian.shapelessrules.{HZip, HFold}

object Validation {

  object Writes {
    implicit def hlistWrite[H, O](implicit c: Write[H, O]): Write[H :: HNil, O] =
      Write[H :: HNil, O] { h : H :: HNil => c.writes(h.select[H]) }

    implicit def anyvalWriteDerivation[N <: AnyVal, H <: HList, O](implicit gen: Generic.Aux[N, H], c: Write[H, O]): Write[N, O] =
      Write[N, O] { n => c.writes(gen.to(n)) }

  }

  object Rules extends HZip with HFold {

    implicit def vaApp = implicitly[cats.Applicative[VA]]

    implicit def hlistRule[I, H](implicit c: Rule[I, H]): Rule[I, H :: HNil] =
      c.fmap(_ ::HNil)

    implicit def anyvalRuleDerivation[I, N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Rule[I, H]): Rule[I, N] =
      c.fmap(r => gen.from(r))

    implicit class toHListRule[I, O](val rule: Rule[I, O]) extends AnyVal {
      def hlisted[P <: HList](implicit gen: Generic.Aux[O, P]): Rule[I, P] =
        rule compose Rule.fromMapping[O, P] { o => Success(gen.to(o)) }
    }
  }
}