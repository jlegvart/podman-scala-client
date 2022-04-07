package io.podmanclient.api.response

import io.circe.Json

object PodmanErrors {
  sealed trait PodmanError                                        extends Throwable
  final case class PodmanException(status: Int, response: String) extends PodmanError
}
