package servlet;

import config.ConfigLoader;
import http.Dispatcher;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class TimeServletTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void timeServletReturnsCurrentTime() {
        LocalDateTime before = LocalDateTime.now();

        HttpRequest request = HttpRequest.testRequest(
                "GET",
                "/Time",
                "localhost"
        );



        HttpResponse response = dispatcher.dispatch(request);

        LocalDateTime after = LocalDateTime.now();


        assertEquals(200, response.getStatusCode());


        String body = new String(response.getBody(), StandardCharsets.UTF_8);
        assertTrue(body.startsWith("현재 시각: "));

        String timePart = body.replace("현재 시각: ", "").trim();

        LocalDateTime responseTime = LocalDateTime.parse(
                timePart,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        // 2. 현재 시각 범위 검증 (before ~ after)
        assertFalse(responseTime.isBefore(before.minusSeconds(1)));
        assertFalse(responseTime.isAfter(after.plusSeconds(1)));
    }
}
