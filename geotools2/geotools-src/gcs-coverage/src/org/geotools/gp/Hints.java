/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.gp;

// J2SE dependencies
import java.awt.RenderingHints;
import java.util.Locale;

// JAI dependencies
import javax.media.jai.JAI;

// Geotools Dependencies
import org.geotools.resources.Utilities;
import org.geotools.cv.SampleDimensionType;
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;


/**
 * A set of {@link RenderingHints} keys for grid coverage operations. Hints are memorized by
 * {@link GridCoverageProcessor}, which formard them to {@link Operation#doOperation} at
 * every invocation. Rendering hints can be used to control some low-level details, like
 * the {@link JAI} instance to use when performing operation. Operations may use the
 * hints or ignore them.
 * <br><br>
 * For example, if a user wants to use the <code>"Resample"</code> operation with
 * a custom {@link CoordinateTransformation}, he should first create its own
 * {@link CoordinateTransformationFactory} implementation. Then, he can create
 * a {@link GridCoverageProcessor} with its factory as a rendering hint:
 *
 * <blockquote><pre>
 * CoordinateTransformationFactory myFactory = <FONT FACE="Arial">...</FONT>
 * RenderingHints hints = new RenderingHints(Hints.{@link #COORDINATE_TRANSFORMATION_FACTORY}, myFactory);
 * GridCoverageProcessor processor = new GridCoverageProcessor(hints);
 * </pre></blockquote>
 *
 * @version $Id: Hints.java,v 1.5 2003/05/19 15:05:54 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints.Key {
    /**
     * Key for setting a {@link JAI} instance other than the default one when
     * JAI operation must be performed at rendering time.
     */
    public static final RenderingHints.Key JAI_INSTANCE =
            new Hints(0, "javax.media.jai.JAI");

    /**
     * Key for setting a {@link CoordinateTransformationFactory} object other
     * than the default one when coordinate transformations must be performed
     * at rendering time.
     */
    public static final RenderingHints.Key COORDINATE_TRANSFORMATION_FACTORY =
            new Hints(1, "org.geotools.ct.CoordinateTransformationFactory");

    /**
     * Key for setting a {@link SampleDimensionType} other than the default one
     * when sample values must be rescaled at rendering time.
     */
    public static final RenderingHints.Key SAMPLE_DIMENSION_TYPE =
            new Hints(2, "org.geotools.cv.SampleDimensionType");

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
     * Returns <code>true</code> if the specified object is a valid value for this Key.
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
            Utilities.unexpectedException("org.geotools.gp", "Hints", "isCompatibleValue",
                                          exception);
            valueClass = Object.class;
        }
        return valueClass.isAssignableFrom(value.getClass());
    }
}
