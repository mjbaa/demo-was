package http;

import config.ConfigLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class DispatcherConcurrencyTest {

    private Dispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new Dispatcher(ConfigLoader.load());
    }

    @Test
    public void concurrentRequestsShouldBeHandledSafely() throws Exception {
        int threadCount = 20;
        int requestCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<HttpResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            tasks.add(() -> {
                HttpRequest request = HttpRequest.testRequest(
                        "GET",
                        "/Hello",
                        "localhost"
                );
                return dispatcher.dispatch(request);
            });
        }

        List<Future<HttpResponse>> results = executor.invokeAll(tasks);

        for (Future<HttpResponse> future : results) {
            HttpResponse response = future.get();
            assertEquals(200, response.getStatusCode());
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
}
