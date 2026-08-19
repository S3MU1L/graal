public class DeadStoreElim {

    private static class Counter {
        int value;
    }

    private static class Logger {
        int count;
    }

    static void unrelatedWork(Logger logger) {
        logger.count++;
    }

    static void resetCounter(Counter counter, Logger logger) {
        counter.value = 1;
        unrelatedWork(logger);
        counter.value = 2;
    }

    public static void main(String[] args) {
        Counter counter = new Counter();
        Logger logger = new Logger();
        for (int i = 0; i < 10; ++i) {
            unrelatedWork(logger);
            resetCounter(counter, logger);
        }
        System.out.println("counter value: " + counter.value);
        System.out.println("counter value: " + counter.value);
    }
}
