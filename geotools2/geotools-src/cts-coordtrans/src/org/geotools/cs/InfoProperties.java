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
import java.util.HashMap;
import java.rmi.RemoteException;


/**
 * A set of properties fetched from a {@link CS_Info} object.
 * Note: current implementation search all info immediatly.
 * Future implementation may differ fetching until needed.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class InfoProperties extends HashMap implements CharSequence
{
    /**
     * The source name.
     */
    private final String name;
    
    /**
     * Construct an <code>InfoProperties</code> for
     * the specified source.
     *
     * @param  info The OpenGIS structure.
     * @throws RemoteException if a remote call failed.
     */
    public InfoProperties(final CS_Info info) throws RemoteException
    {
        super(16);
        name = info.getName();
        put("authority",      info.getAuthority());
        put("authorityCode",  info.getAuthorityCode());
        put("alias",          info.getAlias());
        put("abbreviation",   info.getAbbreviation());
        put("remarks",        info.getRemarks());
        put("WKT",            info.getWKT());
        put("XML",            info.getXML());
        put("proxy",          info);
    }

    /**
     * Returns the length of this character sequence.
     */
    public int length() {
        return name.length();
    }

    /**
     * Returns the character at the specified index.
     */
    public char charAt(int index) {
        return name.charAt(index);
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int start, int end) {
        return name.substring(start, end);
    }

    /**
     * Returns the name.
     */
    public String toString() {
        return name;
    }
}
