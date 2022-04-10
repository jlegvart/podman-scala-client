package io.podmanclient.uri

import org.http4s.Uri

object PodmanUri {

  def infoUri(base: Uri)        = base / "info"
  def pingUri(base: Uri)        = base / "_ping"
  def eventsUri(base: Uri)      = base / "events"
  def createImageUri(base: Uri) = base / "build"
  def dfUri(base: Uri)          = systemUri(base) / "df"
  def systemUri(base: Uri)      = base / "system"

  def listContainersUri(base: Uri)                 = containersUri(base) / "json"
  def createContainerUri(base: Uri)                = containersUri(base) / "create"
  def startContainerUri(base: Uri, name: String)   = containersUri(base) / name / "start"
  def stopContainerUri(base: Uri, name: String)    = containersUri(base) / name / "stop"
  def deleteContainerUri(base: Uri, name: String)  = containersUri(base) / name
  def inspectContainerUri(base: Uri, name: String) = containersUri(base) / name / "json"
  def logsContainerUri(base: Uri, name: String)    = containersUri(base) / name / "logs"
  def containersUri(base: Uri)                     = base / "containers"

  def listImagesUri(base: Uri)                 = imagesUri(base) / "json"
  def inspectImageUri(base: Uri, name: String) = imagesUri(base) / name / "json"
  def imagesUri(base: Uri)                     = base / "images"
}
