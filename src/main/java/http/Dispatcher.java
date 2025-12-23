package http;

import config.HostConfig;
import config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import security.DirectoryTraversalRule;
import security.ExecutableExtensionRule;
import security.SecurityRule;
import servlet.SimpleServlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
    private final Map<String, SimpleServlet> servletCache = new ConcurrentHashMap<>();

    private final ServerConfig serverConfig;
    private final SecurityRule securityChain;

    public Dispatcher(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;

        //SecurityRule 체인 구성
        SecurityRule RuleDT = new DirectoryTraversalRule();
        SecurityRule RuleEX = new ExecutableExtensionRule();
        RuleDT.setNext(RuleEX);
        this.securityChain = RuleDT;
    }

    public HttpResponse dispatch(HttpRequest request){
        log.info("Request: {} {}", request.getMethod(), request.getPath());

        //host 헤더 해석
        String hostHeader = request.getHeader("host");
        if(hostHeader == null){
            log.warn("Missing Host header");
            return HttpResponse.text(400, "Bad Request", "400 Bad Request");
        }

        String host = hostHeader.split(":")[0];
        if(!serverConfig.hasHostConfig(host)){
            log.warn("Unknown host: {}", hostHeader);
            return ErrorResponseBuilder.build(403,null);
        }
        HostConfig hostConfig = serverConfig.getHostConfig(host);//hostConfig 선택



        HttpResponse securityResult = securityChain.check(request, hostConfig);
        if(securityResult != null){
            return securityResult;
        }

        if (request.getPath().equals("/")) {
            return StaticFileHandler.handle(request, hostConfig);
        }

        try{
            String className = resolveClassName(request.getPath());
            SimpleServlet servlet = loadServlet(className);

            HttpResponse response = new HttpResponse(200, "OK", new byte[0]);
            response.addHeader("Content-Type", "text/html; charset=utf-8");

            servlet.service(request,response);
            return response;
        } catch (Exception e){
            //정적 파일 처리
            if (e.getCause() instanceof ClassNotFoundException) {
                return StaticFileHandler.handle(request, hostConfig);
            }
            log.error("Internal server error", e);
            return ErrorResponseBuilder.build(500,hostConfig);
        }

    }
    
    //url을 클래스명으로 변환
    private String resolveClassName(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (!path.contains(".")) {
            return serverConfig.getDefaultServletPackage() + "." + path;
        }

        return path.replace("/", "."); // 자바 패키지 : '/' 이 아닌 '.' 사용
    }
    
    //클래스명으로 servlet 반환
    private SimpleServlet loadServlet(String className) throws Exception {
        return servletCache.computeIfAbsent(className, name -> {
            try {
                Class<?> temp = Class.forName(name);

                if (!SimpleServlet.class.isAssignableFrom(temp)) {
                    throw new IllegalArgumentException();
                }

                return (SimpleServlet) temp.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
