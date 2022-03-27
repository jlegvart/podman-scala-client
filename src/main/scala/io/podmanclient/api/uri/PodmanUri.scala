package io.podmanclient.api.uri

import org.http4s.Uri

object PodmanUri {

  val infoUri   = "info"
  val pingUri   = "_ping"
  val dfUri     = "system/df"
  val eventsUri = "events"

  val listContainersUri = "containers/json"

}
