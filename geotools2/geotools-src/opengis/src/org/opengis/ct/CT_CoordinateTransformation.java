package org.opengis.ct;
import org.opengis.cs.*;

// interface CS_CoordinateSystem;

/** Describes a coordinate transformation.
 *  This interface only describes a coordinate transformation, it does not
 *  actually perform the transform operation on points.  To transform
 *  points you must use a math transform.
 *
 *  The math transform will transform positions in the source coordinate
 *  system into positions in the target coordinate system.
 */
public interface CT_CoordinateTransformation extends java.rmi.Remote
{
    /** Name of transformation. */
    String getName();

    /** Authority which defined transformation and parameter values.
     *  An Authority is an organization that maintains definitions of Authority
     *  Codes.  For example the European Petroleum Survey Group (EPSG) maintains
     *  a database of coordinate systems, and other spatial referencing objects,
     *  where each object has a code number ID.  For example, the EPSG code for a
     *  WGS84 Lat/Lon coordinate system is '4326'.
     */
    String getAuthority();

    /** Code used by authority to identify transformation.
     *  The AuthorityCode is a compact string defined by an Authority to reference
     *  a particular spatial reference object.  For example, the European Survey
     *  Group (EPSG) authority uses 32 bit integers to reference coordinate systems,
     *  so all their code strings will consist of a few digits.  The EPSG code for
     *  WGS84 Lat/Lon is '4326'.
     *
     *  An empty string is used for no code.
     */
    String getAuthorityCode();

    /** Gets the provider-supplied remarks.*/
    String getRemarks();

    /** Human readable description of domain in source coordinate system.
     */
    String getAreaOfUse();

    /** Semantic type of transform.
     *  For example, a datum transformation or a coordinate conversion.
     */
    CT_TransformType getTransformType();

    /** Source coordinate system. */
    CS_CoordinateSystem getSourceCS();

    /** Target coordinate system. */
    CS_CoordinateSystem getTargetCS();

    /** Gets math transform.*/
    CT_MathTransform getMathTransform();
}

