package http;

import config.HostConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler {


    public static HttpResponse handle(HttpRequest request, HostConfig hostConfig) {
        try {
            String requestPath = request.getPath();

            //"/" 요청은 "/index.html"로 매핑 ( 기본 문서 매핑 )
            if(requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            //HostConfig 기반 webRoot
            Path webRoot = Path.of(hostConfig.getHttpRoot()).toAbsolutePath().normalize();
            
            //실제 파일 경로
            Path targetPath = webRoot
                    //-> 문자열 결합과 달리 OS별 경로 구분자 자동 처리, 절대경로/상대경로 의미 유지
                    .resolve(requestPath.substring(1)) // "/index.html" → "index.html"
                    .normalize();

            //파일 존재, 디렉터리 여부 확인
            if(!Files.exists(targetPath) || Files.isDirectory(targetPath)) {
                return ErrorResponseBuilder.build(404, hostConfig);
            }

            //파일 읽기
            byte[] body = Files.readAllBytes(targetPath);
            HttpResponse response = new HttpResponse(200, "OK", body);

            //Content-Type 결정
            response.addHeader("Content-Type", guessContentType(targetPath));
            return response;

        }catch (IOException e) {
            return ErrorResponseBuilder.build(500, hostConfig);
        }
    }
    
    //확장자 기반 추론
    private static String guessContentType(Path path) {
        String fileName = path.toString();

        if (fileName.endsWith(".html")) return "text/html; charset=utf-8";
        if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";

        return "application/octet-stream";
    }

}
