package io.podmanclient.api.uri

import org.http4s.Uri

object PodmanUri {

  private val uriPrefix = "http://d/v3.0.0/libpod/"

  val infoUri   = Uri.unsafeFromString(s"${uriPrefix}info")
  val pingUri   = Uri.unsafeFromString(s"${uriPrefix}_ping")
  val eventsUri = Uri.unsafeFromString(s"${uriPrefix}events")
  val diskUri   = Uri.unsafeFromString(s"${uriPrefix}system/df")

  val listContainers = Uri.unsafeFromString(s"${uriPrefix}containers/json")

}
