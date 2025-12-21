package config;

import java.util.Map;

//특정 호스트에 대한 정책 : 어떤 루트를 기준으로 파일을 찾는지, 에러 시 어떤 페이지 내보낼지
public class HostConfig {
    private String httpRoot;
    private Map<String, String> errorPages;

    public HostConfig() {}

    public String getHttpRoot() {
        return httpRoot;
    }

    public Map<String, String> getErrorPages() {
        return errorPages;
    }

    public String getErrorPage(String statusCode) {
        if (errorPages == null) return null;
        return errorPages.get(statusCode);
    }
}
