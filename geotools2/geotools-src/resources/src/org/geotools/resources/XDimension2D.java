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
 */
package org.geotools.resources;

// Miscellaneous
import java.io.Serializable;
import java.awt.geom.Dimension2D;


/**
 * Implement float and double version of {@link Dimension2D}. This class
 * is only temporary; it will disappear if <em>JavaSoft</em> implements
 * <code>Dimension2D.Float</code> and <code>Dimension2D.Double</code>.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public final class XDimension2D {
    /**
     * Do not allow instantiation of this class.
     */
    private XDimension2D() {
    }

    /**
     * Implement float version of {@link Dimension2D}. This class is
     * temporary;  it will disappear if <em>JavaSoft</em> implements
     * <code>Dimension2D.Float</code> and <code>Dimension2D.Double</code>.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    public static final class Float extends Dimension2D implements Serializable {
        /**
         * The width.
         */
        public float width;

        /**
         * The height.
         */
        public float height;

        /**
         * Construct a new dimension initialized to (0,0).
         */
        public Float() {
        }

        /**
         * Construct a new dimension with the specified values.
         *
         * @param w The width.
         * @param h The height.
         */
        public Float(final float w, final float h) {
            width  = w;
            height = h;
        }

        /**
         * Set width and height for this dimension.
         *
         * @param w The width.
         * @param h The height.
         */
        public void setSize(final double w, final double h) {
            width  = (float) w;
            height = (float) h;
        }

        /**
         * Returns the width.
         */
        public double getWidth() {
            return width;
        }

        /**
         * Returns the height.
         */
        public double getHeight() {
            return height;
        }

        /**
         * Returns a string representation of this dimension.
         */
        public String toString() {
            return "Dimension2D["+width+','+height+']';
        }
    }

    /**
     * Implement double version of {@link Dimension2D}. This class is
     * temporary; it will disappear if <em>JavaSoft</em> implements
     * <code>Dimension2D.Float</code> and <code>Dimension2D.Double</code>.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    public static final class Double extends Dimension2D implements Serializable {
        /**
         * The width.
         */
        public double width;

        /**
         * The height.
         */
        public double height;

        /**
         * Construct a new dimension initialized to (0,0).
         */
        public Double() {
        }

        /**
         * Construct a new dimension with the specified values.
         *
         * @param w The width.
         * @param h The height.
         */
        public Double(final double w, final double h) {
            width  = w;
            height = h;
        }

        /**
         * Set width and height for this dimension.
         *
         * @param w The width.
         * @param h The height.
         */
        public void setSize(final double w, final double h) {
            width  = w;
            height = h;
        }

        /**
         * Returns the width.
         */
        public double getWidth() {
            return width;
        }

        /**
         * Returns the height.
         */
        public double getHeight() {
            return height;
        }

        /**
         * Returns a string representation of this dimension.
         */
        public String toString() {
            return "Dimension2D["+width+','+height+']';
        }
    }
}
