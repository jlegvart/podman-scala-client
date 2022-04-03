package io.podmanclient.api

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
import io.podmanclient.server.service.SystemService
import io.podmanclient.server.service.SystemServiceResponse
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.podmanclient.api.response.PodmanResult
import io.podmanclient.api.response.ResultSuccess
import io.podmanclient.api.response.ResponseBody
import io.podmanclient.api.response.ResponseEmpty

class SystemTest extends PodmanClientTest {

  "Info" should "return podman info JSON" in {
    assert(
      System.info(clientPrefix, client),
      SystemServiceResponse
        .infoResponseSuccess
        .map(resp => ResultSuccess(200, ResponseBody(Some(resp)))),
    )
  }

  "Ping" should "return successful response without content" in {
    assert(System.ping(clientPrefix, client), ResultSuccess(200, ResponseEmpty).pure[IO])
  }

  "df" should "return JSON with disk usage info" in {
    assert(
      System.df(clientPrefix, client),
      SystemServiceResponse
        .dfResponseSuccess
        .map(json => ResultSuccess(200, ResponseBody(Some(json)))),
    )
  }

  "Events" should "return collection of JSON events" in {
    val jsonList =
      for {
        jsonList <- SystemServiceResponse.eventsResponseSuccess
        a        <- jsonList.split("\n").map(str => decode[Json](str).liftTo[IO]).toList.sequence
      } yield a

    assert(
      System.events(clientPrefix, client),
      jsonList.map(col => ResultSuccess(200, ResponseBody(Some(col)))),
    )
  }

}
