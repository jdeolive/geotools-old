package org.opengis.ct;

/** Creates coordinate transformation objects from codes.
 *  The codes are maintained by an external authority.
 *  A commonly used authority is EPSG, which is also used in the GeoTIFF
 *  standard
 */
public interface CT_CoordinateTransformationAuthorityFactory extends java.rmi.Remote
{
    /** The name of the authority.
     */
    String getAuthority();

    /** Creates a transformation from a single transformation code.
     *  The 'Authority' and 'AuthorityCode' values of the created object will be set
     *  to the authority of this object, and the code specified by the client,
     *  respectively.  The other metadata values may or may not be set.
     *  @param code Coded value for transformation.
     */
    CT_CoordinateTransformation createFromTransformationCode(String code);

    /** Creates a transformation from coordinate system codes.
     *  @param sourceCode   Coded value of source coordinate system.
     *  @param targetCode   Coded value of target coordinate system.
     */
    CT_CoordinateTransformation createFromCoordinateSystemCodes(String sourceCode,String targetCode);

}
