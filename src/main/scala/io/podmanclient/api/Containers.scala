package io.podmanclient.api

import cats._
import cats.effect._
import cats.syntax.all._
import cats.effect.Concurrent
import org.http4s.client.Client
import io.podmanclient.api.response.PodmanResponse
import io.podmanclient.client.PodmanClient._
import io.podmanclient.api.uri.PodmanUri._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.client._
import org.http4s.implicits._
import io.circe.syntax._
import io.circe.Json
import io.circe.generic.auto._
import io.podmanclient._
import org.http4s.Request
import org.http4s.Method
import io.podmanclient.api.response.ResponseSuccess
import org.http4s.Uri

object Containers {

  def list[F[_]: Concurrent](
    prefix: String,
    all: Boolean = false,
    filters: Map[String, List[String]] = Map.empty,
    limit: Int = 10,
  )(
    client: Client[F]
  ): F[PodmanResponse[Json]] = {
    val r = asUri(prefix, listContainersUri)
      .withQueryParam("all", all)
      .withQueryParam("filters", filters.asJson.noSpaces)
      .withQueryParam("limit", limit)
    client.get(r)(mapJsonResponse)
  }

  def create[F[_]: Concurrent](
    prefix: String,
    image: String,
    name: Option[String] = None,
    env: Map[String, String] = Map.empty,
    labels: Map[Int, String] = Map.empty,
    portMappings: List[PortMapping] = List.empty,
  )(
    client: Client[F]
  ): F[PodmanResponse[Json]] = {
    val body = Json.obj(
      "image"        -> image.asJson,
      "name"         -> name.asJson,
      "env"          -> env.asJson,
      "labels"       -> labels.asJson,
      "portMappings" -> portMappings.asJson,
    )
    val request: Request[F] = Request[F](Method.POST, asUri(prefix, createContainerUri))
      .withEntity[Json](body)
    client.expect[Json](request).map(json => ResponseSuccess(Some(json)))
  }

  def start[F[_]: Concurrent](
    prefix: String,
    name: String)(
    client: Client[F]
  ): F[PodmanResponse[Json]] = {
    val u = Uri.unsafeFromString(prefix) / "containers" / name / "start"
    val request: Request[F] = Request[F](Method.POST, u)
      
    client.expect[Json](request).map(json => ResponseSuccess(Some(json)))
  } 
}

case class PortMapping(
  container_port: Int = 0,
  host_port: Int = 0,
  protocol: String = "tcp",
  host_ip: Option[String] = None,
  range: Option[Int] = None,
)
