package security;

import config.HostConfig;
import http.ErrorResponseBuilder;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class DirectoryTraversalRule extends AbstractSecurityRule{
    private static final Logger log = LoggerFactory.getLogger(DirectoryTraversalRule.class);

    @Override
    protected HttpResponse doCheck(HttpRequest request, HostConfig hostConfig) {
        Path webRoot = Path.of(hostConfig.getHttpRoot())
                .toAbsolutePath()
                .normalize();

        Path targetPath = webRoot
                .resolve(request.getPath().equals("/")
                        ? "index.html"
                        : request.getPath().substring(1))
                .normalize();

        if(!targetPath.startsWith(webRoot)) {
            log.warn("Directory traversal attempt: {}", request.getPath());
            return ErrorResponseBuilder.build(403, hostConfig);
        }
        
        return null; // 통과
    }
}
