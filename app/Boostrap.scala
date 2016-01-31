package env

import play.api.ApplicationLoader.Context
import play.api.cache.EhCacheComponents
import play.api.db.{ DBComponents }
import play.api.db.evolutions.EvolutionsComponents
import play.api._
import router.Routes

import service._

class BootstrapLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    // Bootstrap the injected application
    Logger.info("All good")
    new env.ApplicationEnv(context).application
  }
}

class ApplicationEnv(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EhCacheComponents
    with DBComponents
    with EvolutionsComponents {

  val gEc = models.EC.GlobalEC(scala.concurrent.ExecutionContext.Implicits.global)
  val dbEc = models.EC.DatabaseEC(scala.concurrent.ExecutionContext.Implicits.global)

  val connectionPool = new play.api.db.HikariCPConnectionPool(Environment.simple())
  val defaultDB = dbApi.database("default")
  val dynamicEvolutions = new play.api.db.evolutions.DynamicEvolutions()

  object Components {
    val articles = ArticleComponent(dbEc)
  }

  object Controllers {
    // an injected instance of the application controller
    val application = new controllers.Application(gEc)
    val articles = new controllers.Articles(Components.articles, gEc)
    val assets = new controllers.Assets(httpErrorHandler)
  }

  // routes using injected controllers
  val router = new Routes(httpErrorHandler, Controllers.articles, Controllers.assets)

  applicationEvolutions.start()
}