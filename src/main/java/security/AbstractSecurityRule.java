package security;

import config.HostConfig;
import http.HttpRequest;
import http.HttpResponse;

//체인 공통 처리
public abstract class AbstractSecurityRule implements SecurityRule{
    private SecurityRule next;

    @Override
    public void setNext(SecurityRule next) {
        this.next = next;
    }

    @Override
    public HttpResponse check(HttpRequest request, HostConfig hostConfig) {
        HttpResponse response = doCheck(request, hostConfig);
        if(response != null){
            return response;
        }

        if(next != null){
            return next.check(request, hostConfig);
        }
        return null;
    }

    protected abstract HttpResponse doCheck(HttpRequest request, HostConfig hostConfig);

}
