/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.feature;

import org.geotools.data.*;
import java.util.*;


/**
 * This class handles Extents. The Extents it contains can only be flat,
 * non-overlapping Extent. If a new Extent is added, it is split into only
 * those Extents which make up the difference between it and the current
 * extents. $Id: ExtentMosaic.java,v 1.3 2003/05/16 21:10:18 jmacgill Exp $
 *
 * @author Ray Gallagher
 */
public class ExtentMosaic implements Extent {
    /** The extents which have already been added */
    Vector mosaic = new Vector();

    /**
     * The overlap mode - whether to split an Extent added to this mosaic to
     * fit into the parts around the edges (false), or split the mosaic to fit
     * around the newly added Extent (true).
     */
    boolean overlap = true;

    /**
     * Constructor.
     */
    public ExtentMosaic() {
        overlap = true;
    }

    /**
     * Constructor.
     *
     * @param overlap If true, then the mosaic is applied in favour of any new
     *        Extents added.  That is, the last added Extent is kept whole,
     *        chopping the mosaic to fit around it. If false, the reverse
     *        applies, as the newest Extent is always chopped around the
     *        mosaic already in place.
     */
    public ExtentMosaic(boolean overlap) {
        this.overlap = overlap;
    }

    /**
     * Sets the overlap mode for this ExtentMosaic.
     *
     * @param over If true, then the mosaic is applied in favour of any new
     *        Extents added.  That is, the last added Extent is kept whole,
     *        chopping the mosaic to fit around it. If false, the reverse
     *        applies, as the newest Extent is always chopped around the
     *        mosaic already in place.
     */
    public void setOverlap(boolean over) {
        overlap = over;
    }

    /**
     * Sets the overlap mode for this ExtentMosaic.
     *
     * @return The current overlap mode of this ExtentMosaic.
     */
    public boolean getOverlap() {
        return overlap;
    }

    /**
     * Adds an Extent. Returns the difference between this Extent and those
     * already added to the overall mosaic.  If the overlap mode is true (the
     * default), the mosaic is chopped to fit around the new Extent, ex. If
     * false, then the Extent ex is chopped to fit around the mosaic. The
     * return value is the same in both cases, which is the result of chopping
     * the new Extent around the mosaic.
     *
     * @param ex The Extent to apply to the mosaic.
     *
     * @return The difference between the added Extent and the mosaic already
     *         in place.
     */
    public Extent[] addExtent(Extent ex) {
        // Gets the difference between ex and the mosaic
        Vector exes = new Vector();

        // If the extent is an ExtentMosaic then each extent within the
        // other ExtentMosaic will have to be added individually
        if (ex instanceof ExtentMosaic) {
            Extent[] extents = ((ExtentMosaic) ex).getExtents();

            for (int i = 0; ((extents != null) && (i < extents.length)); i++) {
                exes.addElement(extents[i]);
            }
        } else {
            exes.addElement(ex);
        }

        // Gets the difference between the mosaic of extents and the
        // newly added extents
        Extent[] diff = snipExtent(exes, mosaic);

        if (overlap) {
            Extent[] overlapped = snipExtent(mosaic, exes);

            // Resets the mosaic
            mosaic = new Vector();

            for (int i = 0; i < overlapped.length; i++)
                mosaic.addElement(overlapped[i]);

            // Adds the newest extents
            for (int i = 0; i < exes.size(); i++)
                mosaic.addElement(exes.elementAt(i));
        } else {
            // Patches the given extent(s) into the mosaic
            // of extents
            for (int i = 0; i < diff.length; i++)
                mosaic.add(diff[i]);
        }

        return diff;
    }

    /**
     * Removes the oldest Extent from this ExtentMosaic. Useful for
     * memory-management routines.
     *
     * @return The removed Extent (the oldest).
     */
    public Extent getOldest() {
        return (Extent) mosaic.remove(0);
    }

    /**
     * Gets the Extents currently in this mosaic, in the order in which they
     * were added (oldest first).
     *
     * @return The Extents currently held by the mosaic. The Extents will
     *         likely have been changed as they were applied to fit into the
     *         mosaic, and will probably not be the same Extents as those
     *         addded initially.
     */
    public Extent[] getExtents() {
        return (Extent[]) mosaic.toArray(new Extent[mosaic.size()]);
    }

    /**
     * Gets the Extents which make up the leftover space when ex is applied to
     * mosaic.
     *
     * @param extents The Extents to patch into the mosaic of extents.
     * @param mosaic The List of Extents already in place.
     *
     * @return The Extents which make up the difference between ex and mosaic.
     */
    private Extent[] snipExtent(Vector extents, List mosaic) {
        Vector difference = (Vector) extents.clone();
        Iterator it = mosaic.iterator();

        while (it.hasNext()) {
            // Calls difference with the current mosaic extent on
            // each extent in difference
            Extent mEx = (Extent) it.next();
            Hashtable result = new Hashtable();

            for (int v = 0; v < difference.size(); v++) {
                Extent dEx = (Extent) difference.elementAt(v);

                // If there is any overlap, then difference can
                // be calculated
                if (mEx.intersection(dEx) != null) {
                    // Gets the difference
                    Extent[] diff = mEx.difference(dEx);

                    if (diff == null) {
                        diff = new Extent[0];
                    }

                    // Add to hashtable result - to be
                    // applied to difference
                    result.put(dEx, diff);
                }
            }

            // Replaces difference array with the newer version
            // (more snipped than before)
            Iterator keys = result.keySet().iterator();

            while (keys.hasNext()) {
                Extent key = (Extent) keys.next();
                int index = difference.indexOf(key);

                // Removes original from vector
                difference.remove(key);

                // Replaces it with the difference
                Extent[] diff = (Extent[]) result.get(key);

                for (int i = 0; i < diff.length; i++)
                    difference.insertElementAt(diff[i], index);
            }
        }

        return (Extent[]) difference.toArray(new Extent[difference.size()]);
    }

    /**
     * Gets the Extent which represents the intersection between this Extent
     * and other.
     *
     * @param other The Extent to test against.
     *
     * @return An Extent representing the intersecting area between two Extents
     *         (null if there is no overlap).
     */
    public Extent intersection(Extent other) {
        // This method returns an ExtentMosiac object.
        // There's just no other way.
        ExtentMosaic mos = new ExtentMosaic();
        Vector vMos = new Vector();

        if (other instanceof ExtentMosaic) {
            Extent[] exes = ((ExtentMosaic) other).getExtents();

            for (int i = 0; ((exes != null) && (i < exes.length)); i++)
                vMos.addElement(exes[i]);
        } else {
            vMos.addElement(other);
        }

        // Go through each Extent in the mosaic, and get the
        // intersection between that and the Extent other.
        Iterator it = mosaic.iterator();

        while (it.hasNext()) {
            Extent mosaicExtent = (Extent) it.next();

            for (int i = 0; i < vMos.size(); i++)
                if (((Extent) vMos.elementAt(i)).intersection(mosaicExtent) != null) {
                    mos.addExtent(((Extent) vMos.elementAt(i)).intersection(
                            mosaicExtent));
                }
        }

        if ((mos.getExtents() == null) || (mos.getExtents().length == 0)) {
            return null;
        } else {
            return mos;
        }
    }

    /**
     * Gets the difference, represented by another Extent, between this Extent
     * and other.
     *
     * @param other The Extent to test against.
     *
     * @return An array of Extents making up the total area of other not taken
     *         up by this Extent. If there is no overlap between the two, this
     *         returns the same Extent as other.
     */
    public Extent[] difference(Extent other) {
        Vector exes = new Vector();

        if (other instanceof ExtentMosaic) {
            Extent[] otherExes = ((ExtentMosaic) other).getExtents();

            for (int i = 0; ((otherExes != null) && (i < otherExes.length));
                    i++)
                exes.addElement(otherExes[i]);
        } else {
            exes.addElement(other);
        }

        // Snips other to fit around mosaic
        Extent[] leftOver = snipExtent(exes, mosaic);

        return leftOver;
    }

    /**
     * Produces the smallest Extent that will hold both the existing Extent and
     * that of the Extent passed in. TODO: Think about implication of
     * combining.  New Extent may contain areas which were in neither.
     *
     * @param other The Extent to combine with this Extent.
     *
     * @return The new, larger, Extent.
     */
    public Extent combine(Extent other) {
        return null;
    }

    /**
     * Tests whether the given Feature is within this Extent. This Extent
     * implementation must know and be able to read the object types contained
     * in the Feature.
     *
     * @param feature The Feature to test.
     *
     * @return True if the Feature is within this Extent, otherwise false.
     */
    public boolean containsFeature(Feature feature) {
        Iterator it = mosaic.iterator();

        while (it.hasNext())

            if (((Extent) it.next()).containsFeature(feature)) {
                return true;
            }

        return false;
    }
}
