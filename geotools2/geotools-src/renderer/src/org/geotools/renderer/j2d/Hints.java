/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.util.Locale;
import java.lang.Runtime; // For Javadoc
import java.awt.Graphics2D; // For Javadoc
import java.awt.RenderingHints;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage; // For Javadoc
import javax.media.jai.operator.ScaleDescriptor; // For Javadoc

// Geotools Dependencies
import org.geotools.resources.Utilities;
import org.geotools.renderer.geom.Isoline;
import org.geotools.gp.GridCoverageProcessor;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;


/**
 * A set of {@link RenderingHints} keys for rendering operations. Hints are memorized by
 * {@link Renderer}, which formard them to {@link Graphics2D} at rendering time.
 * Rendering hints can be used to control some low-level details, like the expected
 * resolution.
 *
 * @version $Id: Hints.java,v 1.13 2003/05/19 15:06:20 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints.Key {
    /**
     * Key for setting a {@link GridCoverageProcessor} instance other than the default.
     * Grid coverage processor are used for resampling grid coverage when the rendering
     * coordinate system is different than the grid coverage one.
     */
    public static final RenderingHints.Key GRID_COVERAGE_PROCESSOR =
                                        new Hints(0, "org.geotools.gp.GridCoverageProcessor");

    /**
     * Key for setting a {@link JAI} object other than the default one when a JAI operation must be
     * applied. This is used especially for the {@linkplain javax.media.jai.operator.ScaleDescriptor
     * scale} operation when rendering an image with {@link RenderedGridCoverage}. This is the same
     * key than <code>org.geotools.gp.{@linkplain org.geotools.gp.Hints#JAI_INSTANCE}</code> and is
     * declared here only for convenience. If there is no value for this key but a value exists for
     * {@link #GRID_COVERAGE_PROCESSOR}, then the JAI instance will be inherited from the specified
     * <code>GridCoverageProcessor</code>.
     */
    public static final RenderingHints.Key JAI_INSTANCE = org.geotools.gp.Hints.JAI_INSTANCE;

    /**
     * Key for setting a {@link CoordinateTransformationFactory} object other
     * than the default one when coordinate transformations must be performed
     * at rendering time. This is the same key than <code>org.geotools.gp</code>
     * {@link org.geotools.gp.Hints#COORDINATE_TRANSFORMATION_FACTORY} and is
     * declared here only for convenience. If there is no value for this key but
     * a value exists for {@link #GRID_COVERAGE_PROCESSOR}, then the JAI instance
     * will be inherited from the specified <code>GridCoverageProcessor</code>.
     */
    public static final RenderingHints.Key COORDINATE_TRANSFORMATION_FACTORY =
            org.geotools.gp.Hints.COORDINATE_TRANSFORMATION_FACTORY;

    /**
     * The finest rendering resolution in unit of dots (1/72 of inch). This is the unit used
     * by the default {@linkplain RenderingContext#textCS Java2D coordinate system}. If an
     * {@link Isoline} to be rendered has a finer resolution, it will be decimated in order
     * to speed up rendering. By convention, a resolution of 0 means the finest resolution
     * available.
     */
    public static final RenderingHints.Key FINEST_RESOLUTION = new Hints(1, Number.class);

    /**
     * The required rendering resolution in unit of dots (1/72 of inch). This is the unit
     * used by the default {@linkplain RenderingContext#textCS Java2D coordinate system}.
     * If an {@link Isoline} has been decimated to a worst resolution, it will be resampled
     * in order to gets a more acceptable resolution. This value should be greater than
     * {@link #FINEST_RESOLUTION}.
     */
    public static final RenderingHints.Key REQUIRED_RESOLUTION = new Hints(2, Number.class);

    /**
     * {@link Boolean#TRUE} if the renderer is allowed to prefetch data before to paint layers.
     * Prefetching data may speed up rendering on machine with more than one processor. If this
     * hint is not provided,  then the {@linkplain Renderer renderer} will prefetch data if and
     * only if the machine has at least two processors.
     *
     * @see RenderedLayer#prefetch
     * @see PlanarImage#prefetchTiles
     * @see Runtime#availableProcessors
     */
    public static final RenderingHints.Key PREFETCH = new Hints(3, Boolean.class);

    /**
     * The class name for {@link #valueClass}.
     */
    private final String className;

    /**
     * Base class of all values for this key. Will be created from {@link #className}
     * only when first required, in order to avoid too early class loading.
     */
    private Class valueClass;

    /**
     * Construct a new key.
     *
     * @param id An ID. Must be unique for all instances of {@link Key}.
     * @param valueClass Base class of all valid values.
     */
    private Hints(final int id, final Class valueClass) {
        super(id);
        this.valueClass = valueClass;
        this.className  = valueClass.getName();
    }

    /**
     * Construct a new key. This constructor is used when a class loading should
     * be deferred until first needed.
     *
     * @param id An ID. Must be unique for all instances of {@link Key}.
     * @param className Name of base class for all valid values.
     */
    private Hints(final int id, final String className) {
        super(id);
        this.className = className;
        try {
            assert !Class.forName(className).isPrimitive();
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Returns <code>true</code> if the specified object is a valid value for this key.
     *
     * @param  value The object to test for validity.
     * @return <code>true</code> if the value is valid; <code>false</code> otherwise.
     */
    public boolean isCompatibleValue(final Object value) {
        if (value == null) {
            return false;
        }
        if (valueClass == null) try {
            valueClass = Class.forName(className);
        } catch (ClassNotFoundException exception) {
            Utilities.unexpectedException("org.geotools.renderer", "Hints", "isCompatibleValue",
                                          exception);
            valueClass = Object.class;
        }
        if (!valueClass.isAssignableFrom(value.getClass())) {
            return false;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() >= 0;
        }
        return true;
    }
}
