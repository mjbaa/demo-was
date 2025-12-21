package servlet;

import config.ConfigLoader;
import http.Dispatcher;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class HelloServletTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void helloServletShouldPrintName() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/Hello",
                "localhost",
                Map.of("name", "jun")
        );

        HttpResponse response = dispatcher.dispatch(request);
        String body = new String(response.getBody());

        assertEquals(200, response.getStatusCode());
        assertTrue(body.contains("jun"));
    }
}
