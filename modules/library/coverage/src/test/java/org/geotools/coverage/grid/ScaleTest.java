/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.grid;

// J2SE and JAI dependencies
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;


// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;

// Geotools dependencies
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;


/**
 * Tests the scale operation.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @since 2.3
 */
public class ScaleTest extends GridCoverageTest {
    /**
     * {@code true} if the result should be displayed in windows during test execution.
     * Default to {@code false}. This flag is set to {@code true} only if this test suite
     * is executed explicitly though the {@link #main} method.
     */
    private static boolean SHOW;

    private GridCoverage2D originallyIndexedCoverage;

    private GridCoverage2D indexedCoverage;

    private GridCoverage2D indexedCoverageWithTransparency;

    private GridCoverage2D floatCoverage;

    /**
     * Creates a test suite for the given name.
     */
    public ScaleTest(String name) {
        super(name);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScaleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        originallyIndexedCoverage       = GridCoverageExamples.getExample(0);
        indexedCoverage                 = GridCoverageExamples.getExample(2);
        indexedCoverageWithTransparency = GridCoverageExamples.getExample(3);
        floatCoverage                   = GridCoverageExamples.getExample(4);
    }

    /**
     * Tests the "Scale" operation.
     * 
     * @throws IOException
     */

    public void testScale() throws IOException {
        ///////////////////////////////////////////////////////////////////////
        //
        // Nearest neighbor interpolation and non-geo view 
        //
        ///////////////////////////////////////////////////////////////////////
        scale(originallyIndexedCoverage.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverage.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverageWithTransparency.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));

        ///////////////////////////////////////////////////////////////////////
        //
        // Nearest neighbor interpolation and geo view 
        //
        ///////////////////////////////////////////////////////////////////////
        scale(originallyIndexedCoverage.geophysics(true), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverage.geophysics(true), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverageWithTransparency.geophysics(true), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));

        ///////////////////////////////////////////////////////////////////////
        //
        // Bilinear interpolation and non-geo view 
        //
        ///////////////////////////////////////////////////////////////////////
        scale(originallyIndexedCoverage.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverage.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverageWithTransparency.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));

        ///////////////////////////////////////////////////////////////////////
        //
        // Bilinear interpolation and geo view 
        //
        ///////////////////////////////////////////////////////////////////////
        scale(originallyIndexedCoverage.geophysics(true), new InterpolationBilinear(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverage.geophysics(true), new InterpolationBilinear(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        scale(indexedCoverageWithTransparency.geophysics(true), new InterpolationBilinear(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));

        ///////////////////////////////////////////////////////////////////////
        //
        // Bilinear interpolation and non-geo view for a float coverage
        //
        ///////////////////////////////////////////////////////////////////////
        // on this one the subsample average should NOT go back to the
        // geophysiscs view before being applied
        scale(floatCoverage.geophysics(false), new InterpolationBilinear(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));


        ///////////////////////////////////////////////////////////////////////
        //
        // Nearest neighbor  interpolation and non-geo view for a float coverage
        //
        ///////////////////////////////////////////////////////////////////////
        // on this one the subsample average should NOT go back to the
        // geophysiscs
        // view before being applied
        scale(floatCoverage.geophysics(false), new InterpolationNearest(),
                new RenderingHints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        
        
        //play with a rotated coverage
        scale(rotateCoverage(GridCoverageExamples.getExample(4).geophysics(true),Math.PI/4),
               null);
    }

    public void scale(GridCoverage2D coverage, Interpolation interp) {
            scale(coverage, interp, null);
    }

    public void scale(GridCoverage2D coverage, Interpolation interp, RenderingHints hints) {
        // caching initial properties
        RenderedImage originalImage = coverage.getRenderedImage();
        final int w = originalImage.getWidth();
        final int h = originalImage.getHeight();

        // creating a default processor
        final DefaultProcessor processor = new DefaultProcessor(hints);
        // getting parameters for doing a scale
        final ParameterValueGroup param = processor.getOperation("Scale").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("xScale").setValue(Float.valueOf(0.5f));
        param.parameter("yScale").setValue(Float.valueOf(0.5f));
        param.parameter("xTrans").setValue(Float.valueOf(0.0f));
        param.parameter("yTrans").setValue(Float.valueOf(0.0f));
        param.parameter("Interpolation").setValue(interp);
        param.parameter("BorderExtender").setValue(
                BorderExtenderCopy.createInstance(BorderExtender.BORDER_COPY));
        // doing a first scale
        GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
        checkEnvelopes(coverage, scaled);
        RenderedImage scaledImage = scaled.getRenderedImage();
        assertTrue(scaledImage.getWidth() == w / 2.0f);
        assertTrue(scaledImage.getHeight() == h / 2.0f);

        if (SHOW) {
            Viewer.show(coverage, coverage.getName().toString());
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(coverage.getRenderedImage().getData());
            assertNotNull(scaledImage.getData());
        }

        // doing another scale using the Default processor
        scaled = (GridCoverage2D) Operations.DEFAULT.scale(scaled, 3, 3, 0.0,
                0.0, interp, BorderExtender.createInstance(BorderExtender.BORDER_COPY));
        scaledImage = scaled.getRenderedImage();
        checkEnvelopes(coverage, scaled);
        assertTrue(scaledImage.getWidth()  == 3 * w / 2.0f);
        assertTrue(scaledImage.getHeight() == 3 * h / 2.0f);

        if (SHOW) {
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(scaledImage.getData());
        }
    }
}
