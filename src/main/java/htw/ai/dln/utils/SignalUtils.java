package htw.ai.dln.utils;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 26.02.21
 **/
public class SignalUtils {

    /**
     * Plot a signal to a graph
     * Is saved at project source root
     *
     * @param signal        Signal to plot, can't be null
     * @param samplesToPlot number of samples to plot
     * @throws IllegalArgumentException if samplesToPlot is &gt; signal.length
     */
    public static void plotSignal(@NotNull double[] signal, int samplesToPlot) {
        if (samplesToPlot > signal.length)
            throw new IllegalArgumentException(samplesToPlot + " is too big");

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


    /**
     * Plots two signals (1 in blue, 2 in orange)
     * Is saved at project source root
     *
     * @param signal        Signal 1
     * @param signal2       Signal 2
     * @param samplesToPlot number of samples to plot
     * @throws IllegalArgumentException if samplesToPlot is &gt; signal.length
     */
    public static void plotSignals(@NotNull double[] signal, @NotNull double[] signal2, int samplesToPlot) {
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

    /**
     * Function that returns correlation coefficient.
     *
     * @param x array x, must have same length as y
     * @param y array y, must have same length as x
     * @return value between -1 and 1
     * - 1 X and Y are negatively correlated
     * + 1 X and Y are positively correlated
     */
    public static double correlation(@NotNull int[] x, @NotNull int[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException("Arrays not of the same length");

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = x.length;

        for (int i = 0; i < n; ++i) {
            double x2 = x[i];
            double y2 = y[i];

            sx += x2;
            sy += y2;
            sxx += x2 * x2;
            syy += y2 * y2;
            sxy += x2 * y2;
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }

    /**
     * Low Pass Filter.
     * Needs improvements
     *
     * @param signal       Signal
     * @param frequency    Frequency
     * @param frequencyCut Frequency to cut
     * @return Low Pass Filtered Signal
     */
    public static double[] crudeLowPass(double[] signal, double frequency, double frequencyCut) {
        //Low pass filter (kinda)
        int N = signal.length;

        double k = frequencyCut / frequency;
        int index = (int) (k * N);

        //Low pass filter
        for (int i = index; index < N; index++)
            signal[i] = 0;

        return signal;
    }
}
