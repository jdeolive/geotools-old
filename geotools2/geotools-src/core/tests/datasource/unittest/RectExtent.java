package unittest;

import uk.ac.leeds.ccg.geotools.*;
import com.sun.java.util.collections.*;
import org.geotools.datasource.*;

public class RectExtent implements Extent
{
	GeoRectangle bounds = new GeoRectangle();
	
	public void setBounds(GeoRectangle r)
	{
		bounds = r;
	}	
	
	public GeoRectangle getBounds()
	{
		return bounds;
	}
	
	/** Gets the Extent which represents the intersection between this and another
	 * @param other The extent to test against
	 * @return An Extent representing the intersecting area between two Extents, null if there is no overlap.
	 */
	public Extent intersection(Extent other)
	{
		RectExtent newExtent = new RectExtent();
		RectExtent otherExtent = (RectExtent)other;
		GeoRectangle otherGeo = otherExtent.getBounds();
		newExtent.setBounds(otherGeo.createIntersect(bounds));
		return newExtent;
	}

	/** Gets the difference, represented by another Extent, between this Extent and other.
	 * @param other The extent to test against
	 * @return An array of Extents making up the total area of other not taken up by this Extent.
	 * If there is no overlap between the two, this returns the same extent as other.
	 */	
	public Extent [] difference(Extent other)
	{
		RectExtent newExtent = new RectExtent();
		RectExtent otherExtent = (RectExtent)other;
		GeoRectangle otherGeo = otherExtent.getBounds();
		GeoRectangle[] side = otherGeo.remainder(bounds);
		Vector v = new Vector();
		if (side!=null)
			for (int i=0;i<side.length;i++)
				if (side[i]!=null)
					v.addElement(side[i]);
		return (Extent[])v.toArray(new GeoRectangle[v.size()]);
	}
	
	/** Tests whether the given Feature is within this Extent. This Extent implementation must be 
	 * able to read the contents of the Feature.
	 * @return True is the Feature is within this Extent, false if otherwise.
	 */
	public boolean containsFeature(Feature feature)
	{
		// Assume the Feature contains a GeoShape
		if (feature==null || feature.row==null)
			return false;
		GeoShape s = (GeoShape)feature.row[0];
		return bounds.contains(s.getBounds());
	}
}

