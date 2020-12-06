package io.github.antolius.scg.examples.routing;

import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;

@Primary
@Service
public class MultiRouteHandlerMapping extends RoutePredicateHandlerMapping {

    private final RouteLocator routeLocator;
    private final PathBasedRouteComparator pathBasedRouteComparator;

    public MultiRouteHandlerMapping(
            FilteringWebHandler webHandler,
            RouteLocator routeLocator,
            GlobalCorsProperties globalCorsProperties,
            Environment environment,
            PathBasedRouteComparator pathBasedRouteComparator
    ) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        this.routeLocator = routeLocator;
        this.pathBasedRouteComparator = pathBasedRouteComparator;
    }


    /**
     * Returns Route that best matches given exchange, taking into account
     * all matching routes.
     * <p>
     * Implementation is taken from the parent class' {@code lookupRoute}
     * method with one key difference: this version sorts all matching paths
     * and only then takes the most exact one.
     *
     * @param exchange  related to a single HTTP request
     * @return          mono with Route that best matches given exchange
     * @see             PathBasedRouteComparator
     */
    @Override
    protected Mono<Route> lookupRoute(ServerWebExchange exchange) {
        return this.routeLocator.getRoutes()
                .concatMap(route -> Mono.just(route).filterWhen(r -> {
                    exchange.getAttributes().put(GATEWAY_PREDICATE_ROUTE_ATTR, r.getId());
                    return r.getPredicate().apply(exchange);
                })
                        .doOnError(e -> logger.error("Error applying predicate for route: " + route.getId(), e))
                        .onErrorResume(e -> Mono.empty()))
                .sort(pathBasedRouteComparator)
                .next()
                .map(route -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Route matched: " + route.getId());
                    }
                    validateRoute(route, exchange);
                    return route;
                });
    }


}
