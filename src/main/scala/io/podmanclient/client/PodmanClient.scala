package io.podmanclient.client

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import fs2.io.net.unixsocket.UnixSocketAddress
import io.circe.Json
import io.podmanclient.config._
import org.http4s._
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
