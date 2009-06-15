/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory.epsg;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.sql.DataSource;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Loggings;
import org.geotools.util.Version;
import org.geotools.util.logging.Logging;
import org.h2.jdbcx.JdbcDataSource;



/**
 * Connection to the EPSG database in H2 database engine format using JDBC. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The SQL scripts (modified for the HSQL syntax as <A HREF="doc-files/HSQL.html">explained
 * here</A>) are bundled into this plugin. The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The HSQL database is read only.
 * <P>
 * <H3>Implementation note</H3>
 * The SQL scripts are executed the first time a connection is required. The database
 * is then created as cached tables ({@code HSQL.properties} and {@code HSQL.data} files)
 * in a temporary directory. Future connections to the EPSG database while reuse the cached
 * tables, if available. Otherwise, the scripts will be executed again in order to recreate
 * them.
 * <p>
 * If the EPSG database should be created in a different directory (or already exists in that
 * directory), it may be specified as a {@linkplain System#getProperty(String) system property}
 * nammed {@value #DIRECTORY_KEY}.
 *
 * @since 2.6
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/plugin/epsg-hsql/src/main/java/org/geotools/referencing/factory/epsg/ThreadedHsqlEpsgFactory.java $
 * @version $Id: ThreadedH2EpsgFactory.java 32612 2009-03-09 16:32:57Z aaime $
 * @author Martin Desruisseaux
 * @author Didier Richard
 */
public class ThreadedH2EpsgFactory extends ThreadedEpsgFactory {
    /**
     * Current version of EPSG-h2 plugin. This is usually the same version number than the
     * one in the EPSG database bundled in this plugin. However this field may contains
     * additional minor version number if there is some changes related to the EPSG-H2
     * plugin rather then the EPSG database itself (for example additional database index).
     */
    public static final Version VERSION = new Version("7.1.0");

    /**
     * The key for fetching the database directory from {@linkplain System#getProperty(String)
     * system properties}.
     */
    public static final String DIRECTORY_KEY = "EPSG-H2.directory";

    /**
     * The database name.
     */
    public static final String DATABASE_NAME = "EPSG";

    /**
     * The prefix to put in front of URL to the database.
     */
    private static final String PREFIX = "jdbc:h2:";

    /**
     * The logger name.
     */
    private static final String LOGGER = "org.geotools.referencing.factory.epsg";

    /**
     * Creates a new instance of this factory. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains
     * the name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise, a
     * temporary directory will be used.
     */
    public ThreadedH2EpsgFactory() {
        this(null);
    }

    /**
     * Creates a new instance of this data source using the specified hints. The priority
     * is set to a lower value than the {@linkplain FactoryOnAccess}'s one in order to give
     * precedence to the Access-backed database, if presents. Priorities are set that way
     * because:
     * <ul>
     *   <li>The MS-Access format is the primary EPSG database format.</li>
     *   <li>If a user downloads the MS-Access database himself, he probably wants to use it.</li>
     * </ul>
     */
    public ThreadedH2EpsgFactory(final Hints hints) {
        super(hints, PRIORITY + 1);
    }

    /**
     * Returns the default directory for the EPSG database. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains the
     * name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise,
     * a temporary directory will be used.
     */
    private static File getDirectory() {
        try {
            final String property = System.getProperty(DIRECTORY_KEY);
            if (property != null) {
                final File directory = new File(property);
                /*
                 * Creates the directory if needed (mkdir), but NOT the parent directories (mkdirs)
                 * because a missing parent directory may be a symptom of an installation problem.
                 * For example if 'directory' is a subdirectory in the temporary directory (~/tmp/),
                 * this temporary directory should already exists. If it doesn't, an administrator
                 * should probably looks at this problem.
                 */
                if (directory.isDirectory() || directory.mkdir()) {
                    return directory;
                }
            }
        } catch (SecurityException e) {
            /*
             * Can't fetch the base directory from system properties.
             * Fallback on the default temporary directory.
             */
        }
        return getTemporaryDirectory();
    }

    /**
     * Returns the directory to uses in the temporary directory folder.
     */
    private static File getTemporaryDirectory() {
        File directory = new File(System.getProperty("java.io.tmpdir", "."), "Geotools");
        if (directory.isDirectory() || directory.mkdir()) {
            directory = new File(directory, "Databases/EPSG-H2-" + VERSION + "/");
            if (directory.isDirectory() || directory.mkdirs()) {
                return directory;
            }
        }
        return null;
    }

    /**
     * Extract the directory from the specified data source, or {@code null} if this
     * information is not available.
     */
    private static File getDirectory(final DataSource source) {
        if (source instanceof JdbcDataSource) {
            String path = ((JdbcDataSource) source).getURL();
            if (path!=null && PREFIX.regionMatches(true, 0, path, 0, PREFIX.length())) {
                path = path.substring(PREFIX.length());
                return new File(path).getParentFile();
            }
        }
        return null;
    }

    /**
     * Returns a data source for the HSQL database.
     */
    protected DataSource createDataSource() throws SQLException {
        DataSource candidate = super.createDataSource();
        if (candidate instanceof JdbcDataSource) {
            return candidate;
        }
        final JdbcDataSource source = new JdbcDataSource();
        File directory = getDirectory();
        if (directory != null) {
            /*
             * Constructs the full path to the HSQL database. Note: we do not use
             * File.toURI() because HSQL doesn't seem to expect an encoded URL
             * (e.g. "%20" instead of spaces).
             */
            final StringBuilder url = new StringBuilder(PREFIX);
            final String path = directory.getAbsolutePath().replace(File.separatorChar, '/');
            if (path.length()==0 || path.charAt(0)!='/') {
                url.append('/');
            }
            url.append(path);
            if (url.charAt(url.length()-1) != '/') {
                url.append('/');
            }
            url.append(DATABASE_NAME);
            // no need for validation query, saves some work
            url.append(";AUTO_RECONNECT=TRUE;CACHE_SIZE=131072;CACHE_TYPE=TQ");
            source.setURL("jdbc:h2:zip:/tmp/EPSG3.ZIP!/EPSG");
            source.setURL((url.toString()));
            source.setUser("sa");
            source.setPassword("");
            assert directory.equals(getDirectory(source)) : url;
        }
        /*
         * If the temporary directory do not exists or can't be created, lets the 'database'
         * attribute unset. If the user do not set it explicitly (through JNDI or by overrding
         * this method), an exception will be thrown when 'createBackingStore()' will be invoked.
         */
        source.setUser("SA"); // System administrator. No password.
        return source;
    }

    /**
     * Returns {@code true} if the database contains data. This method returns {@code false}
     * if an empty EPSG database has been automatically created by HSQL and not yet populated.
     */
    private static boolean dataExists(final Connection connection) throws SQLException {
        final ResultSet tables = connection.getMetaData().getTables(
                null, null, "EPSG_%", new String[] {"TABLE"});
        final boolean exists = tables.next();
        tables.close();
        return exists;
    }

    /**
     * Returns the backing-store factory for HSQL syntax. If the cached tables are not available,
     * they will be created now from the SQL scripts bundled in this plugin.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using HSQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    protected AbstractAuthorityFactory createBackingStore(final Hints hints) throws SQLException {
        final DataSource source = getDataSource();
        final File directory    = getDirectory(source);
        Connection connection   = source.getConnection();
        if (!dataExists(connection)) {
            /*
             * HSQL has created automatically an empty database. We need to populate it.
             * Executes the SQL scripts bundled in the JAR. In theory, each line contains
             * a full SQL statement. For this plugin however, we have compressed "INSERT
             * INTO" statements using Compactor class in this package.
             */
            final Logger logger = Logging.getLogger(LOGGER);
            final LogRecord record = Loggings.format(Level.INFO,
                    LoggingKeys.CREATING_CACHED_EPSG_DATABASE_$1, VERSION);
            record.setLoggerName(logger.getName());
            logger.log(record);
            final Statement statement = connection.createStatement();
            try {
                // read and execute the scripts that make up the database
                executeScript("EPSG_Tables_PostgreSQL.sql", statement);
                executeScript("EPSG_Data_PostgreSQL.sql", statement);
                executeScript("EPSG_FKeys_PostgreSQL.sql", statement);
                executeScript("EPSG_Indexes_H2.sql", statement);
            } catch (IOException exception) {
                SQLException e = new SQLException("Error occurred while executing " +
                		"the EPSG database creation scripts");
                e.initCause(exception); 
                throw e;
            } finally {
                statement.close();
            }
            statement.close();
            connection.close();
            
            // the database has been created, not make it read only
            setReadOnly(directory);
            
            connection = source.getConnection();
            assert dataExists(connection);
        }
        // we should use AnsiDialectEpsgFactory but the finder is not properly implemented there
        FactoryUsingAnsiSQL factory = new FactoryUsingAnsiSQL(hints, connection);
        return factory;
    }

    private void setReadOnly(final File directory) {
        for (File file : directory.listFiles()) {
            if(file.isDirectory()) {
                setReadOnly(file);
            } else if(file.getName().endsWith(".log.db"))
                file.delete();
            else
                file.setReadOnly();
        }
    }
    
    void executeScript(String scriptName, Statement statement) throws IOException, SQLException {
        SqlScripReader reader = null;
        try {
            // first read in the tables
            reader = new SqlScripReader(new InputStreamReader(new GZIPInputStream(
                    ThreadedH2EpsgFactory.class.getResourceAsStream(scriptName + ".gz")), "ISO-8859-15"));
            while(reader.hasNext()) {
                statement.execute(reader.next());
            }
        } finally {
            if(reader != null) 
                reader.dispose();
        }
    }
}
