/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.cs;

// J2SE dependencies
import java.io.Serializable;
import javax.vecmath.GMatrix;  // For javadoc

// Geotools dependencies
import org.geotools.pt.Matrix;
import org.geotools.util.Cloneable;
import org.geotools.resources.Utilities;


/**
 * Parameters for a geographic transformation into WGS84.
 * The Bursa Wolf parameters should be applied to geocentric coordinates,
 * where the X axis points towards the Greenwich Prime Meridian, the Y axis
 * points East, and the Z axis points North.
 *
 * @version $Id: WGS84ConversionInfo.java,v 1.7 2004/04/05 03:48:01 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_WGS84ConversionInfo
 */
public class WGS84ConversionInfo implements Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3427461418504464735L;
    
    /** Bursa Wolf shift in meters. */
    public double dx;
    
    /** Bursa Wolf shift in meters. */
    public double dy;
    
    /** Bursa Wolf shift in meters. */
    public double dz;
    
    /** Bursa Wolf rotation in arc seconds. */
    public double ex;
    
    /** Bursa Wolf rotation in arc seconds. */
    public double ey;
    
    /** Bursa Wolf rotation in arc seconds. */
    public double ez;
    
    /** Bursa Wolf scaling in parts per million. */
    public double ppm;
    
    /** Human readable text describing intended region of transformation. */
    public String areaOfUse;
    
    /**
     * Constructs a conversion info with all parameters set to 0.
     */
    public WGS84ConversionInfo() {
    }

    /**
     * Concatenates Bursa Wolf parameters <var><b>f</b></var> to this set of parameters.
     * When <code>WGS84ConversionInfo</code> objects are {@linkplain #getAffineTransform
     * exposed as affine transforms}, transforming a point <var>p</var> by the combined
     * transform is equivalent to first transforming <var>p</var> by <var><b>f</b></var>
     * and then transforming the result by <code>this</code>. In matrix notation, this
     * method does the following:
     * <br><br>
     * <center><code>[combined] = [this] x [f]</code></center>
     * <br><br>
     * The matrix multiplication result may not fit in a <code>WGS84ConversionInfo</code>
     * object. Consequently, <strong>this method computes an approximation valid only if
     * the rotation angles are small</strong>. More specifically, for all expressions of
     * the form &theta;+&theta;<sup>2</sup> (in radians), the &theta;<sup>2</sup> term is
     * dropped. If an exact concatenation is wanted, then the following must be used instead:
     * <br><br>
     * <pre>Matrix c = {@linkplain #getAffineTransform() getAffineTransform()};
     * c.{@linkplain GMatrix#mul(GMatrix) mul}(f.getAffineTransform())</pre>
     *
     * @param  The Bursa Wolf parameters to concatenate to this one.
     * @return The combined Bursa Wolf parameters.
     */
    final WGS84ConversionInfo approximativeConcatenate(final WGS84ConversionInfo f) {
        /*
         * Result of matrix multiplication.  The left column is the destination
         * term. A term may be defined up to 3 times, and those definitions may
         * not be consistent (i.e., the result of a matrix multiplication can't
         * be retrofited in a WGS84ConversionInfo object in all cases). However,
         * expressions are close to the form A+A² where A is some rotation angle
         * in radians, and the disagrement always appears in the A² term. This
         * term can be dropped if |A| << 1 (typical values are below 1E-4).
         *
         *      S:       S*f.S           + -ez*RS*f.ez*f.RS + -ey*RS*f.ey*f.RS
         * -ez*RS:      -S*f.ez*f.RS     + -ez*RS*f.S       +  ey*RS*f.ex*f.RS
         * +ey*RS:       S*f.ey*f.RS     +  ez*RS*f.ex*f.RS +  ey*RS*f.S
         *     dx:       S*f.dx          + -ez*RS*f.dy      +  ey*RS*f.dz       + dx
         *
         * +ez*RS:       ez*RS*f.S       +  S*f.ez*f.RS     +  ex*RS*f.ey*f.RS
         *      S:      -ez*RS*f.ez*f.RS +  S*f.S           + -ex*RS*f.ex*f.RS
         * -ex*RS:       ez*RS*f.ey*f.RS + -S*f.ex*f.RS     + -ex*RS*f.S
         *     dy:       ez*RS*f.dx      +  S*f.dy          + -ex*RS*f.dz       + dy
         *
         * -ey*RS:      -ey*RS*f.S       +  ex*RS*f.ez*f.RS + -S*f.ey*f.RS
         * +ex*RS:       ey*RS*f.ez*f.RS +  ex*RS*f.S       +  S*f.ex*f.RS
         *      S:      -ey*RS*f.ey*f.RS + -ex*RS*f.ex*f.RS +  S*f.S
         *     dz:      -ey*RS*f.dx      +  ex*RS*f.dy      +  S*f.dz           + dz
         */
        final WGS84ConversionInfo c = new WGS84ConversionInfo();
        double errorX, errorY, errorZ;

        final double R  = Math.PI/(180*3600); // Arc seconds to radians
        final double S  = 1 + ppm/1E+6;
        final double S1 = 1 - (ez*f.ez + ey*f.ey)*(R*R);
        final double S2 = 1 - (ez*f.ez + ex*f.ex)*(R*R);
        final double S3 = 1 - (ey*f.ey + ex*f.ex)*(R*R);

        c.ppm = ppm + S*f.ppm;

        double t1, t2;
        errorZ  = Math.abs(t1 =  0.5*(ey*f.ez));
        errorY  = Math.abs(t2 = -0.5*(ez*f.ey));
        c.ex    = R*(t1+t2) + (ex + f.ex);

        errorX  = Math.abs(t1 =  0.5*(ez*f.ex));
        errorZ += Math.abs(t2 = -0.5*(ex*f.ez));
        c.ey    = R*(t1+t2) + (ey + f.ey);

        errorY += Math.abs(t1 =  0.5*(ex*f.ey));
        errorX += Math.abs(t2 = -0.5*(ey*f.ex));
        c.ez    = R*(t1+t2) + (ez + f.ez);

        // Translation terms: no approximation there.
        c.dx = dx + S*(f.dx  + R*(f.dz*ey - f.dy*ez));
        c.dy = dy + S*(f.dy  + R*(f.dx*ez - f.dz*ex));
        c.dz = dz + S*(f.dz  + R*(f.dy*ex - f.dx*ey));

        return c;
    }
    
    /**
     * Returns an affine maps that can be used to define this
     * Bursa Wolf transformation. The formula is as follows:
     *
     * <blockquote><pre>
     * S = 1 + {@link #ppm}/1000000
     *
     * [ X’ ]    [     S   -{@link #ez}*S   +{@link #ey}*S   {@link #dx} ]  [ X ]
     * [ Y’ ]  = [ +{@link #ez}*S       S   -{@link #ex}*S   {@link #dy} ]  [ Y }
     * [ Z’ ]    [ -{@link #ey}*S   +{@link #ex}*S       S   {@link #dz} ]  [ Z ]
     * [ 1  ]    [     0       0       0    1 ]  [ 1 ]
     * </pre></blockquote>
     *
     * This affine transform can be applied on <strong>geocentric</strong>
     * coordinates.
     */
    public Matrix getAffineTransform() {
        // Note: (ex, ey, ez) is a rotation in arc seconds.
        //       We need to convert it into radians (the R
        //       factor in RS). TODO: to be strict, are we
        //       suppose to take the sinus of rotation angles?
        final double  S = 1 + ppm/1E+6;
        final double RS = (Math.PI/(180*3600)) * S;
        return new Matrix(4,4, new double[] {
                 S,  -ez*RS,  +ey*RS,  dx,
            +ez*RS,       S,  -ex*RS,  dy,
            -ey*RS,  +ex*RS,       S,  dz,
                 0,       0,       0,   1
        });
    }
    
    /**
     * Returns a hash value for this object.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        long code = serialVersionUID;
        code = code*37 + Double.doubleToLongBits(dx );
        code = code*37 + Double.doubleToLongBits(dy );
        code = code*37 + Double.doubleToLongBits(dz );
        code = code*37 + Double.doubleToLongBits(ex );
        code = code*37 + Double.doubleToLongBits(ey );
        code = code*37 + Double.doubleToLongBits(ez );
        code = code*37 + Double.doubleToLongBits(ppm);
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Returns a copy of this object.
     */
    public Object clone() {
        try {
            return super.clone();
        }  catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            final InternalError error = new InternalError(exception.getMessage());
            error.initCause(exception);
            throw error;
        }
    }
    
    /**
     * Compares the specified object with
     * this object for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof WGS84ConversionInfo) {
            final WGS84ConversionInfo that = (WGS84ConversionInfo) object;
            return Double.doubleToLongBits(this.dx)  == Double.doubleToLongBits(that.dx)  &&
                   Double.doubleToLongBits(this.dy)  == Double.doubleToLongBits(that.dy)  &&
                   Double.doubleToLongBits(this.dz)  == Double.doubleToLongBits(that.dz)  &&
                   Double.doubleToLongBits(this.ex)  == Double.doubleToLongBits(that.ex)  &&
                   Double.doubleToLongBits(this.ey)  == Double.doubleToLongBits(that.ey)  &&
                   Double.doubleToLongBits(this.ez)  == Double.doubleToLongBits(that.ez)  &&
                   Double.doubleToLongBits(this.ppm) == Double.doubleToLongBits(that.ppm) &&
                   Utilities.equals(this.areaOfUse, that.areaOfUse);
        }
        return false;
    }
    
    /**
     * Returns the Well Known Text (WKT) for this object.
     * The WKT is part of OpenGIS's specification and
     * looks like <code>TOWGS84[dx, dy, dz, ex, ey, ez, ppm]</code>.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer("TOWGS84[");
        buffer.append(dx);        buffer.append(", ");
        buffer.append(dy);        buffer.append(", ");
        buffer.append(dz);        buffer.append(", ");
        buffer.append(ex);        buffer.append(", ");
        buffer.append(ey);        buffer.append(", ");
        buffer.append(ez);        buffer.append(", ");
        buffer.append(ppm);
        buffer.append(']');
        return buffer.toString();
    }
}
