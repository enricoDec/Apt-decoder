package htw.ai.dln;

import htw.ai.dln.Exceptions.UnsupportedAudioSampleRate;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class Apt {
    public static final float INTERMEDIATE_SAMPLE_RATE = 20800f;
    public static final int LINE_LENGTH = 2080;
    private final AudioFormat audioFormat;
    private File audioFile;
    private byte[] audioAsBytes;

    /**
     * Create APT Object
     * Audio File should have a sample rate of 20800, this is to speed up decoding
     *
     * @throws UnsupportedAudioFileException if the File does not point to valid audio file data recognized by the system
     * @throws IOException                   if an I/O exception occurs
     * @throws IllegalArgumentException      if file does not exist or JVM couldn't read file
     * @throws UnsupportedAudioSampleRate    if file has an unsupported audio sample rate
     */
    public Apt(File audioFile) throws IOException, UnsupportedAudioFileException, UnsupportedAudioSampleRate {
        if (!audioFile.exists())
            throw new IllegalArgumentException(audioFile.getAbsolutePath() + " does not exist");
        if (!audioFile.canRead())
            throw new IllegalArgumentException(audioFile.getAbsolutePath() + " couldn't be read by the JVM");

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        audioAsBytes = audioInputStream.readAllBytes();
        this.audioFormat = audioInputStream.getFormat();
        float inputSampleRate = audioFormat.getSampleRate();

        if (inputSampleRate != INTERMEDIATE_SAMPLE_RATE)
            throw new UnsupportedAudioSampleRate("Input Audio has to have sample rate of " + INTERMEDIATE_SAMPLE_RATE + " but was " + inputSampleRate);
        this.audioFile = audioFile;
        audioInputStream.close();
    }

    public File getAudioFile() {
        return audioFile;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public byte[] getAudioAsBytes() {
        return audioAsBytes;
    }
}
