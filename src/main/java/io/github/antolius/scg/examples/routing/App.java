package io.github.antolius.scg.examples.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public RouteLocator routeLocator(
            RouteLocatorBuilder builder,
            @Value("${backend.url}") String backendUrl
    ) {
        return builder.routes()
                .route(r -> r.path("/1/**")
                        .filters(f -> f.setRequestHeader("X-Route", "something-else"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/**"))
                        .uri(backendUrl)
                ).route(r -> r.path("/1/bars")
                        .filters(f -> f.setRequestHeader("X-Route", "bars"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/bars"))
                        .uri(backendUrl)
                ).route(r -> r.path("/1/bars/{id}")
                        .filters(f -> f.setRequestHeader("X-Route", "one-bar"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/bars/{id}"))
                        .uri(backendUrl)
                ).route(r -> r.path("/1/bars/{id}/foo")
                        .filters(f -> f.setRequestHeader("X-Route", "foo"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/bars/{id}/foo"))
                        .uri(backendUrl)
                ).route(r -> r.path("/1/bars/special")
                        .filters(f -> f.setRequestHeader("X-Route", "special-bar"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/bars/special"))
                        .uri(backendUrl)
                ).route(r -> r.path("/1/bars/special/foo")
                        .filters(f -> f.setRequestHeader("X-Route", "special-foo"))
                        .metadata(PathInfo.KEY, PathInfo.from("/1/bars/special/foo"))
                        .uri(backendUrl)
                ).build();
    }

}
