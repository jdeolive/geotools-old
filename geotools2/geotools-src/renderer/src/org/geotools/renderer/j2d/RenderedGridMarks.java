/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.ObjectInputStream;
import java.io.IOException;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.resources.XMath;
import org.geotools.resources.XAffineTransform;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Représentation graphique de marques disposées sur une grille. Cette classe reprend
 * les fonctionalités de {@link RenderedMarks} en ajoutant la contrainte que les marques
 * doivent être disposées sur une grille régulière. Cette contrainte supplémentaire permet
 * de:
 *
 * <ul>
 *   <li>Optimiser la vitesse d'affichage.</li>
 *   <li>Décimer la densité des marques en fonction du zoom.</li>
 * </ul>
 *
 * @version $Id: RenderedGridMarks.java,v 1.2 2003/02/20 11:18:08 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class RenderedGridMarks extends RenderedMarks {
    /**
     * Nombre de points selon l'axe des <var>x</var>.
     */
    private int width;

    /**
     * Nombre de points selon l'axe des <var>y</var>.
     */
    private int height;

    /**
     * Nombre de points à moyenner selon l'axe des <var>x</var> et des <var>y</var>.
     * Ce nombre doit être supérieur à 0. La valeur <code>1</code> signifie qu'aucune
     * décimation ne sera faite.
     */
    private int decimateX=1, decimateY=1;

    /**
     * Espae minimal (en points) à laisser entre les points de la grille selon les axes
     * <var>x</var> et <var>y</var>. La valeur 0 désactive la décimation selon cet axe.
     */
    private int spaceX=0, spaceY=0;

    /**
     * Indique si la décimation est active. Ce champ prend la valeur
     * <code>true</code> si <code>decimateX</code> ou <code>decimateY</code>
     * sont supérieurs à 1.
     */
    private boolean decimate = false;

    /**
     * Indique si la décimation automatique est active. Ce champ prend la
     * valeur <code>true</code> lorsque {@link #setAutoDecimation} est
     * appellée et que <code>spaceX</code> ou <code>spaceY</code> sont
     * supérieurs à 0.
     */
    private boolean autoDecimate = false;

    /**
     * Transformation affine servant à passer des indices
     * vers les coordonnées (<var>x</var>,<var>y</var>).
     */
    private final AffineTransform transform = new AffineTransform();

    /**
     * Index du dernier élément dont on a obtenu les composantes U et V du vecteur.
     */
    private transient int lastIndex = -1;

    /**
     * Indices X et Y calculées lors du dernier appel de {@link #computeUV}.
     */
    private transient double lastI, lastJ;

    /**
     * Composante U et V calculées lors du dernier appel de {@link #computeUV}.
     */
    private transient double lastU, lastV;

    /**
     * Procède à la lecture binaire de cet objet, puis initialise des champs internes.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        lastIndex = -1;
    }

    /**
     * Construit un ensemble de marques. Les coordonnées de ces
     * marques seront exprimées selon le système de coordonnées
     * par défaut (WGS 1984).    Ce système de coordonnées peut
     * être changé par un appel à {@link #setCoordinateSystem}.
     */
    protected RenderedGridMarks() {
        super();
    }

    /**
     * Définit la dimension de la grille (en nombre de points) ainsi
     * que la tranformation qui convertit les indices en coordonnées.
     *
     * @param size Dimension de la grille. <code>width</code> est le nombre
     *             de points selon <var>x</var>, et <code>height</code> est
     *             le nombre de points selon <var>y</var>.
     * @param transform Transformation affine convertissant les indices (<var>i</var>,<var>j</var>)
     *             en coordonnées (<var>x</var>,<var>y</var>). Les indices <var>i</var> et
     *             <var>j</var> sont des entiers compris dans les plages <code>[0..width-1]</code>
     *             et <code>[0..height-1]</code> respectivement. Les coordonnées <var>x</var> et
     *             <var>y</var> sont des nombres réels exprimés selon le système de coordonnées
     *             de cette couche ("WGS 1984" par défaut).
     */
    protected void setGrid(final Dimension size, final AffineTransform transform) {
        if (size.width<=1 || size.height<=1) {
            throw new IllegalArgumentException(size.toString());
        }
        synchronized (getTreeLock()) {
            this.width  = size.width;
            this.height = size.height;
            this.transform.setTransform(transform);
        }
        repaint();
    }

    /**
     * Définit la dimension de la grille (en nombre de points) ainsi les coordonnées géographiques
     * de la région couverte par les points de la grille. Le point situé aux index (0,0)
     * correspondra au coin supérieur gauche de la région <code>area</code>. Appeller cette
     * methode est équivalent à appeller {@link #setGrid(Dimension, AffineTransform)} avec
     * la transformation affine suivante:
     *
     * <blockquote><pre>
     * | dx/(width-1)      0           Xmin  |
     * |     0       -dy/(height-1)    Ymax  |
     * |     0             0             1   |
     * </pre></blockquote>
     *
     * où <var>dx</var> et <var>dy</var> sont les largeur et hauteur de <code>area</code> (en
     * coordonnées de cette couche), <var>X<sub>min</sub></var> et <var>Y<sub>max</sub></var>
     * sont les coordonnées <var>x</var> et <var>y</var> minimale ou maximale de <code>area</code>,
     * et <var>width</var> et <var>height</var> sont le nombre de points selon <var>x</var> et
     * <var>y</var> respectivement.
     *
     * @param size Dimension de la grille. <code>width</code> est le nombre
     *             de points selon <var>x</var>, et <code>height</code> est
     *             le nombre de points selon <var>y</var>.
     * @param area Coordonnées géographiques de la région couverte par les
     *             points de la grille. Ces coordonnées doivent être exprimées
     *             selon le système de coordonnées de cette couche ("WGS 1984" par défaut).
     */
    protected void setGrid(final Dimension size, final Rectangle2D area) {
        double    dx     = area.getWidth ();
        double    dy     = area.getHeight();
        final int width  = size.width;
        final int height = size.height;
        if (dx>0 && dy>0) {
            if (width>=2 && height>=2) {
                dx /= (width  -1);
                dy /= (height -1);
            } else {
                throw new IllegalArgumentException(size.toString());
            }
        } else if (!(dx==0 && dy==0 && width==1 && height==1)) {
            throw new IllegalArgumentException(area.toString());
        }
        synchronized (getTreeLock()) {
            transform.setTransform(dx, 0, 0, -dy, area.getMinX(), area.getMaxY());
            this.width  = width;
            this.height = height;
        }
        repaint();
    }

    /**
     * Spécifie une décimation à appliquer sur la grille lors de l'affichage. Cette
     * décimation n'affecte pas les indices <var>i</var> et <var>j</var> transmis aux
     * méthodes {@link #getAmplitude(int,int)} et {@link #getDirection(int,int)}. Elle
     * affecte toutefois les index transmis aux méthodes qui ne reçoivent qu'un argument
     * <code>index</code>, comme {@link #getAmplitude(int)} et {@link #getDirection(int)}.
     * Par défaut, ces dernières retourneront la moyenne des vecteurs décimés.
     *
     * @param decimateX Décimation selon <var>x</var>, ou 1 pour ne pas en faire.
     * @param decimateY Décimation selon <var>y</var>, ou 1 pour ne pas en faire.
     */
    public void setDecimation(final int decimateX, final int decimateY) {
        if (decimateX <=0) {
            throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_NOT_GREATER_THAN_ZERO_$1, new Integer(decimateX)));
        }
        if (decimateY <=0) {
            throw new IllegalArgumentException(Resources.format(
                            ResourceKeys.ERROR_NOT_GREATER_THAN_ZERO_$1, new Integer(decimateY)));
        }
        if (decimateX!=this.decimateX || decimateY!=this.decimateY) {
            synchronized (getTreeLock()) {
                autoDecimate   = false;
                this.decimateX = decimateX;
                this.decimateY = decimateY;
                decimate = (decimateX!=1 || decimateY!=1);
            }
            repaint();
        }
    }

    /**
     * Décime automatiquement les points de la grille de façon à conserver un espace
     * d'au moins <code>spaceX</code> et <code>spaceY</code> entre chaque point.
     *
     * @param spaceX Espae minimale (en points) selon <var>x</var> à laisser entre les
     *        points de la grille. La valeur 0 désactive la décimation selon cet axe.
     * @param spaceY Espae minimale (en points) selon <var>y</var> à laisser entre les
     *        points de la grille. La valeur 0 désactive la décimation selon cet axe.
     */
    public void setAutoDecimation(final int spaceX, final int spaceY) {
        if (spaceX < 0) {
            throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_BAD_ARGUMENT_$2, "spaceX", new Integer(spaceX)));
        }
        if (spaceY < 0) {
            throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_BAD_ARGUMENT_$2, "spaceY", new Integer(spaceY)));
        }
        if (spaceX!=this.spaceX || spaceY!=this.spaceY) {
            synchronized (getTreeLock()) {
                this.spaceX  = spaceX;
                this.spaceY  = spaceY;
                autoDecimate = (spaceX!=0 || spaceY!=0);
            }
            repaint();
        }
    }

    /**
     * Retourne le nombre de points selon <var>x</var> (<code>width</code>)
     * et selon <var>y</var> (<code>height</code>).
     */
    public Dimension getSize() {
        synchronized (getTreeLock()) {
            return new Dimension(width, height);
        }
    }

    /**
     * Retourne l'amplitude de la valeur à un point donné.
     *
     * @param  i Index du point selon <var>x</var>, dans la plage <code>[0..width-1]</code>.
     * @param  j Index du point selon <var>y</var>, dans la plage <code>[0..height-1]</code>.
     * @return Amplitude de la valeur à la position spécifiée, selon les unités {@link #getAmplitudeUnit}.
     */
    public abstract double getAmplitude(final int i, final int j);

    /**
     * Retourne la direction de la valeur à un point donné. L'implémentation par
     * défaut retourne toujours 0.
     *
     * @param  i Index du point selon <var>x</var>, dans la plage <code>[0..width-1]</code>.
     * @param  j Index du point selon <var>y</var>, dans la plage <code>[0..height-1]</code>.
     * @return Direction de la valeur à la position spécifiée, en radians arithmétiques.
     */
    public double getDirection(final int i, final int j) {
        return 0;
    }

    /**
     * Retourne les indices qui correspondent aux coordonnées spécifiées.
     * Ces indices seront utilisées par {@link #isVisible(int,Rectangle)}
     * pour vérifier si un point est dans la partie visible.
     *
     * @param visibleArea Coordonnées logiques de la région visible à l'écran.
     */
    final Rectangle getUserClip(final Rectangle2D visibleArea) {
        assert Thread.holdsLock(getTreeLock());
        if (visibleArea != null) try {
            // Note: on profite du fait que {@link Rectangle#setRect}
            //       arrondie correctement vers les limites supérieures.
            final Rectangle bounds= (Rectangle) XAffineTransform.inverseTransform(transform,
                                                              visibleArea, new Rectangle());
            bounds.x      = (bounds.x      -1) / decimateX;
            bounds.y      = (bounds.y      -1) / decimateY;
            bounds.width  = (bounds.width  +2) / decimateX +1;
            bounds.height = (bounds.height +2) / decimateY +1;
            return bounds;
        } catch (NoninvertibleTransformException exception) {
            // Retourne un clip englobant toutes les coordonnées.
        }
        return new Rectangle(0, 0, width, height);
    }

    /**
     * Indique si la station à l'index spécifié est visible
     * dans le clip spécifié. Le rectangle <code>clip</code>
     * doit avoir été obtenu par {@link #getUserClip}.
     */
    final boolean isVisible(final int index, final Rectangle clip) {
        if (clip == null) {
            return true;
        }
        assert Thread.holdsLock(getTreeLock());
        final int decWidth = width/decimateX;
        return clip.contains(index%decWidth, index/decWidth);
    }

    /**
     * Procède au traçage des marques de cette grille.
     *
     * @throws TransformException si une projection
     *         cartographique était nécessaire et a échouée.
     */
    protected void paint(final RenderingContext context) throws TransformException {
        if (autoDecimate) {
            assert Thread.holdsLock(getTreeLock());
            Point2D delta = new Point2D.Double(1,1);
            delta = transform.deltaTransform(delta, delta);
            delta = context.getAffineTransform(context.mapCS, context.textCS).deltaTransform(delta, delta);
            final int decimateX = Math.max(1, (int)Math.ceil(spaceX/delta.getX()));
            final int decimateY = Math.max(1, (int)Math.ceil(spaceY/delta.getY()));
            if (decimateX!=this.decimateX || decimateY!=this.decimateY) {
                this.decimateX = decimateX;
                this.decimateY = decimateY;
                decimate = (decimateX!=1 || decimateY!=1);
                clearCache();
            }
        }
        super.paint(context);
    }

    /**
     * Retourne le nombre de points de cette grille. Le nombre de point retourné
     * tiendra compte de la décimation spécifiée avec {@link #setDecimation}.
     */
    public final int getCount() {
        synchronized (getTreeLock()) {
            return (width/decimateX) * (height/decimateY);
        }
    }

    /**
     * Retourne les coordonnées (<var>x</var>,<var>y</var>) d'un point de la grille.
     * Les coordonnées <var>x</var> et <var>y</var> seront exprimées selon le système
     * de coordonnées spécifié lors de la construction (WGS 1984 par défaut).
     *
     * Si une décimation a été spécifiée avec la méthode {@link #setDecimation},
     * alors la position retournée sera située au milieu des points à moyenner.
     */
    public final Point2D getPosition(final int index) {
        final Point2D point;
        if (!decimate) {
            point = new Point2D.Double(index%width, index/width);
        } else {
            if (lastIndex != index) {
                computeUV(index);
            }
            point = new Point2D.Double(lastI, lastJ);
        }
        return transform.transform(point, point);
    }

    /**
     * Retourne l'amplitude à la position d'une marque. Si aucune décimation n'est à
     * faire, alors cette méthode ne fait qu'appeler {@link #getAmplitude(int,int)}.
     * Si une décimation a été spécifiée avec la méthode {@link #setDecimation}, alors
     * cette méthode calcule la moyenne vectorielle (la moyenne des composantes
     * <var>u</var> et <var>v</var>) aux positions des marques à décimer, et retourne
     * l'amplitude du vecteur moyen.
     */
    public final double getAmplitude(final int index) {
        if (!decimate) {
            return getAmplitude(index%width, index/width);
        }
        if (lastIndex != index) {
            computeUV(index);
        }
        return XMath.hypot(lastU, lastV);
    }

    /**
     * Retourne la direction de la valeur d'une marque. Si aucune décimation n'est à
     * faire, alors cette méthode ne fait qu'appeler {@link #getAmplitude(int,int)}.
     * Si une décimation a été spécifiée avec la méthode {@link #setDecimation}, alors
     * cette méthode calcule la moyenne vectorielle (la moyenne des composantes
     * <var>u</var> et <var>v</var>) aux positions des marques à décimer, et retourne
     * la direction du vecteur moyen.
     */
    public final double getDirection(final int index) {
        if (!decimate) {
            return getDirection(index%width, index/width);
        }
        if (lastIndex != index) {
            computeUV(index);
        }
        return Math.atan2(lastV, lastU);
    }

    /**
     * Calcule les composantes U et V du vecteur à l'index spécifié.
     */
    private void computeUV(final int index) {
        int    count = 0;
        int    sumI  = 0;
        int    sumJ  = 0;
        double vectU = 0;
        double vectV = 0;
        final int decWidth = width/decimateX;
        final int imin = (index % decWidth)*decimateX;
        final int jmin = (index / decWidth)*decimateY;
        for (int i=imin+decimateX; --i>=imin;) {
            for (int j=jmin+decimateY; --j>=jmin;) {
                final double amplitude = getAmplitude(i, j);
                final double direction = getDirection(i, j);
                final double U = amplitude*Math.cos(direction);
                final double V = amplitude*Math.sin(direction);
                if (!Double.isNaN(U) && !Double.isNaN(V)) {
                    vectU += U;
                    vectV += V;
                    sumI  += i;
                    sumJ  += j;
                    count++;
                }
            }
        }
        this.lastIndex = index;
        this.lastI     = (double)sumI / count;
        this.lastJ     = (double)sumJ / count;
        this.lastU     = vectU/count;
        this.lastV     = vectV/count;
    }
}
