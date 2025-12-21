package servlet;

import http.HttpRequest;
import http.HttpResponse;

import java.io.Writer;

/*
/hello로 요청 오면 동적 응답(Java코드로 HTML 생성)
 */
public class Hello implements SimpleServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception{
        Writer writer = response.getWriter();
        writer.write("Hello, ");
        writer.write(request.getParameter("name"));
    }
}
