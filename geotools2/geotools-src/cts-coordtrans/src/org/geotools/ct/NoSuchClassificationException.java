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

// JAI dependencies
import javax.media.jai.ParameterList; // For javadoc

// Geotools dependencies
import org.geotools.cs.Projection; // For javadoc
import org.geotools.cs.FactoryException;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Thrown when a math transform was requested with an unknow classification name.
 * The classification name is provided by {@link Projection#getClassName}, but this
 * exception may be thrown when the projection is given to
 * {@link MathTransformFactory#createParameterizedTransform(Projection)}.
 *
 * @version $Id: NoSuchClassificationException.java,v 1.3 2003/08/04 17:11:17 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Projection#getClassName
 * @see MathTransformFactory#getMathTransformProvider(String)
 * @see MathTransformFactory#createParameterizedTransform(String,ParameterList)
 */
public class NoSuchClassificationException extends FactoryException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4773900673763563575L;
    
    /**
     * The classification name.
     */
    private final String classification;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param msg the detail message, or <code>null</code> to construct
     *        a default message from the classification name.
     * @param classification The classification name.
     */
    public NoSuchClassificationException(final String msg, final String classification) {
        super((msg!=null || classification==null) ? msg : Resources.format(
                ResourceKeys.ERROR_NO_TRANSFORM_FOR_CLASSIFICATION_$1, classification));
        this.classification = classification;
    }
    
    /**
     * Returns the classification name.
     */
    public String getClassName() {
        return classification;
    }
}
