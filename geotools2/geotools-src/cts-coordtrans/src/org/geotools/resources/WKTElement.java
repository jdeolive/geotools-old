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

// Collections
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;

// Parsing
import java.util.Locale;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.ParseException;

// Input/output
import java.io.PrintWriter;

// Resources
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * An element in a <cite>Well Know Text</cite> (WKT).
 * A <code>WKTElement</code> is made of {@link String}, {@link Number}
 * and other {@link WKTElement}. For example:
 *
 * <blockquote><pre>
 * PRIMEM["Greenwich", 0.0, AUTHORITY["some authority", "Greenwich"]]
 * </pre></blockquote>
 *
 * Each <code>WKTElement</code> object can contains an arbitrary amount of other elements.
 * The result is a tree, which can be printed with {@link #print}.
 * Elements can be pull in a <cite>first in, first out</cite> order.
 *
 * @version $Id: WKTElement.java,v 1.4 2002/09/08 11:05:08 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public final class WKTElement {    
    /**
     * The position where this element starts in the string to be parsed.
     */
    private final int offset;

    /**
     * Keyword of this entity. For example: "PRIMEM".
     */
    public final String keyword;

    /**
     * An ordered list of {@link String}s, {@link Number}s and other {@link WKTElement}s.
     * May be <code>null</code> if the keyword was not followed by a pair of brackets
     * (e.g. "NORTH").
     */
    private final List list;

    /**
     * Construct a root element.
     *
     * @param element The only children for this root.
     */
    WKTElement(final WKTElement singleton) {
        offset  = 0;
        keyword = null;
        list    = new LinkedList();
        list.add(singleton);
    }

    /**
     * Construct a new <code>WKTElement</code>.
     *
     * @param  text       The text to parse.
     * @param  position   In input, the position where to start parsing from.
     *                    In output, the first character after the separator.
     * @param  separator  The character to search.
     */ 
    WKTElement(final WKTFormat format, final String text, final ParsePosition position)
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
        offset = lower;
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
        /*
         * Parse the opening bracket. According CTS's specification, two characters
         * are acceptable: '[' and '('.  At the end of this method, we will require
         * the matching closing bracket. For example if the opening bracket was '[',
         * then we will require that the closing bracket is ']' and not ')'.
         */
        int bracketIndex = -1;
        do {
            if (++bracketIndex >= format.openingBrackets.length) {
                list = null;
                return;
            }
        }
        while (!parseOptionalSeparator(text, position, format.openingBrackets[bracketIndex]));
        list = new LinkedList();
        /*
         * Parse all elements inside the bracket. Elements are parsed sequentially
         * and their type are selected according their first character:
         *
         *   - If the first character is a quote, then the element is parsed as a String.
         *   - Otherwise, if the first character is a unicode identifier start, then the
         *     element is parsed as a chidren WKTElement.
         *   - Otherwise, the element is parsed as a number.
         */
        do {
            if (position.getIndex() >= length) {
                throw missingCharacter(format.closingBracket, length);
            }
            //
            // Try to parse the next element as a quoted string. We will take
            // it as a string if the first non-blank character is a quote.
            //
            if (parseOptionalSeparator(text, position, format.textDelimitor)) {
                lower = position.getIndex();        
                upper = text.indexOf(format.textDelimitor, lower);
                if (upper < lower) {
                    position.setErrorIndex(++lower);
                    throw missingCharacter(format.textDelimitor, lower);
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
                synchronized (format.number) {
                    number = format.number.parse(text, position);
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
        parseSeparator(text, position, format.closingBrackets[bracketIndex]);
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




    //////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                      ////////
    ////////    Construction of a ParseException when a string can't be parsed    ////////
    ////////                                                                      ////////
    //////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a {@link ParseException} with the specified cause. A localized string
     * <code>"Error in <{@link #keyword}>"</code> will be prepend to the message.
     * The error index will be the starting index of this <code>WKTElement</code>.
     *
     * @param  cause   The cause of the failure, or <code>null</code> if none.
     * @param  message The message explaining the cause of the failure, or <code>null</code>
     *                 for reusing the same message than <code>cause</code>.
     * @return The exception to be thrown.
     */
    public ParseException parseFailed(final Exception cause, String message) {
        if (message == null) {
            message = cause.getLocalizedMessage();
        }
        ParseException exception = new ParseException(complete(message), offset);
        exception = trim("parseFailed", exception);
        exception.initCause(cause);
        return exception;
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
        return trim("unparsableString", new ParseException(complete(
                    Resources.format(ResourceKeys.ERROR_UNPARSABLE_STRING_$2,
                    text.substring(position.getIndex()), text.substring(lower, upper))), lower));
    }

    /**
     * Returns an exception saying that a character is missing.
     *
     * @param c The missing character.
     * @param position The error position.
     */
    private ParseException missingCharacter(final char c, final int position) {
        return trim("missingCharacter", new ParseException(complete(
                    Resources.format(ResourceKeys.ERROR_MISSING_CHARACTER_$1, new Character(c))),
                    position));
    }

    /**
     * Returns an exception saying that a parameter is missing.
     *
     * @param key The name of the missing parameter.
     */
    private ParseException missingParameter(final String key) {
        return trim("missingParameter", new ParseException(complete(
                    Resources.format(ResourceKeys.ERROR_MISSING_PARAMETER_$1, key)),
                    offset + keyword.length()));
    }

    /**
     * Append a prefix "Error in <keyword>: " before the error message.
     *
     * @param  message The message to complete.
     * @return The completed message.
     */
    private String complete(String message) {
        if (keyword != null) {
            message = Resources.format(ResourceKeys.ERROR_IN_$1, keyword) + ' ' + message;
        }
        return message;
    }

    /**
     * Remove the exception factory method from the stack trace. The factory is
     * not the place where the failure occurs; the error occurs in the factory's
     * caller.
     *
     * @param  factory   The name of the factory method.
     * @param  exception The exception to trim.
     * @return <code>exception</code> for convenience.
     */
    private static ParseException trim(final String factory, final ParseException exception) {
        StackTraceElement[] trace = exception.getStackTrace();
        if (trace!=null && trace.length!=0) {
            if (factory.equals(trace[0].getMethodName())) {
                trace = (StackTraceElement[]) XArray.remove(trace, 0, 1);
                exception.setStackTrace(trace);
            }
        }
        return exception;
    }




    //////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                      ////////
    ////////    Pull elements from the tree                                       ////////
    ////////                                                                      ////////
    //////////////////////////////////////////////////////////////////////////////////////
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
     * Removes the next {@link Number} from the list and returns it
     * as an integer.
     *
     * @param  key The parameter name. Used for formatting
     *         an error message if no number are found.
     * @return The next {@link Number} on the list as an <code>int</code>.
     * @throws ParseException if no more number is available, or the number
     *         is not an integer.
     */
    public int pullInteger(final String key) throws ParseException {
        final Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof Number) {
                iterator.remove();
                final Number number = (Number) object;
                if (number instanceof Float || number instanceof Double) {
                    throw new ParseException(complete(Resources.format(
                            ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, key, number)), offset);
                }
                return number.intValue();
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
        final WKTElement element = pullOptionalElement(key);
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
     * Returns the next element, or <code>null</code> if there is no more
     * element. The element is <strong>not</strong> removed from the list.
     */
    public Object peek() {
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Close this element.
     *
     * @throws ParseException If the list still contains some unprocessed elements.
     */
    public void close() throws ParseException {
        if (list!=null && !list.isEmpty()) {
            throw new ParseException(complete(Resources.format(
                        ResourceKeys.ERROR_UNEXPECTED_PARAMETER_$1, list.get(0))),
                        offset+keyword.length());
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
