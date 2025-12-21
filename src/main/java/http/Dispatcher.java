package http;

import config.HostConfig;
import config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import security.DirectoryTraversalRule;
import security.ExecutableExtensionRule;
import security.SecurityRule;
import servlet.SimpleServlet;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
    private final Map<String, SimpleServlet> servletCache = new HashMap<>();

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
        HostConfig hostConfig = serverConfig.getHostConfig(host);//hostConfig 선택
        if(hostConfig == null){
            log.warn("Unknown host: {}", hostHeader);
            return ErrorResponseBuilder.build(403,null);
        }

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
        } catch (ClassNotFoundException e){
            //servlet 없음 -> 정적 파일 핸들러로 넘김
        } catch (Exception e){
            log.error("Internal server error", e);
            return ErrorResponseBuilder.build(500,hostConfig);
        }

        //정적 파일 처리
        return StaticFileHandler.handle(request,hostConfig);
        

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

        //캐시에 있으면 재사용
        if(servletCache.containsKey(className)){
            return servletCache.get(className);
        }

        //클래스 로딩 ( 문자열 -> Class 객체 )
        //클래스 없을 때 -> Class Not Found Exception 발생
        Class<?> temp = Class.forName(className);

        //클래스가 SimpleServlet인지 검증
        if(!SimpleServlet.class.isAssignableFrom(temp)){
            throw new IllegalArgumentException();
        }
        
        //객체 생성 : Class -> 새로운 인스턴스
        SimpleServlet servlet = (SimpleServlet) temp.getDeclaredConstructor().newInstance();

        servletCache.put(className, servlet);
        return servlet;
    }

}
