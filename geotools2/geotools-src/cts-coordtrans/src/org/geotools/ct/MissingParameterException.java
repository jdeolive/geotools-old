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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.cs.FactoryException;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Thrown when a required parameter was missing.
 * For example, this exception may be thrown when a map projection
 * was requested but the "semi_major" parameter was not specified.
 *
 * @version $Id: MissingParameterException.java,v 1.4 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Projection#getValue
 */
public class MissingParameterException extends FactoryException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3365753083955970327L;
    
    /**
     * The missing parameter name.
     */
    private final String parameter;
    
    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message, or <code>null</code> to construct
     *        a default message from the missing parameter name.
     * @param parameter The missing parameter name.
     */
    public MissingParameterException(final String msg, final String parameter) {
        super((msg!=null || parameter==null) ? msg : Resources.format(
                ResourceKeys.ERROR_MISSING_PARAMETER_$1, parameter));
        this.parameter = parameter;
    }
    
    /**
     * Returns the missing parameter name.
     */
    public String getMissingParameterName() {
        return parameter;
    }
}
