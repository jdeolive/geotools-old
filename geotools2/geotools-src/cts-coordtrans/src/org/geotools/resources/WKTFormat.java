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

// Formatting
import java.util.Locale;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;


/**
 * The base class for <cite>Well Know Text</cite> (WKT) parser and formatter. This base class
 * contains information about the symbols to use (opening and closing bracket, element separator,
 * etc.). This is a relatively light object compared to their subclasses and can be used when
 * parsing are not needed.
 *
 * @version $Id: WKTFormat.java,v 1.2 2002/09/03 17:53:00 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public abstract class WKTFormat extends Format {
    /**
     * The locale for number parsing and formatting.
     */
    public final Locale locale;

    /**
     * The object to use for parsing and formatting numbers.
     * Note: {@link NumberFormat} object are usually not thread safe.
     * Consider using this format in a synchronized block if thread safe
     * behavior is wanted.
     */
    public final NumberFormat number;

    /**
     * The character to use as an element separator.
     * This is usually the coma <code>','</code>.
     */
    public final char elementSeparator;

    /**
     * The character to use as text delimitor.
     * This is usually the quote <code>'"'</code>.
     */
    public final char textDelimitor = '"';

    /**
     * The character to use for openining element's parameters.
     * This is usually <code>'['</code> or <code>'('</code>.
     */
    public final char openingBracket = '[';

    /**
     * The character to use for closing element's parameters.
     * This is usually <code>']'</code> or <code>')'</code>.
     */
    public final char closingBracket = ']';
    
    /**
     * Construct a format for the specified locale.
     *
     * @param local The locale for parsing and formatting numbers.
     */
    public WKTFormat(final Locale locale) {
        this.locale = locale;
        this.number = NumberFormat.getNumberInstance(locale);
        char decimalSeparator = '.';
        if (number instanceof DecimalFormat) {
            final DecimalFormat df = (DecimalFormat) number;
            decimalSeparator = df.getDecimalFormatSymbols().getDecimalSeparator();
        }
        elementSeparator = (decimalSeparator==',') ? ';' : ',';
        number.setGroupingUsed(false);
    }

    /**
     * Returns a tree of {@link WKTElement} for the specified text.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @param  separator  The character to search.
     */
    protected final WKTElement getTree(final String text, final ParsePosition position)
        throws ParseException
    {
        return new WKTElement(new WKTElement(this, text, position));
    }


    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     */
    protected abstract Object parse(final WKTElement element) throws ParseException;

    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @return The object.
     * @throws ParseException if the string can't be parsed.
     */
    public final Object parseObject(final String text) throws ParseException {
        final WKTElement element = getTree(text, new ParsePosition(0));
        final Object object = parse(element);
        element.close();
        return object;
    }
    
    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @param  position The position to start parsing from.
     * @return The object.
     */
    public final Object parseObject(final String text, final ParsePosition position) {
        final int origin = position.getIndex();
        try {
            return parse(getTree(text, position));
        } catch (ParseException exception) {
            position.setIndex(origin);
            if (position.getErrorIndex() < origin) {
                position.setErrorIndex(exception.getErrorOffset());
            }
            return null;
        }
    }
}
