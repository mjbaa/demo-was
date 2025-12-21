package http;

import config.ConfigLoader;
import config.HostConfig;
import config.ServerConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class StaticFileHandlerTest {

    @Test
    public void rootPathReturnsIndexHtml() {
        ServerConfig config = ConfigLoader.load();
        HostConfig host = config.getHostConfig("localhost");

        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/",
                "localhost"
        );

        HttpResponse response = StaticFileHandler.handle(request, host);

        assertEquals(200, response.getStatusCode());
        assertTrue(new String(response.getBody()).contains("index"));
    }

    @Test
    public void missingFileReturns404() {
        ServerConfig config = ConfigLoader.load();
        HostConfig host = config.getHostConfig("localhost");

        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/not-exist.html",
                "localhost"
        );

        HttpResponse response = StaticFileHandler.handle(request, host);

        assertEquals(404, response.getStatusCode());
    }
}
