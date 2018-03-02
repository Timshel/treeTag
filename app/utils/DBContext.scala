package utils

import scala.concurrent.{ExecutionContext, Future}
import doobie._
import doobie.util.transactor.Transactor
import cats.effect.IO

trait DBHelper {

	def dbContext: DBContext

	implicit def ec: ExecutionContext = dbContext.dbEc.ec

}

case class DBContext(
  ds   : javax.sql.DataSource,
  dbEc : models.EC.DatabaseEC
) {
  import doobie.implicits._

  implicit val ec = dbEc.ec

  val xa  = Transactor.fromDataSource[IO](ds)
  
  def run[T](a: ConnectionIO[T]): Future[T] = a.transact(xa).unsafeToFuture()

  def check(): Future[Int] = {
    run(sql"""SELECT 1""".query[Int].unique)
  }
}