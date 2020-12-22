package io.github.antolius.scg.examples.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.route.Route;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

public class PathBasedRouteComparatorTest {

    private final PathBasedRouteComparator comparator = new PathBasedRouteComparator();

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("/1/**", 0, "/1/bars", 0, 1),
                Arguments.of("/1/bars", 0, "/1/**", 0, -1),
                Arguments.of("/1/{path-var}", 0, "/1/bars", 0, 1),
                Arguments.of("/1/bars", 0, "/1/{path-var}", 0, -1),
                Arguments.of("/1/bars", 0, "/1/foos", 0, 0),
                Arguments.of("/1/bars", 1, "/1/foos", 0, 1),
                Arguments.of("/1/bars", 0, "/1/foos", 1, -1),
                Arguments.of("/1/bars", 0, "/1/bars", 0, 0),
                Arguments.of("/1/bars", 1, "/1/bars", 0, 1),
                Arguments.of("/1/bars", 0, "/1/bars", 1, -1),
                Arguments.of(null, 0, null, 0, 0),
                Arguments.of(null, 0, null, 1, -1),
                Arguments.of(null, 1, null, 0, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void shouldCompareCorrectly(
            String givenFirstPath,
            Integer givenFirstOrder,
            String givenSecondPath,
            Integer givenSecondOrder,
            int expectedSignum
    ) {
        // given
        var givenFirstRoute = givenRouteForPath(givenFirstPath, givenFirstOrder);
        var givenSecondRoute = givenRouteForPath(givenSecondPath, givenSecondOrder);

        // when
        var actual = comparator.compare(givenFirstRoute, givenSecondRoute);

        // then
        then(Integer.signum(actual)).isEqualTo(expectedSignum);
    }

    @Test
    void shouldSortRoutesCorrectly() {
        // given
        var givenRoutes = List.of(
                givenRouteForPath("/1/**", 0),
                givenRouteForPath("/1/bars/{id}", 0),
                givenRouteForPath("/1/bars/special", 1),
                givenRouteForPath("/1/bars/unique", 0)
        );

        // when
        var actual = Flux.fromIterable(givenRoutes)
                .sort(comparator)
                .toIterable();

        // then
        then(actual).extracting(Route::getId)
                .containsExactly(
                        "/1/bars/unique",
                        "/1/bars/special",
                        "/1/bars/{id}",
                        "/1/**"
                );
    }

    private Route givenRouteForPath(String givenPath, Integer givenOrder) {
        var mockRoute = Mockito.mock(Route.class);
        given(mockRoute.getId()).willReturn(givenPath);
        given(mockRoute.getOrder()).willReturn(givenOrder);
        if (givenPath != null) {
            given(mockRoute.getMetadata()).willReturn(Map.of(
                    PathInfo.KEY, PathInfo.from(givenPath)
            ));
        }
        return mockRoute;
    }

}
