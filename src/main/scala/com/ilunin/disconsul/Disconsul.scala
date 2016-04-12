package com.ilunin.disconsul

import com.ilunin.disconsul.http.{HttpResponse, HttpClient}
import com.ilunin.disconsul.json.{ConsulService, JsonParser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Disconsul(httpClient: HttpClient, jsonParser: JsonParser, serviceName: String) {

  def discover[T](block: Option[Service] => Future[T]): Future[T] = {
    val consulResponse: Future[HttpResponse] = httpClient.get(s"/v1/health/service/$serviceName?passing")
    val services: Future[Seq[Service]] = consulResponse.map {
      case HttpResponse(200, body) => jsonParser.parse(body).map { consulService: ConsulService =>
        val serviceAddress = if (!consulService.serviceAddress.isEmpty) consulService.serviceAddress else consulService.address
        Service(serviceAddress, consulService.port)
      }

    }
    services.flatMap { healthyServices =>
      block(healthyServices.headOption)
    }
  }

}

