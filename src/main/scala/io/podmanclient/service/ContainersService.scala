package io.podmanclient.service

import cats._
import cats.effect.Concurrent
import cats.effect._
import cats.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.podmanclient._
import io.podmanclient.uri.PodmanUri._
import io.podmanclient.client.PodmanClient._
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.Status
import io.podmanclient.error.PodmanErrors._

class ContainersService[F[_]: Concurrent] private (
  base: Uri,
  client: Client[F],
) {

  def list(
    all: Boolean = false,
    filters: Map[String, List[String]] = Map.empty,
    limit: Int = 10,
  ): F[Either[PodmanError, Json]] = {
    val r = listContainersUri(base)
      .withQueryParam("all", all)
      .withQueryParam("filters", filters.asJson.noSpaces)
      .withQueryParam("limit", limit)

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

  def create(
    image: String,
    name: Option[String] = None,
    env: Map[String, String] = Map.empty,
    labels: Map[Int, String] = Map.empty,
    portMappings: List[PortMapping] = List.empty,
  ): F[Either[PodmanError, Json]] = {
    val body = Json.obj(
      "image"        -> image.asJson,
      "name"         -> name.asJson,
      "env"          -> env.asJson,
      "labels"       -> labels.asJson,
      "portMappings" -> portMappings.asJson,
    )
    val request: Request[F] = Request[F](Method.POST, createContainerUri(base))
      .withEntity[Json](body)

    client.run(request).use { response =>
      response.status match {
        case status @ Status.Created => response.as[Json].map(_.asRight)
        case _                       => orError(response)
      }
    }
  }

  def start(name: String): F[Either[PodmanError, Unit]] = {
    val request: Request[F] = Request[F](Method.POST, startContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ (Status.NoContent | Status.NotModified) => ().asRight.pure[F]
        case status @ Status.NotFound => NoSuchContainer(name).asLeft[Unit].pure[F].widen
        case _                        => orError(response)
      }
    }
  }

  def stop(name: String): F[Either[PodmanError, Unit]] = {
    val request: Request[F] = Request[F](Method.POST, stopContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ (Status.NoContent | Status.NotModified) => ().asRight.pure[F]
        case status @ Status.NotFound => NoSuchContainer(name).asLeft[Unit].pure[F].widen
        case _                        => orError(response)
      }
    }
  }

  def delete(name: String): F[Either[PodmanError, Unit]] = {
    val request: Request[F] = Request[F](Method.DELETE, deleteContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ Status.NoContent => ().asRight.pure[F]
        case status @ Status.NotFound  => NoSuchContainer(name).asLeft[Unit].pure[F].widen
        case _                         => orError(response)
      }
    }
  }

  def inspect(name: String): F[Either[PodmanError, Json]] = {
    val request: Request[F] = Request[F](Method.GET, inspectContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ Status.Ok       => response.as[Json].map(_.asRight)
        case status @ Status.NotFound => NoSuchContainer(name).asLeft[Json].pure[F].widen
        case _                        => orError(response)
      }
    }
  }

  def logs(
    name: String,
    stderr: Boolean = true,
    stdout: Boolean = true,
    timestamps: Boolean = true,
  ): F[Either[PodmanError, String]] = {
    val request: Request[F] = Request[F](
      Method.GET,
      logsContainerUri(base, name)
        .withQueryParam("stderr", stderr)
        .withQueryParam("stdout", stdout)
        .withQueryParam("timestamps", timestamps),
    )

    client.run(request).use { response =>
      response.status match {
        case status @ Status.Ok       => response.as[String].map(_.asRight)
        case status @ Status.NotFound => NoSuchContainer(name).asLeft[String].pure[F].widen
        case _                        => orError(response)
      }
    }
  }

}

object ContainersService {

  def apply[F[_]: Concurrent](
    base: Uri,
    client: Client[F],
  ) = new ContainersService(base, client)

}

case class PortMapping(
  container_port: Int = 0,
  host_port: Int = 0,
  protocol: String = "tcp",
  host_ip: Option[String] = None,
  range: Option[Int] = None,
)
