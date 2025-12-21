package servlet;

import http.HttpRequest;
import http.HttpResponse;

public interface SimpleServlet {
    void service(HttpRequest request, HttpResponse response) throws Exception;
}
