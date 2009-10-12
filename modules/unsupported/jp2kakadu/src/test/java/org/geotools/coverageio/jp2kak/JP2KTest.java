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
 *
 */
package org.geotools.coverageio.jp2kak;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Testing {@link JP2KReader}
 *
 * @source $URL$
 */
public final class JP2KTest extends Assert {
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(JP2KTest.class);
	/**
	 * The {@code GridFormatFactorySpi} provided by the specific subclass to
	 * handle a specific format.
	 */
	private final static JP2KFormatFactory factorySpi  = new JP2KFormatFactory();
	
	/**
	 * A String containing the name of the supported format. It will be used to
	 * customize the messages.
	 */
	private static final String supportedFormat="JP2K";;
    
    /**
     * Creates a new instance of JP2KTest
     *
     * @param name
     */
    public JP2KTest() {
    }

    @Test
    public void test() throws Exception {
        if (!testingEnabled()) {
            return;
        }

        File file = null;
        try{
            file = TestData.file(this, "bogota.jp2");
        }catch (FileNotFoundException fnfe){
            LOGGER.warning("test-data not found: bogota.jp2 \nTests are skipped");
            return;
        }

        final JP2KFormat format= factorySpi.createFormat();
        assertTrue(format.accepts(file));
        
        final AbstractGridCoverage2DReader reader = new JP2KReader(file);
        final ParameterValue<GridGeometry2D> gg = JP2KFormat.READ_GRIDGEOMETRY2D.createValue();
        final ParameterValue<Boolean> useMT = JP2KFormat.USE_MULTITHREADING.createValue();
        final ParameterValue<Boolean> useJAI = JP2KFormat.USE_JAI_IMAGEREAD.createValue();
        useMT.setValue(true);
        useJAI.setValue(true);
        final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
        gg.setValue(new GridGeometry2D(reader.getOriginalGridRange(), oldEnvelope));
        final GridCoverage2D gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg,useJAI,useMT });
        assertNotNull(gc);

        if (TestData.isInteractiveTest()) {
        	 // printing CRS information
            LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
            LOGGER.info(gc.getEnvelope().toString());
            gc.show();
        } else {
            PlanarImage.wrapRenderedImage(gc.getRenderedImage()).getTiles();
        }
       
        assertEquals(gc.getRenderedImage().getWidth(), 512);
        assertEquals(gc.getRenderedImage().getHeight(), 512);
        assertTrue(CRS.equalsIgnoreMetadata(gc.getCoordinateReferenceSystem(), CRS.parseWKT("PROJCS[\"Bogota 1975 / Colombia Bogota zone\", GEOGCS[\"Bogota 1975\",DATUM[\"Bogota 1975\", SPHEROID[\"International 1924\", 6378388.0, 297.0, AUTHORITY[\"EPSG\",\"7022\"]], TOWGS84[304.5, 306.5, -318.1, 0.0, 0.0, 0.0, 0.0],AUTHORITY[\"EPSG\",\"6218\"]],PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\", 0.017453292519943295],AXIS[\"Geodetic longitude\", EAST],AXIS[\"Geodetic latitude\", NORTH],AUTHORITY[\"EPSG\",\"4218\"]],PROJECTION[\"Transverse Mercator\", AUTHORITY[\"EPSG\",\"9807\"]],PARAMETER[\"central_meridian\", -74.08091666666668],PARAMETER[\"latitude_of_origin\", 4.599047222222223],PARAMETER[\"scale_factor\", 1.0],PARAMETER[\"false_easting\", 1000000.0],PARAMETER[\"false_northing\", 1000000.0],UNIT[\"m\", 1.0],AXIS[\"Easting\", EAST],AXIS[\"Northing\", NORTH],AUTHORITY[\"EPSG\",\"21892\"]]"))); 
    }

	public void setUp() throws Exception {
	    ImageIO.setUseCache(false);
	    JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
	            10 * 1024 * 1024);
	    JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
	    JAI.getDefaultInstance().getTileScheduler().setParallelism(2);
	    JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(2);
	    JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
	    JAI.getDefaultInstance().getTileScheduler().setPriority(5);
	}

	static boolean testingEnabled() {
	    boolean available = factorySpi.isAvailable();
	
	    if (!available) {
	        LOGGER.warning(supportedFormat
	                + " libraries are not available, skipping tests!");
	    }
	
	    return available;
	}
}
