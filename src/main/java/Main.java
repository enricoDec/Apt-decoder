import com.github.psambit9791.wavfile.WavFileException;
import htw.ai.dln.Apt;
import htw.ai.dln.AptDecoder;
import htw.ai.dln.Exceptions.UnsupportedAudioChannelSize;
import htw.ai.dln.Exceptions.UnsupportedAudioSampleRate;

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
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, UnsupportedAudioSampleRate, UnsupportedAudioChannelSize, WavFileException {

        File inputFile = new File("src/main/resources/example_cut.wav");
        Apt apt = new Apt(inputFile);

        AptDecoder aptDecoder = new AptDecoder(apt);
        //multithread decode() variation
        aptDecoder.decode(1);
        //aptDecoder.save(new File("src/main/resources/out.wav"));
    }
}
