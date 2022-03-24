package io.podmanclient.api.response

import io.circe.Json

trait PodmanResponse[A] extends Product with Serializable {
  def response: Option[A]
}

sealed case class ResponseSuccess[A](response: Option[A])     extends PodmanResponse[A]
sealed case class ResponseError[Json](response: Option[Json]) extends PodmanResponse[Json]
