/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * ImageFilter.java
 *
 * Created on 8 dicembre 2003, 16.21
 */
package org.geotools.gui.swing.sldeditor.util;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.swing.filechooser.FileFilter;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class ImageFilter extends FileFilter {
    private static final Set suffixes = getSuffixes();

    /**
     * Creates a new instance of ImageFilter
     */
    public ImageFilter() {
    }

    private static Set getSuffixes() {
        Set suffixes = new HashSet();
        Iterator it = IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class,
                false);
        while (it.hasNext()) {
            ImageReaderSpi spi = (ImageReaderSpi) it.next();
            String[] spiSuffixes = spi.getFileSuffixes();

            for (int i = 0; i < spiSuffixes.length; i++) {
                suffixes.add(spiSuffixes[i]);
            }
        }

        return suffixes;
    }

    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }

        String extension = getExtension(pathname);
        if (extension == null) {
            return false;
        } else {
            return suffixes.contains(extension.toLowerCase());
        }
    }

    public String getDescription() {
        return "All supported images";
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if ((i > 0) && (i < (s.length() - 1))) {
            ext = s.substring(i + 1).toLowerCase();
        }

        return ext;
    }
}
