package io.podmanclient.api

import cats.effect._
import cats.effect.syntax.all._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.podmanclient.api.response.PodmanResponse
import io.podmanclient.api.response.ResponseSuccess
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.config.UnixSocketPodmanUri
import io.podmanclient.server.service.SystemService
import io.podmanclient.server.service.SystemServiceResponse
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import io.podmanclient.server.service.ContainersService

trait PodmanClientTest extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  val clientPrefix = "/v3.0.0/libpod/"
  val serverPrefix = Root / "v3.0.0" / "libpod"

  val mockServer =
    SystemService.endpoints(serverPrefix) <+>
      ContainersService.endpoints(serverPrefix)

  val client =
    Logger(logHeaders = true, logBody = false)(Client.fromHttpApp(mockServer.orNotFound))

  def assert[A](
    request: => IO[PodmanResponse[A]],
    expectedResponse: IO[PodmanResponse[A]],
  ): IO[Assertion] = request.flatMap { response =>
    expectedResponse.map { expected =>
      response should equal(expected)
    }
  }

}
