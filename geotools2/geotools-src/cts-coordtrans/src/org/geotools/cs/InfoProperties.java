/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_Info;

// J2SE dependencies
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;


/**
 * A map of properties for {@link Info} objects. This map doesn't obey strictly to
 * the {@link Map} contract. For example, {@link #equals} doesn't accept arbitrary
 * {@link Map} object and {@link #putAll} may ignore some entries.  However, since
 * this map is used only internally by {@link Info} objects,   users don't need to
 * know those short-comming.
 *
 * @version $Id: InfoProperties.java,v 1.4 2002/08/24 12:28:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class InfoProperties implements Map, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 841701484507360819L;

    /**
     * Keys of properties held by {@link Info} objects.
     * <STRONG>This array must be in alphabetical order</STRONG>.
     * <br><br>
     * Note: this array contains a "proxy" key.  This "proxy" value may be the same
     *       than {@link Info#proxy}. However, the "proxy" declared in {@link Info}
     *       may be an object dynamically built  by  {@link Adapters#export(Info)},
     *       while the proxy declared here is always a {@link CS_Info} object explicitly
     *       specified by the user. The {@link Info#proxy} object is ignored when comparing
     *       {@link Info} objects (since we can rebuilt it at any time),  while the "proxy"
     *       declared in this <code>InfoProperties</code> is take in account during comparaison.
     */
    private static final String[] PROPERTY_KEYS = {
        "abbreviation",
        "alias",
        "authority",
        "authorityCode",
        "proxy",         // Value is not a String
        "remarks"
    };

    /**
     * Keys of properties held by {@link Info} objects.  This is usually equals
     * to <code>PROPERTY_KEYS</code>. We keep a reference to this array in each
     * <code>Info</code> instance in order to save them during serialization.
     * If the set of property keys change in a future version, this reference
     * will make it possible to retreive properties from the older version.
     */
    private String[] propertyKeys = PROPERTY_KEYS;
    
    /**
     * Properties for all <code>get</code>methods except {@link Info#getName}.
     * For example, the method {@link Info#getAuthorityCode} returns the value
     * of property <code>"authorityCode"</code>.
     */
    private final Object[] properties = new Object[propertyKeys.length];

    /**
     * Construct a map with initial entries for the specified map.
     */
    public InfoProperties(final Map map) {
        putAll(map); // Copy only String objects
        put("proxy", (map instanceof InfoProperties) ? map.get("proxy") : null);
    }

    /**
     * Returns the number of elements in this map.
     */
    public final int size() {
        int count = 0;
        for (int i=0; i<properties.length; i++) {
            if (properties[i] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     */
    public final boolean isEmpty() {
	return size() == 0;
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to this value.
     */
    public final boolean containsValue(final Object value) {
        if (value != null) {
            for (int i=0; i<properties.length; i++) {
                if (value.equals(properties[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key.
     */
    public final boolean containsKey(final Object key) {
        return Arrays.binarySearch(propertyKeys, key) >= 0;
    }

    /**
     * Returns the value to which this map maps the specified key. Returns
     * <code>null</code> if the map contains no mapping for this key. This
     * method may be overrided by {@link InfoProperties.Adapter}  in order
     * to fetches properties only when first requested.
     */
    public Object get(final Object key) {
        final int i = Arrays.binarySearch(propertyKeys, key);
        return (i>=0) ? properties[i] : null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param  key key with which the specified value is to be associated.
     * @param  value value to be associated with the specified key, or
     *         <code>null</code> to remove the key-value mapping.
     * @throws IllegalArgumentException if <code>key</code> is not a recognized key.
     */
    public final Object put(final Object key, final Object value) {
        final int i = Arrays.binarySearch(propertyKeys, key);
        if (i<0) {
            throw new IllegalArgumentException(String.valueOf(key));
        }
        final Object old = properties[i];
        properties[i] = value;
        return old;
    }

    /**
     * Removes the mapping for this key from this map if present.
     */
    public final Object remove(final Object key) {
        return put(key, null);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * Only values for key recognized by this map will be copied.
     * Other key-value pairs will be ignored.
     */
    public final void putAll(final Map map) {
        if (map != null) {
            for (int i=0; i<propertyKeys.length; i++) {
                final Object value = map.get(propertyKeys[i]);
                if (value instanceof String) {
                    properties[i] = value;
                }
            }
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public final void clear() {
        Arrays.fill(properties, null);
    }

    /**
     * Unsupported view.
     */
    public final Set keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported view.
     */
    public final Collection values() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported view.
     */
    public final Set entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Compares the specified object with this map for equality.
     */
    public final boolean equals(final Object other) {
        if (other instanceof InfoProperties) {
            final InfoProperties that = (InfoProperties) other;
            return Arrays.equals(this.properties,   that.properties) &&
                   Arrays.equals(this.propertyKeys, that.propertyKeys);
        }
        return false;
    }

    /**
     * Returns the hash code value for this map.
     */
    public final int hashCode() {
        int code = 0;
        for (int i=0; i<properties.length; i++) {
            if (properties[i] != null) {
                code += properties[i].hashCode() ^ propertyKeys[i].hashCode();
            }
        }
        return code;
    }

    /**
     * Canonicalize the reference to {@link PROPERTY_KEYS}, if possible.
     * It help to reduce slightly memory usage.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (Arrays.equals(PROPERTY_KEYS, propertyKeys)) {
            propertyKeys = PROPERTY_KEYS;
        }
    }

    /**
     * A set of properties together with a name.
     */
    static class Named extends InfoProperties implements CharSequence {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 7538378414620363218L;

        /**
         * The source name.
         */
        private final String name;

        /**
         * Constructs an <code>InfoProperties</code> with
         * the specified name.
         *
         * @param name the name.
         */
        public Named(final String name) {
            super(null);
            this.name = name;
        }

        /**
         * Returns the length of this character sequence.
         */
        public final int length() {
            return name.length();
        }

        /**
         * Returns the character at the specified index.
         */
        public final char charAt(int index) {
            return name.charAt(index);
        }

        /**
         * Returns a new character sequence that is a subsequence of this sequence.
         */
        public final CharSequence subSequence(int start, int end) {
            return name.substring(start, end);
        }

        /**
         * Returns the name.
         */
        public final String toString() {
            return name;
        }
    }

    /**
     * A set of properties fetched from a {@link CS_Info} object. The default {@link Info}
     * implementation use instances of {@link InfoProperties}, which have no notion of any
     * source {@link CS_Info} object.    Instances of  <code>InfoProperties.Adapter</code>
     * (constructed by <code>Adapters.export(...)</code> methods)  give an opportunity for
     * overriding the {@link InfoProperties#get} method  and fetches infos only when first
     * requested.
     *
     * @task TODO: Current implementation fetches all infos immediately (at construction time).
     *             Future implementation may defers fetching until needed. More specifically,
     *             we need to think for a framework for XML and WKT, since they may be heavy
     *             properties.
     */
    static final class Adapter extends Named {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 7453632109986034253L;

        /**
         * Constructs an <code>InfoProperties</code> for
         * the specified source.
         *
         * @param  info The OpenGIS object.
         * @throws RemoteException if a remote call fails.
         */
        public Adapter(final CS_Info info) throws RemoteException {
            super(info.getName());
            put("authority",      info.getAuthority());
            put("authorityCode",  info.getAuthorityCode());
            put("alias",          info.getAlias());
            put("abbreviation",   info.getAbbreviation());
            put("remarks",        info.getRemarks());
            put("proxy",          info);
            if (false) {
                // TODO: To fetch when first requested (see comment in class description)
                put("WKT",        info.getWKT());
                put("XML",        info.getXML());
            }
        }
    }
}
