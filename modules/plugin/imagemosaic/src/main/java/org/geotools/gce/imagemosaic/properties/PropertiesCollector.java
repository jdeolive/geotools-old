package org.geotools.gce.imagemosaic.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageReader;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.opengis.feature.simple.SimpleFeature;


public abstract class PropertiesCollector  {
	
	private List<String> propertyNames;
	private PropertiesCollectorSPI spi;
	private List<String> matches= new ArrayList<String>();
	
	
	public PropertiesCollector(
			final PropertiesCollectorSPI spi,
			final List<String> propertyNames) {
		this.spi = spi;
		this.propertyNames=new ArrayList<String>(propertyNames);
	}
	

	public PropertiesCollectorSPI getSpi() {
		return spi;
	}

	public PropertiesCollector collect(final File  file){
		return this;
	}
	
	public PropertiesCollector collect(final ImageReader  imageReader){
		return this;
	}
	
	public PropertiesCollector collect(final AbstractGridCoverage2DReader  abstractGridCoverageReader){
		return this;
	}		
	
	abstract public void setProperties(final SimpleFeature feature);
	
	public void reset(){
		matches= new ArrayList<String>();
	}

	public List<String> getPropertyNames() {
		return Collections.unmodifiableList(propertyNames);
	}
	
	protected void addMatch(String match){
		matches.add(match);
	}

	protected List<String> getMatches() {
		return Collections.unmodifiableList(matches);
	}
	
}
