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
package org.geotools.gp;

// J2SE dependencies
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.gc.GC_GridRange;
import org.opengis.gc.GC_GridGeometry;
import org.opengis.gc.GC_GridCoverage;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability
 * with <code>org.opengis.gc</code> package.</FONT>
 * All methods accept null argument.
 *
 * @version $Id: Adapters.java,v 1.1 2002/09/15 21:50:59 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Adapters extends org.geotools.gc.Adapters {
    /**
     * Default adapters. Will be constructed
     * only when first requested.
     */
    private static Adapters DEFAULT;
    
    /**
     * Default constructor.
     *
     * @param CS The underlying adapters from the <code>org.geotools.ct</code> package.
     */
    protected Adapters(final org.geotools.ct.Adapters CT) {
        super(CT);
    }
    
    /**
     * Returns the default adapters.
     */
    public static synchronized Adapters getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Adapters(org.geotools.ct.Adapters.getDefault());
        }
        return DEFAULT;
    }
}
