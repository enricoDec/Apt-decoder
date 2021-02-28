package htw.ai.dln.utils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 22.02.21
 **/
public class WavUtils {

    /**
     * Converts the byte array into an array of integers where each integer
     * corresponds to an audio sample.
     *
     * @param audioData the audio data
     * @param format    the audio format
     * @return the corresponding array of integers
     */
    public static int[] convertByteArray(byte[] audioData, AudioFormat format) {
        int MAX_SIZE_RMS = audioData.length;

        if (format.getFrameSize() == 2 || format.getFrameSize() == 4) {
            int[] samples = new int[Math.min(audioData.length / 2, MAX_SIZE_RMS)];
            int offset = audioData.length - 2 * samples.length;
            for (int i = 0; i < samples.length; i++) {
                if (format.isBigEndian()) {
                    samples[i] = ((audioData[offset + i * 2] << 8)
                            | (audioData[offset + i * 2 + 1] & 0xFF));
                } else {
                    samples[i] = ((audioData[offset + i * 2 + 0] & 0xFF)
                            | (audioData[offset + i * 2 + 1] << 8));
                }
            }
            return samples;
        } else if (format.getFrameSize() == 1) {
            int[] samples = new int[Math.min(audioData.length, MAX_SIZE_RMS)];
            int offset = audioData.length - samples.length;
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (audioData[offset + i] << 8);
            }
            return samples;
        } else {
            throw new RuntimeException(
                    "unsupported frame size: " + format.getFrameSize());
        }
    }

    /**
     * Convert stereo to mono (ignores second channel)
     *
     * @param stereoAudio      audio bytes of stereo audio
     * @param sampleSizeInBits the size of a sample in bits
     * @return byte[] with one channel
     */
    public static byte[] stereoToMono(byte[] stereoAudio, int sampleSizeInBits) {
        // Make buffer to fit one channel
        byte[] buffer = new byte[stereoAudio.length / 2];

        // 16bits per channel -> 2 bytes per channel
        int sampleByteSize = sampleSizeInBits / 8;

        for (int i = 0; i < stereoAudio.length / sampleByteSize; i = i + 2) {
            buffer[i] = stereoAudio[sampleByteSize * i];
            buffer[i] = stereoAudio[sampleByteSize * i + 1];
        }
        return buffer;
    }

    /**
     * Save Wav Audio to given output
     *
     * @param audio       audio to save
     * @param outputFile  output file
     * @param audioFormat audio format
     * @throws IOException if any I/O errors
     */
    public static void saveAudio(byte[] audio, File outputFile, AudioFormat audioFormat) throws IOException {
        AudioFormat outFormat = new AudioFormat(audioFormat.getEncoding(), audioFormat.getSampleRate(), audioFormat.getSampleSizeInBits(),
                1, audioFormat.getFrameSize() / 2, audioFormat.getFrameRate(), audioFormat.isBigEndian());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audio);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, outFormat, audio.length / outFormat.getFrameSize());
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        audioInputStream.close();
    }
}
