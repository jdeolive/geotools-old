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
 */
package org.geotools.resources;

// Collections and arrays
import java.util.Arrays;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;


/**
 * A set of miscellaneous methods.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public final class Utilities {
    /**
     * An array of strings containing only white spaces. Strings' lengths are
     * equal to their index + 1 in the <code>spacesFactory</code> array.
     * For example, <code>spacesFactory[4]</code> contains a string of
     * length 5.  Strings are constructed only when first needed.
     */
    private static final String[] spacesFactory = new String[20];

    /**
     * Forbid object creation.
     */
    private Utilities() {
    }

    /**
     * Determines whether the character is a superscript. Most superscripts have
     * unicode values from \\u2070 to \\u207F inclusive. Superscripts are the
     * following symbols:
     *
     * <blockquote><code>
     *     \u2070 \u00B9 \u00B2 \u00B3 \u2074 \u2075 \u2076 \u2077
     *     \u2078 \u2079 \u207A \u207B \u207C \u207D \u207E \u207F
     * </code></blockquote>
     */
    public static boolean isSuperScript(final char c) {
        switch (c) {
            /*1*/case '\u2071':
            /*2*/case '\u2072':
            /*3*/case '\u2073': return false;
            /*1*/case '\u00B9':
            /*2*/case '\u00B2':
            /*3*/case '\u00B3': return true;
        }
        return (c>='\u2070' && c<='\u207F');
    }

    /**
     * Determines whether the character is a subscript. Most subscripts have
     * unicode values from \\u2080 to \\u208E inclusive. Subscripts are the
     * following symbols:
     *
     * <blockquote><code>
     *     \u2080 \u2081 \u2082 \u2083 \u2084 \u2085 \u2086 \u2087
     *     \u2088 \u2089 \u208A \u208B \u208C \u208D \u208E
     * </code></blockquote>
     */
    public static boolean isSubScript(final char c) {
        return (c>='\u2080' && c<='\u208E');
    }

    /**
     * Converts the character argument to superscript.
     * Only the following characters can be converted
     * (other characters are left unchanged):
     *
     * <blockquote><pre>
     * 0 1 2 3 4 5 6 7 8 9 + - = ( ) n
     * </pre></blockquote>
     */
    public static char toSuperScript(final char c) {
        switch (c) {
            case '1': return '\u00B9';
            case '2': return '\u00B2';
            case '3': return '\u00B3';
            case '+': return '\u207A';
            case '-': return '\u207B';
            case '=': return '\u207C';
            case '(': return '\u207D';
            case ')': return '\u207E';
            case 'n': return '\u207F';
        }
        if (c>='0' && c<='9') {
            return (char) (c+('\u2070'-'0'));
        }
        return c;
    }

    /**
     * Converts the character argument to subscript.
     * Only the following characters can be converted
     * (other characters are left unchanged):
     *
     * <blockquote><pre>
     * 0 1 2 3 4 5 6 7 8 9 + - = ( ) n
     * </pre></blockquote>
     */
    public static char toSubScript(final char c) {
        switch (c) {
            case '+': return '\u208A';
            case '-': return '\u208B';
            case '=': return '\u208C';
            case '(': return '\u208D';
            case ')': return '\u208E';
        }
        if (c>='0' && c<='9') {
            return (char) (c+('\u2080'-'0'));
        }
        return c;
    }

    /**
     * Converts the character argument to normal script.
     */
    public static char toNormalScript(final char c) {
        switch (c) {
            case '\u00B9': return '1';
            case '\u00B2': return '2';
            case '\u00B3': return '3';
            case '\u2071': return c;
            case '\u2072': return c;
            case '\u2073': return c;
            case '\u207A': return '+';
            case '\u207B': return '-';
            case '\u207C': return '=';
            case '\u207D': return '(';
            case '\u207E': return ')';
            case '\u207F': return 'n';
            case '\u208A': return '+';
            case '\u208B': return '-';
            case '\u208C': return '=';
            case '\u208D': return '(';
            case '\u208E': return ')';
        }
        if (c>='\u2070' && c<='\u2079') return (char) (c-('\u2070'-'0'));
        if (c>='\u2080' && c<='\u2089') return (char) (c-('\u2080'-'0'));
        return c;
    }

    /**
     * Returns a string of the specified length filled with white spaces.
     * This method tries to return a pre-allocated string if possible.
     *
     * @param  length The string length. Negative values are clamped to 0.
     * @return A string of length <code>length</code> filled with white spaces.
     */
    public static String spaces(int length) {
        // No need to synchronize.  In the unlikely event of two threads
        // calling this method at the same time and the two calls creating a
        // new string, the String.intern() call will take care of
        // canonicalizing the strings.
        final int last = spacesFactory.length-1;
        if (length<0) length=0;
        if (length <= last) {
            if (spacesFactory[length]==null) {
                if (spacesFactory[last]==null) {
                    char[] blancs = new char[last];
                    Arrays.fill(blancs, ' ');
                    spacesFactory[last]=new String(blancs).intern();
                }
                spacesFactory[length] = spacesFactory[last].substring(0,length).intern();
            }
            return spacesFactory[length];
        } else {
            char[] blancs = new char[length];
            Arrays.fill(blancs, ' ');
            return new String(blancs);
        }
    }

    /**
     * Returns a short class name for the specified class. This method will
     * omit the package name.  For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object. It will also name
     * array according Java language usage,  for example "double[]" instead
     * of "[D".
     *
     * @param  object The object (may be <code>null</code>).
     * @return A short class name for the specified object.
     */
    public static String getShortName(Class classe) {
        if (classe == null) {
            return "<*>";
        }
        int dimension = 0;
        Class el;
        while ((el = classe.getComponentType()) != null) {
            classe = el;
            dimension++;
        }
        String name = classe.getName();
        final int lower = name.lastIndexOf('.');
        final int upper = name.length();
        name = name.substring(lower+1, upper).replace('$','.');
        if (dimension != 0) {
            StringBuffer buffer = new StringBuffer(name);
            do {
                buffer.append("[]");
            } while (--dimension != 0);
            name = buffer.toString();
        }
        return name;
    }

    /**
     * Returns a short class name for the specified object. This method will
     * omit the package name. For example, it will return "String" instead
     * of "java.lang.String" for a {@link String} object.
     *
     * @param  object The object (may be <code>null</code>).
     * @return A short class name for the specified object.
     */
    public static String getShortClassName(final Object object) {
        return getShortName(object!=null ? object.getClass() : null);
    }

    /**
     * Convenience method for testing two objects for
     * equality. One or both objects may be null.
     */
    public static boolean equals(final Object object1, final Object object2) {
        return (object1==object2) || (object1!=null && object1.equals(object2));
    }

    /**
     * Invoked when an unexpected error occurs. The action taken by this method
     * is implementation dependent. On JRE 1.4, it may record the error in the
     * logging. On JRE 1.3, it may just dump the stack trace on the error
     * stream.
     *
     * @param paquet  The package where the error occurred. This information
     *                may be used to fetch an appropriate {@link Logger} for
     *                logging the error.
     * @param classe  The class name where the error occurred.
     * @param method  The method name where the error occurred.
     * @param error   The error.
     */
    public static void unexpectedException(final String   paquet,
                                           final String   classe,
                                           final String   method,
                                           final Throwable error) {
        final StringBuffer buffer = new StringBuffer(getShortClassName(error));
        final String message = error.getLocalizedMessage();
        if (message!=null) {
            buffer.append(": ");
            buffer.append(message);
        }
        final LogRecord record = new LogRecord(Level.WARNING, buffer.toString());
        record.setSourceClassName (classe);
        record.setSourceMethodName(method);
        record.setThrown          (error);
        Logger.getLogger(paquet).log(record);
    }
}
