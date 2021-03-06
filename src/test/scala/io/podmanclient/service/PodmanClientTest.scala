package io.podmanclient.service

import cats.effect._
import cats.effect.syntax.all._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.config.UnixSocketPodmanUri
import io.podmanclient.server.service._
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.compatible.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import io.podmanclient.error.PodmanErrors._
import io.podmanclient.service._

trait PodmanClientTest extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  val baseUri      = Root / "v3.0.0" / "libpod"
  val clientPrefix = Uri(path = baseUri)

  val mockServer =
    System.endpoints(baseUri) <+>
      Containers.endpoints(baseUri) <+> Images.endpoints(baseUri)

  val client = Logger(logHeaders = true, logBody = false)(Client.fromHttpApp(mockServer.orNotFound))

  val systemService     = SystemService(clientPrefix, client)
  val containersService = ContainersService(clientPrefix, client)
  val imagesService     = ImagesService(clientPrefix, client)

  def assert[A](
    request: => IO[Either[PodmanError, A]],
    expectedResponse: IO[Either[PodmanError, A]],
  ): IO[Assertion] = request.flatMap { response =>
    expectedResponse.map { expected =>
      response should equal(expected)
    }
  }

}
