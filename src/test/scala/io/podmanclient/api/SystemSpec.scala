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
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.global

class SystemTest extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  val uriPrefix = "/v3.0.0/libpod/"

  val mockServer = SystemService.endpoints(Root / "v3.0.0" / "libpod").orNotFound
  val clientRaw  = Logger(logHeaders = true, logBody = false)(Client.fromHttpApp(mockServer))

  "Info" should "return podman info JSON" in {
    assert(
      System.info(uriPrefix, clientRaw),
      SystemServiceResponse.infoResponseSuccess.map(resp => ResponseSuccess(Some(resp))),
    )
  }

  "Ping" should "return successful response without content" in {
    assert(System.ping(uriPrefix, clientRaw), ResponseSuccess[Json](None).pure[IO])
  }

  "df" should "return JSON with disk usage info" in {
    assert(
      System.df(uriPrefix, clientRaw),
      SystemServiceResponse.dfResponseSuccess.map(resp => ResponseSuccess(Some(resp))),
    )
  }

  "Events" should "return collection of JSON events" in {
    val jsonList =
      for {
        jsonList <- SystemServiceResponse.eventsResponseSuccess
        a        <- jsonList.split("\n").map(str => decode[Json](str).liftTo[IO]).toList.sequence
      } yield a

    assert(
      System.events(uriPrefix, clientRaw),
      jsonList.map(col => ResponseSuccess(Some(col))),
    )
  }

  def assert[A](
    request: => IO[PodmanResponse[A]],
    expectedResponse: IO[PodmanResponse[A]],
  ): IO[Assertion] = request.flatMap { response =>
    expectedResponse.map { expected =>
      response should equal(expected)
    }
  }

}
