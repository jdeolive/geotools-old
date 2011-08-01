/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geotools.process.raster.gs;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.process.raster.BaseCoverageAlgebraProcess;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.process.ProcessException;
import org.opengis.coverage.processing.Operation;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.ProgressListener;

/**
 * Multiply two coverages together (pixel by pixel).
 * Output pixel[i][j] = source0CoveragePixel[i][j] * source1CoveragePixel[i][j]   
 *  
 * The two coverages need to have the same envelope and same resolution (same gridGeometry).
 * 
 * @author Daniele Romagnoli - GeoSolutions
 */
@DescribeProcess(title = "multiplyCoverages", description = "Multiply a coverage A by a coverage B. \n" +
        "The two coverages need to have the same envelope and the same resolution. \n" +
        "The operation will do pixel by pixel multiplication:\n " +
        "outputCoveragePixel[i][j] = sourceCoverageAPixel[i][j] * sourceCoverageBPixel[i][j]")
public class MultiplyCoveragesProcess implements GSProcess {

    private static final CoverageProcessor PROCESSOR = CoverageProcessor.getInstance();
    private static final Operation MULTIPLY = PROCESSOR.getOperation("Multiply");

    @DescribeResult(name = "result", description = "The resulting coverage")
    public GridCoverage2D execute(
            @DescribeParameter(name = "coverageA", description = "The first coverage") GridCoverage2D coverageA,
            @DescribeParameter(name = "coverageB", description = "The second coverage") GridCoverage2D coverageB,
            ProgressListener progressListener) throws ProcessException {
        
        // //
        //
        // Initialization: compatibility checks
        //
        // //
        BaseCoverageAlgebraProcess.checkCompatibleCoverages(coverageA, coverageB);

        // //
        //
        // Doing the Operation
        //
        // //
        final ParameterValueGroup param = MULTIPLY.getParameters();
        param.parameter("Source0").setValue(coverageA);
        param.parameter("Source1").setValue(coverageB);
        return (GridCoverage2D) PROCESSOR.doOperation(param);
    }

}
