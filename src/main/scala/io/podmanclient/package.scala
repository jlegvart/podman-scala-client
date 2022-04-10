package io

import cats.effect.Concurrent
import cats.syntax.all._
import io.podmanclient.error.PodmanErrors._
import org.http4s.Response
import org.http4s.Uri

package object podmanclient {

    def orError[F[_]: Concurrent, A](response: Response[F]): F[Either[PodmanError, A]] = response
    .as[String]
    .map(body => PodmanException(response.status.code, body).asLeft)

}
