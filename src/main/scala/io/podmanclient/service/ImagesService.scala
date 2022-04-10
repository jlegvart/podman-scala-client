package io.podmanclient.service

import cats.effect.Concurrent
import cats.syntax.all._
import io.circe.Json
import io.circe._
import io.circe.syntax._
import io.podmanclient._
import io.podmanclient.error.PodmanErrors._
import io.podmanclient.uri.PodmanUri._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl._

class ImagesService[F[_]: Concurrent] private (
  base: Uri,
  client: Client[F],
) {

  def list(
    all: Boolean = false,
    filters: Map[String, List[String]] = Map.empty,
    size: Boolean = true,
  ): F[Either[PodmanError, Json]] = {
    val r = listImagesUri(base)
      .withQueryParam("all", all)
      .withQueryParam("filters", filters.asJson.noSpaces)
      .withQueryParam("size", size)

    client.get(r) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(_.asRight)
        case _ => orError(response)
      }
    }
  }

  def inspect(name: String): F[Either[PodmanError, Json]] =
    client.get(inspectImageUri(base, name)) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(_.asRight)
        case status @ Status.NotFound => NoSuchImage(name).asLeft[Json].pure[F].widen
        case _                        => orError(response)
      }
    }

}

object ImagesService {

  def apply[F[_]: Concurrent](base: Uri, client: Client[F]) = new ImagesService(base, client)

}
