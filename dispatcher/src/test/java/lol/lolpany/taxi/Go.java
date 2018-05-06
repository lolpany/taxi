package lol.lolpany.taxi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Go {

    private static final int NUMBER_OF_PNONES = 100;

    @Test
    public void test() throws InterruptedException, ExecutionException {
        List<Callable<Void>> generators= new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_PNONES; i++) {
            generators.add(new RequestGenerator(100));
        }
        ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_PNONES, NUMBER_OF_PNONES, 5,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(NUMBER_OF_PNONES));
        List<Future<Void>> futures = executorService.invokeAll(generators);
        for (Future<Void> future : futures) {
            future.get();
        }
    }
}
