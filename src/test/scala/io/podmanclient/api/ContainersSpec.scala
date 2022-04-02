package io.podmanclient.api

import cats._
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all._
import io.circe.syntax._
import io.podmanclient.api.response.ResponseSuccess
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.server.service.ContainersServiceResponse
import io.podmanclient.server.service.SystemService
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration._

class ContainersSpec extends PodmanClientTest {

  "List endpoint" should "return JSON of running containers" in {
    assert(
      Containers.list(clientPrefix)(client),
      ContainersServiceResponse.runningContainers.map(resp => ResponseSuccess(Some(resp.asJson))),
    )
  }

  it should "return JSON of all containers when parameter 'all=true'" in {
    assert(
      Containers.list(clientPrefix, all = true)(client),
      ContainersServiceResponse.allContainers.map(resp => ResponseSuccess(Some(resp.asJson))),
    )
  }

  it should "return JSON of running containers when filter by 'status=running'" in {
    assert(
      Containers.list(clientPrefix, filters = Map("status" -> List("running")))(client),
      ContainersServiceResponse.runningContainers.map(resp => ResponseSuccess(Some(resp.asJson))),
    )
  }

  it should "return JSON of exited containers when filter by 'status=exited'" in {
    assert(
      Containers.list(clientPrefix, filters = Map("status" -> List("exited")))(client),
      ContainersServiceResponse.exitedContainers.map(resp => ResponseSuccess(Some(resp.asJson))),
    )
  }

  "Create endpoint" should "return JSON with id of created container" in {
    assert(
      Containers.create(clientPrefix, "docker.io/postgres:latest")(client),
      ContainersServiceResponse
        .createdContainerSuccess
        .map((resp => ResponseSuccess(Some(resp)))),
    )
  }

  ignore should "return no content" in {
    assert(
      Containers.start(clientPrefix, "postgres")(client),
      ContainersServiceResponse
        .createdContainerSuccess
        .map((resp => ResponseSuccess(Some(resp)))),
    )
  }

}
