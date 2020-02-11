import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Sounds {

    public static void hello() {
        new Thread(() -> {
            playSound(1, 80, 100, 100);
            playSound(1, 90, 100, 100);
            playSound(1, 100, 100, 100);
        }).start();
    }

    public static void success1() {
        new Thread(() -> playSound(1, 80, 100, 1000)).start();
    }

    public static void success2() {
        new Thread(() -> playSound(1, 90, 100, 1000)).start();
    }

    public static void fail() {
        new Thread(() -> playSound(1, 40, 100, 100)).start();
    }

    public static void successfulFinish() {
        new Thread(() -> {
            playSound(1, 90, 100, 100);
            playSound(1, 100, 100, 200);
            playSound(1, 90, 100, 100);
            playSound(1, 100, 100, 200);
            playSound(1, 90, 100, 100);
            playSound(1, 100, 100, 200);
            playSound(1, 90, 100, 100);
            playSound(1, 100, 100, 600);
        }).start();
    }

    public static void unsuccessfulFinish() {
        new Thread(() -> {
            playSound(1, 50, 100, 300);
            playSound(1, 50, 100, 300);
            playSound(1, 40, 100, 300);
        }).start();
    }

    private static void playSound(int channel, int note, int volume, int duration) {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            MidiChannel[] channels = synth.getChannels();
            channels[channel].noteOn(note, volume);
            delay(duration);
            channels[channel].allNotesOff();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void delay(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
