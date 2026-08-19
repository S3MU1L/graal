public class Example {
    private static class Image {
        int brightness;
    }

    private static class Audio {
        int volume;
    }

    static void brighten(Image image) {
        image.brightness++;
    }

    static void amplify(Audio audio) {
        audio.volume++;
    }

    static void process(Image image, Audio audio) {
        brighten(image);
        amplify(audio);
    }

    public static void main(String[] args) {
        Image image = new Image(100);
        Audio audio = new Audio(50);
        for (int i = 0; i < 100; i++) {
            process(image, audio);
        }
        System.out.println(image.brightness);
        System.out.println(audio.volume);
    }
}
