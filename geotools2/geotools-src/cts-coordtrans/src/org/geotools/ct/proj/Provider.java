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
 */
package org.geotools.ct.proj;

// J2SE and JAI dependencies
import java.util.Locale;
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformProvider;
import org.geotools.ct.MissingParameterException;
import org.geotools.resources.cts.Resources;

    
/**
 * Base class for {@link MapProjection} provider.
 *
 * @version $Id: Provider.java,v 1.2 2003/03/28 10:21:57 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class Provider extends MathTransformProvider {
    /**
     * Resources key for a human readable name. This
     * is used for {@link #getName} implementation.
     *
     * @task REVISIT: This key duplicate the private <code>MathTransformProvider.nameKey</code>.
     *       This duplication would not be needed if the package-private constructor in 
     *       <code>MathTransformProvider</code> was protected. However, we don't want to
     *       expose resource keys in the API...
     */
    private final int nameKey;

    /**
     * Construct a new provider.
     *
     * @param classification The classification name.
     * @param nameKey Resources key for a human readable name.
     *        This is used for {@link #getName} implementation.
     */
    Provider(final String classname, final int nameKey) {
        super(classname, DEFAULT_PROJECTION_DESCRIPTOR);
        this.nameKey = nameKey;
    }
    
    /**
     * Returns a human readable name localized for the specified locale.
     *
     * @task REVISIT: This method overrides the <code>MathTransformProvider</code> implementation.
     *       It would not be needed if the package-private constructor in 
     *       <code>MathTransformProvider</code> was protected.
     */
    public String getName(final Locale locale) {
        return (nameKey>=0) ? Resources.getResources(locale).getString(nameKey)
                            : super.getName(locale);
    }

    /**
     * Create a new map projection from a parameter list.
     *
     * @param  parameters The parameters list.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    public final MathTransform create(final ParameterList parameters)
            throws MissingParameterException
    {
        return create(new Projection("Generated", getClassName(), parameters));
    }

    /**
     * Create a new map projection from a <code>Projection</code> parameter.
     *
     * @param  parameters The projection.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    public abstract MathTransform create(final Projection parameters)
            throws MissingParameterException;

    /**
     * Tells if the specified projection uses a spherical model.
     *
     * @returns <code>true</code> if the model is spherical,
     *          or <code>false</code> if it is ellipsoidal.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected final boolean isSpherical(final Projection parameters)
            throws MissingParameterException
    {
        return parameters.getValue("semi_major") ==
               parameters.getValue("semi_minor");
    }
}
