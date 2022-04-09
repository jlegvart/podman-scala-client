package io.podmanclient.service

import cats._
import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import fs2.Stream
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.typelevel.jawn.fs2._
import io.podmanclient.uri.PodmanUri._
import io.podmanclient._

import scala.concurrent.ExecutionContext.global
import io.podmanclient.error.PodmanErrors._

class SystemService[F[_]: Concurrent] private (base: Uri, client: Client[F]) {

  implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

  def info(): F[Either[PodmanError, Json]] =
    client.get(infoUri(base)) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(_.asRight)

        case _ => orError(response)
      }
    }

  def ping(): F[Either[PodmanError, Boolean]] =
    client.get(pingUri(base)) { response =>
      response.status match {
        case status @ Status.Ok => true.asRight.pure[F]
        case _                  => orError(response)
      }
    }

  def df(): F[Either[PodmanError, Json]] =
    client.get(dfUri(base)) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(_.asRight)
        case _ => orError(response)
      }
    }

  def events(): F[Either[PodmanError, List[Json]]] = {
    val stream = client
      .stream(Request[F](Method.GET, eventsUri(base).withQueryParam("stream", false)))

    stream
      .flatMap(_.body.chunks.parseJsonStream)
      .compile
      .toList
      .map(_.asRight)
  }

  def eventsStream(): Stream[F, Json] = client
    .stream(Request[F](Method.GET, eventsUri(base).withQueryParam("stream", true)))
    .flatMap(_.body.chunks.parseJsonStream)

}

object SystemService {

  def apply[F[_]: Concurrent](base: Uri, client: Client[F]) = new SystemService(base, client)

}
