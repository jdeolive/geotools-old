package org.geotools.datasource;

/** Represents the extent of a loaded Feature set.
 */
public interface Extent
{
	/** Gets the Extent which represents the intersection between this and another
	 * @param other The extent to test against
	 * @return An Extent representing the intersecting area between two Extents, null if there is no overlap.
	 */
	public Extent intersection(Extent other);

	/** Gets the difference, represented by another Extent, between this Extent and other.
	 * @param other The extent to test against
	 * @return An array of Extents making up the total area of other not taken up by this Extent.
	 * If there is no overlap between the two, this returns the same extent as other.
	 */	
	public Extent [] difference(Extent other);
	
	/** Tests whether the given Feature is within this Extent. This Extent implementation must
	 * know and be able to read the object types contained in the Feature.
	 * @return True is the Feature is within this Extent, false if otherwise.
	 */
	public boolean containsFeature(Feature feature);
}

