/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 * @version $Id: LogarithmicNumberIterator.java,v 1.3 2003/05/13 10:58:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LogarithmicNumberIterator extends NumberIterator {
    /**
     * Scale and offset factors for {@link #currentPosition}
     */
    private double scale, offset;

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
     * Initialise l'itérateur.
     *
     * @param minimum           Valeur minimale de la première graduation.
     * @param maximum           Valeur limite des graduations. La dernière
     *                          graduation n'aura pas nécessairement cette valeur.
     * @param visualLength      Longueur visuelle de l'axe sur laquelle tracer la graduation.
     *                          Cette longueur doit être exprimée en pixels ou en points.
     * @param visualTickSpacing Espace à laisser visuellement entre deux marques de graduation.
     *                          Cet espace doit être exprimé en pixels ou en points (1/72 de pouce).
     */
    protected void init(final double minimum,
                        final double maximum,
                        final float  visualLength,
                        final float  visualTickSpacing)
    {
        final double logMin = XMath.log10(minimum);
        final double logMax = XMath.log10(maximum);
        super.init(logMin, logMax, visualLength, visualTickSpacing);
        scale  = (maximum-minimum) / (logMax-logMin);
        offset = minimum - scale*logMin;
    }

    /**
     * Returns the position where to draw the current tick. The
     * position is scaled from the graduation's minimum to maximum.
     */
    public double currentPosition() {
        return super.currentPosition() * scale + offset;
    }

    /**
     * Retourne la valeur de la graduation courante. Cette méthode
     * peut être appelée pour une graduation majeure ou mineure.
     */
    public double currentValue() {
        return XMath.pow10(super.currentValue());
    }
}
