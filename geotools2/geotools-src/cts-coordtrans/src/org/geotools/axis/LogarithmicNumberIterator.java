/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2000, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.axis;

// Dependencies
import java.util.Locale;
import org.geotools.resources.XMath;


/**
 * Itérateur balayant les barres et étiquettes de graduation d'un axe logarithmique.
 * Cet itérateur retourne les positions des graduations à partir de la valeur minimale
 * jusqu'à la valeur maximale.
 *
 * @version $Id: LogarithmicNumberIterator.java,v 1.1 2003/03/07 23:36:13 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LogarithmicNumberIterator extends NumberIterator {
    /**
     * Construit un itérateur par défaut. La méthode {@link #init}
     * <u>doit</u> être appelée avant que cet itérateur ne soit
     * utilisable.
     *
     * @param locale Conventions à utiliser pour le formatage des nombres.
     */
    protected LogarithmicNumberIterator(final Locale locale) {
        super(locale);
    }

    /**
     * Retourne la valeur de la graduation courante. Cette méthode
     * peut être appelée pour une graduation majeure ou mineure.
     */
    public double currentValue() {
        return XMath.log10(super.currentValue());
    }
}
