package io.podmanclient.service

import cats._
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import io.circe.syntax._
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.server.service.ContainersServiceResponse
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration._
import io.podmanclient.error.PodmanErrors._
import io.circe.Json
import io.podmanclient.server.service.ImagesServiceResponse

class ImagesServiceSpec extends PodmanClientTest {

  "List" should "return JSON of running containers" in {
    assert(
      imagesService.list(),
      ImagesServiceResponse
        .listImages
        .map(_.asJson.asRight),
    )
  }

  "Inspect" should "return JSON of a specific image" in {
    assert(
      imagesService.inspect("postgres_image"),
      ImagesServiceResponse
        .postgresImage
        .map(_.asJson.asRight),
    )
  }

  it should "return NoSuchImage if an image does not exist" in {
    assert(
      imagesService.inspect("mysql"),
      NoSuchImage("mysql").asLeft.pure[IO],
    )
  }

}
