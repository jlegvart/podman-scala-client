package io.podmanclient.server.service

import cats.effect._
import cats._
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._
import io.podmanclient.api.uri.PodmanUri._
import scala.io.Source
import io.circe._
import io.circe.syntax._
import org.http4s.circe._
import io.circe.parser._
import org.http4s.Uri

class SystemService(prefix: Uri.Path) {

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

object SystemService {

  def endpoints(prefix: Uri.Path): HttpRoutes[IO] = new SystemService(prefix).endpoints

}

object SystemServiceResponse {

  def infoResponseSuccess: IO[Json] = loadResponse("info.json").flatMap(asJsonUnsafe)

  def dfResponseSuccess: IO[Json] = loadResponse("df.json").flatMap(asJsonUnsafe)

  def eventsResponseSuccess: IO[String] = loadResponse("events.json")

  def asJsonUnsafe(str: String): IO[Json] = decode[Json](str).liftTo[IO]

  def loadResponse(fileName: String): IO[String] = Resource
    .make(IO.blocking(Source.fromResource(s"response/${fileName}")))(s => IO.blocking(s.close))
    .use { source =>
      source.mkString.pure[IO]
    }

}