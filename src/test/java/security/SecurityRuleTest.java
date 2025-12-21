package security;

import config.ConfigLoader;
import config.HostConfig;
import config.ServerConfig;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SecurityRuleTest {

    private SecurityRule securityChain;
    private HostConfig hostConfig;

    @Before
    public void setUp() {
        SecurityRule dt = new DirectoryTraversalRule();
        SecurityRule exe = new ExecutableExtensionRule();
        dt.setNext(exe);
        securityChain = dt;

        ServerConfig config = ConfigLoader.load();
        hostConfig = config.getHostConfig("localhost");
    }

    @Test
    public void directoryTraversalIsForbidden() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/../../etc/passwd",
                "localhost"
        );

        HttpResponse response = securityChain.check(request, hostConfig);

        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void exeFileIsForbidden() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/hack.exe",
                "localhost"
        );

        HttpResponse response = securityChain.check(request, hostConfig);

        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void EXEFileIsForbidden() {
        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/hack.EXE",
                "localhost"
        );

        HttpResponse response = securityChain.check(request, hostConfig);

        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
    }
}
