package io.podmanclient.server.service

import org.http4s.Uri
import org.http4s.HttpRoutes
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
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration._
import fs2.Stream

class Images(prefix: Uri.Path) {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  import ImagesServiceResponse._

  val list = HttpRoutes.of[IO] { case req @ GET -> prefix / "images" / "json" =>
    log(req.toString()) >> {
      listImages.flatMap(resp => Ok(resp))
    }
  }

  val inspect = HttpRoutes.of[IO] { case req @ GET -> prefix / "images" / name / "json" =>
    log(req.toString()) >> {
      name match {
        case "postgres_image" => postgresImage.flatMap(json => Ok(json))
        case _                => Response[IO](Status.NotFound).pure[IO]
      }
    }
  }

  def endpoints: HttpRoutes[IO] = list <+> inspect

}

object Images {

  def endpoints(prefix: Uri.Path): HttpRoutes[IO] = new Images(prefix).endpoints

}

object ImagesServiceResponse {

  def listImages: IO[Json] = List(postgresImage, kibanaImage).sequence.map(_.asJson)

  def postgresImage: IO[Json] = Json
    .obj(
      "Id"         -> "de1037fd2494248e13da280".asJson,
      "RepoTags"   -> List("docker.io/library/postgres:latest").asJson,
      "Containers" -> 2.asJson,
    )
    .pure[IO]

  def kibanaImage: IO[Json] = Json
    .obj(
      "Id"         -> "8a94aeb3facb2aa92b7933c".asJson,
      "RepoTags"   -> List("docker.io/library/kibana:8.1.1").asJson,
      "Containers" -> 1.asJson,
    )
    .pure[IO]

}
