package htw.ai.dln;

import htw.ai.dln.Exceptions.NoSyncFrameFoundException;
import htw.ai.dln.Exceptions.UnsupportedFrameSizeException;

import java.io.File;

/**
 * @author : Enrico Gamil Toros
 * Project name : apt-decoder
 * @version : 1.0
 * @since : 21.02.21
 **/
public interface IAptDecoder {

    int[] decode() throws UnsupportedFrameSizeException;

    int[] syncFrames(int[] digitalized) throws NoSyncFrameFoundException;

    void saveImage(int[] data, File saveLocation);
}
