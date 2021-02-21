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
public class UnsupportedAudioSampleRate extends Exception {

    public UnsupportedAudioSampleRate(String errorMessage) {
        super(errorMessage);
    }
}
