package http;

import config.HostConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


// HostConfig 기반 에러 응답 생성
public class ErrorResponseBuilder {
    private static final Logger log =
            LoggerFactory.getLogger(ErrorResponseBuilder.class);

    public static HttpResponse build(int statusCode, HostConfig hostConfig){
        //hostconfig 없는 경우
        if(hostConfig == null){
            return defaultError(statusCode);
        }

        String errorPage = hostConfig.getErrorPage(String.valueOf(statusCode));
        if(errorPage == null){
            return defaultError(statusCode);
        }

        String resourcePath =
                hostConfig.getHttpRoot() + "/" + errorPage;

        try (InputStream is = ErrorResponseBuilder.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (is == null) {
                log.warn("Error page not found: {}", resourcePath);
                return defaultError(statusCode);
            }

            byte[] body = is.readAllBytes();
            HttpResponse response =
                    new HttpResponse(statusCode, reasonPhrase(statusCode), body);
            response.addHeader("Content-Type", "text/html; charset=utf-8");
            return response;

        } catch (Exception e){
            log.error("Failed to load error page: {}", resourcePath, e);
            return defaultError(statusCode);
        }

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
