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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// J2SE dependencies
import java.io.Serializable;
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.WGS84ConversionInfo;
import org.geotools.pt.CoordinatePoint;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.XAffineTransform;


/**
 * Transforms a three dimensional geographic points using
 * abridged versions of formulas derived by Molodenski.
 *
 * @version $Id: AbridgedMolodenskiTransform.java,v 1.5 2002/10/09 08:45:00 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 */
class AbridgedMolodenskiTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1759367353860977791L;

    /**
     * <code>true</code> for a 3D transformation, or
     * <code>false</code> for a 2D transformation.
     */
    private final boolean source3D, target3D;
    
    /**
     * X,Y,Z shift in meters
     */
    private final double dx, dy, dz;
    
    /**
     * Source equatorial (<var>a</var>) and polar (<var>b/<var>) radius in meters.
     */
    private final double a, b;
    
    /**
     * Difference in the semi-major (<code>da=a1-a2</code>) and semi-minor
     * (<code>db=b1-b2</code>) axes of the first and second ellipsoids.
     */
    private final double da, db;
    
    /**
     * Square of the eccentricity of the ellipsoid.
     */
    private final double e2;
    
    /**
     * Defined as <code>(a*df) + (f*da)</code>.
     */
    private final double adf;
    
    /**
     * Construct a transform from the specified datum.
     */
    protected AbridgedMolodenskiTransform(final HorizontalDatum source,
                                          final HorizontalDatum target,
                                          final boolean source3D, final boolean target3D)
    {
        double f, df;
        final WGS84ConversionInfo srcInfo = source.getWGS84Parameters();
        final WGS84ConversionInfo tgtInfo = source.getWGS84Parameters();
        final Ellipsoid      srcEllipsoid = source.getEllipsoid();
        final Ellipsoid      tgtEllipsoid = target.getEllipsoid();
        dx =     srcInfo.dx - tgtInfo.dx;
        dy =     srcInfo.dy - tgtInfo.dy;
        dz =     srcInfo.dz - tgtInfo.dz;
        a  =     srcEllipsoid.getSemiMajorAxis();
        b  =     srcEllipsoid.getSemiMinorAxis();
        f  = 1 / srcEllipsoid.getInverseFlattening();
        da = a - tgtEllipsoid.getSemiMajorAxis();
        db = b - tgtEllipsoid.getSemiMinorAxis();
        df = f - 1/tgtEllipsoid.getInverseFlattening();
        e2  = 1 - (b*b)/(a*a);
        adf = (a*df) + (f*da);
        this.source3D = source3D;
        this.target3D = target3D;
    }
    
    /**
     * Construct a transform from the specified parameters.
     */
    protected AbridgedMolodenskiTransform(final ParameterList parameters) {
        final int dim = parameters.getIntParameter("dim");
        switch (dim) {
            case 2:  source3D=target3D=false; break;
            case 3:  source3D=target3D=true;  break;
            default: throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "dim", new Integer(dim)));
        }
        final double ta, tb, f, df;
        dx = parameters.getDoubleParameter("dx");
        dy = parameters.getDoubleParameter("dy");
        dz = parameters.getDoubleParameter("dz");
        a  = parameters.getDoubleParameter("src_semi_major");
        b  = parameters.getDoubleParameter("src_semi_minor");
        ta = parameters.getDoubleParameter("tgt_semi_major");
        tb = parameters.getDoubleParameter("tgt_semi_minor");
        da = a - ta;
        db = b - tb;
        f  = (a-b)/a;
        df = f - (ta-tb)/ta;
        e2  = 1 - (b*b)/(a*a);
        adf = (a*df) + (f*da);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        int step = 0;
        if (srcPts==dstPts && srcOff<dstOff && srcOff+numPts*getDimSource()>dstOff) {
            if (source3D != target3D) {
                // TODO: we need to figure out a general way to handle this case
                //       (overwritting the source array  while source and target
                //       dimensions are not the same).   This case occurs enough
                //       in the CTS implementation...
                throw new UnsupportedOperationException();
            }
            step = -getDimSource();
            srcOff -= (numPts-1)*step;
            dstOff -= (numPts-1)*step;
        }
        while (--numPts >= 0) {
            double x = Math.toRadians(srcPts[srcOff++]);
            double y = Math.toRadians(srcPts[srcOff++]);
            double z = (source3D) ? srcPts[srcOff++] : 0;
            final double sinX = Math.sin(x);
            final double cosX = Math.cos(x);
            final double sinY = Math.sin(y);
            final double cosY = Math.cos(y);
            final double sin2Y = sinY*sinY;
            final double nu = a / Math.sqrt(1 - e2*sin2Y);
            final double rho = nu * (1 - e2) / (1 - e2*sin2Y);
            
            // Note: Computation of 'x' and 'y' ommit the division by sin(1"), because
            //       1/sin(1") / (60*60*180/PI) = 1.0000000000039174050898603898692...
            //       (60*60 is for converting the final result from seconds to degrees,
            //       and 180/PI is for converting degrees to radians). This is an error
            //       of about 8E-7 arc seconds, probably close to rounding errors anyway.
            y += (dz*cosY - sinY*(dy*sinX + dx*cosX) + adf*Math.sin(2*y)) / rho;
            x += (dy*cosX - dx*sinX) / (nu*cosY);
            
            dstPts[dstOff++] = Math.toDegrees(x);
            dstPts[dstOff++] = Math.toDegrees(y);
            if (target3D) {
                z += dx*cosY*cosX + dy*cosY*sinX + dz*sinY + adf*sin2Y - da;
                dstPts[dstOff++] = z;
            }
            srcOff += step;
            dstOff += step;
        }
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, final int srcOff,
                          final float[] dstPts, final int dstOff, int numPts)
    {
        // TODO: Copy the implementation from 'transform(double[]...)'.
        try {
            super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        }
        catch (TransformException exception) {
            // Should not happen.
        }
    }
    
    /**
     * Gets the dimension of input points.
     */
    public int getDimSource() {
        return source3D ? 3 : 2;
    }
    
    /**
     * Gets the dimension of output points.
     */
    public final int getDimTarget() {
        return target3D ? 3 : 2;
    }
    
    /**
     * Returns a hash value for this transform.
     */
    public final int hashCode() {
        final long code = Double.doubleToLongBits(dx) +
                          37*(Double.doubleToLongBits(dy) +
                          37*(Double.doubleToLongBits(dz) +
                          37*(Double.doubleToLongBits(a ) +
                          37*(Double.doubleToLongBits(b ) +
                          37*(Double.doubleToLongBits(da) +
                          37*(Double.doubleToLongBits(db)))))));
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Compares the specified object with
     * this math transform for equality.
     */
    public final boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final AbridgedMolodenskiTransform that = (AbridgedMolodenskiTransform) object;
            return Double.doubleToLongBits(this.dx) == Double.doubleToLongBits(that.dx) &&
                   Double.doubleToLongBits(this.dy) == Double.doubleToLongBits(that.dy) &&
                   Double.doubleToLongBits(this.dz) == Double.doubleToLongBits(that.dz) &&
                   Double.doubleToLongBits(this.a ) == Double.doubleToLongBits(that.a ) &&
                   Double.doubleToLongBits(this.b ) == Double.doubleToLongBits(that.b ) &&
                   Double.doubleToLongBits(this.da) == Double.doubleToLongBits(that.da) &&
                   Double.doubleToLongBits(this.db) == Double.doubleToLongBits(that.db) &&
                   this.source3D == that.source3D &&
                   this.target3D == that.target3D;
        }
        return false;
    }
    
    /**
     * Returns the WKT for this math transform.
     */
    public final String toString() {
        final StringBuffer buffer = paramMT("Abridged_Molodenski");
        addParameter(buffer, "dim", getDimSource());
        addParameter(buffer, "dx",              dx);
        addParameter(buffer, "dy",              dy);
        addParameter(buffer, "src_semi_major",   a);
        addParameter(buffer, "src_semi_minor",   b);
        addParameter(buffer, "tgt_semi_major",   a-da);
        addParameter(buffer, "tgt_semi_minor",   b-db);
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * The provider for {@link AbridgedMolodenskiTransform}.
     *
     * @version $Id: AbridgedMolodenskiTransform.java,v 1.5 2002/10/09 08:45:00 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Provider extends MathTransformProvider {
        /**
         * Create a provider.
         */
        public Provider() {
            super("Abridged_Molodenski", ResourceKeys.ABRIDGED_MOLODENSKI_TRANSFORM, null);
            putInt("dim",         3, POSITIVE_RANGE);
            put("dx",             Double.NaN, null);
            put("dy",             Double.NaN, null);
            put("dz",             0,          null);
            put("src_semi_major", Double.NaN, POSITIVE_RANGE);
            put("src_semi_minor", Double.NaN, POSITIVE_RANGE);
            put("tgt_semi_major", Double.NaN, POSITIVE_RANGE);
            put("tgt_semi_minor", Double.NaN, POSITIVE_RANGE);
        }
        
        /**
         * Returns a transform for the specified parameters.
         *
         * @param  parameters The parameter values in standard units.
         * @return A {@link MathTransform} object of this classification.
         */
        public MathTransform create(final ParameterList parameters) {
            return new AbridgedMolodenskiTransform(parameters);
        }
    }
}
