package io.github.antolius.scg.examples.routing;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Comparator;

/**
 * Provides an implementation of Route Comparator such that for a
 * pair of routes {@code R1} and {@code R2} corresponding to path patterns {@code P1} and {@code P2}:
 * <p>
 *     {@code R1 <= R2 if and only if P2 matches P1}
 * <p>
 *     where matching of path patterns is defined in
 *     {@link PathPattern#matches(org.springframework.http.server.PathContainer)}.
 * <p>
 *     In practice this means that if a collection of routes is sorted
 *     using this comparator the first element will be the most exact.
 * <p>
 *     In order to be handled properly by this comparator routes should
 *     put the {@code String} representation of their path pattern into the
 *     route metadata map under the {@link PathBasedRouteComparator#PATH} key.
 */
@Component
public class PathBasedRouteComparator implements Comparator<Route> {

    public static final String PATH = PathBasedRouteComparator.class.getCanonicalName();

    private final PathPatternParser pathPatternParser;

    public PathBasedRouteComparator(PathPatternParser pathPatternParser) {
        this.pathPatternParser = pathPatternParser;
    }

    @Override
    public int compare(Route r1, Route r2) {
        var path1 = (String) r1.getMetadata().get(PATH);
        var path2 = (String) r2.getMetadata().get(PATH);
        if (path1 == null || path2 == null) {
            return 0;
        }

        var pattern1 = pathPatternParser.parse(path1);
        var pattern2 = pathPatternParser.parse(path2);

        var m1 = pattern1.matches(PathContainer.parsePath(path2));
        var m2 = pattern2.matches(PathContainer.parsePath(path1));

        if (m1 && !m2) {
            return 1;
        }

        if (m2 && !m1) {
            return -1;
        }

        return 0;
    }

}
