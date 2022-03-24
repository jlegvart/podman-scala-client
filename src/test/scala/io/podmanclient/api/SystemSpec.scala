package io.podmanclient.api

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.UnixSocketPodmanUri
import cats.effect.IO
import scala.concurrent.ExecutionContext.global
import cats.effect.IOApp
import org.scalatest.funsuite.AsyncFunSuite
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.http4s.client.Client
import org.scalatest.compatible.Assertion
import io.podmanclient.api.response.PodmanResponse
import org.http4s.Response
import io.circe.Json

class SystemTest
  extends AsyncFlatSpec
  with AsyncIOSpec
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll {

  var _podmanClient: Option[Resource[IO, Client[IO]]] = None

  def podmanClient: Resource[IO, Client[IO]] = _podmanClient.getOrElse(
    Resource.eval(IO.raiseError(new RuntimeException("No podman client available")))
  )

  override protected def beforeAll(): Unit = {
    val config = PodmanConfig(
      new UnixSocketPodmanUri("/run/user/1000/podman/podman.sock")
    )

    _podmanClient = Some(PodmanClient.createClient[IO](config))
  }

  "Info" should "return podman info JSON with specific APIVersion" in {
    val f: Client[IO] => IO[PodmanResponse[Json]] = client => System.info(client)
    val assertion: IO[PodmanResponse[Json]] => IO[Assertion] =
      response =>
        response.asserting(_.response should not be empty) >>
          response.asserting(_.response.get \\ "Version" should not be empty) >>
          response.asserting(pr =>
            (pr.response.get \\ "APIVersion").head.toString should ===("\"3.4.4\"")
          )

    check(f, assertion)
  }

  "Ping" should "return successful response without content" in {
    val f: Client[IO] => IO[PodmanResponse[Json]] = client => System.ping(client)
    val assertion: IO[PodmanResponse[Json]] => IO[Assertion] =
      response => response.asserting(_.response should equal(None))

    check(f, assertion)
  }

  "df" should "return JSON with disk usage info" in {
    val f: Client[IO] => IO[PodmanResponse[Json]] = client => System.df(client)
    val assertion: IO[PodmanResponse[Json]] => IO[Assertion] =
      response =>
        response.asserting(_.response should not be empty) >>
          response.asserting { pr =>
            (pr.response.get \\ "Images") should not be empty
            (pr.response.get \\ "Containers") should not be empty
            (pr.response.get \\ "Volumes") should not be empty
          }

    check(f, assertion)
  }

  "Events" should "return collection of JSON events" in {
    val f: Client[IO] => IO[PodmanResponse[List[Json]]] = client => System.events(client)
    val assertion: IO[PodmanResponse[List[Json]]] => IO[Assertion] =
      response =>
        response.asserting(_.response should not be empty) >>
          response.asserting(_.response.get should not be empty)

    check(f, assertion)
  }

  def check[A](
    request: Client[IO] => IO[PodmanResponse[A]],
    assertion: IO[PodmanResponse[A]] => IO[Assertion],
  ): IO[Assertion] = podmanClient.use { client =>
    val response = request(client)
    assertion(response)
  }

}
