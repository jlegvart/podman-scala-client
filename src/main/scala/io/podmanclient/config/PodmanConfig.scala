package io.podmanclient.config

trait PodmanClientUri {
  def uri(): String
}

sealed case class UnixSocketPodmanUri(val uri: String) extends PodmanClientUri
sealed case class TcpPodmanUri(val uri: String)        extends PodmanClientUri

final case class PodmanConfig(uri: PodmanClientUri)
