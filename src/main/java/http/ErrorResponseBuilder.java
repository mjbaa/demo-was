package http;

import config.HostConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// HostConfig 기반 에러 응답 생성
public class ErrorResponseBuilder {

    public static HttpResponse build(int statusCode, HostConfig hostConfig){
        //hostconfig 없는 경우
        if(hostConfig == null){
            return defaultError(statusCode);
        }

        String errorPage = hostConfig.getErrorPage(String.valueOf(statusCode));
        if(errorPage == null){
            return defaultError(statusCode);
        }

        try {
            Path webRoot = Path.of(hostConfig.getHttpRoot())
                    .toAbsolutePath()
                    .normalize();

            Path errorFilePath = webRoot
                    .resolve(errorPage)
                    .normalize();

            if(!errorFilePath.startsWith(webRoot)){
                return defaultError(statusCode);
            }

            if(Files.exists(errorFilePath) && !Files.isDirectory(errorFilePath)){
                byte[] body = Files.readAllBytes(errorFilePath);
                HttpResponse response = new HttpResponse(statusCode, reasonPhrase(statusCode),body);
                response.addHeader("Content-Type", "text/html; charset=utf-8");
                return response;
            }

        } catch(IOException e){

        }

        return defaultError(statusCode);
    }

    //공통 fallback 에러 응답
    private static HttpResponse defaultError(int statusCode){
        return HttpResponse.text(
                statusCode,
                reasonPhrase(statusCode),
                statusCode + " " + reasonPhrase(statusCode)
        );
    }

    private static String reasonPhrase(int statusCode){
        return switch(statusCode){
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }
}
