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

// Collections
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

// Parsing
import java.util.Locale;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.ParseException;

// Input/output
import java.io.PrintWriter;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An element in a <cite>Well Know Text</cite> (WKT). Such element are made of {@link String},
 * {@link Number} and other {@link WKTElement}. For example:
 *
 * <blockquote><pre>
 * PRIMEM["Greenwich", 0.0, AUTHORITY["some authority", "Greenwich"]]
 * </pre></blockquote>
 *
 * Each <code>WKTElement</code> object can contains an arbitrary amount of other elements.
 * The result is a tree, which can be printed with {@link #print}.
 * Elements can be pull in a <cite>first in, first out</cite> order.
 *
 * @version $Id: WKTElement.java,v 1.1 2002/09/02 17:55:39 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
final class WKTElement {    
    /**
     * The position where children elements start in the string to be parsed.
     */
    final int offset;

    /**
     * Keyword of this entity. For example: "PRIMEM".
     */
    private final String keyword;

    /**
     * An ordered list of {@link String}s, {@link Number}s and other {@link WKTElement}s.
     * May be <code>null</code> if the keyword was not followed by a pair of brackets
     * (e.g. "NORTH").
     */
    private final List list;

    /**
     * Construct a new <code>WKTElement</code>.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @param  separator  The character to search.
     */ 
    public WKTElement(final WKTFormat format, final String text, final ParsePosition position)
        throws ParseException
    {
        /*
         * Find the first keyword in the specified string. If a keyword is found, then
         * the position is set to the index of the first character after the keyword.
         */
        int lower = position.getIndex();
        final int length = text.length();
        while (lower<length && Character.isSpaceChar(text.charAt(lower))) {
            lower++;
        }
        int upper = lower;
        while (upper<length && Character.isUnicodeIdentifierPart(text.charAt(upper))) {
            upper++;
        }
        if (upper <= lower) {
            position.setErrorIndex(lower);
            throw unparsableString(text, position);
        }
        keyword = text.substring(lower, upper).toUpperCase(format.locale);
        position.setIndex(upper);
        offset = upper;
        /*
         * Parse the opening bracket, then parse all elements inside the bracket.
         * Elements are parser sequentially and their type are selected according
         * their first character:
         *
         *   - If the first character is a quote, then the element is parsed as a String.
         *   - Otherwise, if the first character is a unicode identifier start, then the
         *     element is parsed as a chidren WKTElement.
         *   - Otherwise, the element is parsed as a number.
         */
        if (!parseOptionalSeparator(text, position, format.openingBracket)) {
            list = null;
            return;
        }
        list = new LinkedList();
        do {
            if (position.getIndex() >= length) {
                throw new ParseException(missingCharacter(format.closingBracket), length);
            }
            //
            // Try to parse the next element as a quoted string. We will take
            // it as a string if the first non-blank character is a quote.
            //
            if (parseOptionalSeparator(text, position, format.textDelimitor)) {
                lower = position.getIndex();        
                upper = text.indexOf(format.textDelimitor, lower+1);
                if (upper < lower) {
                    position.setErrorIndex(++lower);
                    throw new ParseException(missingCharacter(format.textDelimitor), lower);
                }
                list.add(text.substring(lower, upper).trim());
                position.setIndex(upper + 1);
                continue;
            }
            //
            // Try to parse the next element as a number. We will take it as a number if
            // the first non-blank character is not the begining of an unicode identifier.
            //
            lower = position.getIndex();        
            if (!Character.isUnicodeIdentifierStart(text.charAt(lower))) {
                final Number number;
                synchronized (format.format) {
                    number = format.format.parse(text, position);
                }
                if (number == null) {
                    // Do not update the error index; it is already updated by NumberFormat.
                    throw unparsableString(text, position);
                }
                list.add(number);
                continue;
            }
            // Otherwise, add the element as a child element.
            list.add(new WKTElement(format, text, position));
        } while (parseOptionalSeparator(text, position, format.elementSeparator));
        parseSeparator(text, position, format.closingBracket);
    }

    /**
     * Returns <code>true</code> if the next non-whitespace character is the specified separator.
     * Search is performed in string <code>text</code> from position <code>position</code>. If the
     * separator is found, then the position is set to the first character after the separator.
     * Otherwise, the position is set on the first non-blank character.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @param  separator  The character to search.
     * @return <code>true</code> if the next non-whitespace character is the separator,
     *         or <code>false</code> otherwise.
     */
    private static boolean parseOptionalSeparator(final String        text,
                                                  final ParsePosition position,
                                                  final char          separator)
    {
        final int length = text.length();
        int index = position.getIndex();
        while (index < length) {
            final char c = text.charAt(index);
            if (Character.isSpaceChar(c)) {
                index++;
                continue;
            }
            if (c == separator) {
                position.setIndex(++index);
                return true;
            }
            break;
        }
        position.setIndex(index); // MANDATORY for correct working of the constructor.
        return false;
    }

    /**
     * Moves to the next non-whitespace character and checks if this character is the
     * specified separator. If the separator is found, it is skipped. Otherwise, this
     * method thrown a {@link ParseException}.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @param  separator  The character to search.
     * @throws ParseException if the separator was not found.
     */
    private void parseSeparator(final String        text,
                                       final ParsePosition position,
                                       final char          separator)
        throws ParseException
    {
        if (!parseOptionalSeparator(text, position, separator)) {
            position.setErrorIndex(position.getIndex());
            throw unparsableString(text, position);
        }
    }

    /**
     * Returns a {@link ParseException} with a "Unparsable string" message. The error message
     * is built from the specified string starting at the specified position. Properties
     * {@link ParsePosition#getIndex} and {@link ParsePosition#getErrorIndex} must be accurate
     * before this method is invoked.
     *
     * @param  text The unparsable string.
     * @param  position The position in the string.
     * @return An exception with a formatted error message.
     */
    private ParseException unparsableString(final String text, final ParsePosition position) {
        final int lower = position.getErrorIndex();
        int upper = lower;
        final int length = text.length();
        if (upper < length) {
            final int type = Character.getType(text.charAt(upper));
            while (++upper < length) {
                if (Character.getType(text.charAt(upper)) != type) {
                    break;
                }
            }
        }
        return new ParseException(complete(Resources.format(ResourceKeys.ERROR_UNPARSABLE_STRING_$2,
                text.substring(position.getIndex()), text.substring(lower, upper))), lower);
    }

    /**
     * Returns a message saying that a character is missing.
     *
     * @param c The missing character.
     */
    private String missingCharacter(final char c) {
        return complete(Resources.format(
                        ResourceKeys.ERROR_MISSING_CHARACTER_$1, new Character(c)));
    }

    /**
     * Returns an exception saying that a parameter is missing.
     *
     * @param key The name of the missing parameter.
     */
    private ParseException missingParameter(final String key) {
        return new ParseException(complete(Resources.format(
                    ResourceKeys.ERROR_MISSING_PARAMETER_$1, key)), offset);
    }

    /**
     * Append a prefix "Error in XXX" before the error message.
     *
     * @param  message The message to complete.
     * @return The completed message.
     */
    private String complete(String message) {
        if (keyword != null) {
            message = Resources.format(ResourceKeys.ERROR_IN_$2, keyword, message);
        }
        return message;
    }

    /**
     * Removes the next {@link Number} from the list and returns it.
     *
     * @param  key The parameter name. Used for formatting
     *         an error message if no number are found.
     * @return The next {@link Number} on the list as a <code>double</code>.
     * @throws ParseException if no more number is available.
     */
    public double pullDouble(final String key) throws ParseException {
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof Number) {
                iterator.remove();
                return ((Number)object).doubleValue();
            }
        }
        throw missingParameter(key);
    }

    /**
     * Removes the next {@link String} from the list and returns it.
     *
     * @param  key The parameter name. Used for formatting
     *         an error message if no number are found.
     * @return The next {@link String} on the list.
     * @throws ParseException if no more string is available.
     */
    public String pullString(final String key) throws ParseException {
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof String) {
                iterator.remove();                
                return (String)object;
            }
        }
        throw missingParameter(key);
    }

    /**
     * Removes the next {@link WKTElement} from the list and returns it.
     *
     * @param  key The element name (e.g. <code>"PRIMEM"</code>).
     * @return The next {@link WKTElement} on the list.
     * @throws ParseException if no more element is available.
     */
    public WKTElement pullElement(final String key) throws ParseException {
        final WKTElement element = pullOptionalElement(keyword);
        if (element != null) {
            return element;
        }
        throw missingParameter(key);
    }        

    /**
     * Removes the next {@link WKTElement} from the list and returns it.
     *
     * @param  key The element name (e.g. <code>"PRIMEM"</code>).
     * @return The next {@link WKTElement} on the list,
     *         or <code>null</code> if no more element is available.
     */
    public WKTElement pullOptionalElement(String key) {
        key = key.toUpperCase();
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof WKTElement) {
                final WKTElement element = (WKTElement) object;
                if (element.list!=null && element.keyword.equals(key)) {
                    iterator.remove();
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Removes and returns the next {@link WKTElement} with no bracket.
     * The key is used only for only for formatting an error message.
     *
     * @param  key The parameter name. Used only for formatting an error message.
     * @param  The next {@link WKTElement} on the list with no bracket.
     * @throws ParseException if no more void element is available.
     */
    public WKTElement pullVoidElement(final String key) throws ParseException {
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof WKTElement) {
                final WKTElement element = (WKTElement) object;
                if (element.list == null) {
                    iterator.remove();
                    return element;
                }
            }
        }
        throw missingParameter(key);
    }

    /**
     * Close this element.
     *
     * @throws ParseException If the list still contains some unprocessed elements.
     */
    public void close() throws ParseException {
        if (list!=null && !list.isEmpty()) {
            throw new ParseException(complete(Resources.format(
                        ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, list.get(0))), offset);
        }
    }

    /**
     * Returns the keyword. This overriding is needed for correct
     * formatting of the error message in {@link #close}.
     */
    public String toString() {
        return keyword;
    }

    /**
     * Print this <code>WKTElement</code> as a tree.
     * This method is used for debugging purpose only.
     *
     * @param  out    The output stream.
     * @param  level  The indentation level (usually 0).
     */
    public void print(final PrintWriter out, final int level) {
        final int tabWidth = 4;
        out.print(Utilities.spaces(tabWidth * level));
        out.println(keyword);
        if (list == null) {
            return;
        }
        final int size = list.size();
        for (int j=0; j<size; j++) {
            final Object object = list.get(j);
            if (object instanceof WKTElement) {
                ((WKTElement)object).print(out, level+1);
            } else {
                out.print(Utilities.spaces(tabWidth * (level+1)));
                out.println(object);
            }
        }
    }
}
