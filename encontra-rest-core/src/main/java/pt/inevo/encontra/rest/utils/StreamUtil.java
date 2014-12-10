package pt.inevo.encontra.rest.utils;

/**
 * Created by jpvguerreiro on 12/5/2014.
 * Obtained from: http://stackoverflow.com/questions/4317035/how-to-convert-inputstream-to-virtual-file
 */
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    public static final String PREFIX = "stream2file";
    public static final String SUFFIX = ".tmp";

    public static File stream2file (InputStream in, String extension) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, extension);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

}