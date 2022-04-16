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

  def infoResponseSuccess: IO[Json] = Json
    .obj(
      "version" -> Json.obj(
        "APIVersion" -> "3.4.4".asJson,
        "Version"    -> "3.4.4".asJson,
        "OsArch"     -> "linux/amd64".asJson,
      )
    )
    .pure[IO]

  def dfResponseSuccess: IO[Json] = Json
    .obj(
      "Images" -> List(
        Map(
          "Repository" -> "docker.io/library/postgres",
          "Tag"        -> "latest",
          "ImageID"    -> "07e2ee723e2d9c8c141137",
        )
      ).asJson,
      "Containers" -> List(
        Map(
          "ContainerID" -> "35f7fc91ec45c4451fbd9e656e55e26ab63bde83ed68657732ef04bd1977a532",
          "Image"       -> "07e2ee723e2d9c8c141137",
        )
      ).asJson,
    )
    .pure[IO]

  def eventsResponseSuccess: IO[String] = loadResponse("events.json")

}
