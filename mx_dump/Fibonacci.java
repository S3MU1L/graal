public class Fibonacci {

    public static int rec(int n, int a, int b) {
        if (n == 0) {
            return a;
        }

        return rec(n - 1, b, a + b);
    }

    public static int fib(int n) {
        return rec(n, 0, 1);
    }

    public static void main(String[] args) {
        System.out.println(fib(5));
    }
}