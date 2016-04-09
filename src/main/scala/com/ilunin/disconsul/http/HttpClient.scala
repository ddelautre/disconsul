package com.ilunin.disconsul.http

import scala.concurrent.Future

trait HttpClient {

  val host = "localhost"
  val port = 8500

  def get(path: String): Future[HttpResponse]

}
