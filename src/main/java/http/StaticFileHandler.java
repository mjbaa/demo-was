package http;

import config.HostConfig;

import java.io.InputStream;


public class StaticFileHandler {


    public static HttpResponse handle(HttpRequest request, HostConfig hostConfig) {
        try {
            String requestPath = request.getPath();

            //"/" 요청은 "/index.html"로 매핑 ( 기본 문서 매핑 )
            if(requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            String resourcePath = hostConfig.getHttpRoot() + requestPath;

            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            InputStream is =
                    StaticFileHandler.class
                            .getClassLoader()
                            .getResourceAsStream(resourcePath);

            if (is == null) {
                return ErrorResponseBuilder.build(404, hostConfig);
            }

            byte[] body = is.readAllBytes();

            HttpResponse response = new HttpResponse(200, "OK", body);
            response.addHeader("Content-Type", guessContentType(resourcePath));
            return response;

        }catch (Exception e) {
            return ErrorResponseBuilder.build(500, hostConfig);
        }
    }
    
    //확장자 기반 추론
    private static String guessContentType(String path) {

        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";

        return "application/octet-stream";
    }

}
