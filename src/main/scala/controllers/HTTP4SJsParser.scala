
/*
 * Copyright 2018 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on https://github.com/typelevel/jawn/blob/v1.0.3/support/play/src/main/scala/Parser.scala
 * Copyright Erik Osheim, 2012-2020
 * See licenses/LICENSE_jawn
 */

package controllers

import cats.effect.IO
import fs2.Chunk
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.headers.`Content-Type`
import org.http4s.jawn
import org.http4s.MediaType
import org.typelevel.jawn.Facade
import org.typelevel.jawn.SupportParser
import play.api.libs.json._

object HTTP4SJsParser extends SupportParser[JsValue] {

  implicit val facade: Facade[JsValue] =
    new Facade.SimpleFacade[JsValue] {
      def jnull: JsValue = JsNull
      val jfalse: JsValue = JsBoolean(false)
      val jtrue: JsValue = JsBoolean(true)

      def jnum(s: CharSequence, decIndex: Int, expIndex: Int): JsValue = JsNumber(
        BigDecimal(s.toString)
      )
      def jstring(s: CharSequence): JsValue = JsString(s.toString)

      def jarray(vs: List[JsValue]): JsValue = JsArray(vs)
      def jobject(vs: Map[String, JsValue]): JsValue = JsObject(vs)
    }


  implicit def jsonDecoder: EntityDecoder[IO, JsValue] =
    jawn.jawnDecoder[IO, JsValue]

  implicit def jsonEncoder: EntityEncoder[IO, JsValue] =
    EntityEncoder[IO, Chunk[Byte]]
      .contramap[JsValue] { json =>
        val bytes = json.toString.getBytes("UTF8")
        Chunk.array(bytes)
      }
      .withContentType(`Content-Type`(MediaType.application.json))
}