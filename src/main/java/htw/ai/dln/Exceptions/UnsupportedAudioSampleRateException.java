package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 * <p>
 * Unsupported Audio Sample Rate
 * Thrown if the Audio Sample rate is not supported by the method
 */
public class UnsupportedAudioSampleRateException extends Exception {
    public UnsupportedAudioSampleRateException(String errorMessage) {
        super(errorMessage);
    }
}
