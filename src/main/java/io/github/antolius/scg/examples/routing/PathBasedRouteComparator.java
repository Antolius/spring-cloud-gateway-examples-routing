package io.github.antolius.scg.examples.routing;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.stereotype.Component;

import java.util.Comparator;

/**
 * Provides an implementation of Route Comparator that can be used
 * to sort Routes from most to least exact. Route R1 with path p1
 * is considered more exact than route R2 with path p2 if path
 * pattern based on p1 matches p2. For example {@code /1/**} matches
 * {@code /1/foo}, so route corresponding to {@code /1/foo} is
 * more exact. In case paths are incomparable, either because both
 * match each other or neither does, this comparator falls back to
 * route order.
 * <p>
 * In order to be handled properly by this comparator routes should
 * have {@link PathInfo} representation of their path in their
 * metadata map under the {@link PathInfo#KEY} key.
 */
@Component
public class PathBasedRouteComparator implements Comparator<Route> {

    @Override
    public int compare(Route r1, Route r2) {
        var info1 = (PathInfo) r1.getMetadata().get(PathInfo.KEY);
        var info2 = (PathInfo) r2.getMetadata().get(PathInfo.KEY);
        if (info1 == null || info2 == null) {
            return compareOrders(r1, r2);
        }

        var r1MatchesR2 = info1.getPattern().matches(info2.getPath());
        var r2MatchesR1 = info2.getPattern().matches(info1.getPath());

        if (r1MatchesR2 && !r2MatchesR1) {
            return 1;
        }

        if (r2MatchesR1 && !r1MatchesR2) {
            return -1;
        }

        return compareOrders(r1, r2);
    }

    private int compareOrders(Route r1, Route r2) {
        return Integer.compare(r1.getOrder(), r2.getOrder());
    }

}
