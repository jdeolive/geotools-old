package org.opengis.ct;
import org.opengis.pt.*;

/** Transforms multi-dimensional coordinate points.
 *  If a client application wishes to query the source and target
 *  coordinate systems of a transformation, then it should keep hold
 *  of the CT_CoordinateTransformation interface, and use the
 *  contained math transform object whenever it wishes to perform a transform.
 */
public interface CT_MathTransform extends java.rmi.Remote
{
    /** Gets flags classifying domain points within a convex hull.
     *  The supplied ordinates are interpreted as a sequence of points, which
     *  generates a convex hull in the source space.  Conceptually, each of the
     *  (usually infinite) points inside the convex hull is then tested against
     *  the source domain.  The flags of all these tests are then combined.  In
     *  practice, implementations of different transforms will use different
     *  short-cuts to avoid doing an infinite number of tests.
     *  @param ord Packed ordinates of points used to generate convex hull.
     */
    CT_DomainFlags getDomainFlags(double[] ord);

    /** Gets transformed convex hull.
     *  The supplied ordinates are interpreted as a sequence of points, which
     *  generates a convex hull in the source space.  The returned sequence of
     *  ordinates represents a convex hull in the output space.  The number of
     *  output points will often be different from the number of input points.
     *  Each of the input points should be inside the valid domain (this can be
     *  checked by testing the points' domain flags individually).  However,
     *  the convex hull of the input points may go outside the valid domain.
     *  The returned convex hull should contain the transformed image of the
     *  intersection of the source convex hull and the source domain.
     *
     *  A convex hull is a shape in a coordinate system, where if two positions A
     *  and B are inside the shape, then all positions in the straight line
     *  between A and B are also inside the shape.  So in 3D a cube and a sphere
     *  are both convex hulls.  Other less obvious examples of convex hulls are
     *  straight lines, and single points.  (A single point is a convex hull,
     *  because the positions A and B must both be the same - i.e. the point
     *  itself.  So the straight line between A and B has zero length.)
     *
     *  Some examples of shapes that are NOT convex hulls are donuts, and horseshoes.
     *  @param ord Packed ordinates of points used to generate convex hull.
     */
    double[] getCodomainConvexHull(double[] ord);

    /** Gets a Well-Known text representation of this object.*/
    String getWKT();

    /** Gets an XML representation of this object.*/
    String getXML();

    /** Transforms a coordinate point.
     *  The passed parameter point should not be modified.
     *  @param cp Point to transform.
     */
    PT_CoordinatePoint transform(PT_CoordinatePoint cp);

    /** Transforms a list of coordinate point ordinal values.
     *  This method is provided for efficiently transforming many points.
     *  The supplied array of ordinal values will contain packed ordinal
     *  values.  For example, if the source dimension is 3, then the ordinals
     *  will be packed in this order (x0,y0,z0,x1,y1,z1 ...).  The size
     *  of the passed array must be an integer multiple of DimSource.
     *  The returned ordinal values are packed in a similar way.
     *  In some DCPs. the ordinals may be transformed in-place, and the
     *  returned array may be the same as the passed array.
     *  So any client code should not attempt to reuse the passed ordinal
     *  values (although they can certainly reuse the passed array).
     *  If there is any problem then the server implementation will throw an
     *  exception.  If this happens then the client should not make any
     *  assumptions about the state of the ordinal values.
     *  @param ord Packed ordinates of points to transform.
     */
    double[] transformList(double[] ord);

    /** Gets the derivative of this transform at a point.
     *  If the transform does not have a well-defined derivative at the point,
     *  then this function should fail in the usual way for the DCP.
     *  The derivative is the matrix of the non-translating portion of the
     *  approximate affine map at the point.  The matrix will have dimensions
     *  corresponding to the source and target coordinate systems.
     *  If the input dimension is M, and the output dimension is N, then
     *  the matrix will have size [M][N].  The elements of the matrix
     *  {elt[n][m] : n=0..(N-1)} form a vector in the output space which is
     *  parallel to the displacement caused by a small change in the m'th
     *  ordinate in the input space.
     *  @param cp Point in domain at which to get derivative.
     */
    PT_Matrix derivative(PT_CoordinatePoint cp);

    /** Creates the inverse transform of this object.
     *  This method may fail if the transform is not one to one.
     *  However, all cartographic projections should succeed.
     */
    CT_MathTransform inverse();

    /** Gets the dimension of input points. */
    int getDimSource();

    /** Gets the dimension of output points. */
    int getDimTarget();

    /** Tests whether this transform does not move any points. */
    boolean isIdentity();
}

