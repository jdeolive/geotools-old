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
import org.opengis.cs.CS_Datum;
import org.opengis.cs.CS_DatumType;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;

// J2SE dependencies
import java.rmi.RemoteException;


/**
 * A set of quantities from which other quantities are calculated.
 * It may be a textual description and/or a set of parameters describing the
 * relationship of a coordinate system to some predefined physical locations
 * (such as center of mass) and physical directions (such as axis of spin).
 * It can be defined as a set of real points on the earth that have coordinates.
 * For example a datum can be thought of as a set of parameters defining completely
 * the origin and orientation of a coordinate system with respect to the earth.
 * The definition of the datum may also include the temporal behavior (such
 * as the rate of change of the orientation of the coordinate axes).
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_Datum
 */
public class Datum extends Info {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2175857309476007487L;
    
    /**
     * The datum type.
     */
    private final DatumType type;
    
    /**
     * Construct a new datum with the
     * specified name and datum type.
     *
     * @param name The datum name.
     * @param type The datum type.
     */
    public Datum(final CharSequence name, final DatumType type) {
        super(name);
        this.type = type;
        ensureNonNull("type", type);
    }
    
    /**
     * Gets the type of the datum as an enumerated code.
     *
     * @see org.opengis.cs.CS_Datum#getDatumType()
     */
    public DatumType getDatumType() {
        return type;
    }
    
    /**
     * Returns a hash value for this datum.
     */
    public int hashCode() {
        int code = 37*super.hashCode();
        final DatumType type  = getDatumType();
        if (type!=null) code += type.hashCode();
        return code;
    }
    
    /**
     * Compares the specified object
     * with this datum for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final Datum that = (Datum) object;
            return Utilities.equals(this.type, that.type);
        }
        return false;
    }
    
    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Know Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(type.getName());
        return "DATUM";
    }
    
    /**
     * Returns an OpenGIS interface for this datum.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid too early class loading of OpenGIS interface.
     */
    Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link Datum} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    class Export extends Info.Export implements CS_Datum {
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Gets the type of the datum as an enumerated code.
         */
        public CS_DatumType getDatumType() throws RemoteException {
            return adapters.export(Datum.this.getDatumType());
        }
    }
}
