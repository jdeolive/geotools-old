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
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_VerticalDatum;

// Geotools dependencies
import org.geotools.units.Unit;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * Procedure used to measure vertical distances.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_VerticalDatum
 */
public class VerticalDatum extends Datum {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1663224345779675117L;
    
    /**
     * Default vertical datum for ellipsoidal heights. Ellipsoidal heights
     * are measured along the normal to the ellipsoid used in the definition
     * of horizontal datum.
     */
    public static final VerticalDatum ELLIPSOIDAL = (VerticalDatum) pool.canonicalize(
                    new VerticalDatum("Ellipsoidal", DatumType.ELLIPSOIDAL));
    
    /**
     * Creates a vertical datum from an enumerated type value.
     *
     * @param name Name to give new object.
     * @param type Type of vertical datum to create.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createVerticalDatum
     */
    public VerticalDatum(final CharSequence name, final DatumType.Vertical type) {
        super(name, type);
    }
    
    /**
     * Gets the type of the datum as an enumerated code.
     *
     * Note: return type will be changed to {@link DatumType.Vertical}
     *       when we will be able to use generic types (with JDK 1.5).
     *
     * @see org.opengis.cs.CS_VerticalDatum#getDatumType()
     */
    public DatumType/*.Vertical*/ getDatumType() {
        return (DatumType.Vertical) super.getDatumType();
    }
    
    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Know Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        super.addString(buffer, context);
        return "VERT_DATUM";
    }
    
    /**
     * Returns an OpenGIS interface for this datum.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid too early class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link VerticalDatum} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Export extends Datum.Export implements CS_VerticalDatum {
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
    }
}
