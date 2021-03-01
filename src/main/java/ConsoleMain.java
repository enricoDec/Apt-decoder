import htw.ai.dln.Apt;
import htw.ai.dln.AptDecoder;
import htw.ai.dln.Exceptions.UnsupportedAudioChannelSizeException;
import htw.ai.dln.Exceptions.UnsupportedAudioSampleRateException;
import htw.ai.dln.Exceptions.UnsupportedFrameSizeException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 01.03.21
 **/
public class ConsoleMain {
    public static void main(String[] args) {
        // Arguments
        // java -jar aptdecoder.jar audio.wav output.png -R --Raw (no sync)
        // default sync on

        // Check args
        if (args.length < 2)
            usage();

        File outputImage = new File(args[1]);
        if (outputImage.exists()) {
            print("File: " + outputImage.getAbsolutePath() + " already exists");
            print("Replace ? (y/n)");
            Scanner scanner = new Scanner(System.in);
            boolean replace;
            replace = scanner.hasNext("[Yy]");
            if (!replace)
                System.exit(0);
        }


        Apt apt = null;
        try {
            File inputAudio = new File(args[0]);
            apt = new Apt(inputAudio);
        } catch (IOException | UnsupportedAudioFileException | IllegalArgumentException e) {
            printError(e.getMessage());
        } catch (UnsupportedAudioSampleRateException e) {
            printError("Input Audio has Unsupported Sample Rate." + System.lineSeparator()
                    + "Please resample Audio to " + (int) Apt.INTERMEDIATE_SAMPLE_RATE + "Hz");
        } catch (UnsupportedAudioChannelSizeException e) {
            printError("Input Audio has more then 2 channels");
        }

        if (apt == null)
            printError("Audio could not be read.");

        AptDecoder aptDecoder = new AptDecoder(apt);
        try {
            int[] image = aptDecoder.decode();
            aptDecoder.saveImage(image, outputImage);
        } catch (UnsupportedFrameSizeException e) {
            printError(e.getMessage());
        }


    }

    public static void usage() {
        String raw = "-R or --Raw to output the raw image without sync or corrections";
        String options = raw;
        String usage = "Arguments: [Input Audio Path] [Output Image Path] [Options]";
        String example = "Example: audio.wav Raw_Output.png -R";

        System.out.println(usage + System.lineSeparator()
                + options + System.lineSeparator()
                + example);
    }

    public static void print(String message) {
        System.out.println(message);
    }

    public static void printError(String message) {
        System.err.println(message);
    }
}
