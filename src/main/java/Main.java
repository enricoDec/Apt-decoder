import com.github.psambit9791.wavfile.WavFileException;
import htw.ai.dln.Apt;
import htw.ai.dln.AptDecoder;
import htw.ai.dln.Exceptions.NoSyncFrameFoundException;
import htw.ai.dln.Exceptions.UnsupportedAudioChannelSizeException;
import htw.ai.dln.Exceptions.UnsupportedAudioSampleRateException;
import htw.ai.dln.Exceptions.UnsupportedFrameSizeException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class Main {
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, UnsupportedAudioSampleRateException, UnsupportedAudioChannelSizeException, WavFileException, NoSyncFrameFoundException, UnsupportedFrameSizeException {

        File inputFile = new File("src/main/resources/example_stereo.wav");
        Apt apt = new Apt(inputFile);

        Long start = System.currentTimeMillis();
        AptDecoder aptDecoder = new AptDecoder(apt);
        int[] digitalized = aptDecoder.decode(1);
        System.out.println("Millis for hilbert: " + (System.currentTimeMillis() - start));
        aptDecoder.saveImage(digitalized, new File("src/main/resources/output/raw.png"));
        start = System.currentTimeMillis();
        int[] synced = aptDecoder.syncFrames(digitalized);
        System.out.println("Millis for sync: " + (System.currentTimeMillis() - start));
        aptDecoder.saveImage(synced, new File("src/main/resources/output/synced.png"));
    }
}
