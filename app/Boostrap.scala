package env

import play.api.ApplicationLoader.Context
import play.api.cache.ehcache.EhCacheComponents
import play.api.db.{ DBComponents }
import play.api.db.evolutions.EvolutionsComponents
import router.Routes
import play.api.{Application, ApplicationLoader, Environment, Logger}
import service._

class BootstrapLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    // Bootstrap the injected application
    Logger.info("All good")
    new env.ApplicationEnv(context).application
  }
} 

class ApplicationEnv(context: Context)
    extends play.api.BuiltInComponentsFromContext(context)
    with controllers.AssetsComponents 
    with EhCacheComponents
    with DBComponents
    with EvolutionsComponents {

  implicit val gEc = models.EC.GlobalEC(scala.concurrent.ExecutionContext.Implicits.global)
  implicit val dbEc = models.EC.DatabaseEC(scala.concurrent.ExecutionContext.Implicits.global)

  val connectionPool = new play.api.db.HikariCPConnectionPool(Environment.simple())
  val dbContext = utils.DBContext(dbApi.database("default").dataSource, dbEc)

  object Services {
    val articleTable = db.ArticleTable(dbContext)
  }

  object Controllers {
    // an injected instance of the application controller
    val application = new controllers.Application(controllerComponents)
    val articles = new controllers.Articles(Services.articleTable)(controllerComponents, gEc)
  }

  val httpFilters = Seq.empty
  val router = new Routes(httpErrorHandler, Controllers.articles, assets)

  applicationEvolutions.start()
}