public class Demo {

    public static int sum(int from, int to) {
        int sum = 0;
        for (int i = from; i <= to; ++i) {
            sum += i;
        }
        return sum;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int i = scan.nextInt();
        int a = sum(i, 10);
        int b = sum(10, 20);
        int c = sum(20, 30);
        System.out.println(a + b);
    }
}
