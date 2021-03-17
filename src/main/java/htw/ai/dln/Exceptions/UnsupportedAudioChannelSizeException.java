package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 * <p>
 * Unsupported Audio Channel Size Exception
 * Thrown if the size of the audio channel is not supported
 * For example if the method only supports or expects audio with 1 channel but got audio with 2 channels,
 * this exception should be thrown
 */
public class UnsupportedAudioChannelSizeException extends Exception {
    public UnsupportedAudioChannelSizeException(String message) {
        super(message);
    }
}
