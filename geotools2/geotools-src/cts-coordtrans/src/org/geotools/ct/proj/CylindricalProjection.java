/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.ct.proj;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MissingParameterException;


/**
 * Classe de base des projections cartographiques cylindriques. Les projections cylindriques
 * consistent à projeter la surface de la Terre sur un cylindre tangeant ou sécant à la Terre.
 * Les parallèles et mes méridiens apparaissent habituellement comme des lignes droites.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="../doc-files/CylindricalProjection.png"></p>
 *
 * @version $Id: CylindricalProjection.java,v 1.3 2003/05/13 10:58:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see <A HREF="http://mathworld.wolfram.com/CylindricalProjection.html">Cylindrical projection on MathWorld</A>
 */
public abstract class CylindricalProjection extends MapProjection {
    /**
     * Construct a new map projection from the suplied parameters.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected CylindricalProjection(final Projection parameters) throws MissingParameterException {
        super(parameters);
    }
}
