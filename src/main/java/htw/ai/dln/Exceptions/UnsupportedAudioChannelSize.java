package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public class UnsupportedAudioChannelSize extends Exception{

    public UnsupportedAudioChannelSize(String message) {
        super(message);
    }
}
