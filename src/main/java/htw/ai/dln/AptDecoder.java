package htw.ai.dln;

import htw.ai.dln.Exceptions.UnsupportedAudioChannelSize;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class AptDecoder implements IAptDecoder {
    private final Apt apt;
    private byte[] audio;

    public AptDecoder(Apt apt) {
        this.apt = apt;
    }

    public void decode(int threads) throws UnsupportedAudioChannelSize {
        switch (apt.getAudioFormat().getChannels()) {
            case 1:
                audio = apt.getAudioAsBytes();
                break;
            case 2:
                System.out.println("Audio has 2 channels");
                audio = stereoToMono(apt.getAudioAsBytes());
                break;
            default:
                throw new UnsupportedAudioChannelSize("Audio has " + apt.getAudioFormat().getChannels());
        }
    }

    private byte[] stereoToMono(byte[] stereoAudio) {
        // Make buffer to fit one channel
        byte[] buffer = new byte[stereoAudio.length / apt.getAudioFormat().getChannels()];
        System.out.println(apt.getAudioFormat().getFrameSize());

        // 16bits per channel -> 2 bytes per channel
        int sampleByteSize = apt.getAudioFormat().getSampleSizeInBits() / 8;

        for (int i = 0; i < stereoAudio.length / sampleByteSize; i = i + 2) {
            buffer[i] = stereoAudio[sampleByteSize * i];
            buffer[i] = stereoAudio[sampleByteSize * i + 1];
        }
        return buffer;
    }


    public void save(File outputFile) throws IOException {
        AudioFormat outFormat = new AudioFormat(apt.getAudioFormat().getEncoding(),apt.getAudioFormat().getSampleRate(),apt.getAudioFormat().getSampleSizeInBits(),1,apt.getAudioFormat().getFrameSize()/2, apt.getAudioFormat().getFrameRate(),apt.getAudioFormat().isBigEndian());

        ByteArrayInputStream leftbais = new ByteArrayInputStream(audio);
        AudioInputStream leftoutputAIS = new AudioInputStream(leftbais, outFormat, audio.length / outFormat.getFrameSize());
        AudioSystem.write(leftoutputAIS, AudioFileFormat.Type.WAVE, outputFile);

    }
}
