package org.geotools.gce.imagemosaic.indexbuilder;


import java.util.ArrayList;
import java.util.List;

import org.geotools.console.Option;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.util.Utilities;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class IndexBuilderConfiguration{

	public IndexBuilderConfiguration() {
	}
	
	public IndexBuilderConfiguration(final IndexBuilderConfiguration that) {
		this.absolute=that.absolute;
		this.indexingDirectories=new ArrayList<String>(that.indexingDirectories);
		this.indexName=that.indexName;
		this.locationAttribute=that.locationAttribute;
		this.rootMosaicDirectory=that.rootMosaicDirectory;
		this.wildcard=that.wildcard;
		this.propertyCollectors=that.propertyCollectors;
		this.schema=that.schema;
		this.timeAttribute=that.timeAttribute;
		this.recursive=that.recursive;
		
	}

	public void setIndexingDirectories(List<String> indexingDirectories) {
		this.indexingDirectories = indexingDirectories;
	}
	
	private boolean recursive = Utils.DEFAULT_RECURSION_BEHAVIOR;

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	private boolean absolute = Utils.DEFAULT_PATH_BEHAVIOR;
	/**
	 * Index file name. Default is index.
	 */
	private String indexName = Utils.DEFAULT_INDEX_NAME;
	
	private String locationAttribute = Utils.DEFAULT_LOCATION_ATTRIBUTE;
	
	@Option(description="Root directory where to place the index file",mandatory=true,name="rootDirectory")
	private String rootMosaicDirectory;
	
	@Option(description="Wildcard to use for building the index of this mosaic",mandatory=false,name="wildcard")
	private String wildcard = Utils.DEFAULT_WILCARD;
	
	/** String to pass to the featuretypebuilder for building the schema for the index.*/
	private String schema;
	
	private String propertyCollectors;
	
	public String getPropertyCollectors() {
		return propertyCollectors;
	}

	public void setPropertyCollectors(String propertyCollectors) {
		this.propertyCollectors = propertyCollectors;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTimeAttribute() {
		return timeAttribute;
	}

	public void setTimeAttribute(String timeAttribute) {
		this.timeAttribute = timeAttribute;
	}


	private String timeAttribute;
	
	
	private List<String> indexingDirectories;

	public List<String> getIndexingDirectories() {
		return indexingDirectories;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#getIndexName()
	 */
	public String getIndexName() {
		return indexName;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#getLocationAttribute()
	 */
	public String getLocationAttribute() {
		return locationAttribute;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#getRootMosaicDirectory()
	 */
	public String getRootMosaicDirectory() {
		return rootMosaicDirectory;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#getWildcardString()
	 */
	public String getWildcard() {
		return wildcard;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#isAbsolute()
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#setAbsolute(boolean)
	 */
	public void setAbsolute(boolean absolute) {
		this.absolute = absolute;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#setIndexName(java.lang.String)
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#setLocationAttribute(java.lang.String)
	 */
	public void setLocationAttribute(String locationAttribute) {
		this.locationAttribute = locationAttribute;
	}

	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#setRootMosaicDirectory(java.lang.String)
	 */
	public void setRootMosaicDirectory(final String rootMosaicDirectory) {
		 Utils.ensureNonNull("rootMosaicDirectory", rootMosaicDirectory);
		 String testingDirectory = rootMosaicDirectory;
		 Utils.checkDirectory(testingDirectory);
		 this.rootMosaicDirectory=testingDirectory;

	}		


	/* (non-Javadoc)
	 * @see org.geotools.gce.imagemosaic.JMXIndexBuilderMBean#setWildcardString(java.lang.String)
	 */
	public void setWildcard(String wildcardString) {
		this.wildcard = wildcardString;
	}

	@Override
	public IndexBuilderConfiguration clone() throws CloneNotSupportedException {
		return new IndexBuilderConfiguration(this);
	}

	@Override
	public boolean equals(Object obj) {
		if(this==obj)
			return true;
		if(!(obj instanceof IndexBuilderConfiguration))
			return false;
		final IndexBuilderConfiguration that=(IndexBuilderConfiguration) obj;
		
		if(this.absolute!=that.absolute)
			return false;
		if(!(this.indexName==null&&that.indexName==null)&&!this.indexName.equals(that.indexName))
			return false;	
		if(!(this.locationAttribute==null&&that.locationAttribute==null)&&!this.locationAttribute.equals(that.locationAttribute))
			return false;			
		if(!(this.rootMosaicDirectory==null&&that.rootMosaicDirectory==null)&&!this.rootMosaicDirectory.equals(that.rootMosaicDirectory))
			return false;		
		if(!Utilities.deepEquals(this.indexingDirectories, that.indexingDirectories))
			return false;
		
			
		return true;
	}

	@Override
	public int hashCode() {
		int seed=37;
		seed=Utilities.hash(absolute, seed);
		seed=Utilities.hash(locationAttribute, seed);
		seed=Utilities.hash(indexName, seed);
		seed=Utilities.hash(wildcard, seed);
		seed=Utilities.hash(rootMosaicDirectory, seed);
		seed=Utilities.hash(indexingDirectories, seed);
		return seed;
	}

	@Override
	public String toString() {
		final StringBuilder builder= new StringBuilder();
		builder.append("IndexBuilderConfiguration").append("\n");
		builder.append("wildcardString:\t\t\t").append(wildcard).append("\n");
		builder.append("indexName:\t\t\t").append(indexName).append("\n");
		builder.append("absolute:\t\t\t").append(absolute).append("\n");
		builder.append("locationAttribute:\t\t\t").append(locationAttribute).append("\n");
		builder.append("rootMosaicDirectory:\t\t\t").append(rootMosaicDirectory).append("\n");
		builder.append("indexingDirectories:\t\t\t").append(Utilities.deepToString(indexingDirectories)).append("\n");
		return builder.toString();
	}

	public void check() throws IllegalStateException{
		//check parameters
		if(indexingDirectories==null||indexingDirectories.size()<=0)
			throw new IllegalStateException("Indexing directories are empty");
		final List<String> directories= new ArrayList<String>();
		for(String dir:indexingDirectories)
			directories.add(Utils.checkDirectory(dir));		
		indexingDirectories=directories;
		
		if(indexName==null||indexName.length()==0)
			throw new IllegalStateException("Index name cannot be empty");
		
		if(rootMosaicDirectory==null||rootMosaicDirectory.length()==0)
			throw new IllegalStateException("RootMosaicDirectory name cannot be empty");
		
		rootMosaicDirectory=Utils.checkDirectory(rootMosaicDirectory);
		if(wildcard==null||wildcard.length()==0)
			throw new IllegalStateException("WildcardString name cannot be empty");
		
	}
	
}
