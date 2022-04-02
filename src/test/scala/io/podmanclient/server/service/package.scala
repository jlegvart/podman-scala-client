package io.podmanclient.server

import cats.effect._
import cats.syntax.all._
import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.io.Source
import org.typelevel.log4cats.Logger

package object service {

  def asJsonUnsafe(str: String): IO[Json] = decode[Json](str).liftTo[IO]

  def loadResponse(fileName: String): IO[String] = Resource
    .make(IO.blocking(Source.fromResource(s"response/${fileName}")))(s => IO.blocking(s.close))
    .use { source =>
      source.mkString.pure[IO]
    }

  def log(s: => String)(implicit logger: Logger[IO]): IO[Unit] = logger.debug(s)

}
