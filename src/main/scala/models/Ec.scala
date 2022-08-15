package models

import scala.concurrent.ExecutionContext


object EC {

	case class GlobalEC(ec: ExecutionContext)
	case class DatabaseEC(ec: ExecutionContext)

}