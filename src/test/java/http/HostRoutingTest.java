package http;

import config.ConfigLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class HostRoutingTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void bComhostIndexShouldLoadBComHtml() throws Exception {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "b.com"
        );

        HttpResponse response = dispatcher.dispatch(request);

        byte[] expected = loadResource("webroot/b.com/index.html");

        assertEquals(200, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    public void aComIndexShouldLoadAComHtml() throws Exception {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "a.com"
        );

        HttpResponse response = dispatcher.dispatch(request);

        byte[] expected = loadResource("webroot/a.com/index.html");

        assertEquals(200, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    public void aComNotFoundShouldReturnACom404Page() throws Exception {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/not-exist.html",
                "a.com"
        );

        HttpResponse response = dispatcher.dispatch(request);

        byte[] expected = loadResource("webroot/a.com/404.html");

        assertEquals(404, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    @Test
    public void bComNotFoundShouldReturnBCom404Page() throws Exception {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/not-exist.html",
                "b.com"
        );

        HttpResponse response = dispatcher.dispatch(request);

        byte[] expected = loadResource("webroot/b.com/404.html");

        assertEquals(404, response.getStatusCode());
        assertArrayEquals(expected, response.getBody());
    }

    private byte[] loadResource(String path) throws Exception {
        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(path);

        assertNotNull("Test resource not found: " + path, is);
        return is.readAllBytes();
    }
}
