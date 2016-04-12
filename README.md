# disconsul
Service discovery client library in scala that uses Consul.

## Installation

### SBT
Add this to your libraryDependencies:
`"com.ilunin" %% "disconsul" % "1.0"`

## Features
The following features are supported:
- [Service Discovery](#service-discovery)
- *Java Support (Not Implemented yet)*
- *Load Balancing (Not Implemented yet)*
- *Consul Resilience (Not Implemented yet)*
- *Option to bypass Consul (Not Implemented yet)*
- *Service Resilience (Not Implemented yet)*
- *Play Support (Not Implemented yet)*
- *Ning AsyncHttpClient Support (Not Implemented yet)*
- *Json4s Support (Not Implemented yet)*
- *Spray support (Not Implemented yet)*
- [Custom HTTP Client and JSON parser](#custom-http-client-and-json-parser)

### Service Discovery
The goal of disconsul is to provide you with the host and port of the service you want to connect to by connecting to Consul.

The first step is to create the `Disconsul` instance that you will use to discover the service host and port. 
 
To avoid dependency hell, disconsul does not force you to use a specific HTTP client to connect to Consul and a specific JSON parser to parse its response.
So you will need to provide an instance of `HttpClient` and an instance of `JsonParser`.
You can [create your own implementation](#custom-http-client-and-json-parser) using your preferred HTTP client and JSON parser or use one of the provided implementations ([Play](#play-support), [Ning AsyncHttpClient](#ning-asynchttp-client-support), [Json4s](#json4s-support), [Spray](#spray-support))

When you have an instance of `HttpClient` and `JsonParser`, simply create the `Disconsul` instance by passing these objects and the name of the service you want to discover:
`val disconsul = new Disconsul(httpClient, jsonParser, "myService")`

You can then call the `discover` method by passing a `Option[Service] => Future[T]` function that will take a `Service` if one exists and return the result of your call to the discovered service.
This function needs to return a `Future` as most calls to external services are IO intensive and should be asynchronous. If you use something synchronous to call your service (as a JDBC driver for example), just wrap it in a `Future {...}` block.
Example:
```
val externalServiceClient = ... // Could be a HTTP client or anything that call your external service
val result: Future[String] = disconsul.discover { service: Service =>
    val externalServiceResult: Future[Srtring] = externalServiceClient.call(host = service.host, port = service.port)
    externalServiceResult
}
```
 
Disconsul will populate the `host` field of the `Service` with the `serviceAddress` returned by Consul if it is not empty or the `address` if it is.

### Custom HTTP Client and JSON parser
The `HTTPClient` trait has just one method to implement which is `def get(path: String): Future[HttpResponse]` where `HttpResponse` is a case class containing the response status and its body (defined by `case class HttpResponse(status: Int, body: String)`)
`HTTPCLient` also provide the Consul host and port that is by default `localhost` and `8500`. You can override these vals if your Consul installation differ.

The `JsonParser` trait has just one method to implement which is `def parse(json: String): Seq[ConsulService]` where `ConsulService` is a case class containing the host and port of a service (defined by `case class ConsulService(address: String, serviceAddress: String, port: Int)`)
The json passed to the parse method is the result of the `/v1/health/service/<service>` endpoint in Consul.
Each object in array returned by Consul should correspond to one element in the `Seq` returned by the parse method. Disconsul will only pass the healthy instances to the `parse` method.
The `address` field should contain the `Address` attribute of the `Node` object, the `serviceAddress` field should contain the `Address` attribute of the `Service` object and the `port` field should contain the `Port` attribute of the `Service` object.

Here is an example of the response sent by Consul:
```
[
  {
    "Node": {
      "Node": "foobar",
      "Address": "10.1.10.12"
    },
    "Service": {
      "ID": "redis",
      "Service": "redis",
      "Tags": null,
      "Address": "10.1.10.13",
      "Port": 8000
    },
    "Checks": [
      {
        "Node": "foobar",
        "CheckID": "service:redis",
        "Name": "Service 'redis' check",
        "Status": "passing",
        "Notes": "",
        "Output": "",
        "ServiceID": "redis",
        "ServiceName": "redis"
      },
      {
        "Node": "foobar",
        "CheckID": "serfHealth",
        "Name": "Serf Health Status",
        "Status": "passing",
        "Notes": "",
        "Output": "",
        "ServiceID": "",
        "ServiceName": ""
      }
    ]
  }
]
```
With this example, `parse` should return a sequence of one `ConsulService` with `10.1.10.12` for `address`, `10.1.10.13` for `serviceAddress` and `8000` for `port`.

