package config;

import java.util.Map;

//서버 전체 설정 : port, host 설정들
public class ServerConfig {
    private int port;
    private String defaultServletPackage;
    private Map<String, HostConfig> hosts;

    public int getPort() {
        return port;
    }

    public String getDefaultServletPackage() {
        return defaultServletPackage;
    }

    public Map<String, HostConfig> getHosts() {
        return hosts;
    }

    public HostConfig getHostConfig(String host){
        return getHosts().get(host);
    }
}
