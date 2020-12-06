# Spring Cloud Gateway Routing Example

An example project that illustrates how to tweak [Spring Cloud Gateway's](https://spring.io/projects/spring-cloud-gateway) default routing.

## Use-case

Out of the box Spring Cloud Gateway comes with rather flexible but ultimately simple routing. It goes like this:

* [`RoutePredicateHandlerMapping` class](https://github.com/spring-cloud/spring-cloud-gateway/blob/v3.0.0-M6/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/RoutePredicateHandlerMapping.java) will go over all registered routes in order.
* Each route has a single (asynchronous) predicate that gets a chance to match the incoming exchange object.
* First route to match is validated (out of the box validation does nothing) and picked as the route to use for the given request.

This is powerful because predicate can be entirely overridden with custom logic. However, the "first match" strategy puts a lot of burden on the ordering of routes. Consider 2 routes with following path patterns:

1. `/1/*`
1. `/1/bars`

Given HTTP request to path `/1/bars`, should it be handled by route 1 or route 2?

With a sizeable API this issue becomes more and more pronounced. There are 2 contributing factors:

1. Routes might be dynamically added at runtime, making it impossible to assert the correct order in advanced.
1. With large enough API surface it becomes increasingly likely that some otherwise generic endpoints will need to be overridden and handled as special cases. For example one service might be capable of handling `/{v}/bars` in general, but `/2/bars` is a special case that should be handheld by a dedicated micro-service.

## Solution

One solution is to extend the [`RoutePredicateHandlerMapping` class](https://github.com/spring-cloud/spring-cloud-gateway/blob/v3.0.0-M6/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/RoutePredicateHandlerMapping.java). Then method for finding the matching route can be overridden in a way that takes the multiple overlapping patterns into account. You can find one such implementation in this project.

This solution has a couple of characteristics:

1. It does not require the knowledge of all available routes ahead of time.
1. It incurs no performance penalty in case an incoming HTTP request matches only one route.
1. It does require more runtime processing in case there are multiple routes that match the request. This gets progressively more taxing as the number of matched routes increases.

### Alternative

Points 1. and 3. could be swapped in case routes are known ahead of time and runtime performance is more important. In this alternative solution all routes can be compared at startup time. More exact routes can be assigned higher priority order. With such ordering the default `RoutePredicateHandlerMapping` is sufficient as the first match will always be the more exact one.

## Getting started

In case you want to run the app yourself, feel free to clone the git repo and play around with it.

### Prerequisites

This project uses:

* [Java 15](https://openjdk.java.net/projects/jdk/15/)
* [Maven](https://maven.apache.org/)

### Installing

Like with any maven project, you can:

```shell script
$ mvn clean install
```

## Testing

Tests use  [WireMock](http://wiremock.org/) to simulate the downstream server and [`WebTestClient`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/reactive/server/WebTestClient.html) to simulate the client. Test boots up the app and then makes HTTP requests, which are proxied to the WireMock server.

You can run test with maven:

```shell script
$ mvn test
```

## Author

* [Josip Antoliš](https://github.com/Antolius)