package controllers

import java.net.URL

import models._
import play.api.mvc._
import views.html.helper._

object RouteBindings {

  def urlDecode(string: String)(implicit codec: play.api.mvc.Codec): String =
    java.net.URLDecoder.decode(string, codec.charset)

  private def msg(t: String) = (key: String, e: Exception) => s"Cannot parse parameter %key as $t: ${e.getMessage}"

  implicit object urlQSBindable extends QueryStringBindable.Parsing[URL](
    s => new URL(urlDecode(s)), u => urlEncode(u.toString), msg("URL"))

  implicit object articleUUIDBindables extends PathBindable.Parsing[Article.UUID](
    s => Article.UUID(s), _.value, msg("Article.UUID"))

  implicit object articleUUIDBindable extends QueryStringBindable.Parsing[Article.UUID](
    s => Article.UUID(s), _.value, msg("Article.UUID"))

}