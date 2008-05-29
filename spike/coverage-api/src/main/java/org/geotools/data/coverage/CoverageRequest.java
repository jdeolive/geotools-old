package org.geotools.data.coverage;

import org.geotools.factory.Hints;
import org.opengis.util.ProgressListener;

/**
 * @author     Administrator
 */
public interface CoverageRequest {

	/**
	 * @return
	 * @uml.property  name="coverageIdentifier"
	 */
	public abstract String getCoverageIdentifier();

	/**
	 * @param  coverageIdentifier
	 * @uml.property  name="coverageIdentifier"
	 */
	public abstract void setCoverageIdentifier(final String coverageIdentifier);

	/**
	 * The handle attribute is included to allow a client to associate  a mnemonic name to the Query request. The purpose of the handle attribute is to provide an error handling mechanism for locating  a statement that might fail.
	 * @return     the mnemonic name of the query request.
	 * @uml.property  name="handle"
	 */
	public abstract String getHandle();
	
	/**
	 * @param  handle
	 * @uml.property  name="handle"
	 */
	public void setHandle(final String handle);

	/**
	 * Specifies some hints to drive the query execution and results build-up. Hints examples can be the GeometryFactory to be used, a generalization distance to be applied right in the data store, to data store specific things such as the fetch size to be used in JDBC queries. The set of hints supported can be fetched by calling  {@links   FeatureSource#getSupportedHints()}  . Depending on the actual values of the hints, the data store is free to ignore them. No mechanism is in place, at the moment, to figure out which hints where actually used during the query execution.
	 * @return   the Hints the data store should try to use when executing the query  (eventually empty but never null).
	 * @uml.property  name="hints"
	 */
	public abstract Hints getHints();

	/**
	 * @param  hints
	 * @uml.property  name="hints"
	 */
	public abstract void setHints(final Hints hints);

	/**
	 * @param  additionalParameters
	 * @uml.property  name="additionalParameters"
	 */
	public abstract void setAdditionalParameters(
			final Param[] additionalParameters);

	/**
	 * @uml.property  name="additionalParameters"
	 * @uml.associationEnd  multiplicity="(0 -1)" container="org.geotools.data.coverage.Param"
	 */
	public abstract Param[] getAdditionalParameters();
	
	
	public void setListener (final ProgressListener listener);
	
	public ProgressListener getListener();

}