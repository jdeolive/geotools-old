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
import org.geotools.pt.Matrix;


/**
 * Interface for linear {@link MathTransform}s.  A linear transform can be express as an affine
 * transform using a {@linkplain #getMatrix matrix}. The {@linkplain Matrix#getNumCol number of
 * columns} is equals to the number of {@linkplain #getDimSource source dimension} plus 1,  and
 * the {@linkplain #getNumRow number of rows} is equals to the number of {@linkplain #getDimTarget
 * target dimension} plus 1.
 *
 * @version $Id: LinearTransform.java,v 1.3 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface LinearTransform extends MathTransform {
    /**
     * Returns this transform as an affine transform matrix.
     */
    public abstract Matrix getMatrix();
}
