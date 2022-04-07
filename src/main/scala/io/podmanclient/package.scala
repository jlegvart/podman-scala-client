package io

import org.http4s.Uri
import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.Response
import io.podmanclient.api.response.PodmanErrors._

package object podmanclient {

  def orError[F[_]: Concurrent, A](response: Response[F]): F[Either[PodmanError, A]] = response
    .as[String]
    .map(body => PodmanException(response.status.code, body).asLeft)

}
