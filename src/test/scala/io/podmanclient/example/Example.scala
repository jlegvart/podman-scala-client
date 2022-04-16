package io.podmanclient.example

import cats.effect._
import cats._
import cats.syntax.all._
import cats.effect.IOApp
import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.podmanclient.client.PodmanClient
import io.podmanclient.config.PodmanConfig
import io.podmanclient.uri.PodmanUri
import io.podmanclient.config.TcpPodmanUri
import io.podmanclient.error.PodmanErrors.PodmanException
import io.podmanclient.service.ContainersService
import io.podmanclient.service.SystemService
import io.podmanclient.service.ImagesService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.podmanclient.config.UnixSocketPodmanUri

object Example extends IOApp.Simple {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] = {
    // val base      = Uri(path = Root / "v3.0.0" / "libpod")
    val base = Uri.unsafeFromString("http://d/v3.0.0/libpod/")

    // val tcpClient = getTcpClient
    val client = socketClient

    client.use { client =>
      val containersService = ContainersService(base, client)
      val systemService     = SystemService(base, client)
      val imagesService     = ImagesService(base, client)

      for {
        ping <- systemService.ping.rethrow
        _    <- logger.debug(s"Service available: $ping")
        info <- systemService.info.rethrow
        _    <- logger.debug(s"System info: $info")

        _       <- logger.debug("Preparing container services")
        running <- containersService.list(filters = Map("status" -> List("running"))).rethrow
        _       <- logger.debug(s"Currently running containers: $running")

        _ <- logger.debug("Creating new container: postgres")
        _ <- containersService.create("docker.io/postgres:latest", "postgres-example".some).rethrow

        _       <- logger.debug("Inspecting container: postgres")
        inspect <- containersService.inspect("postgres-example").rethrow
        _       <- logger.debug(s"Container info: $inspect")

        _ <- logger.debug("Starting container: postgres")
        _ <- containersService.start("postgres-example")

        running <- containersService.list(filters = Map("status" -> List("running"))).rethrow
        _       <- logger.debug(s"Currently running containers: $running")

        _ <- logger.debug("Stopping container: postgres-example")
        _ <- containersService.stop("postgres-example").rethrow
        _ <- logger.debug("Container stopped: postgres-example")

        _ <- logger.debug("Delete container: postgres-example")
        _ <- containersService.delete("postgres-example").rethrow
        _ <- logger.debug("Container deleted: postgres-example")
      } yield ()
    }
  }

  def socketClient = {
    val config = PodmanConfig(UnixSocketPodmanUri("/run/user/1000/podman/podman.sock"))
    PodmanClient.createClient[IO](config)
  }

  def getTcpClient = {
    val config = PodmanConfig(TcpPodmanUri("http://localhost:8888"))
    PodmanClient.createClient[IO](config)
  }

}
