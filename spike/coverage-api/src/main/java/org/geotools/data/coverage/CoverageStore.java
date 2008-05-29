package org.geotools.data.coverage;


public interface CoverageStore extends CoverageSource {
	public CoverageResponse addCoverage(CoverageWriteRequest writeRequest);
	public CoverageResponse modifyCoverage(CoverageWriteRequest writeRequest);
	public CoverageResponse removeCoverage(CoverageWriteRequest writeRequest);
	
}
