package utils

import scala.deriving.Mirror

implicit class TupleSelector[T <: Tuple](t: T){
  def select[U](implicit selector: TupleSelector.Selector[T, U]): U = selector(t)
}

object TupleSelector {
  /**
   * Type class supporting access to the first element of this `HList` of type `U`. Available only if this `HList`
   * contains an element of type `U`.
   */
  //@implicitNotFound("Implicit not found: shapeless.Ops.Selector[${L}, ${U}]. You requested an element of type ${U}, but there is none in the HList ${L}.")
  trait Selector[T <: Tuple, U] { 
    type Out = U 

    def apply(t: T): Out
  }

  object Selector {

    def apply[T <: Tuple, U](implicit selector: Selector[T, U]): Selector[T, U] = selector

    implicit def select[H, T <: Tuple]: Selector[H *: T, H] =
      new Selector[H *: T, H] {
        def apply(t : H *: T) = t.head
      }

    implicit def recurse[H, T <: Tuple, U]
      (implicit st : Selector[T, U]): Selector[H *: T, U] =
        new Selector[H *: T, U] {
          def apply(l : H *: T) = st(l.tail)
        }
  }
}