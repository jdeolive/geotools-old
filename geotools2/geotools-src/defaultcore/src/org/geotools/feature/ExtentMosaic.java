package org.geotools.feature;

import java.util.*;
import org.geotools.data.*;

/** This class handles Extents. The Extents it contains can only be flat, non-overlapping Extent.
 * If a new Extent is added, it is split into only those Extents which make up the difference between it 
 * and the current extents.
 */
public class ExtentMosaic implements Extent
{
	/** The extents which have already been added */
	Vector mosaic = new Vector();	
	/** The overlap mode - whether to split an Extent added to this mosaic to fit into the parts around the edges (false), or split the mosaic to fit around the newly added Extent (true) */
	boolean overlap = true;

	/** Constructor.
	 */	
	public ExtentMosaic()
	{
		overlap = true;
	}
	
	/** Constructor. 
	 * @param overlap If true, then the mosaic is applied in favour of any new Extents added, that is the last added
	 * Extent is kept whole, chopping the mosaic to fit around it. If false, the reverse applies, as the newest Extent is always
	 * chopped around the mosaic already in place.
	 */
	public ExtentMosaic(boolean overlap)
	{
		this.overlap = overlap;
	}
	
	/** Sets the overlap mode for this ExtentMosaic. 
	 * @param over If true, then the mosaic is applied in favour of any new Extents added, that is the last added
	 * Extent is kept whole, chopping the mosaic to fit around it. If false, the reverse applies, as the newest Extent is always
	 * chopped around the mosaic already in place.
	 */
	public void setOverlap(boolean over)
	{
		overlap = over;
	}
	
	/** Sets the overlap mode for this ExtentMosaic. 
	 * @return The current overlap mode of this ExtentMosaic
	 */
	public boolean getOverlap()
	{
		return overlap;
	}
	
	/** Adds an Extent. Returns the difference between this Extent and those already added to the overall mosaic.
	 * If the overlap mode is true(the default), the mosaic is chopped to fit around the new Extent, ex. If false, then the
	 * Extent ex is chopped to fit around the mosaic. The return value is the same in both cases, which is the result of chopping
	 * the new Extent around the mosaic.
	 * @param ex The Extent to apply to the mosaic
	 * @return The difference between the added extent and the mosaic already in place.
	 */
	public Extent [] addExtent(Extent ex)
	{
		// Get the difference between ex and the mosaic
		Vector exes = new Vector();
		// If the extent is an ExtentMosaic then each extent within the other ExtentMosiac will have to be added individually
		if (ex instanceof ExtentMosaic)
		{
			Extent [] extents = ((ExtentMosaic)ex).getExtents();
			for (int i=0;(extents!=null && i<extents.length);i++)
				exes.addElement(extents[i]);
		}
		else
			exes.addElement(ex);

		// Get the difference between the mosaic of extents and the newly added extents
		Extent [] diff = snipExtent(exes, mosaic);
		
		if (overlap)
		{
			Extent [] overlapped = snipExtent(mosaic, exes);
			// Reset the mosaic
			mosaic = new Vector();
			for (int i=0;i<overlapped.length;i++)
				mosaic.addElement(overlapped[i]);
			// Add the newest extents
			for (int i=0;i<exes.size();i++)
				mosaic.addElement(exes.elementAt(i));
		}
		else
		{
			// Patch the given extent(s) into the mosiac of extents
			for (int i=0;i<diff.length;i++)
				mosaic.add(diff[i]);
		}
		
		return diff;
	}
	
	/** Removes the oldest Extent from this ExtentMosaic. Useful for memory-management routines.
	 * @return The removed Extent (the oldest).
	 */
	public Extent getOldest()
	{
		return (Extent)mosaic.remove(0);
	}
	
	/** Gets the Extents currently in this mosaic, in the order in which they were added (oldest fist)
	 * @return The Extents currently held by the mosaic. The Extents will likey have been changed as they were applied to fit into the mosaic, and will probably not be the same Extents as those addded initialliy.
	 */
	public Extent [] getExtents()
	{
		return (Extent[])mosaic.toArray(new Extent[mosaic.size()]);
	}
	
	/** Gets the Extents which make up the leftover space when ex is applied to mosaic
	 * @param extents The Extents to patch into the mosaic of extents
	 * @param mosaic The List of Extents already in place
	 * @return The Extents which make up the difference between ex and mosaic
	 */
	private Extent [] snipExtent(Vector extents, List mosaic)
	{
		Vector difference = (Vector)extents.clone();
		Iterator it = mosaic.iterator();
		while (it.hasNext())
		{
			// call difference with the current mosaic extent on each extent in difference
			Extent mEx = (Extent)it.next();
			Hashtable result = new Hashtable();
			for (int v=0;v<difference.size();v++)
			{
				Extent dEx = (Extent)difference.elementAt(v);
				// If there is any overlap, then difference can be calculated
				if (mEx.intersection(dEx)!=null)
				{
					// Get the difference
					Extent [] diff = mEx.difference(dEx);
					if (diff==null)
						diff = new Extent[0];
					// Add to hashtable result - to be applied to difference
					result.put(dEx, diff);
				}
			}
			// Replace difference array with the newer version (more snipped than before)
			Iterator keys = result.keySet().iterator();
			while (keys.hasNext())
			{
				Extent key = (Extent)keys.next();
				int index = difference.indexOf(key);
				// Remove original from vector
				difference.remove(key);
				// Replace it with the difference
				Extent [] diff = (Extent[])result.get(key);
				for (int i=0;i<diff.length;i++)
					difference.insertElementAt(diff[i], index);
			}
		}
		
		return (Extent[])difference.toArray(new Extent[difference.size()]);
	}
	
	/**
	 * Gets the Extent which represents the intersection between this
	 * Extent and other.
	 * @param other The extent to test against.
	 * @return An Extent representing the intersecting area between two
	 * Extents (null if there is no overlap).
	 */
	public Extent intersection(Extent other)
	{
		// This method returns an ExtentMosiac object. There's just no other way.
		ExtentMosaic mos = new ExtentMosaic();
		Vector vMos = new Vector();
		if (other instanceof ExtentMosaic)
		{
			Extent[] exes = ((ExtentMosaic)other).getExtents();
			for (int i=0;(exes!=null && i<exes.length);i++)
				vMos.addElement(exes[i]);
		}
		else
			vMos.addElement(other);
			
		// Go throught each extent in the mosaic, and get the intersection between that and the Extent other
		Iterator it = mosaic.iterator();
		while (it.hasNext())
		{
			Extent mosaicExtent = (Extent)it.next();
			for (int i=0;i<vMos.size();i++)
				if (((Extent)vMos.elementAt(i)).intersection(mosaicExtent)!=null)
					mos.addExtent(((Extent)vMos.elementAt(i)).intersection(mosaicExtent));
		}
		
		if (mos.getExtents()==null || mos.getExtents().length==0)
			return null;
		else
			return mos;
	}

	/**
	 * Gets the difference, represented by another Extent, between this
	 * Extent and other.
	 * @param other The extent to test against.
	 * @return An array of Extents making up the total area of other not
	 * taken up by this Extent.
	 * If there is no overlap between the two, this returns the same extent
	 * as other.
	 */
	public Extent[] difference(Extent other)
	{
		Vector exes = new Vector();
		if (other instanceof ExtentMosaic)
		{
			Extent [] otherExes = ((ExtentMosaic)other).getExtents();
			for (int i=0;(otherExes!=null && i<otherExes.length);i++)
				exes.addElement(otherExes[i]);
		}
		else
			exes.addElement(other);
		// Snip other to fit around mosaic
		Extent [] leftOver = snipExtent(exes, mosaic);
		return leftOver;
	}

	/**
	 * Produces the smallest extent that will hold both the existing extent
	 * and that of the extent passed in.
	 * TODO: Think about implication of combining.  New extent may contain
	 * areas which were in neither.
	 * @param other The extent to combine with this extent.
	 * @return The new, larger, extent.
	 **/
	public Extent combine(Extent other)
	{
		return null;
	}

	/**
	 * Tests whether the given Feature is within this Extent. This Extent
	 * implementation must know and be able to read the object types
	 * contained in the Feature.
	 * @param feature The Feature to test.
	 * @return True if the Feature is within this Extent, otherwise false.
	 */
	public boolean containsFeature(Feature feature)
	{
		Iterator it = mosaic.iterator();
		while (it.hasNext())
			if (((Extent)it.next()).containsFeature(feature))
				return true;
		return false;
	}
	
}

