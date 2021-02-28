package htw.ai.dln;

import htw.ai.dln.Exceptions.UnsupportedAudioChannelSizeException;
import htw.ai.dln.Exceptions.UnsupportedAudioSampleRateException;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class Apt {
    public static final float INTERMEDIATE_SAMPLE_RATE = 20800f;
    public static final int LINE_LENGTH = 2080;
    public final AudioFormat AUDIO_FORMAT;
    public final byte[] AUDIO_BYTES;

    /**
     * Create APT Object
     * Audio File should have a sample rate of 20800, this is to speed up decoding
     *
     * @throws UnsupportedAudioFileException        if the File does not point to valid audio file data recognized by the system
     * @throws IOException                          if an I/O exception occurs
     * @throws IllegalArgumentException             if file does not exist or JVM couldn't read file
     * @throws UnsupportedAudioSampleRateException  if file has an unsupported audio sample rate
     * @throws UnsupportedAudioChannelSizeException if file has more then 2 channels
     */
    public Apt(@NotNull File audioFile) throws IOException, UnsupportedAudioFileException, UnsupportedAudioSampleRateException, UnsupportedAudioChannelSizeException {
        // Check if file exists and can read
        if (!audioFile.exists())
            throw new IllegalArgumentException(audioFile.getAbsolutePath() + " does not exist");
        if (!audioFile.canRead())
            throw new IllegalArgumentException(audioFile.getAbsolutePath() + " couldn't be read by the JVM");

        // Read audio and set properties
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        AUDIO_BYTES = audioInputStream.readAllBytes();
        this.AUDIO_FORMAT = audioInputStream.getFormat();
        float inputSampleRate = AUDIO_FORMAT.getSampleRate();

        // TODO: Implement resampling
        // Check if sample rate is correct and if number of channels is < 2
        if (inputSampleRate != INTERMEDIATE_SAMPLE_RATE)
            throw new UnsupportedAudioSampleRateException("Input Audio has to have sample rate of " + INTERMEDIATE_SAMPLE_RATE + " but was " + inputSampleRate);
        if (AUDIO_FORMAT.getChannels() > 2)
            throw new UnsupportedAudioChannelSizeException("Input Audio has " + AUDIO_FORMAT.getChannels() + " channels");
        audioInputStream.close();
    }
}
