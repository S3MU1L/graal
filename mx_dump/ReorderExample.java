import java.util.Random;

public class ReorderExample {

    static int[] data = new int[2];

    static void write(int index, int value) {
        data[index] = value;
    }

    public static void main(String[] args) {
        Random r = new Random();
        int i = r.nextInt(2);
        int j = 1 - i;

        write(i, 100);
        write(j, 200);

        System.out.println(data[0] + " " + data[1]);
    }
}
