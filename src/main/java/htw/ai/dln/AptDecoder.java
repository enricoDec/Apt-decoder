package htw.ai.dln;

import com.github.psambit9791.wavfile.WavFileException;
import htw.ai.dln.Exceptions.UnsupportedAudioChannelSize;
import htw.ai.dln.hilbert.ComplexArray;
import htw.ai.dln.hilbert.Hilbert;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
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
    private double[] signal;

    public AptDecoder(Apt apt) {
        this.apt = apt;
    }

    public void decode(int threads) throws UnsupportedAudioChannelSize, IOException, WavFileException {
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
        // every second byte is 0 or -1 for some reason ?!
        signal = new double[audio.length / 2];
        int j = 0;
        for (int i = 0; i < audio.length; i = i + 2) {
            signal[j] = audio[i];
            j++;
        }

        // Reduce unnecessary sample rate
        int truncate = (int) (apt.getAudioFormat().getSampleRate() * (signal.length / (int) apt.getAudioFormat().getSampleRate()));
        signal = Arrays.copyOf(signal, truncate);
        double[] amplitudeEnvelope = hilbert();
        makeImage(amplitudeEnvelope);

    }

    private byte[] stereoToMono(byte[] stereoAudio) {
        // Make buffer to fit one channel
        byte[] buffer = new byte[stereoAudio.length / apt.getAudioFormat().getChannels()];

        // 16bits per channel -> 2 bytes per channel
        int sampleByteSize = apt.getAudioFormat().getSampleSizeInBits() / 8;

        for (int i = 0; i < stereoAudio.length / sampleByteSize; i = i + 2) {
            buffer[i] = stereoAudio[sampleByteSize * i];
            buffer[i] = stereoAudio[sampleByteSize * i + 1];
        }
        return buffer;
    }

    private double[] hilbert() throws IOException, WavFileException {
        ComplexArray complexArray = Hilbert.transform(signal);
        double[] amplitudeEnvelope = new double[complexArray.imag.length];
        int i = 0;
        for (double imag : complexArray.imag) {
            amplitudeEnvelope[i] = imag;
            i++;
        }

        return amplitudeEnvelope;

        //plotSignals(signal, amplitudeEnvelope, 150);

//        System.out.println(amplitudeEnvelope[0]);
//        System.out.println(amplitudeEnvelope[1]);


//        System.out.println(signal.length);
//        Hilbert hilbert = new Hilbert(signal);
//        hilbert.hilbertTransform();
//        double[] signalEnvelope = hilbert.getAmplitudeEnvelope();
//        double[] instantaneousPhase = hilbert.getInstantaneousPhase();
//        System.out.println(Arrays.toString(signalEnvelope));
//        System.out.println(Arrays.toString(instantaneousPhase));

//        Wav objRead1 = new Wav();
//        objRead1.readWav(apt.getAudioFile().getAbsolutePath());
//        //[channel content][channel]
//        double[][] signal2d = objRead1.getData("double");
//        Hashtable<String, Long> propsOut = objRead1.getProperties();
//        Long sampleRate = propsOut.get("SampleRate");
//        double[] signal = new double[signal2d.length];
//
//        // we need one channel (channel 0)
//        for (int i = 0; i < signal2d.length; i++) {
//            signal[i] = signal2d[i][0];
//        }
//
//        // sqrt input yields the square of the original signal and sideband with the twice the carrier frequency
//        double signalSqr[] = new double[(signal.length)];
//        for (int i = 0; i < signal.length; i++) {
//            signalSqr[i] = Math.sqrt(signal[i]);
//        }
//
//        Wav objWrite = new Wav();
//        double[][] signalout = new double[signalSqr.length][1];
//
//        int i = 0;
//        for (double d : signalSqr) {
//            signalout[i][0] = d;
//            i++;
//        }
//
//        objWrite.putData(signalout, sampleRate, "double", new File("src/main/resources/out.wav").getPath()); // Can be "int", "long" or "double' depending on the co
    }

    private void makeImage(double[] amplitudeEnvelope) {
        // Digitalized data
        int[] data = new int[amplitudeEnvelope.length / 5];

        double amplitudeEnvelopeMinValue = (int) Arrays.stream(amplitudeEnvelope).min().getAsDouble();
        double amplitudeEnvelopeMaxValue = (int) Arrays.stream(amplitudeEnvelope).max().getAsDouble();
        for (int i = 2; i < data.length; i = i + 5) {
            int newValue = (int) mapOneRangeToAnother(amplitudeEnvelope[i], amplitudeEnvelopeMinValue, amplitudeEnvelopeMaxValue,
                    0, 255, 1);

            //Just in case
            if (newValue > 255)
                newValue = 255;
            if (newValue < 0)
                newValue = 0;

            data[i] = newValue;
        }

//        System.out.println(Arrays.toString(data));


//        double[] temp = new double[data.length];
//
//        int ij = 0;
//        for (int i : data) {
//            temp[ij] = i;
//            ij++;
//        }
//        System.out.println(signal.length);
//        System.out.println(temp.length);
//        plotSignals(signal, temp, signal.length);


        int width = apt.getLINE_LENGTH();
        int height = data.length / apt.getLINE_LENGTH();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int line = 0;
        for (int i = 0; i < data.length; i++) {
            if (i % apt.getLINE_LENGTH() == 0 && i != 0)
                line++;
//            try {
            bufferedImage.setRGB(i - (line * apt.getLINE_LENGTH()), line, new Color(data[i], data[i], data[i]).getRGB());

//            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
//                System.out.println("(" + (i - (line * apt.getLINE_LENGTH())) + "),(" + line + ")");
//                System.out.println(data[i]);
//                e.printStackTrace();
//                System.exit(1);
//            }
        }
        try {
            File file = new File("src/main/resources/out.png");
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveAudio(File outputFile) throws IOException {
        AudioFormat outFormat = new AudioFormat(apt.getAudioFormat().getEncoding(), apt.getAudioFormat().getSampleRate(), apt.getAudioFormat().getSampleSizeInBits(), 1, apt.getAudioFormat().getFrameSize() / 2, apt.getAudioFormat().getFrameRate(), apt.getAudioFormat().isBigEndian());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audio);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, outFormat, audio.length / outFormat.getFrameSize());
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);

    }

    public void plotSignal(@NotNull double[] signal, int samplesToPlot) {
        // There is prob a better way, but hey it works
        double[] sampleIter = new double[samplesToPlot];

        for (int j = 0; j < samplesToPlot; j++) {
            sampleIter[j] = j;
        }

        double[] sampleIter_cut = Arrays.copyOf(sampleIter, samplesToPlot);
        double[] signal_cut = Arrays.copyOf(signal, samplesToPlot);

        Plot plot = Plot.plot(Plot.plotOpts().
                title("Signal").
                legend(Plot.LegendFormat.BOTTOM)).
                xAxis("Samples", Plot.axisOpts().
                        range(0, samplesToPlot)).
                yAxis("Amplitude", Plot.axisOpts().
                        range(Arrays.stream(signal_cut).min().getAsDouble() - 20, Arrays.stream(signal_cut).max().getAsDouble() + 20)).
                series("Data", Plot.data().
                                xy(sampleIter_cut, signal_cut)
                        , null);

        try {
            plot.save("signal", "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void plotSignals(@NotNull double[] signal, @NotNull double[] signal2, int samplesToPlot) {
        if (samplesToPlot > signal.length || samplesToPlot > signal2.length)
            throw new IllegalArgumentException(samplesToPlot + " is too big");

        // There is prob a better way, but hey it works
        double[] sampleIter = new double[samplesToPlot];

        for (int j = 0; j < samplesToPlot; j++) {
            sampleIter[j] = j;
        }

        double[] sampleIter_cut = Arrays.copyOf(sampleIter, samplesToPlot);
        double[] signal_cut = Arrays.copyOf(signal, samplesToPlot);
        double[] signal2_cut = Arrays.copyOf(signal2, samplesToPlot);

        double min = Math.min(Arrays.stream(signal).min().getAsDouble(), Arrays.stream(signal2).max().getAsDouble());
        double max = Math.max(Arrays.stream(signal).max().getAsDouble(), Arrays.stream(signal2).max().getAsDouble());

        Plot plot2 = Plot.plot(Plot.plotOpts().
                title("Signals Comparison").
                legend(Plot.LegendFormat.BOTTOM)).
                xAxis("Samples", Plot.axisOpts().
                        range(0, samplesToPlot)).
                yAxis("Amplitude", Plot.axisOpts().
                        range(min - 20, max + 20)).
                series("Signal", Plot.data().
                                xy(sampleIter_cut, signal_cut),
                        Plot.seriesOpts().color(Color.BLUE)).
                series("Signal Envelope", Plot.data().
                                xy(sampleIter_cut, signal2_cut),
                        Plot.seriesOpts().color(Color.ORANGE));
        try {
            plot2.save("signals", "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
