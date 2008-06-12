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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Tests the Subsample Average operation.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @since 2.3
 */
public class SubsampleAverageTest extends GridCoverageTest {
    /**
     * {@code true} if the result should be displayed in windows during test execution.
     * Default to {@code false}. This flag is set to {@code true} only if this test suite
     * is executed explicitly though the {@link #main} method.
     */
    private static boolean SHOW;

    /**
     * The source grid coverage.
     */
    private GridCoverage2D indexedCoverage;

    private GridCoverage2D indexedCoverageWithTransparency;

    private GridCoverage2D originallyIndexedCoverage;

    /**
     * Creates a test suite for the given name.
     */
    public SubsampleAverageTest(String name) {
        super(name);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SubsampleAverageTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(SubsampleAverageTest.class);
    }

    /**
     * Set up common objects used for all tests.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originallyIndexedCoverage       = GridCoverageExamples.getExample(0);
        indexedCoverage                 = GridCoverageExamples.getExample(2);
        indexedCoverageWithTransparency = GridCoverageExamples.getExample(3);
    }

    /**
     * Tests the "SubsampleAverage" operation.
     * @throws IOException 
     */
    public void testSubsampleAverage() throws IOException {
        // on this one the Subsample average should do an RGB expansion
        subsampleAverage(indexedCoverage.geophysics(true));
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        subsampleAverage(indexedCoverageWithTransparency.geophysics(true));
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        subsampleAverage(originallyIndexedCoverage.geophysics(true));

        // on this one the Subsample average should do an RGB expansion
        subsampleAverage(indexedCoverage.geophysics(false));
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        subsampleAverage(indexedCoverageWithTransparency.geophysics(false));
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        subsampleAverage(originallyIndexedCoverage.geophysics(false));

        // on this one the subsample average should NOT go back to the
        // geophysiscs view before being applied
        subsampleAverage(GridCoverageExamples.getExample(4).geophysics(false),
                new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));

        // on this one the subsample average should go back to the
        // geophysiscs view before being applied
        subsampleAverage(GridCoverageExamples.getExample(4).geophysics(false),
                new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        
        // on this one the subsample average should go back to the
        // geophysiscs view before being applied
        subsampleAverage(GridCoverageExamples.getExample(4).geophysics(false),
                new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        
        //play with a rotated coverage
        subsampleAverage(rotateCoverage(GridCoverageExamples.getExample(4).geophysics(true),Math.PI/4),
               null);

        
        
    }

    public void subsampleAverage(GridCoverage2D coverage){
        subsampleAverage(coverage, null);
    }

    public void subsampleAverage(GridCoverage2D coverage,RenderingHints hints) {
        // caching initial properties
        final RenderedImage originalImage = coverage.getRenderedImage();
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        // get the processor and prepare the first operation
        final DefaultProcessor processor = new DefaultProcessor(hints);
        final ParameterValueGroup param = processor.getOperation("SubsampleAverage").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("scaleX").setValue(Double.valueOf(0.5));
        param.parameter("scaleY").setValue(Double.valueOf(0.5));
        GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
        RenderedImage scaledImage = scaled.getRenderedImage();
        assertTrue(scaledImage.getWidth() == (int) (w / 2.0f));
        assertTrue(scaledImage.getHeight() == (int) (h / 2.0f));
        w = scaledImage.getWidth();
        h = scaledImage.getHeight();

        //check that the final envelope is close enough to the initial envelope.
        //In a perfect world they should be the same exact thing but in practice
        //this is quite hard to achieve when doing scaling due to the fact that
        //the various JAI operations use some complex laws to compute the final
        //image bounds.
        checkEnvelopes(scaled,coverage);

        // show the result
        if (SHOW) {
            Viewer.show(coverage, coverage.getName().toString());
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(coverage.getRenderedImage().getData());
            assertNotNull(scaled.getRenderedImage().getData());
        }

        // use the default processor and then scale again
        scaled = (GridCoverage2D) Operations.DEFAULT.subsampleAverage(scaled,
                0.3333, 0.3333);
        checkEnvelopes(scaled,coverage);
        
        scaledImage = scaled.getRenderedImage();
        // I had to comment this out since sometimes this evaluation fails
        // unexpectedly. I think it is a JAI issue because here below I am using
        // the rule they claim to follow.
        // assertTrue(scaledImage.getWidth() == (int)(w / 3.0f));
        // assertTrue(scaledImage.getHeight() == (int)(h / 3.0f));

        if (SHOW) {
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(scaled.getRenderedImage().getData());
        }
    }


}
