/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.cs;

// J2SE dependencies
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.sql.SQLException;


/**
 * An EPSG factory which replaces some table or column names by other names.
 * This factory is used for EPSG database backed by an other software than
 * MS-Access.
 *
 * This class overrides {@link #adaptSQL} to change the SQL statements so they
 * are suitable for non-ms&nbsp;access versions of EPSG databases (version 6.4). 
 * By default, the new SQL statements use the new table and field names in 
 * the Data Description Language (DDL) scripts provided in version 6.4 to 
 * create the schema for the database. Subclasses can changes this default
 * behavior by modifying the {@link #map}.
 * <br><br> 
 * In order to register this ANSI version as the {@linkplain #getDefault default}
 * EPSG factory, invokes the following <u>only once</u> from the command line.
 * The change will by system-wide:
 *
 * <blockquote><pre>
 * java org.geotools.cs.CoordinateSystemEPSGFactory -implementation=org.geotools.cs.CoordinateSystemModifiedEPSGFactory
 * </pre></blockquote>
 * 
 * <br><br> 
 * <strong>References:</strong><ul>
 *   <li>EPSG geodecy parameters database version 6.4 readme at 
 *       <A HREF="http://www.ihsenergy.com/epsg/geodetic2.html">www.epsg.org</A>
 *   </li>
 * </ul>
 *
 * @version $Id: CoordinateSystemModifiedEPSGFactory.java,v 1.3 2004/02/03 20:37:07 desruisseaux Exp $
 * @author Rueben Schulz
 * @author Martin Desruisseaux
 */
public class CoordinateSystemModifiedEPSGFactory extends CoordinateSystemEPSGFactory {
    /**
     * The default map using ANSI names.
     */
    private static final String[] ANSI = {
        "[Area]",                                   "epsg_area",
        "[Coordinate Axis]",                        "epsg_coordinateaxis",
        "[Coordinate Axis Name]",                   "epsg_coordinateaxisname",
        "[Coordinate_Operation]",                   "epsg_coordoperation",
        "[Coordinate_Operation Method]",            "epsg_coordoperationmethod",
        "[Coordinate_Operation Parameter]",         "epsg_coordoperationparam",
        "[Coordinate_Operation Parameter Usage]",   "epsg_coordoperationparamusage",
        "[Coordinate_Operation Parameter Value]",   "epsg_coordoperationparamvalue",
        "[Coordinate Reference System]",            "epsg_coordinatereferencesystem",
        "[Coordinate System]",                      "epsg_coordinatesystem",
        "[Datum]",                                  "epsg_datum",
        "[Ellipsoid]",                              "epsg_ellipsoid",
        "[Prime Meridian]",                         "epsg_primemeridian",
        "[Unit of Measure]",                        "epsg_unitofmeasure",
        "[ORDER]",                                  "coord_axis_order" //a field in epsg_coordinateaxis
    };

    /**
     * Map the MS-Access names to ANSI names. Key are MS-Access names including bracket.
     * Values are ANSI names. Keys and values are case-sensitive. The default content of
     * this map is:
     *
     * <pre><table>
     *   <tr><th align="center">MS-Access name</th>            <th align="center">ANSI name</th></tr>
     *   <tr><td>[Area]</td>                                   <td>epsg_area</td></tr>
     *   <tr><td>[Coordinate Axis]</td>                        <td>epsg_coordinateaxis</td></tr>
     *   <tr><td>[Coordinate Axis Name]</td>                   <td>epsg_coordinateaxisname</td></tr>
     *   <tr><td>[Coordinate_Operation]</td>                   <td>epsg_coordoperation</td></tr>
     *   <tr><td>[Coordinate_Operation Method]</td>            <td>epsg_coordoperationmethod</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter]</td>         <td>epsg_coordoperationparam</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter Usage]</td>   <td>epsg_coordoperationparamusage</td></tr>
     *   <tr><td>[Coordinate_Operation Parameter Value]</td>   <td>epsg_coordoperationparamvalue</td></tr>
     *   <tr><td>[Coordinate Reference System]</td>            <td>epsg_coordinatereferencesystem</td></tr>
     *   <tr><td>[Coordinate System]</td>                      <td>epsg_coordinatesystem</td></tr>
     *   <tr><td>[Datum]</td>                                  <td>epsg_datum</td></tr>
     *   <tr><td>[Ellipsoid]</td>                              <td>epsg_ellipsoid</td></tr>
     *   <tr><td>[Prime Meridian]</td>                         <td>epsg_primemeridian</td></tr>
     *   <tr><td>[Unit of Measure]</td>                        <td>epsg_unitofmeasure</td></tr>
     *   <tr><td>[ORDER]</td>                                  <td>coord_axis_order</td></tr>
     * </table></pre>
     */
    protected final Map map = new LinkedHashMap();

    /**
     * Construct an authority factory using
     * the specified URL to an EPSG database.
     *
     * @param  factory The underlying factory used for objects creation.
     * @param  url     The url to the EPSG database. For example, a connection
     *                 to postgresql may have an URL like
     *                 <code>"jdbc:postgresql://localhost/epsg?user=&lt;user&gt;&pass=&lt;password&gt;"</code>.
     * @param  driver  An optional driver to load, or <code>null</code> if none.
     *                 This is a convenience argument for the following pseudo-code:
     *                 <blockquote><code>
     *                 Class.forName(driver).newInstance();
     *                 </code></blockquote>
     *                 A message is logged to <code>"org.geotools.cts"</code> stating if
     *                 the loading succeeded of failed. For postgresql, a typical value
     *                 for this argument is <code>"org.postgresql.Driver"</code>.
     *                 This argument needs to be non-null only once for a specific driver.
     *
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public CoordinateSystemModifiedEPSGFactory(final CoordinateSystemFactory factory,
                                               final String url, final String driver)
            throws SQLException
    {
        super(factory, url, driver);
        for (int i=0; i<ANSI.length; i++) {
            map.put(ANSI[i], ANSI[++i]);
        }
    }

    /**
     * Modifies the given SQL string to be suitable for non-ms&nbsp;access databases.
     * This replaces table and field names in the SQL with the new names 
     * in the SQL DDL scripts provided with version 6.4 of the EPSG database.
     *
     * @param  statement The statement in MS-Access syntax.
     * @return The SQL statement to use, suitable for a non-ms access database.     
     */
    protected String adaptSQL(final String statement) {
        final StringBuffer modified = new StringBuffer(statement);
        for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final String  oldName = (String) entry.getKey();
            final String  newName = (String) entry.getValue();
            /*
             * Replace all occurences of 'oldName' by 'newName'.
             */
            int start = 0;
            while ((start=modified.indexOf(oldName, start)) >= 0) {
                modified.replace(start, start+oldName.length(), newName);
                start += newName.length();
            }
        }
        return modified.toString();
    }
}
