package org.geotools.data.coverage;

import java.util.Collection;

import org.opengis.coverage.Coverage;

/**
 * @author     Simone Giannecchini, GeoSolutions 	
 */
public interface CoverageWriteRequest extends CoverageRequest{
	
	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setAdditionalMetadata(Object metadata);
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Object getAdditionalMetadata();
	
	
	
	/**
	 * @param  metadata
	 * @uml.property  name="metadata"
	 */
	public void setData(Collection<? extends Coverage> data);
	
	/**
	 * @return
	 * @uml.property  name="metadata"
	 */
	public Collection<? extends Coverage> getData();
	
}
