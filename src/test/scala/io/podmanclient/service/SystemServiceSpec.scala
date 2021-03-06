package io.podmanclient.service

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.config.UnixSocketPodmanUri
import io.podmanclient.server.service.SystemServiceResponse
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.dsl.io._
import org.http4s.implicits._

class SystemTest extends PodmanClientTest {

  "Info" should "return podman info JSON" in {
    assert(
      systemService.info,
      SystemServiceResponse
        .infoResponseSuccess
        .map(resp => resp.asRight),
    )
  }

  "Ping" should "return successful response without content" in {
    assert(systemService.ping, true.asRight.pure[IO])
  }

  "df" should "return JSON with disk usage info" in {
    assert(
      systemService.df,
      SystemServiceResponse
        .dfResponseSuccess
        .map(_.asRight),
    )
  }

  "Events" should "return collection of JSON events" in {
    val jsonList =
      for {
        jsonList <- SystemServiceResponse.eventsResponseSuccess
        a        <- jsonList.split("\n").map(str => decode[Json](str).liftTo[IO]).toList.sequence
      } yield a

    assert(
      systemService.events,
      jsonList.map(_.asRight),
    )
  }

}
