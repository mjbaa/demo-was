package http;

import config.ConfigLoader;
import config.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HostRoutingTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void localhostIndexShouldLoadLocalhostHtml() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "localhost"
        );

        HttpResponse response = dispatcher.dispatch(request);

        assertEquals(200, response.getStatusCode());
        assertTrue(new String(response.getBody()).contains("localhost"));
    }

    @Test
    public void aComIndexShouldLoadAComHtml() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/index.html",
                "a.com"
        );

        HttpResponse response = dispatcher.dispatch(request);

        assertEquals(200, response.getStatusCode());
        assertTrue(new String(response.getBody()).contains("a.com"));
    }
}
