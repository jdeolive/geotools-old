package org.geotools.demos.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A simple file filter implementation that allow to specify one or more file
 * extensions
 *
 * @author wolf
 */
public class SimpleFileFilter extends FileFilter implements java.io.FileFilter {
    String[] extensions;
    String description;

    public SimpleFileFilter(String extension, String description) {
        this.extensions = new String[] { extension };
        this.description = description;
    }

    public SimpleFileFilter(String[] extensions, String description) {
        this.extensions = extensions;
        this.description = description;
    }

    /**
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileUtils.getFileExtension(f);

        for (int i = 0; i < extensions.length; i++) {
            if (extension.equals(extensions[i])) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return description;
    }
}
