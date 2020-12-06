package io.github.antolius.scg.examples.routing;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureWireMock(port = 0)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"backend.url=http://localhost:${wiremock.server.port}"}
)
public class RoutingTest {

    @Autowired
    private WebTestClient client;

    private static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("/1", "something-else"),
                Arguments.of("/1/", "something-else"),
                Arguments.of("/1/bars", "bars"),
                Arguments.of("/1/bars/", "bars"),
                Arguments.of("/1/bars/special", "special-bar"),
                Arguments.of("/1/bars/1", "one-bar"),
                Arguments.of("/1/bars/2", "one-bar"),
                Arguments.of("/1/bars/2/foo", "foo"),
                Arguments.of("/1/bars/3/foo", "foo"),
                Arguments.of("/1/bars/special/foo", "special-foo"),
                Arguments.of("/1/baz", "something-else"),
                Arguments.of("/1/foo/baz", "something-else")
        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    void shouldRouteCorrectly(String givenPath, String expectedRoute) {
        // given
        stubFor(get(urlEqualTo(givenPath)).willReturn(ok()));

        // when
        var actualRes = client.get().uri(givenPath).exchange();

        // then
        actualRes.expectStatus().isOk();
        verify(getRequestedFor(urlEqualTo(givenPath))
                .withHeader("X-Route", equalTo(expectedRoute))
        );
    }
}
