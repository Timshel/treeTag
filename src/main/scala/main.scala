import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import service._
import models.EC
import service.db.ArticleTable
import utils.DBContext

object Server {

  def stream() = {
    implicit val dbEc: EC.DatabaseEC = models.EC.DatabaseEC(scala.concurrent.ExecutionContext.Implicits.global)

    val dbContext: DBContext = utils.DBContext(dbEc)

    object Services {
      val articleTable: ArticleTable = db.ArticleTable(dbContext)
    }

    object Controllers {
      val articles = new controllers.ArticlesController(Services.articleTable)
    }

    val httpApp = org.http4s.server.Router(
      "/api/articles" -> Controllers.articles.routes
    ).orNotFound

    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Logger.httpApp(true, true)(httpApp))
      .build
      .use(_ => IO.never)
  }
}


object Main extends IOApp {
  def run(args: List[String]) =
    Server.stream().as(ExitCode.Success)
}
