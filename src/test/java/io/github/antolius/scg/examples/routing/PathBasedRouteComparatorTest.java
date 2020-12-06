package io.github.antolius.scg.examples.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.antolius.scg.examples.routing.PathBasedRouteComparator.PATH;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

public class PathBasedRouteComparatorTest {

    private final PathBasedRouteComparator comparator = new PathBasedRouteComparator(
            new PathPatternParser()
    );

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("/1/**", "/1/bars", 1),
                Arguments.of("/1/bars", "/1/**", -1),
                Arguments.of("/1/bars", "/1/foos", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void shouldCompareCorrectly(
            String givenFirstPath,
            String givenSecondPath,
            int expectedSignum
    ) {
        // given
        var givenFirstRoute = givenRouteForPath(givenFirstPath);
        var givenSecondRoute = givenRouteForPath(givenSecondPath);

        // when
        var actual = comparator.compare(givenFirstRoute, givenSecondRoute);

        // then
        then(Integer.signum(actual)).isEqualTo(expectedSignum);
    }

    @Test
    void shouldSortRoutesCorrectly() {
        // given
        var givenRoutes = List.of(
                givenRouteForPath("/1/**"),
                givenRouteForPath("/1/bars/{id}"),
                givenRouteForPath("/1/bars/special")
        );

        // when
        var actual = Flux.fromIterable(givenRoutes)
                .sort(comparator)
                .toIterable();

        // then
        then(actual).extracting(Route::getId)
                .containsExactly(
                        "/1/bars/special",
                        "/1/bars/{id}",
                        "/1/**"
                );
    }

    private Route givenRouteForPath(String givenPath) {
        var mockRoute = Mockito.mock(Route.class);
        given(mockRoute.getId()).willReturn(givenPath);
        given(mockRoute.getMetadata()).willReturn(Map.of(PATH, givenPath));
        return mockRoute;
    }

}
