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
import java.awt.RenderingHints;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Geotools dependencies
import org.geotools.gp.Hints;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * A default renderer for Java2D. Rendering are done in a {@link Graphics2D} object.
 *
 * @version $Id: Renderer.java,v 1.1 2003/01/20 00:06:35 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Renderer {
    /**
     * The logger for the Java2D renderer module.
     */
    static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");

    /**
     * A set of rendering hints. Recognized hints include
     * {@link Hints#COORDINATE_TRANSFORMATION_FACTORY}.
     */
    private RenderingHints hints = new RenderingHints(null);

    /**
     * Système de coordonnées utilisé pour l'affichage à l'écran. Les données des différentes
     * couches devront être converties selon ce système de coordonnées avant d'être affichées.
     * La transformation la plus courante utilisée à cette fin peut être conservée dans le
     * champ {@link #commonestTransform} à des fins de performances.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * Transformation de coordonnées (généralement une projection cartographique) utilisée
     * le plus souvent pour convertir les coordonnées des couches en coordonnées d'affichage.
     * Ce champ n'est conservé qu'à des fins de performances. Si ce champ est nul, ça signifie
     * qu'il a besoin d'être reconstruit.
     */
    private transient CoordinateTransformation commonestTransform;

    /**
     * The rendering resolution, in units of this renderer's coordinate system
     * (usually metres or degree). A larger resolution speed up rendering, while
     * a smaller resolution draw more precise map.
     */
    private float resolution;

    /**
     * Construct a new renderer using the {@linkplain GeographicCoordinateSystem#WGS84
     * WGS84} coordinate system.
     */
    public Renderer() {
        this(GeographicCoordinateSystem.WGS84);
    }

    /**
     * Construct a new renderer using the specified coordinate system.
     *
     * @param cs The view coordinate system. If this coordinate system has
     *           more than 2 dimensions, only the 2 first will be retained.
     */
    public Renderer(final CoordinateSystem cs) {
        coordinateSystem = CTSUtilities.getCoordinateSystem2D(cs);
    }

    /**
     * Returns the view coordinate system. The underlying data doesn't need to be expressed
     * in this coordinate system; transformation will performed on the fly as needed.
     *
     * @return The two dimensional coordinate system used for display.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Returns the rendering resolution. Default value is 0, which (by convention) means
     * the finest resolution available.
     *
     * @return The rendering resolution, in units of {@link Polygon#getResolution}
     *         (usually metres).
     */
    public float getResolution() {
        return resolution;
    }

    /**
     * Set the rendering resolution. A larger resolution speed up rendering, while a smaller
     * resolution draw more precise map. By convention, a resolution of 0 means the finest
     * resolution available. This method may be call often, for example every time the zoom
     * change.
     *
     * @param  resolution The rendering resolution, in units of
     *         {@link Polygon#getResolution} (usually metres).
     * @throws IllegalArgumentException if <code>resolution</code> is negative or NaN.
     */
    public void setResolution(final float resolution) throws IllegalArgumentException {
        if (!(resolution >= 0)) {
            throw new IllegalArgumentException(String.valueOf(resolution));
        }
        this.resolution = resolution;
    }

    /**
     * Returns a rendering hints.
     *
     * @param  key The hint key (e.g. {@link Hints#COORDINATE_TRANSFORMATION_FACTORY}).
     * @return The hint value for the specified key.
     *
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public Object getRenderingHints(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Add a rendering hints. Hints provides optional information used by some
     * rendering code.
     *
     * @param key   The hint key (e.g. {@link Hints#COORDINATE_TRANSFORMATION_FACTORY}).
     * @param value The hint value.
     *
     * @see Hints#COORDINATE_TRANSFORMATION_FACTORY
     */
    public void setRenderingHints(final RenderingHints.Key key, final Object value) {
        hints.put(key, value);
        if (Hints.COORDINATE_TRANSFORMATION_FACTORY.equals(key)) {
            commonestTransform = null;
        }
    }

    /**
     * Construct a transform from two coordinate systems. If a {@link
     * Hints#COORDINATE_TRANSFORMATION_FACTORY} has been provided, the
     * specified {@link CoordinateTransformationFactory} will be used.
     * A message is logged in order to trace down the amount of coordinate
     * transformations created.
     *
     * @param  sourceCS The source coordinate system.
     * @param  targetCS The target coordinate system.
     * @param  sourceClassName The class name of the caller's method.
     * @param  sourceMethodName The caller's method name.
     * @return A transformation from <code>sourceCS</code> to <code>targetCS</code>.
     */
    final CoordinateTransformation getCoordinateTransformation(final CoordinateSystem sourceCS,
                                                               final CoordinateSystem targetCS,
                                                               final String sourceClassName,
                                                               final String sourceMethodName)
            throws CannotCreateTransformException
    {
        /*
         * Copy 'commonestTransform' reference (protect it from change, in order to avoid
         * the need for synchronisation).  Then, check if 'commonestTransform' could work
         * for the specified CS.
         */
        final CoordinateTransformation commonestTransform = this.commonestTransform;
        if (commonestTransform != null) {
            if (sourceCS.equivalents(commonestTransform.getSourceCS()) &&
                targetCS.equivalents(commonestTransform.getTargetCS()))
            {
                return commonestTransform;
            }
        }
        /*
         * Construct the new transform using the specified factory,
         * if one has been explicitely set as rendering hint.
         */
        Object property = hints.get(Hints.COORDINATE_TRANSFORMATION_FACTORY);
        final CoordinateTransformationFactory factory;
        if (property instanceof CoordinateTransformationFactory) {
            factory = (CoordinateTransformationFactory) property;
        } else {
            factory = CoordinateTransformationFactory.getDefault();
        }
        final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINER,
                                           ResourceKeys.INITIALIZING_TRANSFORMATION_$2,
                                           toString(sourceCS), toString(targetCS));
        record.setSourceClassName (sourceClassName);
        record.setSourceMethodName(sourceMethodName);
        LOGGER.log(record);
        return factory.createFromCoordinateSystems(sourceCS, targetCS);
    }

    /**
     * Returns a string representation of a coordinate system.
     * This method is used for formatting a logging message for
     * {@link #getCoordinateTransformation}.
     */
    private static String toString(final CoordinateSystem cs) {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(cs));
        buffer.append('[');
        final String name = cs.getName(null);
        if (name != null) {
            buffer.append('"');
            buffer.append(name);
            buffer.append('"');
        }
        buffer.append(']');
        return buffer.toString();
    }
}
