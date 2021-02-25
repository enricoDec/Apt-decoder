package htw.ai.dln.utils;

import java.nio.ByteBuffer;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 22.02.21
 **/
public class ByteArrayUtils {

    public static double[] toDoubleArray(byte[] bytes) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[bytes.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(bytes, i * times, times).getDouble();
        }
        return doubles;
    }

    public static byte[] toByteArray(double[] doubles) {
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubles.length * times];
        for (int i = 0; i < doubles.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putDouble(doubles[i]);
        }
        return bytes;
    }
}
