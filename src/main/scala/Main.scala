import cats.effect.IO
import cats.effect.IOApp
import cats.effect._
import cats.effect.syntax.all._
import fs2.io.net.unixsocket.UnixSocketAddress
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.{PodmanConfig, UnixSocketPodmanUri}
import org.http4s.MediaType
import org.http4s.Method._
import org.http4s._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.client.middleware.UnixSocket
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers._
import org.http4s.implicits._

import org.http4s.circe._
import io.circe.Json
import io.podmanclient.api

import org.http4s._
import org.http4s.ember.client._
import org.http4s.client.oauth1
import org.http4s.client.oauth1.ProtocolParameter._
import org.http4s.implicits._
import cats.effect._
import fs2.Stream
import fs2.io.stdout
import fs2.text.{lines, utf8Encode}
import io.circe.Json
import org.typelevel.jawn.fs2._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object Main extends IOApp.Simple {

  implicit def unsafeLogger = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] = {
    val config = PodmanConfig(
      new UnixSocketPodmanUri("/run/user/1000/podman/podman.sock")
    )

    IO.unit
  }

}
