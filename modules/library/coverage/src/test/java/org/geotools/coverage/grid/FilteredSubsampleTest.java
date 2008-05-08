/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.grid;

// J2SE and JAI dependencies
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;

// Geotools dependencies
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.geotools.factory.Hints;
import org.geotools.resources.coverage.CoverageUtilities;


/**
 * Tests the "filtered subsample" operation.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @since 2.3
 */
public class FilteredSubsampleTest extends GridCoverageTest {
    /**
     * {@code true} if the result should be displayed in windows during test execution.
     * Default to {@code false}. This flag is set to {@code true} only if this test suite
     * is executed explicitly though the {@link #main} method.
     */
    private static boolean SHOW;

    private GridCoverage2D originallyIndexedCoverage;

    private GridCoverage2D indexedCoverage;

    private GridCoverage2D indexedCoverageWithTransparency;

    /**
     * Creates a test suite for the given name.
     */
    public FilteredSubsampleTest(String name) {
        super(name);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FilteredSubsampleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(FilteredSubsampleTest.class);
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
     * Tests the "Scale" operation.
     * @throws IOException 
     */
    public void testSubsampleAverage() throws IOException {
        // on this one the Subsample average should do an RGB expansion
        filteredSubsample(indexedCoverage.geophysics(true),
                        new InterpolationNearest(),
                        new float[]{1});
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        filteredSubsample(indexedCoverageWithTransparency.geophysics(true),
                        new InterpolationNearest(),
                        new float[]{1});
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        filteredSubsample(originallyIndexedCoverage.geophysics(false),
                        new InterpolationNearest(),
                        new float[]{1});

        // on this one the Subsample average should do an RGB expansion
        filteredSubsample(indexedCoverage.geophysics(true),
                        new InterpolationBilinear(),
                        new float[]{1});
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        filteredSubsample(indexedCoverageWithTransparency.geophysics(true),
                        new InterpolationBilinear(),
                        new float[]{1});
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        filteredSubsample(originallyIndexedCoverage.geophysics(false),
                        new InterpolationBilinear(),
                        new float[]{1});

        // on this one the Subsample average should do an RGB expansion
        filteredSubsample(indexedCoverage.geophysics(true),
                        new InterpolationNearest(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                  0.0F, -1.0F / 12.0F });
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        filteredSubsample(indexedCoverageWithTransparency.geophysics(true),
                        new InterpolationNearest(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                  0.0F, -1.0F / 12.0F });
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        filteredSubsample(originallyIndexedCoverage.geophysics(false),
                        new InterpolationNearest(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                  0.0F, -1.0F / 12.0F });

        // on this one the Subsample average should do an RGB expansion
        filteredSubsample(indexedCoverage.geophysics(true),
                        new InterpolationBilinear(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                   0.0F, -1.0F / 12.0F });
        // on this one the Subsample average should do an RGB expansion
        // preserving alpha
        filteredSubsample(indexedCoverageWithTransparency.geophysics(true),
                        new InterpolationBilinear(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                   0.0F, -1.0F / 12.0F });
        // on this one the subsample average should go back to the geophysiscs
        // view before being applied
        filteredSubsample(originallyIndexedCoverage.geophysics(false),
                        new InterpolationBilinear(), new float[] { 0.5F,  1.0F /  3.0F,
                                                                   0.0F, -1.0F / 12.0F });

        // on this one the subsample average should go back to the
        // geophysiscs
        // view before being applied
        filteredSubsample(GridCoverageExamples.getExample(4).geophysics(false),
                new InterpolationBilinear(),
                new float[]{1},
                new Hints(Hints.REPLACE_NON_GEOPHYSICS_VIEW, Boolean.FALSE));
        
        
        
        //play with a rotated coverage
        filteredSubsample(rotateCoverage(GridCoverageExamples.getExample(4).geophysics(true),Math.PI/4),
        		new InterpolationBilinear(), new float[] { 0.5F,  1.0F /  3.0F,
            0.0F, -1.0F / 12.0F }, null);
    }

    public void filteredSubsample(GridCoverage2D coverage, Interpolation interp, float[] filter) {
        filteredSubsample(coverage, interp, filter, null);
    }

    /**
     * Tests the "FilteredSubsamble" operation.
     */
    public void filteredSubsample(GridCoverage2D coverage,
            Interpolation interp, float[] filter, Hints hints)
    {
        // caching initial properties
        RenderedImage originalImage = coverage.getRenderedImage();
        boolean isIndexed = originalImage.getColorModel() instanceof IndexColorModel;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        // creating a default processor
        final DefaultProcessor processor = new DefaultProcessor(hints);
        // getting parameters for the FilteredSubsample operation
        final ParameterValueGroup param = processor.getOperation("FilteredSubsample").getParameters();
        param.parameter("Source").setValue(coverage);
        param.parameter("scaleX").setValue(Integer.valueOf(2));
        param.parameter("scaleY").setValue(Integer.valueOf(2));
        param.parameter("qsFilterArray").setValue(filter);
        param.parameter("Interpolation").setValue(interp);
        // scale a first time by 2
        GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
        RenderedImage scaledImage = scaled.getRenderedImage();
        assertTrue(scaledImage.getWidth()  == Math.round(w / 2.0f));
        assertTrue(scaledImage.getHeight() == Math.round(h / 2.0f));
        assertTrue(!isIndexed
                || (interp instanceof InterpolationNearest)
                || !(scaledImage.getColorModel() instanceof IndexColorModel)
                || CoverageUtilities.preferredViewForOperation(coverage, interp, false, null) == ViewType.GEOPHYSICS);
        isIndexed = scaledImage.getColorModel() instanceof IndexColorModel;
        w = scaledImage.getWidth();
        h = scaledImage.getHeight();
        checkEnvelopes(coverage, scaled);

        if (SHOW) {
            Viewer.show(coverage, coverage.getName().toString());
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(coverage.getRenderedImage().getData());
            assertNotNull(scaled.getRenderedImage().getData());
        }

        // scale a second time by 3
        scaled = (GridCoverage2D) Operations.DEFAULT.filteredSubsample(scaled,
                3, 3, filter, interp);
        scaledImage = scaled.getRenderedImage();
        assertTrue(scaledImage.getWidth() == Math.round(w / 3.0f));
        assertTrue(scaledImage.getHeight() == Math.round(h / 3.0f));
        assertTrue(!isIndexed
                || (interp instanceof InterpolationNearest)
                || !(scaledImage.getColorModel() instanceof IndexColorModel)
                ||CoverageUtilities.preferredViewForOperation(coverage, interp, false, null) == ViewType.GEOPHYSICS);
        checkEnvelopes(coverage, scaled);
        if (SHOW) {
            Viewer.show(scaled, scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(scaledImage.getData());
        }
    }
}
