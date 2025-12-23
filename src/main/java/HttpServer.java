import config.ConfigLoader;
import config.ServerConfig;
import http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);


    public static void main(String[] args) throws Exception {

        ServerConfig serverConfig = ConfigLoader.load();

        ExecutorService threadPool =
                Executors.newFixedThreadPool(serverConfig.getThreadPoolSize());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutdown");
            threadPool.shutdown();
            try{
                if(!threadPool.awaitTermination(10, TimeUnit.SECONDS)){
                    threadPool.shutdownNow();
                }
            }catch(InterruptedException e){
                threadPool.shutdownNow();
            }
        }));


        try(ServerSocket serverSocket = new ServerSocket(serverConfig.getPort())) {
            log.info("WAS started on port {}", serverConfig.getPort());

            Dispatcher dispatcher = new Dispatcher(serverConfig);

            while(true) {
                Socket client = serverSocket.accept();
                log.info("Client connected: {}", client.getInetAddress());

                threadPool.submit(() -> handleClient(client, dispatcher));

            }
        }catch(Exception e) {
            //서버 시작 실패
            log.error("Failed to start WAS", e);
        }

    }

    private static void handleClient(Socket client, Dispatcher dispatcher) {
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
            try{
                client.close();
                log.info("Client closed");
            }catch(Exception e) {
                log.warn("Failed to close client socket",e);
            }

        }
    }
}