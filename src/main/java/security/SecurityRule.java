package security;

import config.HostConfig;
import http.HttpRequest;
import http.HttpResponse;

public interface SecurityRule {

    void setNext(SecurityRule next);

    HttpResponse check(HttpRequest request, HostConfig hostConfig);
}
