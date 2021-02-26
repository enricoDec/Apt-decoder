package htw.ai.dln;

import htw.ai.dln.Exceptions.UnsupportedAudioChannelSize;
import htw.ai.dln.hilbert.ComplexArray;
import htw.ai.dln.hilbert.Hilbert;
import htw.ai.dln.utils.AudioUtils;
import htw.ai.dln.utils.SignalUtils;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

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

    /**
     * APT Decoder
     * Steps:
     * 1. Check audio channel number
     * 1.1 If more than one convert to mono
     * 2. Convert bytes to samples as doubles
     *
     * @param threads number of threads to use
     * @throws UnsupportedAudioChannelSize if audio has more than two channels
     * @throws IOException
     */
    public void decode(int threads) throws UnsupportedAudioChannelSize, IOException {
        //Check if stereo or mono and get audio without header as byte[]
        switch (apt.getAudioFormat().getChannels()) {
            case 1:
                audio = apt.getAudioAsBytes();
                break;
            case 2:
                System.out.println("Audio has 2 channels");
                audio = AudioUtils.stereoToMono(apt.getAudioAsBytes(), apt.getAudioFormat().getSampleSizeInBits());
                break;
            default:
                throw new UnsupportedAudioChannelSize("Audio has " + apt.getAudioFormat().getChannels());
        }

        //Convert byte[] to samples as double[]
        int[] result = AudioUtils.convertByteArray(audio, apt.getAudioFormat());
        double[] signal = new double[result.length];
        for (int i = 0; i < result.length; i++) {
            signal[i] = result[i];
        }

        // Reduce unnecessary sample rate
        // Not sure why?
        int truncate = (int) (apt.getAudioFormat().getSampleRate() * (signal.length / (int) apt.getAudioFormat().getSampleRate()));
        signal = Arrays.copyOf(signal, truncate);

        // Perform Hilbert Transform and get amplitude Envelope
        double[] amplitudeEnvelope = hilbert(signal);
        int[] digitalized = digitalize(amplitudeEnvelope);
        makeImage(digitalized);
    }


    /**
     * Performs Hilbert Transform
     *
     * @param signal input signal to transform
     * @return input signal amplitude envelope
     */
    private double[] hilbert(double[] signal) {
        ComplexArray complexArray = Hilbert.transform(signal);
        double[] amplitudeEnvelope = new double[signal.length];
        for (int i = 0; i < complexArray.imag.length; i++) {
            amplitudeEnvelope[i] = Math.sqrt(complexArray.real[i] * complexArray.real[i] + complexArray.imag[i] * complexArray.imag[i]);
        }
        SignalUtils.plotSignals(signal, amplitudeEnvelope, signal.length / 4);

        return amplitudeEnvelope;
    }

    /**
     * Convert signal to numbers between 0 and 255
     *
     * @param amplitudeEnvelope Amplitude Envelope of Signal
     * @return digitalized signal as int[]
     */
    private int[] digitalize(double[] amplitudeEnvelope) {
        double[][] reshaped = new double[amplitudeEnvelope.length / 5][5];

        for (int i = 0; i < amplitudeEnvelope.length / 5; i++) {
            System.arraycopy(amplitudeEnvelope, i * 5, reshaped[i], 0, 5);
        }
        double[] reshaped_cut = new double[reshaped.length];
        for (int i = 0; i < reshaped.length; i++) {
            reshaped_cut[i] = reshaped[i][2];
        }

        double minValue = Arrays.stream(reshaped_cut).min().getAsDouble();
        double maxValue = Arrays.stream(reshaped_cut).max().getAsDouble();

        int[] data = new int[reshaped_cut.length];
        for (int i = 0; i < reshaped_cut.length; i++) {
            data[i] = (int) mapOneRangeToAnother(reshaped_cut[i], minValue, maxValue, 0, 255, 2);
        }

        return data;
//        int[] data = new int[amplitudeEnvelope.length / 5];
//
//        double amplitudeEnvelopeMinValue = (int) Arrays.stream(amplitudeEnvelope).min().getAsDouble();
//        double amplitudeEnvelopeMaxValue = (int) Arrays.stream(amplitudeEnvelope).max().getAsDouble();
//        for (int i = 2; i < data.length; i = i + 5) {
//            int newValue = (int) mapOneRangeToAnother(amplitudeEnvelope[i], amplitudeEnvelopeMinValue, amplitudeEnvelopeMaxValue,
//                    0, 255, 1);
//
//            //Just in case
//            if (newValue > 255)
//                newValue = 255;
//            if (newValue < 0)
//                newValue = 0;
//
//            data[i] = newValue;
//        }
//        return data;
    }

    /**
     * Create Image from digitalized signal
     *
     * @param data digitalized Signal
     */
    private void makeImage(int[] data) {
        int width = Apt.LINE_LENGTH;
        int height = data.length / Apt.LINE_LENGTH;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int line = 0;
        for (int i = 0; i < data.length; i++) {
            if (i % Apt.LINE_LENGTH == 0 && i != 0)
                line++;
            try {
                bufferedImage.setRGB(i - (line * Apt.LINE_LENGTH), line, new Color(data[i], data[i], data[i]).getRGB());

            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                System.out.println("(" + (i - (line * Apt.LINE_LENGTH)) + "),(" + line + ")");
                System.out.println(data[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }
        try {
            File file = new File("src/main/resources/out.png");
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sourceNumber
     * @param fromA
     * @param fromB
     * @param toA
     * @param toB
     * @param decimalPrecision
     * @return
     */
    public static double mapOneRangeToAnother(double sourceNumber, double fromA, double fromB, double toA, double toB, int decimalPrecision) {
        double deltaA = fromB - fromA;
        double deltaB = toB - toA;
        double scale = deltaB / deltaA;
        double negA = -1 * fromA;
        double offset = (negA * scale) + toA;
        double finalNumber = (sourceNumber * scale) + offset;
        int calcScale = (int) Math.pow(10, decimalPrecision);
        return (double) Math.round(finalNumber * calcScale) / calcScale;
    }
}
