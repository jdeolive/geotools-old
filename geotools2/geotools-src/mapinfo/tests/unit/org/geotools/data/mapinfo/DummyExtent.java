package org.geotools.data.mapinfo;

import org.geotools.data.Extent;
import org.geotools.feature.Feature;

public class DummyExtent implements Extent
{

	/**
	 * @see Extent#intersection(Extent)
	 */
	public Extent intersection(Extent other)
	{
		return this;
	}

	/**
	 * @see Extent#difference(Extent)
	 */
	public Extent[] difference(Extent other)
	{
		return new Extent[] {this};
	}

	/**
	 * @see Extent#combine(Extent)
	 */
	public Extent combine(Extent other)
	{
		return null;
	}

	/**
	 * @see Extent#containsFeature(Feature)
	 */
	public boolean containsFeature(Feature feature)
	{
		return true;
	}

  public boolean containsExtent(Extent e) {
      return true;
  }
  
}

