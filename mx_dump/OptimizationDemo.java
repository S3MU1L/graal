public class OptimizationDemo {

    private static class Counter {
        int value;
    }

    private static class Config {
        int limit;
    }

    static void bump(Counter counter) {
        counter.value++;
    }

    static void process(Counter counter, Config config, int n) {
        for (int i = 0; i < n; i++) {
            bump(counter);
            if (i > config.limit) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.limit = 10;
        process(new Counter(), config, 10);
    }
}
