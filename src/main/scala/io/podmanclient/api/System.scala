package io.podmanclient.api

import cats._
import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import fs2.Stream
import io.circe.Json
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
import io.podmanclient.api.response.PodmanResult
import io.podmanclient.api.response.ResultSuccess
import io.podmanclient.api.response.ResponseEmpty
import io.podmanclient.api.response.ResultFailure
import io.podmanclient.api.response.ResponseBody

object System {

  implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

  def info[F[_]: Concurrent](base: Uri, client: Client[F]): F[PodmanResult] =
    client.get(infoUri(base)) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(json => ResultSuccess(status.code, ResponseBody(Some(json))))

        case _ => orError(response)
      }
    }

  def ping[F[_]: Concurrent](base: Uri, client: Client[F]): F[PodmanResult] =
    client.get(pingUri(base)) { response =>
      response.status match {
        case status @ Status.Ok => ResultSuccess(status.code, ResponseEmpty).pure[F].widen
        case _                  => orError(response)
      }
    }

  def df[F[_]: Concurrent](base: Uri, client: Client[F]): F[PodmanResult] =
    client.get(dfUri(base)) { response =>
      response.status match {
        case status @ Status.Ok =>
          response
            .as[Json]
            .map(json => ResultSuccess(status.code, ResponseBody(Some(json))))
        case _ => orError(response)
      }
    }

  def events[F[_]: Concurrent](base: Uri, client: Client[F]): F[PodmanResult] = {
    val stream = client
      .stream(Request[F](Method.GET, eventsUri(base).withQueryParam("stream", false)))

    stream
      .flatMap(_.body.chunks.parseJsonStream)
      .compile
      .toList
      .map(jsonList => ResultSuccess(200, ResponseBody(Some(jsonList))))
  }

  def eventsStream[F[_]: Concurrent](base: Uri, client: Client[F]): Stream[F, Json] = client
    .stream(Request[F](Method.GET, eventsUri(base).withQueryParam("stream", true)))
    .flatMap(_.body.chunks.parseJsonStream)

}
