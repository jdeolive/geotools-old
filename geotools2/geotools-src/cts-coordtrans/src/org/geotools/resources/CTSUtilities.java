/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.resources;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.pt.*;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// Miscellaneous
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;


/**
 * A set of static methods working on OpenGIS objects.  Some of those methods
 * are useful, but not really rigorous. This is why they do not appear in the
 * "official" package, but instead in this private one. <strong>Do not rely on
 * this API!</strong> It may change in incompatible way in any future version.
 *
 * @version $Id: CTSUtilities.java,v 1.9 2003/02/23 21:26:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class CTSUtilities {
    /**
     * Do not allow creation of
     * instances of this class.
     */
    private CTSUtilities() {
    }
    
    /**
     * Returns the dimension of the first axis of a particular type.
     * For example, <code>getDimensionOf(cs,&nbsp;AxisInfo.TIME)</code>
     * would returns the dimension number of time axis.
     */
    public static int getDimensionOf(final CoordinateSystem cs, final AxisInfo axis) {
        final int dimension = cs.getDimension();
        final AxisOrientation orientation = axis.orientation.absolute();
        for (int i=0; i<dimension; i++) {
            if (orientation.equals(cs.getAxis(i).orientation.absolute())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a sub-coordinate system for the specified dimension range.
     *
     * @param  cs    The coordinate system to decompose.
     * @param  lower The first dimension to keep, inclusive.
     * @param  upper The last  dimension to keep, exclusive.
     * @return The sub-coordinate system, or <code>null</code> if <code>cs</code> can't
     *         be decomposed for dimensions in the range <code>[lower..upper]</code>.
     */
    public static CoordinateSystem getSubCoordinateSystem(CoordinateSystem cs, int lower, int upper)
    {
        if (lower<0 || lower>upper || upper>cs.getDimension()) {
            throw new IndexOutOfBoundsException(Resources.format(
                            ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1,
                            new Integer(lower<0 ? lower : upper)));
        }
        while (lower!=0 || upper!=cs.getDimension()) {
            if (!(cs instanceof CompoundCoordinateSystem)) {
                return null;
            }
            final CompoundCoordinateSystem ccs = (CompoundCoordinateSystem) cs;
            cs = ccs.getHeadCS();
            final int headDim = cs.getDimension();
            if (lower >= headDim) {
                cs = ccs.getTailCS();
                lower -= headDim;
                upper -= headDim;
            }
        }
        return cs;
    }
    
    /**
     * Returns a two-dimensional coordinate system representing the two first dimensions
     * of the specified coordinate system. If <code>cs</code> is already a two-dimensional
     * coordinate system, then it is returned unchanged. Otherwise, if it is a
     * {@link CompoundCoordinateSystem}, then the head coordinate system is examined.
     *
     * @param  cs The coordinate system, or <code>null</code>.
     * @return A two-dimensional coordinate system that represents the two first dimensions of
     *         <code>cs</code>, or <code>null</code> if <code>cs</code> was <code>null</code>.
     * @throws TransformException if <code>cs</code> can't be reduced to a two-coordinate system.
     *         We use this exception class since this method is usually invoked in the context of
     *         a transformation process.
     */
    public static CoordinateSystem getCoordinateSystem2D(CoordinateSystem cs)
            throws TransformException
    {
        if (cs != null) {
            while (cs.getDimension() != 2) {
                if (!(cs instanceof CompoundCoordinateSystem)) {
                    throw new TransformException(Resources.format(
                            ResourceKeys.ERROR_CANT_REDUCE_TO_TWO_DIMENSIONS_$1, cs.getName(null)));
                }
                cs = ((CompoundCoordinateSystem) cs).getHeadCS();
            }
        }
        return cs;
    }
    
    /**
     * Returns the first horizontal coordinate system found in a
     * coordinate system, or <code>null</code> if there is none.
     */
    public static HorizontalCoordinateSystem getHorizontalCS(final CoordinateSystem cs) {
        if (cs instanceof HorizontalCoordinateSystem) {
            return (HorizontalCoordinateSystem) cs;
        }
        if (cs instanceof CompoundCoordinateSystem) {
            HorizontalCoordinateSystem hcs;
            final CompoundCoordinateSystem comp = (CompoundCoordinateSystem) cs;
            if ((hcs=getHorizontalCS(comp.getHeadCS())) != null) return hcs;
            if ((hcs=getHorizontalCS(comp.getTailCS())) != null) return hcs;
        }
        return null;
    }
    
    /**
     * Returns the first vertical coordinate system found in a
     * coordinate system, or <code>null</code> if there is none.
     */
    public static VerticalCoordinateSystem getVerticalCS(final CoordinateSystem cs) {
        if (cs instanceof VerticalCoordinateSystem) {
            return (VerticalCoordinateSystem) cs;
        }
        if (cs instanceof CompoundCoordinateSystem) {
            VerticalCoordinateSystem vcs;
            final CompoundCoordinateSystem comp = (CompoundCoordinateSystem) cs;
            if ((vcs=getVerticalCS(comp.getHeadCS())) != null) return vcs;
            if ((vcs=getVerticalCS(comp.getTailCS())) != null) return vcs;
        }
        return null;
    }
    
    /**
     * Returns the first temporal coordinate system found in a
     * coordinate system, or <code>null</code> if there is none.
     */
    public static TemporalCoordinateSystem getTemporalCS(final CoordinateSystem cs) {
        if (cs instanceof TemporalCoordinateSystem) {
            return (TemporalCoordinateSystem) cs;
        }
        if (cs instanceof CompoundCoordinateSystem) {
            TemporalCoordinateSystem cts;
            final CompoundCoordinateSystem comp = (CompoundCoordinateSystem) cs;
            if ((cts=getTemporalCS(comp.getHeadCS())) != null) return cts;
            if ((cts=getTemporalCS(comp.getTailCS())) != null) return cts;
        }
        return null;
    }

    /**
     * Returns the first ellipsoid found in a coordinate
     * system, or <code>null</code> if there is none.
     */
    public static Ellipsoid getEllipsoid(final CoordinateSystem cs) {
        if (cs instanceof HorizontalCoordinateSystem) {
            final HorizontalDatum datum = ((HorizontalCoordinateSystem) cs).getHorizontalDatum();
            if (datum != null) {
                return datum.getEllipsoid();
            }
        }
        if (cs instanceof CompoundCoordinateSystem) {
            Ellipsoid ell;
            final CompoundCoordinateSystem comp = (CompoundCoordinateSystem) cs;
            if ((ell=getEllipsoid(comp.getHeadCS())) != null) return ell;
            if ((ell=getEllipsoid(comp.getTailCS())) != null) return ell;
        }
        return null;
    }

    /**
     * Returns the ellipsoid used by the specified coordinate system,
     * providing that the two first dimensions use an instance of
     * {@link GeographicCoordinateSystem}. Otherwise (i.e. if the
     * two first dimensions are not geographic), returns <code>null</code>.
     */
    public static Ellipsoid getHeadGeoEllipsoid(final CoordinateSystem coordinateSystem) {
        if (coordinateSystem instanceof GeographicCoordinateSystem) {
            final HorizontalDatum datum = ((GeographicCoordinateSystem) coordinateSystem).getHorizontalDatum();
            if (datum != null) {
                final Ellipsoid ellipsoid = datum.getEllipsoid();
                if (ellipsoid != null) {
                    return ellipsoid;
                }
            }
            return Ellipsoid.WGS84; // Should not happen with a valid coordinate system.
        }
        if (coordinateSystem instanceof CompoundCoordinateSystem) {
            // Check only head CS. Do not check tail CS!
            return getHeadGeoEllipsoid(((CompoundCoordinateSystem) coordinateSystem).getHeadCS());
        }
        return null;
    }
    
    /**
     * Transform an envelope. The transformation is only approximative.
     *
     * @param  transform The transform to use.
     * @param  envelope Envelope to transform. This envelope will not be modified.
     * @return The transformed envelope. It may not have the same number of dimensions
     *         than the original envelope.
     * @throws TransformException if a transform failed.
     */
    public static Envelope transform(final MathTransform transform, final Envelope envelope)
            throws TransformException
    {
        final int sourceDim = transform.getDimSource();
        final int targetDim = transform.getDimTarget();
        if (envelope.getDimension() != sourceDim) {
            throw new MismatchedDimensionException(sourceDim, envelope.getDimension());
        }
        int           coordinateNumber = 0;
        Envelope           transformed = null;
        CoordinatePoint       targetPt = null;
        final CoordinatePoint sourcePt = new CoordinatePoint(sourceDim);
        for (int i=sourceDim; --i>=0;) sourcePt.ord[i]=envelope.getMinimum(i);
        
  loop: do {
            // Transform a point and add the transformed
            // point to the destination envelope.
            targetPt = transform.transform(sourcePt, targetPt);
            if (transformed!=null) {
                transformed.add(targetPt);
            } else {
                transformed = new Envelope(targetPt, targetPt);
            }
            
            // Get the next point's coordinate.   The 'coordinateNumber' variable should
            // be seen as a number in base 3 where the number of digits is equals to the
            // number of dimensions. For example, a 4-D space would have numbers ranging
            // from "0000" to "2222". The digits are then translated into minimal, central
            // or maximal ordinates.
            int n = ++coordinateNumber;
            for (int i=sourceDim; --i>=0;) {
                switch (n % 3) {
                    case 0: sourcePt.ord[i] = envelope.getMinimum(i); n/=3; break;
                    case 1: sourcePt.ord[i] = envelope.getCenter (i); continue loop;
                    case 2: sourcePt.ord[i] = envelope.getMaximum(i); continue loop;
                }
            }
            break;
        }
        while (true);
        return transformed;
    }
    
    /**
     * Transform an envelope. The transformation is only approximative.
     * Invoking this method is equivalent to invoking the following:
     * <br>
     * <pre>transform(transform, new Envelope(source)).toRectangle2D()</pre>
     *
     * @param  transform The transform to use. Source and target dimension must be 2.
     * @param  source The rectangle to transform (may be <code>null</code>).
     * @param  dest  The destination rectangle (may be <code>source</code>).
     *         If <code>null</code>, a new rectangle will be created and returned.
     * @return <code>dest</code>, or a new rectangle if <code>dest</code> was non-null
     *         and <code>source</code> was null.
     * @throws TransformException if a transform failed.
     */
    public static Rectangle2D transform(final MathTransform2D transform,
                                        final Rectangle2D     source,
                                        final Rectangle2D     dest)
            throws TransformException
    {
        if (source==null) {
            return null;
        }
        double xmin=Double.POSITIVE_INFINITY;
        double ymin=Double.POSITIVE_INFINITY;
        double xmax=Double.NEGATIVE_INFINITY;
        double ymax=Double.NEGATIVE_INFINITY;
        final Point2D.Double point=new Point2D.Double();
        for (int i=0; i<8; i++) {
            /*
             *   (0)----(5)----(1)
             *    |             |
             *   (4)           (7)
             *    |             |
             *   (2)----(6)----(3)
             */
            point.x = (i&1)==0 ? source.getMinX() : source.getMaxX();
            point.y = (i&2)==0 ? source.getMinY() : source.getMaxY();
            switch (i) {
                case 5: // fallthrough
                case 6: point.x=source.getCenterX(); break;
                case 7: // fallthrough
                case 4: point.y=source.getCenterY(); break;
            }
            transform.transform(point, point);
            if (point.x<xmin) xmin=point.x;
            if (point.x>xmax) xmax=point.x;
            if (point.y<ymin) ymin=point.y;
            if (point.y>ymax) ymax=point.y;
        }
        if (dest!=null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new XRectangle2D(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Transforms the relative distance vector specified by <code>source</code> and stores
     * the result in <code>dest</code>.  A relative distance vector is transformed without
     * applying the translation components.
     *
     * @param transform The transform to apply.
     * @param origin The position where to compute the delta transform in the source CS.
     * @param source The distance vector to be delta transformed
     * @param dest   The resulting transformed distance vector, or <code>null</code>
     * @return       The result of the transformation.
     * @throws TransformException if the transformation failed.
     *
     * @see AffineTransform#deltaTransform(Point2D,Point2D)
     */
    public static Point2D deltaTransform(final MathTransform2D transform,
                                         final Point2D         origin,
                                         final Point2D         source,
                                               Point2D         dest)
            throws TransformException
    {
        if (transform instanceof AffineTransform) {
            return ((AffineTransform) transform).deltaTransform(source, dest);
        }
        final double ox = origin.getX();
        final double oy = origin.getY();
        final double dx = source.getX()*0.5;
        final double dy = source.getY()*0.5;
        Point2D P1 = new Point2D.Double(ox-dx, oy-dy);
        Point2D P2 = new Point2D.Double(ox+dx, oy+dy);
        P1 = transform.transform(P1, P1);
        P2 = transform.transform(P2, P2);
        if (dest == null) {
            dest = P2;
        }
        dest.setLocation(P2.getX()-P1.getX(), P2.getY()-P1.getY());
        return dest;
    }
    
    /**
     * Retourne une chaîne de caractères représentant la région géographique spécifiée. La
     * chaîne retournée sera de la forme "45°00.00'N-50°00.00'N 30°00.00'E-40°00.00'E". Si
     * une projection cartographique est nécessaire pour obtenir cette représentation, elle
     * sera faite automatiquement. Cette chaîne sert surtout à des fins de déboguage et sa
     * forme peut varier.
     */
    public static String toWGS84String(final CoordinateSystem cs, Rectangle2D bounds) {
        StringBuffer buffer = new StringBuffer();
        try {
            if (!GeographicCoordinateSystem.WGS84.equals(cs, false)) {
                final CoordinateTransformation tr = CoordinateTransformationFactory.getDefault().
                               createFromCoordinateSystems(cs, GeographicCoordinateSystem.WGS84);
                bounds = transform((MathTransform2D) tr.getMathTransform(), bounds, null);
            }
            final AngleFormat fmt = new AngleFormat("DD°MM.m'");
            buffer = fmt.format(new  Latitude(bounds.getMinY()), buffer, null); buffer.append('-');
            buffer = fmt.format(new  Latitude(bounds.getMaxY()), buffer, null); buffer.append(' ');
            buffer = fmt.format(new Longitude(bounds.getMinX()), buffer, null); buffer.append('-');
            buffer = fmt.format(new Longitude(bounds.getMaxX()), buffer, null);
        } catch (TransformException exception) {
            buffer.append(Utilities.getShortClassName(exception));
            final String message = exception.getLocalizedMessage();
            if (message != null) {
                buffer.append(": ");
                buffer.append(message);
            }
        }
        return buffer.toString();
    }
}
