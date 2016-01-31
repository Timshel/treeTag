package com.mandubian
package shapelessrules

import play.api.data.mapping._
import play.api.libs.json.JsValue
import play.api.libs.functional.syntax._
import play.api.libs.functional._

import shapeless.{Path => _, _}
import ops.hlist._
import poly._

import scala.language.higherKinds

trait HZip {

  type VV[A] = Validation[(Path, Seq[ValidationError]), A]

  object applicativeValidationFolder extends Poly2 {

    implicit def caseApplicative[A, B <: HList](implicit app: Applicative[VV]) =
      at[Rule[A,A], (Int, Rule[B,B])] { case (ra, (idx, rb)) =>
        ( idx+1,
          Rule[A::B, A::B] {
            case (a:A @unchecked) :: (b:B @unchecked) =>
              app apply (
                app map ( rb.validate(b), (bb:B) => (_:A) :: bb ),
                ra.validate(a).fail.map {
                  _.map { case (p, errs) => Path \ idx -> errs }
                }
              )
          }
        )
      }

    implicit def caseApplicative2[A](implicit app: Applicative[VV]) =
      at[Rule[A,A], (Int, HNil)] { case (ra, (idx, rb)) =>
        ( idx+1,
          Rule[A::HNil, A::HNil] {
            case (a:A @unchecked) =>
              app map (ra.validate(a).fail.map {
                _.map { case (p, errs) => Path \ idx -> errs }
              }, (_:A) :: HNil)
          }
        )
      }

  }

  trait RuleApplicativeZipper {
    def apply[L <: HList, M <: HList](l: L)(implicit
      folder: RightFolder.Aux[L, (Int, Rule[HNil, HNil]), applicativeValidationFolder.type, (Int, Rule[M, M])]
    ): Rule[M, M] = l.foldRight(0 -> Rule.zero[HNil])(applicativeValidationFolder)(folder)._2
  }

  def hzip[L <: HList] = new RuleApplicativeZipper {}

}

trait HFold {

  object applicativeFolder2 extends Poly2 {
    implicit def caseApp[A, B <: HList, I, F[_, _]](implicit app: Applicative[({ type λ[O] = F[I, O] })#λ]) =
      at[F[I, A], F[I, B]] { (a, b) ⇒
        app.apply[A, A :: B](app.map[B, A ⇒ A :: B](b, x ⇒ y ⇒ y :: x), a)
      }
    implicit def casePure[A, B <: HList, I, F[_, _]](implicit app: Applicative[({ type λ[O] = F[I, O] })#λ], pure: A <:!< F[_, _]) =
      at[A, F[I, B]] { (a, b) ⇒
        app.apply[A, A :: B](app.map[B, A ⇒ A :: B](b, x ⇒ y ⇒ y :: x), app.pure(a))
      }
  }

  def liftRule[I] = new RuleLifter[I]
  class RuleLifter[I] {
    val app = Rule.applicativeRule[I]
    def apply[L <: HList](l: L)(implicit folder: RightFolder[L, Rule[I, HNil], applicativeFolder2.type]) =
      l.foldRight(app.pure(HNil: HNil))(applicativeFolder2)
  }

  def From[I] = new FromMaker[I]
  class FromMaker[I] {
    def apply[L <: HList, O](l: Reader[I] ⇒ L)(
      implicit folder: RightFolder.Aux[L, Rule[I, HNil], applicativeFolder2.type, Rule[I, O]]
    ) = play.api.data.mapping.From[I](__ ⇒ liftRule[I](l(__)))
  }

}