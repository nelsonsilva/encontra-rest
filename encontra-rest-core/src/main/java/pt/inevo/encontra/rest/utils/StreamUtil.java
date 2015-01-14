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

    public static File stream2file (InputStream in, String filename, Boolean save) throws IOException {
        int dotIndex = filename.lastIndexOf(".");
        String name = filename.substring(0,dotIndex);
        String extension = filename.substring(dotIndex);

        File file;
        if(!save) {
            file = File.createTempFile(name, extension);
            file.deleteOnExit();
        }
        else{
            file = new File("data/models/"+filename);
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }
        return file;
    }
}