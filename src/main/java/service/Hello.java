package service;

import http.HttpRequest;
import http.HttpResponse;
import servlet.SimpleServlet;

public class Hello implements SimpleServlet {
    @Override
    public void service(HttpRequest req, HttpResponse res) throws Exception {
        res.getWriter().write("Hello from service.Hello");
    }
}
