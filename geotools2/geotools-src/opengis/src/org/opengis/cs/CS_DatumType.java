package org.opengis.cs;
import org.opengis.pt.*;

/** Type of the datum expressed as an enumerated value.
 *  The enumeration is split into ranges which indicate the datum's type.
 *  The value should be one of the predefined values, or within the range
 *  for local types.  This will allow OGC to coordinate the
 *  addition of new interoperable codes.
 */
public class CS_DatumType
{
    public int value;

    /** Lowest possible value for horizontal datum types.
     */
    public static final int CS_HD_Min=1000;

    /** Unspecified horizontal datum type.
     *  Horizontal datums with this type should never supply
     *  a conversion to WGS84 using Bursa Wolf parameters.
     */
    public static final int CS_HD_Other=1000;

    /** These datums, such as ED50, NAD27 and NAD83, have been designed to
     *  support horizontal positions on the ellipsoid as opposed to positions
     *  in 3-D space.  These datums were designed mainly to support a
     *  horizontal component of a position in a domain of limited extent, such
     *  as a country, a region or a continent.
     */
    public static final int CS_HD_Classic=1001;

    /** A geocentric datum is a "satellite age" modern geodetic datum mainly of
     *  global extent, such as WGS84 (used in GPS), PZ90 (used in GLONASS) and
     *  ITRF.  These datums were designed to support both a horizontal
     *  component of position and a vertical component of position (through
     *  ellipsoidal heights).  The regional realizations of ITRF, such as
     *  ETRF, are also included in this category.
     */
    public static final int CS_HD_Geocentric=1002;

    /** Highest possible value for horizontal datum types.
     */
    public static final int CS_HD_Max=1999;

    /** Lowest possible value for vertical datum types.
     */
    public static final int CS_VD_Min=2000;

    /** Unspecified vertical datum type.
     */
    public static final int CS_VD_Other=2000;

    /** A vertical datum for orthometric heights that are measured along the
     * plumb line.
     */
    public static final int CS_VD_Orthometric=2001;

    /** A vertical datum for ellipsoidal heights that are measured along the
     *  normal to the ellipsoid used in the definition of horizontal datum.
     */
    public static final int CS_VD_Ellipsoidal=2002;

    /** The vertical datum of altitudes or heights in the atmosphere.  These
     *  are approximations of orthometric heights obtained with the help of
     *  a barometer or a barometric altimeter.  These values are usually
     *  expressed in one of the following units: meters, feet, millibars
     *  (used to measure pressure levels),  or theta value (units used to
     *  measure geopotential height).
     */
    public static final int CS_VD_AltitudeBarometric=2003;

    /** A normal height system.
     */
    public static final int CS_VD_Normal=2004;

    /** A vertical datum of geoid model derived heights, also called
     *  GPS-derived heights. These heights are approximations of
     *  orthometric heights (H), constructed from the ellipsoidal heights
     * (h) by the use of the given geoid undulation model (N) through the
     * equation: H=h-N.
     */
    public static final int CS_VD_GeoidModelDerived=2005;

    /** This attribute is used to support the set of datums generated
     *  for hydrographic engineering projects where depth measurements below
     *  sea level are needed.  It is often called a hydrographic or a marine
     *  datum.  Depths are measured in the direction perpendicular
     *  (approximately) to the actual equipotential surfaces of the earth's
     *  gravity field, using such procedures as echo-sounding.
     */
    public static final int CS_VD_Depth=2006;

    /** Highest possible value for vertical datum types.
     */
    public static final int CS_VD_Max=2999;

    /** Lowest possible value for local datum types.
     */
    public static final int CS_LD_Min=10000;

    /** Highest possible value for local datum types.
     */
    public static final int CS_LD_Max=32767;
}
