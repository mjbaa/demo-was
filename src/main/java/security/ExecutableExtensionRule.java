package security;

import config.HostConfig;
import http.ErrorResponseBuilder;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutableExtensionRule extends AbstractSecurityRule{
    private static final Logger log = LoggerFactory.getLogger(ExecutableExtensionRule.class);

    @Override
    protected HttpResponse doCheck(HttpRequest request, HostConfig hostConfig) {
        if(request.getPath().toLowerCase().endsWith(".exe")) {
            log.warn("executable file request: {}", request.getPath());
            return ErrorResponseBuilder.build(403, hostConfig);
        }
        
        return null; //통과
    }
}
