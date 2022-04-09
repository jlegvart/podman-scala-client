package io.podmanclient.error

import io.circe.Json

object PodmanErrors {

  sealed trait PodmanError extends Throwable {

    def msg: String

  }

  final case class NoSuchContainer(name: String) extends PodmanError {

    def msg = s"Error: no container with name '$name' found"

    override def toString(): String = msg

  }

  final case class PodmanException(status: Int, response: String) extends PodmanError {

    def msg = s"Podman client error: status = $status, response = $response"

    override def toString(): String = msg

  }

}
