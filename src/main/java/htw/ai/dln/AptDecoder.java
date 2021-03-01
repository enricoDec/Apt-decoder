package htw.ai.dln;

import htw.ai.dln.Exceptions.NoSyncFrameFoundException;
import htw.ai.dln.Exceptions.UnsupportedFrameSizeException;
import htw.ai.dln.utils.SignalUtils;
import htw.ai.dln.utils.WavUtils;
import htw.ai.dln.utils.hilbert.ComplexArray;
import htw.ai.dln.utils.hilbert.Hilbert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class AptDecoder implements IAptDecoder {
    private final Apt apt;
    private byte[] audio;
    private final float MIN_SYNC_CORRELATION = 0.9f;
    // Optional Variables used to store store debug info
    private int expectedLines;
    private int actualLines;
    private List<Integer> foundSyncFrames = new ArrayList<>();
    public boolean isInteractive = false;

    /**
     * Apt decoder
     *
     * @param apt Wav to decode
     */
    public AptDecoder(Apt apt) {
        this.apt = apt;
    }

    /**
     * APT Decoder
     * Decodes an audio to an image
     * Result will be the raw decoded image (no sync) or corrections
     * The Result will depend a lot from the amount of noise in the input signal
     *
     * @return int[] each int equals to one pixel, ranges from 0 to 255
     */
    public int[] decode() throws UnsupportedFrameSizeException {
        //Check if stereo or mono and get audio without header as byte[]
        int[] samples;
        switch (apt.AUDIO_FORMAT.getChannels()) {
            case 1:
                audio = apt.AUDIO_BYTES;
                //Convert byte[] to sample[]
                samples = WavUtils.convertByteArray(audio, apt.AUDIO_FORMAT.getFrameSize(),
                        apt.AUDIO_FORMAT.isBigEndian());
                break;
            case 2:
                statusUpdate("Audio has two channels, converting to single channel.");
                audio = WavUtils.stereoToMono(apt.AUDIO_BYTES, apt.AUDIO_FORMAT.getSampleSizeInBits());
                statusUpdate("Conversion done.");
                //Convert byte[] to sample[]
                samples = WavUtils.convertByteArray(audio, 2, apt.AUDIO_FORMAT.isBigEndian());
                break;
            default:
                throw new IllegalStateException("Unexpected value. Channels: " + apt.AUDIO_FORMAT.getChannels());
        }

        // Convert int sample[] to double sample[]
        double[] signal = new double[samples.length];
        for (int i = 0; i < samples.length; i++) {
            signal[i] = samples[i];
        }

        // Reduce unnecessary sample rate
        int truncate = (int) (apt.AUDIO_FORMAT.getSampleRate() * (signal.length / (int) apt.AUDIO_FORMAT.getSampleRate()));
        signal = Arrays.copyOf(signal, truncate);

        // Perform Hilbert Transform and get amplitude Envelope
        statusUpdate("Performing Hilbert Transform.");
        double[] amplitudeEnvelope = hilbert(signal);
        // Map values to "digital" values 0 to 255
        int[] digitalized = digitalize(amplitudeEnvelope);
        statusUpdate("Done.");

        return digitalized;
    }


    /**
     * Sync imag with Sync frames to fix doppler effect and fix first line offset
     *
     * @param digitalized Digitalized signal, should range from 0 to 255
     * @return synced Image with syn Frames
     */
    public int[] syncFrames(int[] digitalized) throws NoSyncFrameFoundException {
        statusUpdate("Searching for Sync Pattern. Expected Lines: " + expectedLines);
        // Sync Patter BB WW BB WW BB...
        int[] syncPattern = new int[]{0, 0, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0, 255, 255, 0, 0};
        int[] synced = new int[0];
        //To get a better correlation you could try to shift all values to get a higher contrast

        // Loop entire array and look for positive correlation of sync A frame
        // If found shift by offset from last synced line to current line
        // and make a copy of synced line(s) in sync[] <- Do this only if offset changed!
        int indexOfLastNotSyncedLine = 0;
        int previousOffset = 0;
        for (int i = 0; i < digitalized.length; i++) {

            // Copy pixels to fit in sync pattern
            int[] y = Arrays.copyOfRange(digitalized, i, i + syncPattern.length);
            // Calc correlation
            double correlation = SignalUtils.correlation(syncPattern, y);

            if (correlation > MIN_SYNC_CORRELATION) {
                // Offset is rest of i / 2080
                int currentOffset = i % Apt.LINE_LENGTH;

                // Copy corrected line from last synced line to current one, this only if the current offset changed
                if (currentOffset != previousOffset) {
                    synced = IntStream.concat(Arrays.stream(synced),
                            Arrays.stream(Arrays.copyOfRange(digitalized,
                                    indexOfLastNotSyncedLine + (currentOffset - previousOffset),
                                    i + Apt.LINE_LENGTH))).toArray();
                    // Add to list to keep track of found lines
                    foundSyncFrames.add(i);
                    // Skip to next line
                    i = i + Apt.LINE_LENGTH;
                    indexOfLastNotSyncedLine = i;
                    previousOffset = currentOffset;
                }
            }
            // If arrived at end and last line could not be synced, add them without offset
            // If last line was synced looping condition will be false
            // So if we arrive at last index it should be safe to say that the line was not synced and add them
            // to result array
            if (i == digitalized.length - 1) {
                synced = IntStream.concat(Arrays.stream(synced),
                        Arrays.stream(Arrays.copyOfRange(digitalized, indexOfLastNotSyncedLine, digitalized.length))).toArray();
            }
        }
        if (foundSyncFrames.size() == 0)
            throw new NoSyncFrameFoundException("Could not find any sync Frame.");

        statusUpdate("Found Sync Lines: " + foundSyncFrames.size());
        return synced;
    }

    /**
     * Create Image from digitalized signal
     *
     * @param data         digitalized Signal
     * @param saveLocation path to output file
     */
    public void saveImage(int[] data, File saveLocation) {
        statusUpdate("Creating Image.");
        //if last line is not complete still count it
        actualLines = (int) Math.ceil((float) data.length / Apt.LINE_LENGTH);

        int width = Apt.LINE_LENGTH;
        int height = actualLines;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int line = 0;
        for (int i = 0; i < data.length; i++) {
            if (i % Apt.LINE_LENGTH == 0 && i != 0)
                line++;

            bufferedImage.setRGB(i - (line * Apt.LINE_LENGTH), line, new Color(data[i], data[i], data[i]).getRGB());
        }
        try {
            ImageIO.write(bufferedImage, "png", saveLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // Cutting interference
        // To get a "better" max to avoid getting an anomaly as max value and getting so a dark image
        // when digitalizing values since it will be mapped with a wrong range
        // (implementing low pass filter or smth would be much better)
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

        // Not sure about this part but it's needed to work
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
            throw new IllegalArgumentException("Data contains no lines");


        double maxValueAvG = Arrays.stream(reshapedCut).max().getAsDouble();
        double minValueAvg = 0;

        // Map values to range from 0 to 255
        int[] data = new int[reshapedCut.length];
        for (int i = 0; i < reshapedCut.length; i++) {
            data[i] = (int) mapOneRangeToAnother(reshapedCut[i], minValueAvg, maxValueAvG, 0, 255, 1);
        }


        // PLOTTING
        // TODO: Remove
//        int[] temp = Arrays.copyOfRange(data, data.length / 2, data.length / 2 + 1040);
//        double[] temp2 = new double[data.length];
//
//        for (int i = 0; i < data.length; i++) {
//            temp2[i] = data[i];
//        }
//        SignalUtils.plotSignal(temp2, data.length);

        return data;
    }


    /**
     * Map two values to new range
     *
     * @param sourceNumber     value to map to new range
     * @param fromA            min of old range
     * @param fromB            max of old range
     * @param toA              min of new range
     * @param toB              max of new range
     * @param decimalPrecision decimal precision
     * @return value mapped to new range (between new min and max)
     */
    private double mapOneRangeToAnother(double sourceNumber, double fromA, double fromB, double toA, double toB, int decimalPrecision) {
        double deltaA = fromB - fromA;
        double deltaB = toB - toA;
        double scale = deltaB / deltaA;
        double negA = -1 * fromA;
        double offset = (negA * scale) + toA;
        double finalNumber = (sourceNumber * scale) + offset;
        int calcScale = (int) Math.pow(10, decimalPrecision);
        return (double) Math.round(finalNumber * calcScale) / calcScale;
    }

    private void statusUpdate(String status) {
        if (isInteractive)
            System.out.println(status);
    }
}
