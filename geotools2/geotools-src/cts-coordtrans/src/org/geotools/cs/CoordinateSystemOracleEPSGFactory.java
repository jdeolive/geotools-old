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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * An EPSG factory suitable for Oracle SQL syntax.
 *
 * @version $Id: CoordinateSystemOracleEPSGFactory.java,v 1.2 2004/02/06 15:52:57 desruisseaux Exp $
 * @author John Grange
 */
public class CoordinateSystemOracleEPSGFactory extends CoordinateSystemModifiedEPSGFactory {
    /**
     * The pattern to use for removing <code>" as "</code> elements from the SQL statements.
     */
    private final Pattern pattern = Pattern.compile("\\sas\\s");

    /**
     * Construct an authority factory using
     * the specified connection.
     *
     * @param factory    The underlying factory used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     */
    public CoordinateSystemOracleEPSGFactory(final CoordinateSystemFactory factory,
                                             final Connection           connection)
    {
        super(factory, connection);
    }

    /**
     * Construct an authority factory using the specified connection
     * and a database schema. If the database schema is not supplied,
     * or it is null or an empty string, then the tables are assumed
     * to be in the same schema as the user which is being used to connect
     * to the database.  You MUST ensure that the connecting user has permissions
     * to select from all the tables in the epsg user schema.
     *
     * @param factory    The underlying factory used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     */
    public CoordinateSystemOracleEPSGFactory(final CoordinateSystemFactory factory,
                                             final Connection           connection,
                                             final String               epsgSchema)
    {
        super(factory, connection);
        adaptTableNames(epsgSchema);
    }

    /**
     * Construct an authority factory using
     * the specified URL to an EPSG database.
     *
     * @param factory The underlying factory used for objects creation.
     * @param url     The url to the EPSG database.
     * @param driver  An optional driver to load, or <code>null</code> if none.
     *                This is a convenience argument for the following pseudo-code:
     *                <blockquote><code>
     *                Class.forName(driver).newInstance();
     *                </code></blockquote>
     *                A message is logged to <code>"org.geotools.cts"</code> stating if
     *                the loading succeeded of failed. This argument needs to be non-null
     *                only once for a specific driver.
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public CoordinateSystemOracleEPSGFactory(final CoordinateSystemFactory factory,
                                             final String url, final String driver)
            throws SQLException
    {
        super(factory, url, driver);
    }

    /**
     * Construct an authority factory using the specified URL to an EPSG database
     * and a database schema. If the database schema is not supplied, or it is null
     * or an empty string, then the tables are assumed to be in the same schema as
     * the user which is being used to connect to the database.  You MUST ensure that
     * the connecting user has permissions to select from all the tables in the epsg
     * user schema.
     *
     * @param factory The underlying factory used for objects creation.
     * @param url     The url to the EPSG database.
     * @param driver  An optional driver to load, or <code>null</code> if none.
     *                This is a convenience argument for the following pseudo-code:
     *                <blockquote><code>
     *                Class.forName(driver).newInstance();
     *                </code></blockquote>
     *                A message is logged to <code>"org.geotools.cts"</code> stating if
     *                the loading succeeded of failed. This argument needs to be non-null
     *                only once for a specific driver.
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public CoordinateSystemOracleEPSGFactory(final CoordinateSystemFactory factory,
                                             final String url, final String driver,
                                             final String epsgSchema)
            throws SQLException
    {
        super(factory, url, driver);
        adaptTableNames(epsgSchema);
    }

    /**
     * Modifies the given SQL string to be suitable for an Oracle databases.
     * This removes <code>" as "</code> elements from the SQL statements as
     * these don't work in oracle.
     *
     * @param statement The statement in MS-Access syntax.
     * @return The SQL statement to use, suitable for an Oracle database.
     */
    protected String adaptSQL(String statement) {
        return pattern.matcher(super.adaptSQL(statement)).replaceAll(" ");
    }

    /**
     * If we have been supplied with a non null <code>epsgSchema</code>,
     * prepend the schema to all the table names.
     *
     * @param epsgSchema The database schema in which the epsg tables are stored (optional).
     */
    private void adaptTableNames(String epsgSchema) {
        if (epsgSchema != null) {
            epsgSchema = epsgSchema.trim();
            if (epsgSchema.length() != 0) {
                for (final Iterator it=map.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry  entry = (Map.Entry) it.next();
                    final String tableName = (String) entry.getValue();
                    /**
                     * Update the map, prepending the schema name to the table name
                     * so long as the value is a table name and not a field. This
                     * algorithm assumes that all old table names start with "epsg_".
                     */
                    if (tableName.startsWith("epsg_")) {
                        entry.setValue(epsgSchema + '.' + tableName);
                    }
                }
            }
        }
    }
}
