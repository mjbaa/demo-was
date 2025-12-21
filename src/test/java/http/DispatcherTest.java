package http;

import config.ConfigLoader;
import config.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DispatcherTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        ServerConfig config = ConfigLoader.load();
        dispatcher = new Dispatcher(config);
    }

    @Test
    public void helloServletShouldReturn200() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/Hello",
                "localhost"
        );

        HttpResponse response = dispatcher.dispatch(request);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void unknownServletFallsBackToStatic404() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/nope",
                "localhost"
        );

        HttpResponse response = dispatcher.dispatch(request);

        assertEquals(404, response.getStatusCode());
    }
}
