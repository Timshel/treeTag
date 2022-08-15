package utils

import scala.concurrent.{ExecutionContext}
import doobie._
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import cats.effect.IO

trait DBHelper {

	def dbContext: DBContext

	implicit def ec: ExecutionContext = dbContext.dbEc.ec

}

case class DBContext(
  dbEc : models.EC.DatabaseEC
) {
  import doobie.implicits._

  val config = new HikariConfig()
  config.setJdbcUrl("jdbc:mysql://localhost:quill_demo")
  config.setUsername("admin")
  config.setPassword("password")
  config.setMaximumPoolSize(5)

  val xa: HikariTransactor[IO] = HikariTransactor.apply[IO](new HikariDataSource(config), dbEc.ec)
  
  def run[T](a: ConnectionIO[T]): IO[T] = a.transact(xa)

  def check(): IO[Int] = {
    run(sql"""SELECT 1""".query[Int].unique)
  }
}