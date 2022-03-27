package io

import org.http4s.Uri

package object podmanclient {

  def asUri(prefix: String, uri: String): Uri = Uri.unsafeFromString(s"${prefix}${uri}")

}
