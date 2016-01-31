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

trait BinaryTCConstraint[L <: HList, TC[_,_]]

object BinaryTCConstraint {
  type **->**[TC[_,_]] = {
    type λ[L <: HList] = BinaryTCConstraint[L, TC]
  }

  implicit def hnilBinaryTC[TC[_,_]] = new BinaryTCConstraint[HNil, TC] {}

  implicit def hlistBinaryTC1[H1, H2, T <: HList, TC[_,_]](implicit utct : BinaryTCConstraint[T, TC]) =
    new BinaryTCConstraint[TC[H1,H2] :: T, TC] {}
}

trait BinaryTCConstraintLeftFixed[L <: HList, I, TC[_,_]]

object BinaryTCConstraintLeftFixed {
  type +*->+*[I, TC[_,_]] = {
    type λ[L <: HList] = BinaryTCConstraintLeftFixed[L, I, TC]
  }

  implicit def hnilBinaryTCLeftFixed[I, TC[_,_]] = new BinaryTCConstraintLeftFixed[HNil, I, TC] {}

  implicit def hlistBinaryTCLeftFixed1[H, T <: HList, I, TC[_,_]](implicit utct : BinaryTCConstraintLeftFixed[T, I, TC]) =
    new BinaryTCConstraintLeftFixed[TC[I,H] :: T, I, TC] {}
}

trait BinaryTCConstraintEquals[L <: HList, TC[_,_]]

object BinaryTCConstraintEquals {
  type *=->*=[TC[_,_]] = {
    type λ[L <: HList] = BinaryTCConstraintEquals[L, TC]
  }

  implicit def hnilBinaryTCEquals[TC[_,_]] = new BinaryTCConstraintEquals[HNil, TC] {}

  implicit def hlistBinaryTCEquals1[H, T <: HList, TC[_,_]](implicit utct : BinaryTCConstraintEquals[T, TC]) =
    new BinaryTCConstraintEquals[TC[H,H] :: T, TC] {}
}



trait HZip {
  object applicativeValidationFolder extends Poly2 {

    implicit def caseApplicative[A, B <: HList](implicit
      app: Applicative[VV]
    ) = at[Rule[A,A], (Int, Rule[B,B])] {
      case (ra, (idx, rb)) =>
      (
        idx+1,
        Rule[A::B, A::B] {
          case (a:A @unchecked) :: (b:B @unchecked) =>
            app apply (
              app map (
                rb.validate(b),
                (bb:B) => (_:A) :: bb
              ),
              ra.validate(a).fail.map {
                _.map {
                  case (p, errs) => Path \ idx -> errs
                }
              }
            )
        } //.repath(_ => Path \ idx)
      )
    }

    implicit def caseApplicative2[A](implicit
      app: Applicative[VV]
    ) = at[Rule[A,A], (Int, HNil)] {
      case (ra, (idx, rb)) =>
      (
        idx+1,
        Rule[A::HNil, A::HNil] {
          case (a:A @unchecked) =>
            app map (ra.validate(a).fail.map {
              _.map {
                case (p, errs) => Path \ idx -> errs
              }
            }, (_:A) :: HNil)
        }
      )
    }

  }

  RightFolder.hnilRightFolder[
    (Int, Rule[HNil, HNil]),
    applicativeValidationFolder.type
  ]

  trait RuleApplicativeZipper {
    def apply[L <: HList, M <: HList](l: L)(implicit
      all: BinaryTCConstraintEquals.*=->*=[Rule]#λ[L],
      folder: RightFolder.Aux[L, (Int, Rule[HNil, HNil]), applicativeValidationFolder.type, (Int, Rule[M, M])]
    ): Rule[M, M] = l.foldRight((0:Int, Rule.zero[HNil]:Rule[HNil, HNil]))(applicativeValidationFolder)(folder)._2
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

  def from[I] = new FromMaker[I]
  class FromMaker[I] {
    def apply[L <: HList, O](l: Reader[I] ⇒ L)(implicit folder: RightFolder.Aux[L, Rule[I, HNil], applicativeFolder2.type, Rule[I, O]]) =
      From[I](__ ⇒ liftRule[I](l(__)))
  }

}