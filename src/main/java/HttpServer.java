import config.ConfigLoader;
import config.ServerConfig;
import http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) throws Exception {

        ServerConfig serverConfig = ConfigLoader.load();

        try(ServerSocket serverSocket = new ServerSocket(serverConfig.getPort())) {
            log.info("WAS started on port {}", serverConfig.getPort());

            Dispatcher dispatcher = new Dispatcher(serverConfig);

            while(true) {
                Socket client = serverSocket.accept();
                log.info("Client connected: {}", client.getInetAddress());

                try (
                        InputStream in = client.getInputStream();
                        OutputStream out = client.getOutputStream();
                ) {
                    HttpRequest request = HttpParser.parse(in);
                    HttpResponse response = dispatcher.dispatch(request);
                    HttpResponseWriter.write(out, response);
                }catch(Exception e) {
                    //요청 처리 중 예외 -> 연결만 종료
                    log.error("Error",e);
                }finally{
                    client.close();
                    log.info("Client closed");
                }
            }
        }catch(Exception e) {
            //서버 시작 실패
            log.error("Failed to start WAS", e);
        }

    }
}