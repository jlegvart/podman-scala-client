package io.podmanclient.server.service

import cats._
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.podmanclient.uri.PodmanUri._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.io.Source

class System(prefix: Uri.Path) {

  import SystemServiceResponse._

  val info = HttpRoutes.of[IO] { case GET -> prefix / "info" =>
    for {
      json <- infoResponseSuccess
      resp <- Ok(json)
    } yield resp
  }

  val ping = HttpRoutes
    .of[IO] { case GET -> prefix / "_ping" => Ok("Ok") }

  val events = HttpRoutes
    .of[IO] { case GET -> prefix / "events" =>
      for {
        json <- eventsResponseSuccess
        resp <- Ok(json)
      } yield resp
    }

  val df = HttpRoutes
    .of[IO] { case GET -> prefix / "system" / "df" =>
      for {
        json <- dfResponseSuccess
        resp <- Ok(json)
      } yield resp

    }

  def endpoints: HttpRoutes[IO] = info <+> ping <+> events <+> df
}

object System {

  def endpoints(prefix: Uri.Path): HttpRoutes[IO] = new System(prefix).endpoints

}

object SystemServiceResponse {

  def infoResponseSuccess: IO[Json] = loadResponse("info.json").flatMap(asJsonUnsafe)

  def dfResponseSuccess: IO[Json] = loadResponse("df.json").flatMap(asJsonUnsafe)

  def eventsResponseSuccess: IO[String] = loadResponse("events.json")

}
