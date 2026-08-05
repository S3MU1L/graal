public class CheckedAdd {

    public static int foo(int x) {
        if (x < 42) {
            return x + 1;
        }
        return x;
    }

    public static void main(String[] args) {
        System.out.println(foo(50));
    }
}
