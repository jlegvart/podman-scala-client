package io.podmanclient.api

import cats._
import cats.effect.Concurrent
import cats.effect._
import cats.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.podmanclient._
import io.podmanclient.api.uri.PodmanUri._
import io.podmanclient.client.PodmanClient._
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import io.podmanclient.api.response.PodmanResult
import org.http4s.Status
import io.podmanclient.api.response.ResultSuccess
import io.podmanclient.api.response.ResponseBody
import io.podmanclient.api.response.ResponseEmpty
import io.podmanclient.api.response.ResultInfo

object Containers {

  def list[F[_]: Concurrent](
    base: Uri,
    all: Boolean = false,
    filters: Map[String, List[String]] = Map.empty,
    limit: Int = 10,
  )(
    client: Client[F]
  ): F[PodmanResult] = {
    val r = listContainersUri(base)
      .withQueryParam("all", all)
      .withQueryParam("filters", filters.asJson.noSpaces)
      .withQueryParam("limit", limit)

    client.get(r) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(json => ResultSuccess(status.code, ResponseBody(Some(json))))
        case _ => orError(response)
      }
    }
  }

  def create[F[_]: Concurrent](
    base: Uri,
    image: String,
    name: Option[String] = None,
    env: Map[String, String] = Map.empty,
    labels: Map[Int, String] = Map.empty,
    portMappings: List[PortMapping] = List.empty,
  )(
    client: Client[F]
  ): F[PodmanResult] = {
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
        case status @ Status.Created =>
          response.as[Json].map(json => ResultSuccess(status.code, ResponseBody(Some(json))))
        case _ => orError(response)
      }
    }
  }

  def start[F[_]: Concurrent](
    base: Uri,
    name: String,
  )(
    client: Client[F]
  ): F[PodmanResult] = {
    val request: Request[F] = Request[F](Method.POST, startContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ Status.NoContent   => ResultSuccess(status.code, ResponseEmpty).pure[F].widen
        case status @ Status.NotModified => ResultInfo(status.code, ResponseEmpty).pure[F].widen
        case _                           => orError(response)
      }
    }
  }

  def stop[F[_]: Concurrent](
    base: Uri,
    name: String,
  )(
    client: Client[F]
  ): F[PodmanResult] = {
    val request: Request[F] = Request[F](Method.POST, stopContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ Status.NoContent   => ResultSuccess(status.code, ResponseEmpty).pure[F].widen
        case status @ Status.NotModified => ResultInfo(status.code, ResponseEmpty).pure[F].widen
        case _                           => orError(response)
      }
    }
  }

  def inspect[F[_]: Concurrent](
    base: Uri,
    name: String,
  )(
    client: Client[F]
  ): F[PodmanResult] = {
    val request: Request[F] = Request[F](Method.POST, inspectContainerUri(base, name))
    client.run(request).use { response =>
      response.status match {
        case status @ Status.Ok =>
          response.as[Json].map(json => ResultSuccess(status.code, ResponseBody(Some(json))))
        case _ => orError(response)
      }
    }
  }

}

case class PortMapping(
  container_port: Int = 0,
  host_port: Int = 0,
  protocol: String = "tcp",
  host_ip: Option[String] = None,
  range: Option[Int] = None,
)
