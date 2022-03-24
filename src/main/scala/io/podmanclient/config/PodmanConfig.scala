package io.podmanclient.config

trait PodmanUri {
  def uri(): String
}

sealed case class UnixSocketPodmanUri(val uri: String) extends PodmanUri
sealed case class TcpPodmanUri(val uri: String)        extends PodmanUri

final case class PodmanConfig(uri: PodmanUri)
