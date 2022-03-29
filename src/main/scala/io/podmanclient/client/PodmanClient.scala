package io.podmanclient.client

import cats.effect._
import cats.effect.implicits._
import cats.effect.kernel.Resource
import cats.implicits._
import fs2.io.net.unixsocket.UnixSocketAddress
import io.circe.Json
import io.podmanclient.api.response._
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.config.UnixSocketPodmanUri
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.middleware.UnixSocket
import org.http4s.ember.client.EmberClientBuilder

object PodmanClient {

  def createClient[F[_]: Async](config: PodmanConfig): Resource[F, Client[F]] =
    config.uri match {
      case UnixSocketPodmanUri(uri) => defaultSocketClient(UnixSocketAddress(uri))
      case TcpPodmanUri(u) =>
        Uri
          .fromString(u)
          .liftTo[F]
          .toResource
          .flatMap(defaultTCPClient(_))
    }

  def mapJsonResponse[F[_]: Concurrent]: Response[F] => F[PodmanResponse[Json]] =
    response =>
      response.status match {
        case Status.Ok => bodyAsJson(response).flatMap(body => ResponseSuccess(body).pure[F]).widen
        case _         => bodyAsJson(response).flatMap(body => ResponseError(body).pure[F]).widen
      }

  private def bodyAsJson[F[_]: Concurrent](
    response: Response[F]
  ): F[Option[Json]] = response.as[Json].attempt.map {
    case Left(value)  => None
    case Right(value) => Some(value)
  }

  private def defaultSocketClient[F[_]: Async](
    socketAddress: UnixSocketAddress
  ): Resource[F, Client[F]] = EmberClientBuilder
    .default[F]
    .build
    .map(client => UnixSocket(socketAddress)(client))

  private def defaultTCPClient[F[_]: Async](uri: Uri): Resource[F, Client[F]] = EmberClientBuilder
    .default[F]
    .build
    .map { c =>
      Client { req =>
        c.run(req.withUri(uri.resolve(req.uri)))
      }
    }

}
