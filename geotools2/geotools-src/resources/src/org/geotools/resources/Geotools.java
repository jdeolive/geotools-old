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
package org.geotools.resources;

import java.util.logging.Level;


/**
 * A central place where to provides system-wide services for Geotools. The 
 * {@link #init} method should be invoked once during some Geotools's class 
 * initialization. Current implementation just setup a custom logger for the
 * <code>"org.geotools"</code> package.
 *
 * @version $Id: Geotools.java,v 1.2 2002/08/30 18:54:03 robhranac Exp $
 * @author Martin Desruisseaux
 */
public final class Geotools {

    /**
     * Do not allow instanciation of this class.
     */
    private Geotools() {
    }

    /**
     * Performs a system-wide initialization for Geotools.
     */
    public static void init() {
        MonolineFormatter.init("org.geotools");
    }

    /**
     * Performs a system-wide initialization for Geotools.
     */
    public static void init(String type, Level level) {
        if(type.equals("Log4JFormatter")) {
            Log4JFormatter.init("org.geotools", level);
        }
        else {
            MonolineFormatter.init("org.geotools");
        }
    }
}
