public class Getter {

    private static class Box {

        int value;

        public Box(int value) {
            this.value = value;
        }

        int get() {
            return value;
        }
    }

    public static void main(String[] args) {
        Box a = new Box(5);
        Box b = new Box(4);
        System.out.println(a.get()+" "+b.get());
    }
}
