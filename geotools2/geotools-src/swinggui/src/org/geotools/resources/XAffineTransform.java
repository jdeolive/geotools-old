/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
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
 *     UNITED KINDOM: James Macgill
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

// Geometry
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;


/**
 * Utility methods for affine transforms. This class provides a set
 * of public static methods working on any {@link AffineTransform}.
 * <br><br>
 * Class <code>XAffineTransform</code>  overrides all mutable methods
 * of {@link AffineTransform} in order to check for permission before
 * to change the transform's state. If {@link #checkPermission} is
 * defined to always thrown an exception, then <code>XAffineTransform</code>
 * is immutable.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public abstract class XAffineTransform extends AffineTransform {
    /**
     * Serial number for interoperability with different versions.
     */
//  private static final long serialVersionUID = 4891543057571195291L;

    /**
     * Tolerance value for floating point comparaisons.
     */
    private static final double EPS=1E-6;

    /**
     * Constructs a new <code>XAffineTransform</code> that is a
     * copy of the specified <code>AffineTransform</code> object.
     */
    protected XAffineTransform(final AffineTransform tr) {
        super(tr);
    }

    /**
     * Check if the caller is allowed to change this
     * <code>XAffineTransform</code>'s state.
     */
    protected abstract void checkPermission();

    /**
     * Check for permission before translating this transform.
     */
    public void translate(double tx, double ty) {
        checkPermission();
        super.translate(tx, ty);
    }

    /**
     * Check for permission before rotating this transform.
     */
    public void rotate(double theta) {
        checkPermission();
        super.rotate(theta);
    }

    /**
     * Check for permission before rotating this transform.
     */
    public void rotate(double theta, double x, double y) {
        checkPermission();
        super.rotate(theta, x, y);
    }

    /**
     * Check for permission before scaling this transform.
     */
    public void scale(double sx, double sy) {
        checkPermission();
        super.scale(sx, sy);
    }

    /**
     * Check for permission before shearing this transform.
     */
    public void shear(double shx, double shy) {
        checkPermission();
        super.shear(shx, shy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToIdentity() {
        checkPermission();
        super.setToIdentity();
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToTranslation(double tx, double ty) {
        checkPermission();
        super.setToTranslation(tx, ty);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToRotation(double theta) {
        checkPermission();
        super.setToRotation(theta);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToRotation(double theta, double x, double y) {
        checkPermission();
        super.setToRotation(theta, x, y);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToScale(double sx, double sy) {
        checkPermission();
        super.setToScale(sx, sy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setToShear(double shx, double shy) {
        checkPermission();
        super.setToShear(shx, shy);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setTransform(AffineTransform Tx) {
        checkPermission();
        super.setTransform(Tx);
    }

    /**
     * Check for permission before setting this transform.
     */
    public void setTransform(double m00, double m10,
                             double m01, double m11,
                             double m02, double m12) {
        checkPermission();
        super.setTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * Check for permission before concatenating this transform.
     */
    public void concatenate(AffineTransform Tx) {
        checkPermission();
        super.concatenate(Tx);
    }

    /**
     * Check for permission before concatenating this transform.
     */
    public void preConcatenate(AffineTransform Tx) {
        checkPermission();
        super.preConcatenate(Tx);
    }

    /**
     * Retourne un rectangle qui contient entièrement la transformation
     * directe de <code>bounds</code>. Cette opération est l'équivalent
     * de <code>createTransformedShape(bounds).getBounds2D()</code>.
     *
     * @param transform Transformation affine à utiliser.
     * @param bounds    Rectangle à transformer. Ce rectangle ne sera pas modifié.
     * @param dest      Rectangle dans lequel placer le résultat. Si nul, un
     *                  nouveau rectangle sera créé.
     *
     * @return La transformation directe du rectangle <code>bounds</code>.
     */
    public static Rectangle2D transform(final AffineTransform transform,
                                        final Rectangle2D     bounds,
                                        final Rectangle2D     dest) {
        double xmin=Double.POSITIVE_INFINITY;
        double ymin=Double.POSITIVE_INFINITY;
        double xmax=Double.NEGATIVE_INFINITY;
        double ymax=Double.NEGATIVE_INFINITY;
        final Point2D.Double point=new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
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
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Retourne un rectangle qui contient entièrement la transformation
     * inverse de <code>bounds</code>. Cette opération est l'équivalent de
     * <code>createInverse().createTransformedShape(bounds).getBounds2D()</code>.
     *
     * @param transform Transformation affine à utiliser.
     * @param bounds    Rectangle à transformer. Ce rectangle ne sera pas modifié.
     * @param dest      Rectangle dans lequel placer le résultat. Si nul, un
     *                  nouveau rectangle sera créé.
     *
     * @return La transformation inverse du rectangle <code>bounds</code>.
     * @throws NoninvertibleTransformException si la transformation affine ne
     *         peut pas être inversée.
     */
    public static Rectangle2D inverseTransform(final AffineTransform transform,
                                               final Rectangle2D     bounds,
                                               final Rectangle2D     dest)
        throws NoninvertibleTransformException
    {
        double xmin=Double.POSITIVE_INFINITY;
        double ymin=Double.POSITIVE_INFINITY;
        double xmax=Double.NEGATIVE_INFINITY;
        double ymax=Double.NEGATIVE_INFINITY;
        final Point2D.Double point=new Point2D.Double();
        for (int i=0; i<4; i++) {
            point.x = (i&1)==0 ? bounds.getMinX() : bounds.getMaxX();
            point.y = (i&2)==0 ? bounds.getMinY() : bounds.getMaxY();
            transform.inverseTransform(point, point);
            if (point.x<xmin) xmin=point.x;
            if (point.x>xmax) xmax=point.x;
            if (point.y<ymin) ymin=point.y;
            if (point.y>ymax) ymax=point.y;
        }
        if (dest!=null) {
            dest.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
            return dest;
        }
        return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }

    /**
     * Calcule la transformation affine inverse d'un
     * point sans prendre en compte la translation.
     *
     * @param transform Transformation affine à utiliser.
     * @param source    Point à transformer. Ce rectangle ne sera pas modifié.
     * @param dest      Point dans lequel placer le résultat. Si nul, un nouveau
     *                  point sera créé.
     *
     * @return La transformation inverse du point <code>source</code>.
     * @throws NoninvertibleTransformException si la transformation affine ne
     *         peut pas être inversée.
     */
    public static Point2D inverseDeltaTransform(final AffineTransform transform,
                                                final Point2D         source,
                                                final Point2D         dest)
        throws NoninvertibleTransformException
    {
        final double m00 = transform.getScaleX();
        final double m11 = transform.getScaleY();
        final double m01 = transform.getShearX();
        final double m10 = transform.getShearY();
        final double det = m00*m11 - m01*m10;
        if (!(Math.abs(det) > Double.MIN_VALUE)) {
            return transform.createInverse().deltaTransform(source, dest);
        }
        final double x = source.getX();
        final double y = source.getY();
        if (dest!=null) {
            dest.setLocation((x*m11 - y*m01)/det,
                             (y*m00 - x*m10)/det);
            return dest;
        }
        return new Point2D.Double((x*m11 - y*m01)/det,
                                  (y*m00 - x*m10)/det);
    }

    /**
     * Retourne le facteur d'échelle <var>x</var> en annulant
     * l'effet d'une éventuelle rotation. Ce facteur est calculé par
     * <IMG src="{@docRoot}/org/geotools/map/layer/doc-files/equation1.gif">.
     */
    public static double getScaleX0(final AffineTransform zoom) {
        return XMath.hypot(zoom.getScaleX(), zoom.getShearX());
    }

    /**
     * Retourne le facteur d'échelle <var>y</var> en annulant
     * l'effet d'une éventuelle rotation. Ce facteur est calculé par
     * <IMG src="{@docRoot}/org/geotools/map/layer/doc-files/equation2.gif">.
     */
    public static double getScaleY0(final AffineTransform zoom) {
        return XMath.hypot(zoom.getScaleY(), zoom.getShearY());
    }

    /**
     * Retourne une transformation affine représentant un zoom fait autour d'un
     * point central (<var>x</var>,<var>y</var>). Les transformations laisseront
     * inchangées la coordonnée (<var>x</var>,<var>y</var>) spécifiée.
     *
     * @param sx Echelle le long de l'axe des <var>x</var>.
     * @param sy Echelle le long de l'axe des <var>y</var>.
     * @param  x Coordonnées <var>x</var> du point central.
     * @param  y Coordonnées <var>y</var> du point central.
     * @return   Transformation affine d'un zoom qui laisse
     *           la coordonnée (<var>x</var>,<var>y</var>)
     *           inchangée.
     */
    public static AffineTransform getScaleInstance(final double sx, final double sy,
                                                   final double  x, final double  y) {
        return new AffineTransform(sx, 0, 0, sy, (1-sx)*x, (1-sy)*y);
    }

    /*
     * Vérifie si les coéfficients de la matrice sont proches de valeurs
     * entières. Si c'est le cas, ces coéfficients seront arrondis aux valeurs
     * entières les plus proches. Cet arrondissement est utile par exemple pour
     * accelérer les affichages d'images. Il est surtout efficace lorsque l'on
     * sait qu'une matrice a des chances d'être proche de la matrice identitée.
     */
    public static void round(final AffineTransform zoom) {
        double r;
        final double m00,m01,m10,m11;
        if (Math.abs((m00=Math.rint(r=zoom.getScaleX()))-r) <= EPS &&
            Math.abs((m01=Math.rint(r=zoom.getShearX()))-r) <= EPS &&
            Math.abs((m11=Math.rint(r=zoom.getScaleY()))-r) <= EPS &&
            Math.abs((m10=Math.rint(r=zoom.getShearY()))-r) <= EPS)
        {
            if ((m00!=0 || m01!=0) && (m10!=0 || m11!=0)) {
                double m02=Math.rint(r=zoom.getTranslateX()); if (!(Math.abs(m02-r)<=EPS)) m02=r;
                double m12=Math.rint(r=zoom.getTranslateY()); if (!(Math.abs(m12-r)<=EPS)) m12=r;
                zoom.setTransform(m00,m10,m01,m11,m02,m12);
            }
        }
    }
}
