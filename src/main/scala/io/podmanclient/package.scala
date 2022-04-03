package io

import org.http4s.Uri
import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.Response
import io.podmanclient.api.response.PodmanResult
import io.podmanclient.api.response.ResultFailure
import io.podmanclient.api.response.ResponseBody

package object podmanclient {

  def orError[F[_]: Concurrent](response: Response[F]): F[PodmanResult] = response
    .as[String]
    .map(body => ResultFailure(response.status.code, ResponseBody(Some(body))))

}
