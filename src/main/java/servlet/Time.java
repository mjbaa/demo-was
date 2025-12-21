package servlet;

import http.HttpRequest;
import http.HttpResponse;

import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Time implements SimpleServlet {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        Writer writer = response.getWriter();

        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        writer.write("현재 시각: " + formattedTime);
    }
}