import htw.ai.dln.Apt;
import htw.ai.dln.AptDecoder;
import htw.ai.dln.Exceptions.NoSyncFrameFoundException;
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
public class ConsoleApp {
    private static boolean sync = true;
    private static boolean isDebug = false;
    private static final int requiredArgs = 2;

    private static final String rawShort = "-r";
    private static final String raw = "--raw";
    private static final String debugShort = "-d";
    private static final String debug = "--debug";

    public static void main(String[] args) {
        // Arguments
        // java -jar aptdecoder.jar audio.wav output.png -R --Raw (no sync) -d --Debug
        // default sync on

        // Check input audio and output path args
        if (args.length < requiredArgs) {
            usage();
            System.exit(1);
        }

        // If more args given
        if (args.length > requiredArgs) {
            for (int i = requiredArgs; i < args.length; i++) {
                switch (args[i].toLowerCase()) {
                    case raw:
                    case rawShort:
                        sync = false;
                        break;
                    case debug:
                    case debugShort:
                        isDebug = true;
                        break;
                    default:
                        printError("Arg: " + args[i] + " not recognised");
                        System.exit(1);
                        break;
                }
            }

        }

        // Check if output file is valid and can write
        File outputImage = new File(args[1]);
        // If the file already exists ask permission to override
        if (outputImage.exists()) {
            print("File: " + outputImage.getAbsolutePath() + " already exists");
            print("Replace ? (y/n)");
            Scanner scanner = new Scanner(System.in);
            boolean replace;
            replace = scanner.hasNext("[Yy]");
            if (!replace)
                System.exit(0);
        }


        // Make Apt object
        Apt apt = null;
        try {
            File inputAudio = new File(args[0]);
            apt = new Apt(inputAudio);
            print(apt.toString());
        } catch (IOException | UnsupportedAudioFileException | IllegalArgumentException e) {
            printError(e.getMessage());
            System.exit(1);
        } catch (UnsupportedAudioSampleRateException e) {
            printError("Input Audio has Unsupported Sample Rate." + System.lineSeparator()
                    + "Please resample Audio to " + (int) Apt.INTERMEDIATE_SAMPLE_RATE + "Hz");
            System.exit(1);
        } catch (UnsupportedAudioChannelSizeException e) {
            printError("Input Audio has more then 2 channels");
            System.exit(1);
        }

        // Make Apt Decoder
        AptDecoder aptDecoder = new AptDecoder(apt);
        if (isDebug)
            aptDecoder.isInteractive = true;
        try {
            int[] image = aptDecoder.decode();
            int[] synced = null;
            if (sync) {
                try {
                    synced = aptDecoder.syncFrames(image);
                } catch (NoSyncFrameFoundException e) {
                    printError(e.getMessage());
                    synced = null;
                }
            }
            if (synced != null)
                aptDecoder.saveImage(synced, outputImage);
            else {
                print("Saving raw version since no sync lines found :(");
                aptDecoder.saveImage(image, outputImage);
            }
        } catch (UnsupportedFrameSizeException e) {
            printError(e.getMessage());
            System.exit(1);
        }

        print("Output Location: " + outputImage.getAbsoluteFile());
    }

    /**
     * Usage info
     */
    public static void usage() {
        String raw = "-R or --Raw to output the raw image without sync or corrections";
        String debug = "-D or --Debug to have some debug output";
        String options = raw + System.lineSeparator() + debug;
        String usage = "Arguments: [Input Audio Path] [Output Image Path] [Options]";
        String example = "Example: audio.wav Raw_Output.png -R";

        print(usage + System.lineSeparator()
                + options + System.lineSeparator()
                + example);
    }

    /**
     * Print message
     *
     * @param message Message to print
     */
    public static void print(String message) {
        System.out.println(message);
    }

    /**
     * Print Error Message
     *
     * @param message Error Message
     */
    public static void printError(String message) {
        System.err.println(message);
    }
}
