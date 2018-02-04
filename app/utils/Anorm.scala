package utils

import shapeless.Generic
import shapeless.{ ::, HList, HNil }
import shapeless.ops.hlist._
import anorm._

import scala.concurrent.{ExecutionContext, Future}

object Anorm {
  implicit def hlistColumn[H](implicit c: Column[H]): Column[H :: HNil] =
    Column(c(_, _).map(_ :: HNil))

  implicit def anyvalDerivation[N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], c: Column[H]): Column[N] =
    Column(c(_, _).map(r => gen.from(r)))

  trait ToHlist[S] {
    type Out <: HList
    def apply(s: S): Out
  }

  trait LowPriorityToHlist {
    implicit def toHlist0[A, B] = new ToHlist[A ~ B] {
      type Out = A :: B :: HNil
      def apply(s: A ~ B): Out = s._1 :: s._2 :: HNil
    }
  }

  object ToHlist extends LowPriorityToHlist {
    type Aux[A, O] = ToHlist[A] { type Out = O }

    def apply[A, B](p: A ~ B)(implicit u: ToHlist[A ~ B]) = u(p)

    implicit def toHlistN[A, B, O <: HList](implicit u: ToHlist.Aux[A, O], p: Prepend[O, B :: HNil]) = new ToHlist[A ~ B] {
      type Out = p.Out
      def apply(s: A ~ B): Out = p(u(s._1), s._2 :: HNil)
    }
  }
}

trait AnormHelper {

  def dbEc: models.EC.DatabaseEC
  def database: play.api.db.Database

  implicit def ec: ExecutionContext = dbEc.ec

  def DB[T](f: java.sql.Connection => T): Future[T] = Future {
    database.withConnection(f)
  }
}