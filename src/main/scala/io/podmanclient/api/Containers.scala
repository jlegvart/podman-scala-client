package io.podmanclient.api

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

object Containers {

  def list[F[_]: Concurrent](
    all: Boolean = false,
    filters: Map[String, List[String]] = Map.empty,
    limit: Int = 10,
    pod: Boolean = false,
    size: Boolean = false,
    sync: Boolean = false,
  )(
    client: Client[F]
  ): F[PodmanResponse[Json]] = {
    val r = listContainers
      .withQueryParam("all", all)
      .withQueryParam("filters", filters.asJson.noSpaces)
      .withQueryParam("limit", limit)
      .withQueryParam("pod", pod)
      .withQueryParam("size", size)
      .withQueryParam("sync", sync)
    client.get(r)(mapJsonResponse)
  }

}
