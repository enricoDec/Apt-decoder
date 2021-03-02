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
     * https://www.geeksforgeeks.org/program-find-correlation-coefficient/
     *
     * @param x      array x
     * @param y      array y
     * @param length array length
     * @return value between -1 and 1
     * - 1 X and Y are negatively correlated
     * + 1 X and Y are positively correlated
     */
    public static double correlationCoefficient(int[] x, int[] y, int length) {

        int sum_X = 0, sum_Y = 0, sum_XY = 0;
        int squareSum_X = 0, squareSum_Y = 0;

        for (int i = 0; i < length; i++) {
            // sum of elements of array X.
            sum_X = sum_X + x[i];

            // sum of elements of array Y.
            sum_Y = sum_Y + y[i];

            // sum of X[i] * Y[i].
            sum_XY = sum_XY + x[i] * y[i];

            // sum of square of array elements.
            squareSum_X = squareSum_X + x[i] * x[i];
            squareSum_Y = squareSum_Y + y[i] * y[i];
        }

        // use formula for calculating correlation
        // coefficient.
        double corr = (length * sum_XY - sum_X * sum_Y) /
                (Math.sqrt((length * squareSum_X -
                        sum_X * sum_X) * (length * squareSum_Y -
                        sum_Y * sum_Y)));

        return corr;
    }

    public static double correlation(@NotNull int[] xs, @NotNull int[] ys) {
        if (xs.length != ys.length)
            throw new IllegalArgumentException("Arrays not of the same length");

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = xs.length;

        for (int i = 0; i < n; ++i) {
            double x = xs[i];
            double y = ys[i];

            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
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

    public static double[] crudeLowPass(double[] signal, double frequency, double frequencyCut) {
        //Low pass filter (kinda)
        int N = signal.length;

        double f_c = frequencyCut;
        double f_s = frequency;

        double k = f_c / f_s;
        int index = (int) (k * N);

        //Low pass filter
        for (int i = index; index < N; index++)
            signal[i] = 0;

        return signal;
    }
}
