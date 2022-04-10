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

class ContainersSpec extends PodmanClientTest {

  "List" should "return JSON of running containers" in {
    assert(
      containersService.list(),
      ContainersServiceResponse
        .runningContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of all containers when parameter 'all=true'" in {
    assert(
      containersService.list(all = true),
      ContainersServiceResponse
        .allContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of running containers when filter by 'status=running'" in {
    assert(
      containersService.list(filters = Map("status" -> List("running"))),
      ContainersServiceResponse
        .runningContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of exited containers when filter by 'status=exited'" in {
    assert(
      containersService.list(filters = Map("status" -> List("exited"))),
      ContainersServiceResponse
        .exitedContainers
        .map(_.asJson.asRight),
    )
  }

  "Create" should "return JSON with id of created container" in {
    assert(
      containersService.create("docker.io/postgres:latest", name = Some("postgres")),
      ContainersServiceResponse
        .createdContainer
        .map(_.asRight),
    )
  }

  it should "return PodmanException with error message when container already exists" in {
    assert(
      containersService.create("docker.io/postgres:latest", name = Some("double")),
      PodmanException(
        500,
        Json
          .obj(
            "cause"    -> "that name is already in use".asJson,
            "message"  -> "error creating container storage".asJson,
            "response" -> 500.asJson,
          )
          .noSpaces
          .toString(),
      ).asLeft.pure[IO],
    )
  }

  "Start" should "return unit when container is started" in {
    assert(containersService.start("postgres"), ().asRight.pure[IO])
  }

  it should "return unit if container is already running" in {
    assert(containersService.start("kibana"), ().asRight.pure[IO])
  }

  it should "return NoSuchContainer if container if container does not exist" in {
    assert(
      containersService.start("mysql"),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Stop" should "return unit when container is stopped" in {
    assert(
      containersService.stop("postgres"),
      ().asRight.pure[IO],
    )
  }

  it should "return unit when container is already stopped" in {
    assert(
      containersService.stop("postgres"),
      ().asRight.pure[IO],
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      containersService.stop("mysql"),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Delete" should "return unit when container is deleted" in {
    assert(
      containersService.delete("postgres"),
      ().asRight.pure[IO],
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      containersService.delete("mysql"),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Inspect" should "return JSON of inspected container" in {
    assert(
      containersService.inspect("postgres"),
      ContainersServiceResponse
        .inspectedContainer
        .map(_.asRight),
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      containersService.inspect("mysql"),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Logs" should "return container logs as string" in {
    assert(
      containersService.logs("postgres"),
      ContainersServiceResponse.containerLogs.map(log => List.fill(2)(log).mkString.asRight),
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      containersService.logs("mysql"),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

}
