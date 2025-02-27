package lila.app
package http

import play.api.mvc.RequestHeader

import lila.common.HTTPRequest

object ResponseHeaders:

  def headersForApiOrApp(req: RequestHeader) =
    val appOrigin = HTTPRequest.appOrigin(req)
    List(
      "Access-Control-Allow-Origin"  -> appOrigin.getOrElse("*"),
      "Access-Control-Allow-Methods" -> allowMethods,
      "Access-Control-Allow-Headers" -> {
        List(
          "Origin",
          "Authorization",
          "If-Modified-Since",
          "Cache-Control",
          "Content-Type"
        ) ::: appOrigin.isDefined.so(List("X-Requested-With", "sessionId"))
      }.mkString(", "),
      "Vary" -> "Origin"
    ) ::: appOrigin.isDefined.so(
      List(
        "Access-Control-Allow-Credentials" -> "true"
      )
    )

  val allowMethods = List("OPTIONS", "GET", "POST", "PUT", "DELETE") mkString ", "
