package io.podmanclient.api

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import fs2.Stream
import io.circe.Json
import io.podmanclient.api.response.PodmanResponse
import io.podmanclient.api.response.ResponseError
import io.podmanclient.api.response.ResponseSuccess
import org.http4s.MediaType
import org.http4s.Method
import org.http4s.Method._
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Status.Ok
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import org.typelevel.jawn.fs2._
import io.podmanclient.api.uri.PodmanUri._
import io.podmanclient.client.PodmanClient._
import io.podmanclient._

import scala.concurrent.ExecutionContext.global

object System {

  implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

  def info[F[_]: Concurrent](prefix: String, client: Client[F]): F[PodmanResponse[Json]] =
    client.get(asUri(prefix, infoUri))(mapJsonResponse)

  def ping[F[_]: Concurrent](prefix: String, client: Client[F]): F[PodmanResponse[Json]] =
    client.get(asUri(prefix, pingUri))(mapJsonResponse)

  def df[F[_]: Concurrent](prefix: String, client: Client[F]): F[PodmanResponse[Json]] =
    client.get(asUri(prefix, dfUri))(mapJsonResponse)

  def events[F[_]: Concurrent](prefix: String, client: Client[F]): F[PodmanResponse[List[Json]]] =
    client
      .stream(Request[F](Method.GET, asUri(prefix, eventsUri).withQueryParam("stream", false)))
      .flatMap(_.body.chunks.parseJsonStream)
      .compile
      .toList
      .map(jsonList => ResponseSuccess(Some(jsonList)))

  def eventsStream[F[_]: Concurrent](prefix: String, client: Client[F]) = client
    .stream(Request[F](Method.GET, asUri(prefix, eventsUri).withQueryParam("stream", true)))
    .flatMap(_.body.chunks.parseJsonStream)

}
