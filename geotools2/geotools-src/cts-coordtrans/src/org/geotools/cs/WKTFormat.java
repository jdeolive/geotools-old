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
package org.geotools.cs;

// Formatting
import java.util.Locale;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;


/**
 * The base class for <cite>Well Know Text</cite> (WKT) parser and formatter. This base class
 * contains information about the symbols to use (opening and closing bracket, element separator,
 * etc.). This is a relatively light object compared to {@link WKTParser} and can be used when
 * parsing are not needed.
 *
 * @version $Id: WKTFormat.java,v 1.1 2002/09/02 17:55:39 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
abstract class WKTFormat extends Format {
    /**
     * The locale for number parsing and formatting.
     */
    final Locale locale;

    /**
     * The object to use for parsing and formatting numbers.
     */
    final NumberFormat format;

    /**
     * The character to use as an element separator.
     * This is usually the coma <code>','</code>.
     */
    final char elementSeparator;

    /**
     * The character to use as text delimitor.
     * This is usually the quote <code>'"'</code>.
     */
    final char textDelimitor = '"';

    /**
     * The character to use for openining element's parameters.
     * This is usually <code>'['</code> or <code>'('</code>.
     */
    final char openingBracket = '[';

    /**
     * The character to use for closing element's parameters.
     * This is usually <code>']'</code> or <code>')'</code>.
     */
    final char closingBracket = ']';
    
    /**
     * Construct a format for the specified locale.
     *
     * @param local The locale for parsing and formatting numbers.
     */
    public WKTFormat(final Locale locale) {
        this.locale = locale;
        this.format = NumberFormat.getNumberInstance(locale);
        char decimalSeparator = '.';
        if (format instanceof DecimalFormat) {
            final DecimalFormat df = (DecimalFormat) format;
            decimalSeparator = df.getDecimalFormatSymbols().getDecimalSeparator();
        }
        elementSeparator = (decimalSeparator==',') ? ';' : ',';
        format.setGroupingUsed(false);
    }

    /**
     * Format the specified object. Default implementation just append {@link Object#toString},
     * since the <code>toString()</code> implementation for most {@link org.geotools.cs.Info}
     * objects is to returns a WKT. Subclasses shoud override this method.
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(obj);
    }
}
