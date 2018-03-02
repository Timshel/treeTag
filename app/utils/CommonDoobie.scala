package utils

import java.net.URL
import doobie.Meta

trait CommonDoobie extends doobie.postgres.Instances {
  implicit def urlM : Meta[URL] = Meta[String].xmap(str => new URL(str), _.toString)
}
