/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.io.coverage;

// J2SE dependencies
import java.lang.Throwable;
import javax.imageio.IIOException;


/**
 * The base class for error related to grid coverage's properties.
 * This exception is thrown by the helper class {@link PropertyParser}.
 *
 * @version $Id: PropertyException.java,v 1.3 2003/05/13 10:59:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PropertyException extends IIOException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3146379152278866037L;

    /**
     * The key for the faulty property, or <code>null</code> if none.
     */
    private final PropertyParser.Key key;

    /**
     * The alias used for the property, or <code>null</code> if none.
     */
    private final String alias;

    /**
     * Construct an exception with the specified message and no key.
     *
     * @param message The message, or <code>null</code> if none.
     */
    public PropertyException(final String message)
    {
        super(message);
        key   = null;
        alias = null;
    }

    /**
     * Construct an exception with the specified message and exception as its cause.
     *
     * @param message The message, or <code>null</code> if none.
     * @param cause   The cause for this exception.
     */
    public PropertyException(final String message, final Throwable cause)
    {
        super(message, cause);
        key   = null;
        alias = null;
    }

    /**
     * Construct an exception with the specified message. This exception is usually
     * raised because no value was defined for the key <code>key</code>, or the
     * value was ambiguous.
     *
     * @param message The message, or <code>null</code> if none.
     * @param key     The property key which was the cause for this exception, or
     *                <code>null</code> if none. This is a format neutral key,
     *                for example {@link PropertyParser#DATUM}.
     * @param alias   The alias used for for the key <code>key</code>, or <code>null</code>
     *                if none. This is usually the name used in the external file parsed.
     */
    public PropertyException(final String message,
                             final PropertyParser.Key key,
                             final String alias)
    {
        super(message);
        this.key   = key;
        this.alias = alias;
    }

    /**
     * Returns the property key which has raised this exception. This exception has usually
     * been raised because no value was defined for this key, or the value was ambiguous.
     *
     * @return The property key, or <code>null</code> if none.
     */
    public PropertyParser.Key getPropertyKey() {
        return key;
    }

    /**
     * Returns the alias used for the key {@link #getPropertyKey}. This is usually the name
     * used in the external file to be parsed. The alias is format-dependent, while the key
     * (as returned by {@link #getPropertyKey}) if format neutral.
     *
     * @return The alias, or <code>null</code> if none.
     */
    public String getPropertyAlias() {
        return alias;
    }

    /**
     * Returns a string representation of this exception. This implementation is similar to
     * {@link Throwable#toString()}, except that the string will includes key and alias names
     * if they are defined. The localized message, if any, may be written on the next line.
     * Example:
     *
     * <blockquote><pre>
     * org.geotools.io.coverage.MissingPropertyException(key="YMaximum", alias="ULY"):
     * Aucune valeur n'est définie pour la propriété "ULY".
     * </pre></blockquote>
     */
    public String toString() {
        final PropertyParser.Key key = getPropertyKey();
        final String alias = getPropertyAlias();
        if (key==null && alias==null) {
            return super.toString();
        }
        final StringBuffer buffer = new StringBuffer(getClass().getName());
        buffer.append('[');
        if (key != null) {
            buffer.append("key=\"");
            buffer.append(key);
            buffer.append('"');
            if (alias != null) {
                buffer.append(", ");
            }
        }
        if (alias != null) {
            buffer.append("alias=\"");
            buffer.append(alias);
            buffer.append('"');
        }
        buffer.append(']');
        final String message = getLocalizedMessage();
        if (message != null) {
            buffer.append(':');
            buffer.append(System.getProperty("line.separator", "\n"));
            buffer.append(message);
        }
        return buffer.toString();
    }
}
