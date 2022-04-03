package io.podmanclient.api.response

import io.circe.Json

sealed trait PodmanResult extends Product with Serializable {

  def status: Int
  def response: PodmanResponse

}

final case class ResultSuccess(status: Int, response: PodmanResponse) extends PodmanResult
final case class ResultFailure(status: Int, response: PodmanResponse) extends PodmanResult
final case class ResultInfo(status: Int, response: PodmanResponse)    extends PodmanResult

sealed trait PodmanResponse extends Product with Serializable

final case class ResponseBody[A](body: Option[A]) extends PodmanResponse
final case object ResponseEmpty                   extends PodmanResponse
