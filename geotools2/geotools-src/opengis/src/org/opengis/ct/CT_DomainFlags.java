package org.opengis.ct;
import org.opengis.pt.*;

/** Flags indicating parts of domain covered by a convex hull.
 *  These flags can be combined.  For example, the value 3 corresponds to
 *  a combination of CT_DF_Inside and MF_DF_Outside, which means that some
 *  parts of the convex hull are inside the domain, and some parts of the
 *  convex hull are outside the domain.
 */
public class CT_DomainFlags
{
    public int value;

    /** At least one point in a convex hull is inside the transform's domain.
     */
    public static final int CT_DF_Inside=1;

    /** At least one point in a convex hull is outside the transform's domain.
     */
    public static final int CT_DF_Outside=2;

    /** At least one point in a convex hull is not transformed continuously.
     *  As an example, consider a "Longitude_Rotation" transform which adjusts
     *  longitude coordinates to take account of a change in Prime Meridian.
     *  If the rotation is 5 degrees east, then the point (Lat=175,Lon=0)
     *  is not transformed continuously, since it is on the meridian line
     *  which will be split at +180/-180 degrees.
     */
    public static final int CT_DF_Discontinuous=4;
}
