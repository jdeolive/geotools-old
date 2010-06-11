package org.geotools.process.dem;

import org.geotools.coverage.grid.GridCoverage2D;

/**
 * This utility class bundles up DEM tools for reuse.
 * <p>
 * This class has been marked with annotations describing extra information required for use via the
 * dynamic ProcessFactory API. For more details please see DEMProcessFactory.
 * 
 * @author Jody
 */
public class DEMTools {

    @DescribeProcess(description = "Convert from a DEM to a slope")
    @DescribeResult(name = "slope", type = GridCoverage2D.class, description = "Slope band one is gradient, band two is direction")
    public GridCoverage2D slope(
            @DescribeParameter( name="DEM", description = "Digital Elevation model") GridCoverage2D DEM) {        
        return DEM;
    }

}
