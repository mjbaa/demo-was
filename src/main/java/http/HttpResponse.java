package http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {
    //HTTP 응답의 첫 줄 ex) HTTP/1.1 200 OK
    private int statusCode;
    private String reasonPhrase;

    //헤더 순서 유지 필요 -> LinkedHashMap
    private final Map<String, String> headers = new LinkedHashMap<String, String>();

    private final ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
    private Writer writer;

    //servlet : 동적 응답용
    public HttpResponse(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    //정적 파일, 에러 페이지용
    public HttpResponse(int statusCode, String reasonPhrase, byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        bodyStream.writeBytes(body);
    }

    public Writer getWriter() {
        if(writer == null) {
            writer = new OutputStreamWriter(bodyStream, StandardCharsets.UTF_8);
        }
        return writer;
    }
    
    //was 내부 사용 메서드
    public byte[] getBody() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (Exception e) {}
        byte[] body = bodyStream.toByteArray();
        headers.putIfAbsent("Content-Length", String.valueOf(body.length));
        return bodyStream.toByteArray();
    }
    
    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    //Content-Type, Connection, Server 등을 나중에 추가 가능
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }


    //범용 factory 메서드
    public static HttpResponse text(int statusCode, String reasonPhrase, String body) {
        HttpResponse response = new HttpResponse(
                statusCode,
                reasonPhrase,
                body.getBytes(StandardCharsets.UTF_8)
        );
        response.addHeader("Content-Type", "text/plain; charset=utf-8");
        return response;
    }

    public static HttpResponse html(int statusCode, String reasonPhrase, byte[] body) {
        HttpResponse response = new HttpResponse(
                statusCode,
                reasonPhrase,
                body
        );
        response.addHeader("Content-Type", "text/html; charset=utf-8");
        return response;
    }


}
