package http;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Socket InputStream을 HttpRequest 객체로 파싱
public class HttpParser {
    public static HttpRequest parse(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        );

        // Request Line 읽기 예) GET /hello?name=jun HTTP/1.1
        String requestLine = br.readLine();
        if(requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] requestParts = requestLine.split(" ");
        if(requestParts.length != 3) {
            throw new IOException("Invalid request line");
        }

        String method = requestParts[0];
        String fullPath = requestParts[1]; // Ex) /hello?name=jun
        String version = requestParts[2];
        
        //Path, Query String 분리
        String path = fullPath;
        Map<String, String> queryParams = new HashMap<>();

        //? 가 있으면 Query String 존재
        int questionIndex = fullPath.indexOf("?");
        if(questionIndex != -1) {
            //? 제외하고 path - queryString 분리
            path = fullPath.substring(0, questionIndex);
            String queryString = fullPath.substring(questionIndex+1);
            parseQueryParams(queryString, queryParams);
        }

        //header 파싱
        Map<String, String> headers = new HashMap<>();
        String line;
        //header : 빈 줄로 끝남
        while((line = br.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if(colonIndex == -1) continue;

            String headerName = line.substring(0, colonIndex).trim().toLowerCase(); // 헤더는 대소문자 구분 X -> 서버는 소문자로 통일
            String headerValue = line.substring(colonIndex+1).trim();
            headers.put(headerName, headerValue);
        }


        byte[] body = new byte[0];
        String contentLengthValue = headers.get("content-length");
        if(contentLengthValue != null){
            int contentLength = Integer.parseInt(contentLengthValue);
            char[] bodyChars = new char[contentLength];
            int read = br.read(bodyChars);
            body = new String(bodyChars, 0, read).getBytes(StandardCharsets.UTF_8);
        }
        
        //문자열 덩어리 -> 요청 객체
        return new HttpRequest(
                method,
                path,
                version,
                headers,
                queryParams,
                body
        );

    }

    private static void parseQueryParams(String queryString, Map<String, String> queryParams){
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                queryParams.put(pair, "");
            } else {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                queryParams.put(key, value);
            }
        }
    }
}
