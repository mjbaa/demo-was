package http;

import config.ConfigLoader;
import config.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class HostRoutingTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void bComhostIndexShouldLoadBComHtml() throws IOException {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "b.com"
        );

        HttpResponse response = dispatcher.dispatch(request);
        byte[] expected = Files.readAllBytes(
                Path.of("webroot/b.com/index.html")
        );
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());

    }

    @Test
    public void aComIndexShouldLoadAComHtml() throws IOException {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "a.com"
        );

        HttpResponse response = dispatcher.dispatch(request);
        byte[] expected = Files.readAllBytes(
                Path.of("webroot/a.com/index.html")
        );
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    public void aComNotFoundShouldReturnACom404Page() throws IOException {
        // given
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/not-exist.html",
                "a.com"
        );

        // when
        HttpResponse response = dispatcher.dispatch(request);

        // then
        byte[] expected = Files.readAllBytes(
                Path.of("webroot/a.com/404.html")
        );

        assertEquals(404, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    public void bComNotFoundShouldReturnBCom404Page() throws IOException {
        // given
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/not-exist.html",
                "b.com"
        );

        // when
        HttpResponse response = dispatcher.dispatch(request);

        // then
        byte[] expected = Files.readAllBytes(
                Path.of("webroot/b.com/404.html")
        );

        assertEquals(404, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }


}
