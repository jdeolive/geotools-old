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
import org.geotools.units.Unit;


/**
 * A graduation using numbers on a logarithmic axis.
 *
 * @version $Id: LogarithmicNumberGraduation.java,v 1.1 2003/03/07 23:36:12 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class LogarithmicNumberGraduation extends NumberGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8514854171546232887L;

    /**
     * Contruct a new logarithmic graduation with the supplied units.
     */
    public LogarithmicNumberGraduation(final Unit unit) {
        super(unit);
    }

    /**
     * Construct or reuse an iterator. This method override
     * the default {@link NumberGraduation} implementation.
     */
    NumberIterator getTickIterator(final TickIterator reuse, final Locale locale) {
        if (reuse instanceof LogarithmicNumberIterator) {
            final NumberIterator it = (NumberIterator) reuse;
            it.setLocale(locale);
            return it;
        } else {
            return new LogarithmicNumberIterator(locale);
        }
    }
}
