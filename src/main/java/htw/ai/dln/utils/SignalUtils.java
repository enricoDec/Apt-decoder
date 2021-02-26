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
     * @throws IllegalArgumentException if samplesToPlot is > signal.length
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
     * @throws IllegalArgumentException if samplesToPlot is > signal.length
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
}
