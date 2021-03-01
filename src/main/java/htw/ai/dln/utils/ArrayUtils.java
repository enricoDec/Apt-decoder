package htw.ai.dln.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 01.03.21
 **/
public class ArrayUtils {

    // Example usage:
    //
    // int[] numbers = {1, 2, 3, 4, 5, 6, 7};
    // int[][] chunks = chunkArray(numbers, 3);
    //
    // chunks now contains [
    //                         [1, 2, 3],
    //                         [4, 5, 6],
    //                         [7]
    //                     ]

    /**
     * Splits an Array into N chunks.
     * <p>
     * Example: <p>
     * double[] numbers = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0}; <p>
     * double[][] chunks = chunkArray(numbers, 3); <p>
     * <p>
     * Chunks now contains:
     * [[1, 2, 3],
     * [4, 5, 6],
     * [7]]
     *
     * @param array     Original Array to be split
     * @param chunkSize Size of wanted chunks
     * @return original array split into N chunks as List
     */
    public static List<double[]> chunkArray(double[] array, int chunkSize) {
        if (chunkSize < 1)
            throw new IllegalArgumentException("Chunk size can't be negative or 0");
        int numOfChunks = (array.length + chunkSize - 1) / chunkSize;
        List<double[]> outputList = new ArrayList<>(numOfChunks);

        for (int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            double[] temp = new double[length];
            System.arraycopy(array, start, temp, 0, length);
            outputList.add(temp);
        }

        return outputList;
    }
}
