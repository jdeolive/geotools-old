/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.cv;

// J2SE dependencies
import java.awt.Image;
import java.io.Serializable;
import java.rmi.RemoteException;

// JAI dependencies
import javax.media.jai.PropertySource;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;

// OpenGIS dependencies
import org.opengis.cv.CV_Coverage;


/**
 * A {@link PropertySource} wrapping a {@link CV_Coverage} object.
 * The method {@link #getPropertyNames()} invokes {@link CV_Coverage#getMetadataNames()}, and
 * the method {@link #getProperty(String)} invokes {@link CV_Coverage#getMetadataValue(String)}.
 * If a checked {@link RemoteException} is thrown, it is wrapped in an unchecked
 * {@link CannotEvaluateException}.
 *
 * @version $Id: CoverageProperties.java,v 1.1 2002/10/16 22:32:19 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class CoverageProperties implements PropertySource, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5507750011525898648L;

    /**
     * The underlying coverage.
     */
    private final CV_Coverage coverage;

    /**
     * Construct a new {@link PropertySource} wrapping
     * the specified {@link CV_Coverage} object.
     */
    public CoverageProperties(final CV_Coverage coverage) {
        this.coverage = coverage;
    }

    /**
     * Returns an array of {@link String}s recognized as names by this property source.
     *
     * @return an array of strings giving the valid property names, or <code>null</code>.
     * @throws CannotEvaluateException if a remote invocation failed.
     */
    public String[] getPropertyNames() throws CannotEvaluateException {
        try {
            return coverage.getMetadataNames();
        } catch (RemoteException exception) {
            throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
        }
    }

    /**
     * Returns an array of {@link String}s recognized as names
     * by this property source that begin with the supplied prefix.
     *
     * @return an array of strings giving the valid property names.
     * @throws CannotEvaluateException if a remote invocation failed.
     */
    public String[] getPropertyNames(final String prefix) throws CannotEvaluateException {
        final String[] names = getPropertyNames();
        int count = names.length;
        for (int i=count; --i>=0;) {
            if (names[i].regionMatches(true, 0, prefix, 0, prefix.length())) {
                System.arraycopy(names, i+1, names, i, (--count)-i);
            }
        }
        return (String[]) XArray.resize(names, count);
    }

    /**
     * Returns the class expected to be returned by a
     * request for the property with the specified name.
     */
    public Class getPropertyClass(final String name) {
        return String.class;
    }

    /**
     * Returns the value of a property. If the property name is not recognized,
     * {@link java.awt.Image#UndefinedProperty} will be returned.
     *
     * @param  name the name of the property, as a string.
     * @return the value of the property, or the value {@link Image#UndefinedProperty}.
     * @throws CannotEvaluateException if a remote invocation failed.
     */
    public Object getProperty(final String name) throws CannotEvaluateException {
        try {
            final String value = coverage.getMetadataValue(name);
            return (value!=null) ? value : Image.UndefinedProperty;
        } catch (RemoteException exception) {
            throw new CannotEvaluateException(Resources.format(ResourceKeys.ERROR_RMI_FAILURE, exception));
        }
    }
}
