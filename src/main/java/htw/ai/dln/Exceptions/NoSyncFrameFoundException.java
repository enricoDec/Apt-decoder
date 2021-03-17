package htw.ai.dln.Exceptions;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 28.02.21
 * <p>
 * No Sync Frame Found Exception
 * Thrown if synced APT was requested, but no sync line was found
 */
public class NoSyncFrameFoundException extends Exception {
    public NoSyncFrameFoundException(String message) {
        super(message);
    }
}
