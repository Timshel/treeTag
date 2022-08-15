package utils

import java.net.URL
import doobie.Meta

trait CommonDoobie extends doobie.postgres.Instances {
  implicit val urlM : Meta[URL] = Meta[String].imap(str => new URL(str))(_.toString)
}
