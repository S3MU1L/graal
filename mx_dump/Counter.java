public class Counter {

    private static class ValueWrapper {
        int value;

        void inc() {
            value++;
        }
    }

    public static void main(String[] args) {
        ValueWrapper a = new ValueWrapper();
        ValueWrapper b = new ValueWrapper();
        a.inc();
        b.inc();
        System.out.println(a.value + b.value);
    }
}
