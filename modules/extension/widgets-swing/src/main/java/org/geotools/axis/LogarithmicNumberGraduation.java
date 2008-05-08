/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2000, Institut de Recherche pour le Développement
 *    (C) 1999, Pêches et Océans Canada
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
 */
package org.geotools.axis;

import java.util.Locale;
import javax.units.Unit;


/**
 * A graduation using numbers on a logarithmic axis.
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LogarithmicNumberGraduation extends NumberGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8514854171546232887L;

    /**
     * Contructs a new logarithmic graduation with the supplied units.
     */
    public LogarithmicNumberGraduation(final Unit unit) {
        super(unit);
    }

    /**
     * Constructs or reuses an iterator. This method override
     * the default {@link NumberGraduation} implementation.
     */
    @Override
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
