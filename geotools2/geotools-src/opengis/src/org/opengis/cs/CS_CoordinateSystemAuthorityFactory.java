package org.opengis.cs;
import org.opengis.pt.*;

/** Creates spatial reference objects using codes
 *  The codes are maintained by an external authority. A commonly used
 *  authority is EPSG, which is also used in the GeoTIFF standard.
 */
public interface CS_CoordinateSystemAuthorityFactory extends java.rmi.Remote
{
    /** Returns the authority name.*/
    String getAuthority();

    /** Returns a ProjectedCoordinateSystem object from a code.
     *  @param code Value allocated by authority.
     */
    CS_ProjectedCoordinateSystem createProjectedCoordinateSystem(String code);

    /** Returns a GeographicCoordinateSystem object from a code.
     *  @param code Value allocated by authority.
     */
    CS_GeographicCoordinateSystem createGeographicCoordinateSystem(String code);

    /** Returns a HorizontalDatum object from a code.
     *  @param code Value allocated by authority.
     */
    CS_HorizontalDatum createHorizontalDatum(String code);

    /** Returns an Ellipsoid object from a code.
     *  @param code Value allocated by authority.
     */
    CS_Ellipsoid createEllipsoid(String code);

    /** Returns a PrimeMeridian object from a code.
     *  @param code Value allocated by authority.
     */
    CS_PrimeMeridian createPrimeMeridian(String code);

    /** Returns a LinearUnit object from a code.
     *  @param code Value allocated by authority.
     */
    CS_LinearUnit createLinearUnit(String code);

    /** Returns an AngularUnit object from a code.
     *  @param code Value allocated by authority.
     */
    CS_AngularUnit createAngularUnit(String code);

    /** Creates a vertical datum from a code.
     *  @param code Value allocated by authority.
     */
    CS_VerticalDatum createVerticalDatum(String code);

    /** Create a vertical coordinate system from a code.
     *  @param code Value allocated by authority.
     */
    CS_VerticalCoordinateSystem createVerticalCoordinateSystem(String code);

    /** Creates a 3D coordinate system from a code.
     *  @param code Value allocated by authority.
     */
    CS_CompoundCoordinateSystem createCompoundCoordinateSystem(String code);

    /** Creates a horizontal co-ordinate system from a code.
     *  The horizontal coordinate system could be geographic or projected.
     *  @param code Value allocated by authority.
     */
    CS_HorizontalCoordinateSystem createHorizontalCoordinateSystem(String code);

    /** Gets a description of the object corresponding to a code.
     *  @param code Value allocated by authority.
     */
    String descriptionText(String code);

    /** Gets the Geoid code from a WKT name.
     *  In the OGC definition of WKT horizontal datums, the geoid is
     *  referenced by a quoted string, which is used as a key value.  This
     *  method converts the key value string into a code recognized by this
     *  authority.
     *  @param wkt Name of geoid defined by OGC (e.g. "European_Datum_1950").
     */
    String geoidFromWKTName(String wkt);

    /** Gets the WKT name of a Geoid.
     *  In the OGC definition of WKT horizontal datums, the geoid is
     *  referenced by a quoted string, which is used as a key value.  This
     *  method gets the OGC WKT key value from a geoid code.
     *  @param geoid Code value for geoid allocated by authority.
     */
    String wktGeoidName(String geoid);
}

