package utils

import scala.concurrent.{ExecutionContext, Future}
import doobie._
import doobie.util.transactor.Transactor
import cats.effect.IO
import cats.effect.{ Blocker, ContextShift }
import javax.sql.DataSource

trait DBHelper {

	def dbContext: DBContext

	implicit def ec: ExecutionContext = dbContext.dbEc.ec

}

case class DBContext(
  ds   : javax.sql.DataSource,
  dbEc : models.EC.DatabaseEC
) {
  import doobie.implicits._

  implicit val ec: ExecutionContext = dbEc.ec
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  val blocker: Blocker = cats.effect.Blocker.liftExecutionContext(ec)

  val xa: Transactor.Aux[IO,DataSource]  = Transactor.fromDataSource[IO](ds, ec, blocker)
  
  def run[T](a: ConnectionIO[T]): Future[T] = a.transact(xa).unsafeToFuture()

  def check(): Future[Int] = {
    run(sql"""SELECT 1""".query[Int].unique)
  }
}