/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Fisheries and Oceans Canada
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
package org.geotools.ct;

// Geotools dependencies
import org.geotools.cs.Projection;


/**
 * Classe de base des projections cartographiques azimuthales (ou planaires).
 * On peut trouver plus de détails sur les projections azimuthales à l'adresse
 * <a href="http://everest.hunter.cuny.edu/mp/plane.html">http://everest.hunter.cuny.edu/mp/plane.html</a>.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/PlanarProjection.png"></p>
 * <p align="center">Représentation d'une projection planaire<br>
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class PlanarProjection extends MapProjection {
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected PlanarProjection(final Projection parameters) throws MissingParameterException {
        super(parameters);
    }
}
