package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 28.02.21
 * <p>
 * Unsupported Frame Size Exception
 * Thrown if the size of the Audio frame is not supported
 */
public class UnsupportedFrameSizeException extends Exception {
    public UnsupportedFrameSizeException(String message) {
        super(message);
    }
}
