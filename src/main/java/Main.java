import htw.ai.dln.Apt;
import htw.ai.dln.AptDecoder;
import htw.ai.dln.Exceptions.NoSyncFrameFoundException;
import htw.ai.dln.Exceptions.UnsupportedAudioChannelSizeException;
import htw.ai.dln.Exceptions.UnsupportedAudioSampleRateException;
import htw.ai.dln.Exceptions.UnsupportedFrameSizeException;
import htw.ai.dln.utils.ArrayUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class Main {
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, UnsupportedAudioSampleRateException, UnsupportedAudioChannelSizeException, NoSyncFrameFoundException, UnsupportedFrameSizeException {

        File inputFile = new File("src/main/resources/24_02.wav");
        Apt apt = new Apt(inputFile);

        AptDecoder aptDecoder = new AptDecoder(apt);
        int[] digitalized = aptDecoder.decode();
        aptDecoder.saveImage(digitalized, new File("src/main/resources/output/raw.png"));
        int[] synced = aptDecoder.syncFrames(digitalized);
        aptDecoder.saveImage(synced, new File("src/main/resources/output/synced.png"));
    }
}
