package org.geotools.demos.gui;

import java.io.File;


/**
 * Utility class that contains some useful function related
 * to file management
 *
 * @author wolf
 */
public class FileUtils {
    private FileUtils() {
    }

    public static String getFileExtension(File f) {
        String fileName = f.getName();
        int extensionStart = fileName.lastIndexOf('.');
        String extension = "";

        if (extensionStart >= 0) {
            extension = fileName.substring(extensionStart + 1);
        }

        return extension;
    }
}
