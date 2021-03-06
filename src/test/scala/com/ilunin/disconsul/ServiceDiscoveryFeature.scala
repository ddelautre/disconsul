package com.ilunin.disconsul

import com.ilunin.disconsul.http.{HttpResponse, HttpClient}
import com.ilunin.disconsul.json.{ConsulService, JsonParser}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.concurrent.Future

class ServiceDiscoveryFeature extends FeatureSpec with GivenWhenThen with Matchers with TypeCheckedTripleEquals with ScalaFutures {

  val emptyJson = "[]"

  feature("Service Discovery") {

    scenario("Consul Discovery client should give address and port returned by consul to the passed function") {
      Given("a Consul server that returns 127.0.0.1 and 9999 for the address and port of the myService service")
      val consul = new Consul(Some(ConsulService("127.0.0.1", "", 9999)))

      When("Consul discovery is called for the service myService")
      val service = discover("myService", consul)

      Then("the service host should be 127.0.0.1")
      service.get.host should === ("127.0.0.1")

      And("the service port should be 9999")
      service.get.port should === (9999)
    }

    scenario("Consul Discovery client should give service address and port returned by consul to the passed function") {
      Given("a Consul server that returns 127.0.0.2 and 9999 for the service address and port of the myService service")
      val consul = new Consul(Some(ConsulService("127.0.0.1", "127.0.0.2", 9999)))

      When("Consul discovery is called for the service myService")
      val service = discover("myService", consul)

      Then("the service host should be 127.0.0.2")
      service.get.host should === ("127.0.0.2")

      And("the service port should be 9999")
      service.get.port should === (9999)
    }

    scenario("Consul Discovery client should give None to the passed function if Consul return nothing") {
      Given("a Consul server that returns an empty json for the myService service")
      val consul = new Consul(None)

      When("Consul discovery is called for the service myService")
      val service = discover("myService", consul)

      Then("no service should be returned")
      service should === (None)
    }

    class Consul(service: Option[ConsulService]) {

      private val consulJson = service.map { service =>
        s"""
           |[
           | {
           |   "Node": {
           |     "Address": "${service.address}"
           |   },
           |   "Service": {
           |     "Port": ${service.port},
           |     "Address:" ${service.serviceAddress}
           |   }
           | }
           |]
        """.stripMargin
      }.getOrElse("[]")


      val consulClient = new HttpClient {
        override def get(path: String): Future[HttpResponse] = {
          if (path == "/v1/health/service/myService?passing") {
            Future.successful(HttpResponse(200, consulJson))
          } else {
            Future.failed(new RuntimeException(s"Bad path: $path is not equal to '/v1/health/service/myService?passing'"))
          }
        }
      }

      val jsonParser = new JsonParser {
        override def parse(json: String): Seq[ConsulService] = if (json == consulJson) service.toSeq else Seq.empty
      }

    }

    def discover(serviceName: String, consul: Consul): Option[Service] = {
      val consulDiscovery = new Disconsul(consul.consulClient, consul.jsonParser, "myService")
      consulDiscovery.discover { service: Option[Service] =>
        Future.successful(service)
      }.futureValue
    }

  }

}
