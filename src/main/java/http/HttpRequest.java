package http;

import java.util.*;

public class HttpRequest {
    //필드가 전부 final인 이유 : 요청은 한 번 들어오면 절대 바뀌지 않는다 -> 읽기 전용 (서버 내부에서 수정 방지 )
    
    //Request Line 관련 필드  :http 요청의 첫 줄 ex) GET /hello HTTP/1.1
    private final String method;
    private final String path;
    private final String version;

    //headers
    private final Map<String,String> headers;

    //query String
    private final Map<String, String> queryParams;

    //servlet 용
    private final Map<String, String> parameters;


    //Body
    private final byte[] body;

    public HttpRequest(String method,
                       String path,
                       String version,
                       Map<String,String> headers,
                       Map<String, String> queryParams,
                       byte[] body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers != null ? headers : new HashMap<>();
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
        this.body = body != null ? body : new byte[0];

        this.parameters = new HashMap<>();
        mergeQueryParams();
    }

    private void mergeQueryParams() {
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase()); // http 헤더 : 대소문자 구분 X -> 소문자로 통일
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getParameter(String name) {
        String value = parameters.get(name);
        return value != null ? value : "";
    }
    public Map<String, String> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public byte[] getBody() {
        return body;
    }
    
    //테스트 전용 팩토리 메서드
    public static HttpRequest testRequest(String method, String path, String host) {
        Map<String, String> headers = new HashMap<>();
        headers.put("host", host);

        return new HttpRequest(
                method,
                path,
                "HTTP/1.1",
                headers,
                new HashMap<>(),
                null
        );
    }
    public static HttpRequest testRequest(
            String method,
            String path,
            String host,
            Map<String, String> params
    ) {
        Map<String, String> headers = new HashMap<>();
        headers.put("host", host);

        return new HttpRequest(
                method,
                path,
                "HTTP/1.1",
                headers,
                params,
                new byte[0]
        );
    }


}
