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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;

// Geotools dependencies
import org.geotools.ct.TransformException;


/**
 * Provides the mechanism for {@link RenderedGridMarks} layer to return the appearance of
 * their marks. This class allows the layer to retrieve the appearance of its marks a mark
 * at a time.  The default implementation paints circles at mark locations. Subclasses must
 * implement at least the following methods:
 *
 * <ul>
 *   <li>{@link #seek}</li>
 *   <li>{@link #next}</li>
 *   <li>{@link #position}</li>
 * </ul>
 *
 * Si, à la position de chaque marque, on souhaite dessiner une figure orientable dans l'espace
 * (par exemple une flèche de courant ou une ellipse de marée), la classe dérivée pourra redéfinir
 * une ou plusieurs des méthodes ci-dessous. Redéfinir ces méthodes permet par exemple de dessiner
 * des flèches dont la forme exacte (par exemple une, deux ou trois têtes) et la couleur varie avec
 * l'amplitude, la direction ou d'autres critères de votre choix.
 *
 * <ul>
 *   <li>{@link #amplitude}</li>
 *   <li>{@link #direction}</li>
 *   <li>{@link #markShape}</li>
 *   <li>{@link #geographicArea}</li>
 *   <li>{@link #label}</li>
 *   <li>{@link #labelPosition}</li>
 * </ul>
 *
 * @version $Id: MarkIterator.java,v 1.1 2003/03/15 12:58:15 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class MarkIterator {
    /**
     * Forme géométrique à utiliser par défaut lorsqu'aucune autre forme n'a
     * été spécifiée. La position centrale de la station doit correspondre à
     * la coordonnée (0,0) de cette forme. La dimension de cette forme est
     * exprimée en pixels. La forme par défaut sera un cercle centré à
     * (0,0) et d'un diamètre de 10 pixels.
     */
    static final Shape DEFAULT_SHAPE = new Ellipse2D.Float(-5, -5, 10, 10);

    /**
     * Construct a default mark iterator.
     */
    public MarkIterator() {
    }

    /**
     * Moves the iterator the specified mark. A call to <code>seek(0)</code> moves the iterator
     * on the first mark. A call to <code>seek(-1)</code> moves the iterator to the front of this
     * <code>MarkIterator</code> object, just before the first mark (i.e. in the same position as
     * if this <code>MarkIterator</code> object were just created).
     *
     * @param index The new position (0 for the first mark, 1 for the second one, etc.).
     */
    public abstract void seek(int index);

    /**
     * Moves the iterator to the next marks. A <code>MarkIterator</code> is initially positioned
     * before the first mark; the first call to the method <code>next()</code> makes the first
     * mark the current mark; the second call makes the second mark the current mark, and so on.
     * This method is usually invoked in a <code>while</code> loop as below:
     *
     * <blockquote><pre>
     * MarkIterator it = layer.{@linkplain RenderedMarks#getMarkIterator getMarkIterator()};
     * while (it.next()) {
     *     // <cite>Query current mark properties here...</cite>
     * }
     * </pre></blockquote>
     *
     * @return <code>true</code> if the new current mark is valid;
     *         <code>false</code> if there are no more marks.
     */
    public abstract boolean next();

    /**
     * Returns <code>true</code> if the current mark should be painted.
     * The default implementation returns always <code>true</code>.
     */
    public boolean visible() {
        return true;
    }

    /**
     * Returns <code>true</code> if the current mark is visible in the specified clip. The rectangle
     * <code>clip</code> must have been created by {@link RenderedMarks#getUserClip}. This method is
     * overriden by {@link RenderedGridMarks#Iterator}.
     */
    boolean visible(final Rectangle clip) {
        return visible();
    }

    /**
     * Returns the (<var>x</var>,<var>y</var>) coordinates for the current mark.  Coordinates must
     * be expressed according the {@linkplain RenderedMarks#getCoordinateSystem layer's coordinate
     * system} (a geographic one by default). This method can returns <code>null</code> if the
     * current mark location is unknow.
     *
     * @throws TransformException if a transform was required and failed.
     *
     * @see #geographicArea
     */
    public abstract Point2D position() throws TransformException;

    /**
     * Returns the horizontal amplitude for the current mark. This amplitude tells how big a
     * mark should be painted. It is useful for painting wind arrows or some other quantifiable
     * marks. The default implementation returns always 1.
     */
    public double amplitude() {
        return 1;
    }

    /**
     * Returns the arithmetic direction for the current mark. This angle must be expressed
     * in arithmetic radians (i.e. angle 0 point toward the right and angles increase
     * counter-clockwise). This information is useful for painting wind arrows for example.
     * The default implementation returns always 0.
     */
    public double direction() {
        return 0;
    }

    /**
     * Returns the geometric shape for the current mark. This shape may be mark dependent, or be
     * the same for all marks. This shape must be centred at the origin (0,0) et its coordinates
     * must be expressed in dots (1/72 of inch). For example in order to paint wind arrows, this
     * shape should be oriented toward positives <var>x</var> positifs (i.e. toward 0 arithmetic
     * radians), has a base centred at (0,0) and have a raisonable size (for example 16&times;4
     * pixels). The method {@link RenderedMarks#paint(RenderingContext)} will automatically takes
     * care of rotation, translation and scale in order to adjust this model to each mark
     * properties. The default implementation returns a circle centred at (0,0) with a diameter
     * of 10 dots.
     */
    public Shape markShape() {
        return DEFAULT_SHAPE;
    }

    /**
     * Returns the geographic area for the current mark, or <code>null</code> if none.
     * This area must be expressed according the {@linkplain RenderedMarks#getCoordinateSystem
     * layer's coordinate system}. Usually (but not mandatory), this area will contains the point
     * returned by {@link #position}. The default implementation returns always <code>null</code>,
     * which means that this layer paint marks without geographic extent.
     */
    public Shape geographicArea() {
        return null;
    }

    /**
     * Returns the label for the current mark, or <code>null</code> if none. The default
     * implementation returns always <code>null</code>.
     */
    public String label() {
        return null;
    }

    /**
     * Returns the label position relative to the mark's {@linkplain #position position}.
     * The default implementation returns always {@link LegendPosition#CENTER}.
     */
    public LegendPosition labelPosition() {
        return LegendPosition.CENTER;
    }

    /**
     * Returns a tooltip text for the current mark. The default implementation returns
     * always <code>null</code>.  <strong>Note:</strong> This method is not a commited
     * part of the API. It may moves elsewhere in a future version.
     *
     * @param  event The mouse event.
     * @return The tool tip text for the current mark, or <code>null</code> if none.
     */
    protected String getToolTipText(GeoMouseEvent event) {
        return null;
    }
}
