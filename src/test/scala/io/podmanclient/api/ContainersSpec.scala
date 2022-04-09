package io.podmanclient.api

import cats._
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import io.circe.syntax._
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.server.service.ContainersServiceResponse
import io.podmanclient.server.service.SystemService
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration._
import io.podmanclient.api.response.PodmanErrors._
import io.circe.Json

class ContainersSpec extends PodmanClientTest {

  "List" should "return JSON of running containers" in {
    assert(
      Containers.list(clientPrefix)(client),
      ContainersServiceResponse
        .runningContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of all containers when parameter 'all=true'" in {
    assert(
      Containers.list(clientPrefix, all = true)(client),
      ContainersServiceResponse
        .allContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of running containers when filter by 'status=running'" in {
    assert(
      Containers.list(clientPrefix, filters = Map("status" -> List("running")))(client),
      ContainersServiceResponse
        .runningContainers
        .map(_.asJson.asRight),
    )
  }

  it should "return JSON of exited containers when filter by 'status=exited'" in {
    assert(
      Containers.list(clientPrefix, filters = Map("status" -> List("exited")))(client),
      ContainersServiceResponse
        .exitedContainers
        .map(_.asJson.asRight),
    )
  }

  "Create" should "return JSON with id of created container" in {
    assert(
      Containers.create(clientPrefix, "docker.io/postgres:latest", name = Some("postgres"))(client),
      ContainersServiceResponse
        .createdContainer
        .map(_.asRight),
    )
  }

  it should "return PodmanException with error message when container already exists" in {
    assert(
      Containers.create(clientPrefix, "docker.io/postgres:latest", name = Some("double"))(client),
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
    assert(Containers.start(clientPrefix, "postgres")(client), ().asRight.pure[IO])
  }

  it should "return unit if container is already running" in {
    assert(Containers.start(clientPrefix, "kibana")(client), ().asRight.pure[IO])
  }

  it should "return NoSuchContainer if container if container does not exist" in {
    assert(
      Containers.start(clientPrefix, "mysql")(client),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Stop" should "return unit when container is stopped" in {
    assert(
      Containers.stop(clientPrefix, "postgres")(client),
      ().asRight.pure[IO],
    )
  }

  it should "return unit when container is already stopped" in {
    assert(
      Containers.stop(clientPrefix, "postgres")(client),
      ().asRight.pure[IO],
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      Containers.stop(clientPrefix, "mysql")(client),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Delete" should "return unit when container is deleted" in {
    assert(
      Containers.delete(clientPrefix, "postgres")(client),
      ().asRight.pure[IO],
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      Containers.delete(clientPrefix, "mysql")(client),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Inspect" should "return JSON of inspected container" in {
    assert(
      Containers.inspect(clientPrefix, "postgres")(client),
      ContainersServiceResponse
        .inspectedContainer
        .map(_.asRight),
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      Containers.inspect(clientPrefix, "mysql")(client),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

  "Logs" should "return container logs as string" in {
    assert(
      Containers.logs(clientPrefix, "postgres")(client),
      ContainersServiceResponse.containerLogs.map(log => List.fill(2)(log).mkString.asRight),
    )
  }

  it should "return NoSuchContainer if container does not exist" in {
    assert(
      Containers.logs(clientPrefix, "mysql")(client),
      NoSuchContainer("mysql").asLeft.pure[IO],
    )
  }

}
