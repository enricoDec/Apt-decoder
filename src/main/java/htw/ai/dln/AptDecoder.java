package htw.ai.dln;

import htw.ai.dln.Exceptions.UnsupportedAudioChannelSize;
import htw.ai.dln.utils.hilbert.ComplexArray;
import htw.ai.dln.utils.hilbert.Hilbert;
import htw.ai.dln.utils.WavUtils;
import htw.ai.dln.utils.SignalUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class AptDecoder implements IAptDecoder {
    private final Apt apt;
    private byte[] audio;
    private int expectedLines;
    private int actualLines;
    private final static float SYNC_CORRELATION = 0.9f;

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
                audio = WavUtils.stereoToMono(apt.getAudioAsBytes(), apt.getAudioFormat().getSampleSizeInBits());
                break;
            default:
                throw new UnsupportedAudioChannelSize("Audio has " + apt.getAudioFormat().getChannels());
        }

        //Convert byte[] to samples as double[]
        int[] result = WavUtils.convertByteArray(audio, apt.getAudioFormat());
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
        // Map values to "digital" values 0 to 255
        int[] digitalized = digitalize(amplitudeEnvelope);
        // Sync lines
        int[] synced = sync(digitalized);
        makeImage(synced);
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

        double avg = Arrays.stream(amplitudeEnvelope).average().getAsDouble();
        for (int i = 0; i < amplitudeEnvelope.length; i++) {
            if (amplitudeEnvelope[i] > avg * 2.5)
                amplitudeEnvelope[i] = 0;
        }

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
        double[] reshapedCut = new double[reshaped.length];
        for (int i = 0; i < reshaped.length; i++) {
            //Don't know why you only get every 3rd value and skip the rest
            //You could also get every 2nd or 4th but you have to skip the rest
            reshapedCut[i] = reshaped[i][2];
        }

        expectedLines = (int) Math.ceil((float) reshapedCut.length / Apt.LINE_LENGTH);

        if (expectedLines < 1)
            throw new IllegalArgumentException("Less than one line found...");

        // Cutting interference
        // To get a "better" max to avoid getting an anomaly as max value and getting so a dark image
        // when digitalizing values since it will be mapped with a wrong range
        // (implementing low pass filter or smth would be much better)

        // AVG
        double avg = 0;
        for (int i = 0; i < reshapedCut.length; i++) {
            avg += reshapedCut[i];
        }
        avg = avg / reshaped.length;

        // Cut values above and under
//        for (int i = 0; i < reshapedCut.length; i++) {
//            if (reshapedCut[i] > avg * 2.5)
//                reshapedCut[i] = 0;
//        }

        double maxValueAvG = Arrays.stream(reshapedCut).max().getAsDouble();
        double minValueAvg = 0;

        // Digitalize
        int[] data = new int[reshapedCut.length];
        for (int i = 0; i < reshapedCut.length; i++) {
            data[i] = (int) mapOneRangeToAnother(reshapedCut[i], minValueAvg, maxValueAvG, 0, 255, 1);
        }


        // PLOTTING
//        int[] temp = Arrays.copyOfRange(data, data.length / 2, data.length / 2 + 1040);
//        double[] temp2 = new double[data.length];
//
//        for (int i = 0; i < data.length; i++) {
//            temp2[i] = data[i];
//        }
//        SignalUtils.plotSignal(temp2, data.length);

        return data;
    }

    private List<Integer> foundSyncFrames = new ArrayList<>();

    // Sync Frame Pattern 36 pixels from WW BB WW BB WW BB ... WWWW WWWW
    // 0 is Black, 255 is White
    private int[] sync(int[] reshaped) {
        int[] x = new int[]{0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0};
        int[] synced = new int[0];
        //int[] shifted = new int[reshaped.length];

//        for (int i = 0; i < reshaped.length; i++) {
//            if (reshaped[i] - 120 > 0)
//                shifted[i] = reshaped[i] - 120;
//        }

        //Loop entire array and look for positive correlation of sync A frame
        int lastLineFound = 0;
        int oldOffset = 0;
        for (int i = 0; i < reshaped.length; i++) {

            int[] y = Arrays.copyOfRange(reshaped, i, i + x.length);
            double correlation = SignalUtils.correlation(x, y);

            if (correlation > SYNC_CORRELATION) {
                System.out.println("Start of a line found with correlation of" + " " + correlation);
                System.out.println("Prob at line: " + i / Apt.LINE_LENGTH + " i= " + i);
                int offset = i % Apt.LINE_LENGTH;
                // Copy corrected current line and lines before if not already corrected and offset changed
                if (offset != oldOffset) {
                    System.out.println("offset = " + offset);
                    synced = IntStream.concat(Arrays.stream(synced), Arrays.stream(Arrays.copyOfRange(reshaped, lastLineFound + (offset - oldOffset), i + Apt.LINE_LENGTH))).toArray();
                    System.out.println("Copyed array from: " + (lastLineFound + offset - oldOffset) + " to: " + i + Apt.LINE_LENGTH);
                    foundSyncFrames.add(i);
                    // Skip to next line
                    i = i + Apt.LINE_LENGTH;
                    lastLineFound = i;
                    oldOffset = offset;
                }
            }
        }
        return synced;
    }


    /**
     * Create Image from digitalized signal
     *
     * @param data digitalized Signal
     */
    private void makeImage(int[] data) {
        int width = Apt.LINE_LENGTH;
        //if last line is not complete still print part
        int height = (int) Math.ceil((float) data.length / Apt.LINE_LENGTH);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int line = 0;
        for (int i = 0; i < data.length; i++) {
            if (i % Apt.LINE_LENGTH == 0 && i != 0)
                line++;
            if (foundSyncFrames.contains(i))
                bufferedImage.setRGB(i - (line * Apt.LINE_LENGTH), line, new Color(255, 0, 0).getRGB());
            else
                bufferedImage.setRGB(i - (line * Apt.LINE_LENGTH), line, new Color(data[i], data[i], data[i]).getRGB());
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
