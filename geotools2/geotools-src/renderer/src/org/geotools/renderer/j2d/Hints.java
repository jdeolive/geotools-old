/*
 * Geotools - OpenSource mapping toolkit
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
import java.awt.RenderingHints;

// JAI dependencies
import javax.media.jai.JAI;

// Geotools Dependencies
import org.geotools.ct.CoordinateTransformation;
import org.geotools.ct.CoordinateTransformationFactory;


/**
 * A set of {@link RenderingHints} keys for rendering operations. Hints are memorized by
 * {@link Renderer}, which formard them to {@link Graphics2D} at rendering time.
 * Rendering hints can be used to control some low-level details, like the expected
 * resolution.
 *
 * @version $Id: Hints.java,v 1.1 2003/01/20 23:21:08 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints.Key {
    /**
     * Key for setting a {@link CoordinateTransformationFactory} object other
     * than the default one when coordinate transformations must be performed
     * at rendering time. This is the same key than <code>org.geotools.gp</code>
     * {@link org.geotools.gp.Hints#COORDINATE_TRANSFORMATION_FACTORY} and is
     * declared here only for convenience.
     */
    public static final RenderingHints.Key COORDINATE_TRANSFORMATION_FACTORY =
            org.geotools.gp.Hints.COORDINATE_TRANSFORMATION_FACTORY;

    /**
     * The rendering resolution in unit of view coordinate system (usually "real world" metres).
     * A larger resolution speed up rendering, while a smaller resolution draw more precise map.
     * By convention, a resolution of 0 means the finest resolution available.
     */
    public static final RenderingHints.Key RESOLUTION = new Hints(0, Number.class);

    /**
     * Base class of all values for this key.
     */
    private final Class valueClass;

    /**
     * Construct a new key.
     *
     * @param id An ID. Must be unique for all instances of {@link Key}.
     * @param valueClass Base class of all valid values.
     */
    private Hints(final int id, final Class valueClass) {
        super(id);
        this.valueClass = valueClass;
    }

    /**
     * Returns <code>true</code> if the specified object is a valid
     * value for this Key.
     *
     * @param  value The object to test for validity.
     * @return <code>true</code> if the value is valid;
     *         <code>false</code> otherwise.
     */
    public boolean isCompatibleValue(final Object value) {
        return (value != null) && valueClass.isAssignableFrom(value.getClass());
    }
}
