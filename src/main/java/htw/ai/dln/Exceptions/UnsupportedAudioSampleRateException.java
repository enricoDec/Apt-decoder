package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/

/**
 * Unsupported Audio Sample Rate
 */
public class UnsupportedAudioSampleRateException extends Exception {

    public UnsupportedAudioSampleRateException(String errorMessage) {
        super(errorMessage);
    }
}
