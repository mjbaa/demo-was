package http;

import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

public class HttpRequestTest {

    @Test
    public void queryParameter_정상_병합된다() {
        // given
        Map<String, String> headers = Map.of(
                "host", "localhost"
        );

        Map<String, String> queryParams = Map.of(
                "name", "jun",
                "age", "20"
        );

        HttpRequest request = new HttpRequest(
                "GET",
                "/Hello",
                "HTTP/1.1",
                headers,
                queryParams,
                null
        );

        // then
        assertEquals("jun", request.getParameter("name"));
        assertEquals("20", request.getParameter("age"));
    }

    @Test
    public void 없는_파라미터는_빈문자열_반환() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/Hello",
                "HTTP/1.1",
                null,
                null,
                null
        );

        assertEquals("", request.getParameter("missing"));
    }

    @Test
    public void 헤더는_대소문자_무시된다() {
        Map<String, String> headers = new HashMap<>();
        headers.put("host", "localhost");

        HttpRequest request = new HttpRequest(
                "GET",
                "/",
                "HTTP/1.1",
                headers,
                null,
                null
        );

        assertEquals("localhost", request.getHeader("HOST"));
        assertEquals("localhost", request.getHeader("host"));
    }

    @Test
    public void addParameter_정상_추가된다() {
        HttpRequest request = new HttpRequest(
                "GET",
                "/Hello",
                "HTTP/1.1",
                null,
                null,
                null
        );

        request.addParameter("name", "jun");

        assertEquals("jun", request.getParameter("name"));
    }
}