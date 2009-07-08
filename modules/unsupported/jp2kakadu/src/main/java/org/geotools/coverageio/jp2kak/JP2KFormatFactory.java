/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverageio.jp2kak;

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.util.KakaduUtilities;

import java.awt.RenderingHints;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.opengis.coverage.grid.Format;


/**
 * Implementation of the {@link Format} service provider interface for JP2K
 * files.
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
@SuppressWarnings("deprecation")
public final class JP2KFormatFactory implements GridFormatFactorySpi {
    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.jp2kak");
    
    static{
    	try{
    		new ParameterBlockJAI("ImageReadMT");
    	} catch (final Exception e){
    		try{
    			ImageReadDescriptorMT.register(JAI.getDefaultInstance());
    		} catch (final Exception e1){
    			//TODO: Log a message
    		}
    	}
    	
    	try{
			//check if our jp2k plugin is in the path
			final String kakaduJp2Name=it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi.class.getName();
			Class.forName(kakaduJp2Name);

			// imageio jp2k reader
			final String standardJp2Name=com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi.class.getName();
			final String jp2CodecLibName=com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLibSpi.class.getName();
			
			ImageIOUtilities.replaceProvider(ImageReaderSpi.class, kakaduJp2Name, standardJp2Name, "JPEG2000");
			ImageIOUtilities.replaceProvider(ImageReaderSpi.class, kakaduJp2Name, jp2CodecLibName, "JPEG2000");
	        
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE,"Unable to load specific JP2K reader spi",e);
		} 
    }

    /**
     * Tells me if the coverage plugin to access JP2K is available or not.
     *
     * @return <code>true</code> if the plugin is available, <code>false</code> otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;

        // if these classes are here, then the runtime environment has
        // access to JAI and the JAI ImageI/O toolbox.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
            Class.forName("it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi");
            available = KakaduUtilities.isKakaduAvailable();

            if (LOGGER.isLoggable(Level.FINE)) {
                if (available) {
                    LOGGER.fine("JP2KFormatFactory is availaible.");
                } else {
                    LOGGER.fine("JP2KFormatFactory is not availaible.");
                }
            }
        } catch (ClassNotFoundException cnf) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("JP2KFormatFactory is not availaible.");
            }

            available = false;
        }

        return available;
    }

    /**
     * Creating a {@link JP2KFormat}.
     *
     * @return A {@link JP2KFormat}.;
     */
    public JP2KFormat createFormat() {
        return new JP2KFormat();
    }

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     *
     * @return An empty map.
     */
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
