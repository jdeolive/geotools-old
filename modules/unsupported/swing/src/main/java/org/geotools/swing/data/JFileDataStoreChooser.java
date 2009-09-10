/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.swing.data;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.FileDataStoreFactorySpi;

/**
 * A file chooser dialog to get user choices for data stores.
 * <p>
 * Examples of use:
 * <pre>{@code
 * // prompt the user for a shapefile
 * File file = JFileDataStoreChooser.showOpenFile("shp", parentFrame);
 * if (file != null) {
 *    ...
 * }
 *
 * // prompt the user for a given data format
 *
 * }</pre>
 *
 * @author Jody Garnett
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class JFileDataStoreChooser extends JFileChooser {

    /**
     * Create a dialog that filters for files with the specified extension.
     * Normally client code will not need to call this
     * directly but will use the static method:
     * {@linkplain #showOpenFile(java.lang.String, java.awt.Component) }
     * @param extension the file extension, with or without the leading '.'
     */
    public JFileDataStoreChooser(final String extension) {
        final String lowerExt;

        if (extension.startsWith(".")) {
            lowerExt = extension.toLowerCase();
        } else {
            lowerExt = "." + extension.toLowerCase();
        }

        setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getPath().endsWith(lowerExt) ||
                        f.getPath().endsWith(lowerExt.toUpperCase());
            }

            public String getDescription() {
                if (".shp".equals(lowerExt)) {
                    return "Shapefiles";

                } else {
                    return lowerExt + " files";
                }
            }
        });
    }

    /**
     * Creates a dialog that filters for files matching the specified
     * data format. Normally client code will not need to call this
     * directly but will use the static method:
     * {@linkplain #showOpenFile(org.geotools.data.FileDataStoreFactorySpi, java.awt.Component)}
     *
     * @param format data file format
     */
    public JFileDataStoreChooser(final FileDataStoreFactorySpi format) {

        setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                for (String ext : format.getFileExtensions()) {
                    if (f.getPath().endsWith(ext)) {
                        return true;
                    }
                    if (f.getPath().endsWith(ext.toUpperCase())) {
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return format.getDescription();
            }
        });
    }

    /**
     * Show a file open dialog that filters for files with the given extension.
     *
     * @param extension file extension, with or without leading '.'
     * @param parent parent GUI component (may be null)
     *
     * @return the selected file or null if the user cancelled the selection
     * @throws java.awt.HeadlessException if run in an unsupported environment
     */
    public static File showOpenFile(String extension, Component parent) throws HeadlessException {
        JFileDataStoreChooser dialog = new JFileDataStoreChooser(extension);
        
        if (dialog.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return dialog.getSelectedFile();
        }
        
        return null;
    }

    /**
     * Show a file open dialog that filters for files that match a given file
     * data store format
     *
     * @param format the file data store format
     * @param parent parent GUI component (may be null)
     *
     * @return the selected file or null if the user cancelled the selection
     * @throws java.awt.HeadlessException if run in an unsupported environment
     */
    public static File showOpenFile(FileDataStoreFactorySpi format, Component parent) throws HeadlessException {
        JFileDataStoreChooser dialog = new JFileDataStoreChooser(format);

        if (dialog.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return dialog.getSelectedFile();
        }

        return null;
    }

    /**
     * Deomonstrates the file data store dialog by prompting for a shapefile
     *
     * @param arg ignored
     */
    public static void main(String arg[]) {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file != null) {
            JOptionPane.showMessageDialog(null, "Selected " + file.getPath());
        } else {
            JOptionPane.showMessageDialog(null, "Selection cancelled");
        }
    }
}
