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
package org.geotools.renderer.geom;

// Geometry
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

// Miscellaneous
import java.util.NoSuchElementException;


/**
 * Arrow oriented toward positives <var>x</var> values (0° arithmetic). This shape doesn't
 * have direct support for rotation. To rotate the arrow toward an other direction, use
 * {@link java.awt.geom.AffineTransform}.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/Arrow2D.png"></p>
 * <p align="center">The <code>Arrow2D</code> shape</p>
 * <p>&nbsp;</p>
 *
 * @version $Id: Arrow2D.java,v 1.2 2003/03/19 15:08:07 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Arrow2D extends RectangularShape {
    /**
     * Coordonnées <var>x</var> et <var>y</var> minimales.
     */
    private double minX, minY;

    /**
     * Longueur de la flèche. Cette longueur est mesurée horizontalement (selon
     * l'axe des <var>x</var>) de la queue jusqu'à la pointe de la flèche.
     */
    private double length;

    /**
     * Largeur de la flèche. Cette largeur est mesurée verticalement (selon l'axe
     * des <var>y</var>) le long de la partie la plus large de cette flèche.
     */
    private double thickness;

    /**
     * The arrow's thickness at the tail (<code>x == minX</code>), as a proportion of
     * the {@linkplain #thickness maximal thickness}. Should be a factor between 0 and 1.
     */
    private double sy0 = 0;

    /**
     * The arrow's thickness at the base (<code>x == minX+sx*length</code>), as a proportion
     * of the {@linkplain #thickness maximal thickness}. Should be a factor between 0 and 1.
     */
    private double sy1 = 1.0/3;
    
    /**
     * The base position, as a factor of the total length. Should be a factor between 0 and 1.
     */
    private double sx = 2.0/3;

    /**
     * Construit une flèche donc la surface initiale est nulle.
     */
    public Arrow2D() {
    }

    /**
     * Construit une flèche située aux coordonnées (<var>x</var>,<var>y</var>) avec
     * une longueur selon <var>x</var> de <code>width</code> et une hauteur selon
     * <var>y</var> de <code>height</code>.
     *
     * @param x Coordonnée <var>x</var> minimale.
     * @param y Coordonnée <var>y</var> minimale.
     * @param width  Longueur selon l'axe des <var>x</var>, de la base à la pointe de la flèche.
     * @param height Largeur maximale de la flèche, mesurée selon l'axe des <var>y</var>.
     */
    public Arrow2D(final double x, final double y, final double width, final double height) {
        this.minX      = x;
        this.minY      = y;
        this.length    = width;
        this.thickness = height;
    }

    /**
     * Modifie la largeur et hauteur de la queue de la flèche, en proportion avec les dimensions
     * totales de cette flèche. Ces facteurs doivent être compris entre 0 et 1. Les valeurs par
     * défaut sont de 1/3 selon <var>y</var> et 2/3 selon <var>x</var>, ce qui signifie que la
     * queue de la flèche aura le tiers de la largeur totale disponible et les deux tiers de la
     * longueur disponible. La pointe de la flèche aura le reste. Ces proportions donnent d'assez
     * bons résultats lorsque la flèche est deux fois plus longue que large.
     *
     * @param sx  Proportion de la longueur occupée par la queue de la flèche, entre 0 et 1.
     * @param sy1 Proportion de la largeur occupée par la queue de la flèche près de la base, entre 0 et 1.
     * @param sy0 Proportion de la largeur occupée par le bout de la queue de la flèche, entre 0 et 1.
     */
    public void setTailProportion(double sx, double sy1, double sy0) {
        if (sy1<0) sy1=0;
        if (sy1>1) sy1=1;
        if (sy0<0) sy0=0;
        if (sy0>1) sy0=1;
        if (sx <0) sx =0;
        if (sx >1) sx =1;
        this.sy1=sy1;
        this.sy0=sy0;
        this.sx =sx;
    }

    /**
     * Renvoie la longueur de la queue de la flèche,
     *
     * @return La longueur de la queue, compris de 0 à <code>getWidth</code>.
     */
    public double getTailLength() {
        return sx*length;
    }

    /**
     * Retourne la coordonnée <var>x</var>. Il s'agira du <var>x</var>
     * le plus bas de la superficie couverte par la flêche.
     *
     * @return La coordonnée <var>x</var> minimale.
     */
    public double getX() {
        return minX;
    }

    /**
     * Retourne la coordonnée <var>y</var>. Il s'agira du <var>y</var>
     * le plus bas de la superficie couverte par la flêche.
     *
     * @return La coordonnée <var>y</var> minimale.
     */
    public double getY() {
        return minY;
    }

    /**
     * Retourne la longueur de la flèche, de la base jusqu'à sa pointe.
     * Cette longueur est mesurée selon l'axe des <var>x</var>.
     */
    public double getWidth() {
        return length;
    }

    /**
     * Retourne la largeur de la partie la plus large de la flèche.
     * Cette largeur est mesurée selon l'axe des <var>y</var>.
     */
    public double getHeight() {
        return thickness;
    }

    /**
     * Renvoie la largeur de la flèche à la position <var>x</var>. Si cette position n'est pas
     * comprise de <code>getMinX()</code> à <code>getMaxX()</code>, alors cette méthode retourne 0.
     * Sinon elle retourne la largeur de la flèche à la position spécifiée.
     *
     * @param x Coordonnée <var>x</var> à laquelle on veut la largeur.
     * @return La largeur de la flèche, comprise entre 0 et <code>getHeight</code>.
     */
    public double getHeight(double x) {
        x = (x-minX)/(sx*length);
        if (x<0 || x>1) {
            return 0;
        } else if (x <= 1) {
            return (sy0+(sy1-sy0)*x)*thickness;
        } else {
            return (x-1)*sx/(1-sx)*thickness;
        }
    }

    /**
     * Détermine si la superficie de cette flèche est nulle.
     *
     * @return <code>true</code> si la superficie de cette flèche est nulle.
     */
    public boolean isEmpty() {
        return !(length>0 && thickness>0);
    }

    /**
     * Affecte une nouvelle position et une nouvelle largeur à la flèche.
     *
     * @param x Coordonnéex <var>x</var> minimale.
     * @param y Coordonnéex <var>y</var> minimale.
     * @param width  Longueur, de la base à la pointe de la flèche.
     * @param height Largeur de la partie la plus large de la flèche.
     */
    public void setFrame(final double x, final double y, final double width, final double height) {
        this.minX      = x;
        this.minY      = y;
        this.length    = width;
        this.thickness = height;
    }

    /**
     * Renvoie les dimensions de cette flèche.
     */
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(minX, minY, length, thickness);
    }

    /**
     * Indique si cette flèche contient le point spécifié.
     *
     * @param x Coordonnée <var>x</var> du point à vérifier.
     * @param y Coordonnée <var>y</var> du point à vérifier.
     */
    public boolean contains(final double x, double y) {
        if (x < minX) {
            return false;
        }
        final double base = minX + sx*length;
        if (x <= base) {
            /*
             * Point dans la queue. Vérifie s'il se trouve dans le triangle...
             */
            double yMaxAtX = 0.5*thickness;
            y -= (minY + yMaxAtX);
            yMaxAtX *= sy0+(sy1-sy0)*((x-minX)/(base-minX));
            return (Math.abs(y) <= yMaxAtX);
        } else {
            /*
             * Point dans la pointe. Vérifie s'il se trouve dans le triangle.
             */
            final double maxX = minX + length;
            if (x > maxX) {
                return false;
            }
            double yMaxAtX = 0.5*thickness;
            y -= (minY + yMaxAtX);
            yMaxAtX *= (maxX-x)/(maxX-base);
            return (Math.abs(y) <= yMaxAtX);
        }
    }

    /**
     * Indique si la flèche contient entièrement le rectangle spécifié. Ce sera
     * le cas si la flèche contient chacun des quatre coins du rectangle.
     *
     * @param x Coordonnée <var>x</var> minimale du rectangle.
     * @param y Coordonnée <var>y</var> minimale du rectangle.
     * @param width Largeur du rectangle.
     * @param height Hauteur du rectangle.
     * @return <code>true</code> si la flèche contient le rectangle.
     */
    public boolean contains(final double x, final double y, final double width, final double height)
    {
        return  contains(x      , y       ) &&
                contains(x+width, y       ) &&
                contains(x+width, y+height) &&
                contains(x      , y+height);
    }

    /**
     * Indique si la flèche intersepte le rectangle spécifié.
     *
     * @param x Coordonnée <var>x</var> minimale du rectangle.
     * @param y Coordonnée <var>y</var> minimale du rectangle.
     * @param width Largeur du rectangle.
     * @param height Hauteur du rectangle.
     * @return <code>true</code> si la flèche intersepte le rectangle.
     */
    public boolean intersects(final double x, final double y, final double width, final double height) {
        final double right = x + width;
        final double maxX  = minX + length;
        if (x <= maxX  &&  right >= minX) {
            final double top = y + height;
            final double maxY = minY + thickness;
            if (y <= maxY  &&  top >= minY) {
                /*
                 * The rectangle intersects this arrow's bounding box. Now, check if a
                 * rectangle corner is outside the arrow (while in the bounding box).
                 * If such a case is found, returns false.
                 */
                final double base = minX + length*sx;
                if (x > base) {
                    double yMaxAtX = 0.5*thickness;
                    final double centerY = minY + yMaxAtX;
                    if (y >= centerY) {
                        yMaxAtX *= (maxX-x)/(maxX-base);
                        if (!(y-centerY <= yMaxAtX)) {
                            return false;
                        }
                    } else if (top <= centerY) {
                        yMaxAtX *= (maxX-x)/(maxX-base);
                        if (!(centerY-top <= yMaxAtX)) {
                            return false;
                        }
                    }
                } else if (right < base) {
                    double yMaxAtX = 0.5*thickness;
                    final double centerY = minY + yMaxAtX;
                    if (y >= centerY) {
                        yMaxAtX *= sy0+(sy1-sy0)*((x-minX)/(base-minX));
                        if (!(y-centerY <= yMaxAtX)) {
                            return false;
                        }
                    } else if (top <= centerY) {
                        yMaxAtX *= sy0+(sy1-sy0)*((x-minX)/(base-minX));
                        if (!(centerY-top <= yMaxAtX)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne un itérateur permettant de balayer les segments
     * formant la flèche. Comme cette flèche ne contient pas de
     * portion arrondie, cette méthode retourne la même chose
     * que l'autre méthode <code>getPathIterator</code>.
     *
     * @param at Une transformation affine facultative.
     * @param flatness Ignoré.
     */
    public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
        return new Iterator(at);
    }

    /**
     * Retourne un itérateur permettant de balayer les segments formant la flèche.
     *
     * @param at Une transformation affine facultative.
     */
    public PathIterator getPathIterator(final AffineTransform at) {
        return new Iterator(at);
    }

    /**
     * Itérateur balayant les coordonnées de la flèche.
     */
    private class Iterator implements PathIterator {
        /**
         * Transformation affine à utiliser pour
         * transformer les coordonnées. Peut être nul.
         */
        private final AffineTransform at;

        /**
         * Quelques variables calculées lors de la construction
         * afin d'éviter d'avoir à les recalculer trop souvent.
         */
        private final double halfBottom0, halfBottom1, center, halfTop1, halfTop0, base;

        /**
         * Code indiquant quel sera la prochaine donnée à retourner pour cette flèche.
         */
        private int code;

        /**
         * Construit un itérateur balayant les coordonnées de la flèche.
         *
         * @param at Transformation affine à utiliser pour transformer les coordonnées. Peut être nul.
         */
        Iterator(final AffineTransform at) {
            this.at = at;
            final double halfheight = 0.5*thickness;
            halfBottom0 = minY + halfheight*(1-sy0);
            halfBottom1 = minY + halfheight*(1-sy1);
            center      = minY + halfheight;
            halfTop1    = minY + halfheight*(1+sy1);
            halfTop0    = minY + halfheight*(1+sy0);
            base        = minX + sx*length;
        }

        /**
         * Retourne la règle utilisé pour remplir le polygone.
         *
         * @return Toujours <code>EVEN_ODD</code>.
         */
        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }

        /**
         * Passe au point suivant.
         */
        public void next() {
            code++;
        }

        /**
         * Renvoie les coordonnées du segment actuel.
         *
         * @param Tableau dans lequel mémoriser les coordonnées.
         * @return Le code des coordonnées mémorisées.
         */
        public int currentSegment(final float[] coords) {
            switch (code) {
                case 0: coords[0]=(float) minX;          coords[1]=(float) halfBottom0;      break;
                case 1: coords[0]=(float) base;          coords[1]=(float) halfBottom1;      break;
                case 2: coords[0]=(float) base;          coords[1]=(float) minY;             break;
                case 3: coords[0]=(float) (minX+length); coords[1]=(float) center;           break;
                case 4: coords[0]=(float) base;          coords[1]=(float) (minY+thickness); break;
                case 5: coords[0]=(float) base;          coords[1]=(float) halfTop1;         break;
                case 6: coords[0]=(float) minX;          coords[1]=(float) halfTop0;         break;
                case 7: coords[0]=(float) minX;          coords[1]=(float) halfBottom0;      break;
                case 8:  return SEG_CLOSE;
                default: throw new NoSuchElementException();
            }
            if (at!=null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            return (code==0) ? SEG_MOVETO : SEG_LINETO;
        }

        /**
         * Renvoie les coordonnées du segment actuel.
         *
         * @param Tableau dans lequel mémoriser les coordonnées.
         * @return Le code des coordonnées mémorisées.
         */
        public int currentSegment(final double[] coords)  {
            switch (code) {
                case 0: coords[0]=minX;        coords[1]=halfBottom0;    break;
                case 1: coords[0]=base;        coords[1]=halfBottom1;    break;
                case 2: coords[0]=base;        coords[1]=minY;           break;
                case 3: coords[0]=minX+length; coords[1]=center;         break;
                case 4: coords[0]=base;        coords[1]=minY+thickness; break;
                case 5: coords[0]=base;        coords[1]=halfTop1;       break;
                case 6: coords[0]=minX;        coords[1]=halfTop0;       break;
                case 7: coords[0]=minX;        coords[1]=halfBottom0;    break;
                case 8:  return SEG_CLOSE;
                default: throw new NoSuchElementException();
            }
            if (at!=null) {
                at.transform(coords, 0, coords, 0, 1);
            }
            return (code==0) ? SEG_MOVETO : SEG_LINETO;
        }

        /**
         * Indique si l'on a terminé de balayer les points.
         *
         * @return <code>true</code> si le balayage est terminé.
         */
        public boolean isDone() {
            return code > 8;
        }
    }

    /**
     * Indique si cette flèche est identique à la flèche spécifiée.
     */
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj!=null && getClass().equals(obj.getClass())) {
            final Arrow2D cast = (Arrow2D) obj;
            return Double.doubleToLongBits(thickness) == Double.doubleToLongBits(cast.thickness) &&
                   Double.doubleToLongBits(length   ) == Double.doubleToLongBits(cast.length   ) &&
                   Double.doubleToLongBits(minX     ) == Double.doubleToLongBits(cast.minX     ) &&
                   Double.doubleToLongBits(minY     ) == Double.doubleToLongBits(cast.minY     ) &&
                   Double.doubleToLongBits(sx       ) == Double.doubleToLongBits(cast.sx       ) &&
                   Double.doubleToLongBits(sy0      ) == Double.doubleToLongBits(cast.sy1      ) &&
                   Double.doubleToLongBits(sy1      ) == Double.doubleToLongBits(cast.sy0      );
        } else {
            return false;
        }
    }

    /**
     * Retourne un code "hash value" pour cette flèche.
     */
    public int hashCode() {
        final long code=Double.doubleToLongBits(thickness) + 37*
                       (Double.doubleToLongBits(length   ) + 37*
                       (Double.doubleToLongBits(minX     ) + 37*
                       (Double.doubleToLongBits(minY     ) + 37*
                       (Double.doubleToLongBits(sx       ) + 37*
                       (Double.doubleToLongBits(sy0      ) + 37*
                       (Double.doubleToLongBits(sy1)))))));
        return (int) code + (int) (code >>> 32);
    }
}
