package http;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//HttpResponse 객체를 문자열(bytes)로 변환하여 Socket의 OutputStream으로 써주는 역할
public class HttpResponseWriter {
    public static void write(OutputStream out, HttpResponse response) throws IOException {
        //status line
        //ex)"HTTP/1.1 200 OK\r\n"
        String statusLine =
                "HTTP/1.1 "+
                        response.getStatusCode()+" "+
                        response.getReasonPhrase() +
                        // Http : CRLF를 줄바꿈으로 사용
                        /*
                        \r = carriage return
                        \n = line feed
                         */
                        "\r\n";


        out.write(statusLine.getBytes(StandardCharsets.UTF_8));

        //headers ->  Key: Value\r\n 한 줄씩 출력
        for(Map.Entry<String, String> header : response.getHeaders().entrySet()){
            String headerLine =
                    header.getKey()+": "+header.getValue()+ "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.US_ASCII));
        }

        //http 규칙 : header - body 사이 빈 줄
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));

        //body
        out.write(response.getBody());

        //버퍼에 쌓인 데이터를 즉시 네트워크로 전송
        out.flush();
    }


}
