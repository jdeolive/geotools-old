/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.cv;

// J2SE dependencies
import java.awt.Color;

// JAI dependencies
import javax.media.jai.util.Range;


/**
 * A qualitative {@link Category}. This class is provided only for performance
 * raison, since {@link #toGeophysicsValue} and {@link #toSampleValue} need to
 * be very fast. The default implementation was too slow for the very simple
 * (and common!) case of qualitative category.
 *
 * @version $Id: QualitativeCategory.java,v 1.1 2002/07/17 23:30:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class QualitativeCategory extends Category {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1975240359431881796L;
    
    /**
     * A set of default category colors.
     */
    private static final Color[] CYCLE = {
        Color.blue,    Color.red,   Color.orange, Color.yellow,    Color.pink,
        Color.magenta, Color.green, Color.cyan,   Color.lightGray, Color.gray
    };
    
    /**
     * Construct a qualitative category for sample value <code>sample</code>.
     *
     * @param  name    The category name.
     * @param  color   The category color, or <code>null</code> for a default color.
     * @param  sample  The sample value as an integer, usually in the range 0 to 255.
     */
    QualitativeCategory(final String  name,
                        final Color   color,
                        final Integer sample)
    {
        this(name, toARGB(color, sample.intValue()), new Range(Integer.class, sample, sample));
    }
    
    /**
     * Create a qualitative category.
     */
    QualitativeCategory(final String name,
                        final int[]  ARGB,
                        final Range  sampleValueRange) throws IllegalArgumentException
    {
        super(name, ARGB, sampleValueRange, null);
        assert Double.isNaN(minValue) : minValue;
        assert Double.isNaN(maxValue) : maxValue;
    }

    /**
     * Returns ARGB values for the specified color. If <code>color</code>
     * is null, a default ARGB code will be returned.
     */
    private static int[] toARGB(Color color, final int sample) {
        if (color==null) {
            color = CYCLE[Math.abs(sample) % CYCLE.length];
        }
        return toARGB(new Color[] {color});
    }
    
    /**
     * Returns the geophysics value.
     */
    protected double toGeophysicsValue(final int s) {
        return minValue;
    }

    /**
     * Returns the sample value from a geophysics value.
     */
    double toSampleValue(final double value) {
        return minSample;
    }
}
