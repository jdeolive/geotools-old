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

// Database connection
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Blob;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Miscellaneous
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

// JAI dependencies
import javax.media.jai.ParameterList;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Default implementation for a coordinate system factory backed
 * by the EPSG database. The EPSG database is freely available at
 * <A HREF="http://www.epsg.org">http://www.epsg.org</a>. Current
 * version of this class requires EPSG database version 6.1.
 *
 * @version $Id: CoordinateSystemEPSGFactory.java,v 1.2 2002/07/29 18:00:24 desruisseaux Exp $
 * @author Yann Cézard
 * @author Martin Desruisseaux
 */
public class CoordinateSystemEPSGFactory extends CoordinateSystemAuthorityFactory {
    /**
     * The default coordinate system authority factory.
     * Will be constructed only when first requested.
     */
    private static CoordinateSystemAuthorityFactory DEFAULT;
    
    /**
     * Maps EPSG parameter names to OGC parameter names.
     * For example, "False easting" (the EPSG name) is mapped to "false_easting" (the OGC name).
     */
    private final Map paramNames = new HashMap();

    /**
     * A pool of prepared statements. Key are {@link String} object related to their
     * originating method name (for example "Ellipsoid" for {@link #createEllipsoid},
     * while values are {@link PreparedStatement} objects.
     */
    private final Map statements = new HashMap();
        
    /**
     * The connection to the EPSG database.
     */
    protected final Connection connection;

    /**
     * Construct an authority factory using
     * the specified connection.
     *
     * @param factory    The underlying factory used for objects creation.
     * @param connection The connection to the underlying EPSG database.
     */
    public CoordinateSystemEPSGFactory(final CoordinateSystemFactory factory,
                                       final Connection connection)
    {
        super(factory);
        this.connection = connection;
        Info.ensureNonNull("connection", connection);
        paramNames.put("Latitude of natural origin",       "latitude_of_origin");
        paramNames.put("Longitude of natural origin",      "central_meridian");
        paramNames.put("Scale factor at natural origin",   "scale_factor");
        paramNames.put("False easting",                    "false_easting");
        paramNames.put("False northing",                   "false_northing");
//        paramNames.put("Latitude of 1st standard parallel","standard_parallel");
//        paramNames.put("Latitude of 2nd standard parallel","standard_parallel");
    }
    
    /**
     * Construct an authority factory using
     * the specified URL to an EPSG database.
     *
     * @param  factory The underlying factory used for objects creation.
     * @param  url     The url to the EPSG database. For example, a connection
     *                 using the ODBC-JDBC bridge may have an URL likes
     *                 <code>"jdbc:odbc:EPSG"</code>.
     * @param  driver  An optional driver to load, or <code>null</code> if none.
     *                 This is a convenience argument for the following pseudo-code:
     *                 <blockquote><code>
     *                 Class.forName(driver).newInstance();
     *                 </code></blockquote>
     *                 A message is logged to <code>"org.geotools.cts"</code> whatever
     *                 the loading succed of fail. For JDBC-ODBC bridge, a typical value
     *                 for this argument is <code>"sun.jdbc.odbc.JdbcOdbcDriver"</code>.
     *                 This argument needs to be non-null only once for a specific driver.
     *
     * @throws SQLException if the constructor failed to connect to the EPSG database.
     */
    public CoordinateSystemEPSGFactory(final CoordinateSystemFactory factory,
                                       final String url, final String driver) throws SQLException
    {
        this(factory, getConnection(url, driver));
    }

    /**
     * Returns a default coordinate system
     * factory backed by the EPSG database.
     *
     * @return The default factory.
     * @throws SQLException if the connection to the database can't be etablished.
     */
    public synchronized static CoordinateSystemAuthorityFactory getDefault() throws SQLException {
        if (DEFAULT == null) {
            final String url    = "jdbc:odbc:EPSG";
            final String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
            // TODO: In a future version, we should fetch the
            //       above properties from system's preferences.
            DEFAULT = new CoordinateSystemEPSGFactory(CoordinateSystemFactory.getDefault(), url, driver);
            // TODO: What should we do with CoordinateSystemEPSGFactory.close()?
            //       We want to close the connection on exit, but we don't want
            //       to allow the user to close the connection by himself since
            //       this object may be shared by many users.
        }
        return DEFAULT;
    }

    /**
     * Returns the authority name, which is <code>"EPSG"</code>.
     */
    public String getAuthority() {
        return "EPSG";
    }

    /**
     * Get the connection to an URL.
     *
     * @param  url     The url to the EPSG database.
     * @param  driver  The driver to load, or <code>null</code> if none.
     * @return The connection to the EPSG database.
     * @throws SQLException if the connection can't be etablished.
     */
    private static Connection getConnection(final String url, String driver) throws SQLException
    {
        Info.ensureNonNull("url", url);
        if (driver!=null)
        {
            LogRecord record;
            try
            {
                final Driver drv = (Driver)Class.forName(driver).newInstance();
                record = Resources.getResources(null).getLogRecord(Level.CONFIG,
                                            ResourceKeys.LOADED_JDBC_DRIVER_$3);
                record.setParameters(new Object[] {
                    drv.getClass().getName(),
                    new Integer(drv.getMajorVersion()),
                    new Integer(drv.getMinorVersion())
                });
            } catch (Exception exception) {
                record = new LogRecord(Level.WARNING, exception.getLocalizedMessage());
                record.setThrown(exception);
                // Try to connect anyway. It is possible that
                // an other driver has already been loaded...
            }
            record.setSourceClassName("CoordinateSystemEPSGFactory");
            record.setSourceMethodName("<init>");
            Logger.getLogger("org.geotools.cts").log(record);
        }
        return DriverManager.getConnection(url);
    }

    /**
     * Returns a prepared statement for the specified name.
     *
     * @param  key A key uniquely identifying the caller
     *         (e.g. "Ellipsoid" for {@link #createEllipsoid}).
     * @param  sql The SQL statement to use if for creating the {@link PreparedStatement}
     *         object. Will be used only if no prepared statement was already created for
     *         the specified key.
     * @return The prepared statement.
     * @throws SQLException if the prepared statement can't be created.
     */
    private PreparedStatement prepareStatement(final String key, final String sql)
            throws SQLException
    {
        assert Thread.holdsLock(this);
        PreparedStatement stat = (PreparedStatement) statements.get(key);
        if (stat == null) {
            stat = connection.prepareStatement(sql);
            statements.put(key, stat);
        }
        return stat;
    }

    /**
     * Gets the string from the specified {@link ResultSet}.
     * The string is required to be non-null. A null string
     * will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The string at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static String getString(final ResultSet result, final int columnIndex)
            throws SQLException, FactoryException
    {
        final String str = result.getString(columnIndex);
        if (result.wasNull()) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$1,
                                       result.getMetaData().getColumnName(columnIndex)));
        }
        return str.trim();
    }

    /**
     * Gets the value from the specified {@link ResultSet}.
     * The value is required to be non-null. A null value
     * (i.e. blank) will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The double at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static double getDouble(final ResultSet result, final int columnIndex)
            throws SQLException, FactoryException
    {
        final double value = result.getDouble(columnIndex);
        if (result.wasNull()) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$1,
                                       result.getMetaData().getColumnName(columnIndex)));
        }
        return value;
    }

    /**
     * Gets the value from the specified {@link ResultSet}.
     * The value is required to be non-null. A null value
     * (i.e. blank) will throw an exception.
     *
     * @param  result The result set to fetch value from.
     * @param  columnIndex The column index (1-based).
     * @return The integer at the specified column.
     * @throws SQLException if a SQL error occured.
     * @throws FactoryException If a null value was found.
     */
    private static int getInt(final ResultSet result, final int columnIndex)
            throws SQLException, FactoryException
    {
        final int value = result.getInt(columnIndex);
        if (result.wasNull()) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$1,
                                       result.getMetaData().getColumnName(columnIndex)));
        }
        return value;
    }

    /**
     * Make sure that an object constructed from the database
     * is not duplicated.
     *
     * @param  newValue The newly constructed object.
     * @param  oldValue The object previously constructed,
     *         or <code>null</code> if none.
     * @param  The EPSG code (for formatting error message).
     * @throws FactoryException if a duplication has been detected.
     */
    private static Object ensureSingleton(final Object newValue, final Object oldValue, String code)
            throws FactoryException
    {
        if (oldValue == null) {
            return newValue;
        }
        if (oldValue.equals(newValue)) {
            return oldValue;
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_DUPLICATED_VALUES_$1, code));
    }

    /**
     * Transform a string from the OGC format to the EPSG format.
     * Parenthesis are removed; spaces are replaced by underscore;
     * first letter is upper case and others are lower case.
     *
     * @param name the string at the EPSG format.
     * @return the String at OGC format.
     *
     * @task REVISIT: How to know if "Geographic/geocentric conversions"
     *                is geographic to geocentric or geocentric to geographic?
     */
    private static String fromEPSGtoOGC(final String name) {
        // For MathTransformAuthorityFactory
        if (false) {
            if (name.equalsIgnoreCase("Geographic/geocentric conversions")) {
                // Geocentric_To_Ellipsoid or Ellipsoid_To_Geocentric?
                return "Geocentric_To_Ellipsoid";
            }
        }
        StringBuffer buf   = new StringBuffer();
        StringTokenizer st = new StringTokenizer(name, " ()");
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            // First letter in upper case
            buf.append(word.substring(0,1).toUpperCase());
            buf.append(word.substring(1));
            buf.append("_");
        }
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    /**
     * Returns the OGC Name from an EPSG parameter name.
     */
    private String getOGCParamName(final String name) throws FactoryException
    {
        final String returnValue = (String) paramNames.get(name);
        if (returnValue == null) {
            // If it has not been defined in the paramNames table
            // We apply the standard transformation.
            return fromEPSGtoOGC(name);
        }
        return returnValue;
    }

    /**
     * Returns an {@link Ellipsoid} object from a code.
     *
     * @param  code The EPSG value.
     * @return The ellipsoid object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Ellipsoid createEllipsoid(final String code)
            throws FactoryException
    {
        Ellipsoid returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Ellipsoid", "select ELLIPSOID_NAME,"
                                                 + " SEMI_MAJOR_AXIS,"
                                                 + " INV_FLATTENING,"
                                                 + " SEMI_MINOR_AXIS,"
                                                 + " UOM_CODE"
                                                 + " from Ellipsoid"
                                                 + " where ELLIPSOID_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                /*
                 * One of 'semiMinorAxis' and 'inverseFlattening' values can be NULL in
                 * the database. Consequently, we don't use 'getString(ResultSet, int)'
                 * because we don't want to thrown an exception if a NULL value is found.
                 */
                final String name              = getString(result, 1);
                final double semiMajorAxis     = getDouble(result, 2);
                final double inverseFlattening = result.getDouble( 3);
                final double semiMinorAxis     = result.getDouble( 4);
                final String unitCode          = getString(result, 5);
                final Unit   unit              = createUnit(unitCode);
                final Ellipsoid ellipsoid;
                if (inverseFlattening == 0) {
                    if (semiMinorAxis == 0) {
                        // Both are null, which is not allowed.
                        result.close();
                        throw new FactoryException(Resources.format(
                                                   ResourceKeys.ERROR_NULL_VALUE_$1,
                                                   result.getMetaData().getColumnName(3)));
                    } else {
                        // We only have semiMinorAxis defined -> it's OK
                        ellipsoid = factory.createEllipsoid(name, semiMajorAxis,
                                                            semiMinorAxis, unit);
                    }
                } else {
                    if (semiMinorAxis != 0) {
                        // Both 'inverseFlattening' and 'semiMinorAxis' are defined.
                        // Log a warning and create the ellipsoid using the inverse flattening.
                        Logger.getLogger("org.geotools.cts").warning(Resources.format(
                                            ResourceKeys.WARNING_AMBIGUOUS_ELLIPSOID));
                    }
                    ellipsoid = factory.createFlattenedSphere(name, semiMajorAxis,
                                                              inverseFlattening, unit);
                }
                /*
                 * Now that we have built an ellipsoid, compare
                 * it with the previous one (if any).
                 */
                returnValue = (Ellipsoid) ensureSingleton(ellipsoid, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a {@link Unit} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The unit object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized Unit createUnit(final String code)
            throws FactoryException
    {
        Unit returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Unit", "select UNIT_OF_MEAS_TYPE,"
                                            + " FACTOR_B,"
                                            + " FACTOR_C"
                                            + " from Unit_of_Measure"
                                            + " where UOM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final String type = getString(result, 1);
                final double b    = result.getDouble( 2);
                final double c    = result.getDouble( 3);
                /*
                 * Factor b or (XOR) c should not be 0. If they are,
                 * we will consider them as if they were null.
                 */
                if (b==0 ^ c==0) {
                    result.close();
                    throw new FactoryException(Resources.format(ResourceKeys.ERROR_NULL_VALUE_$1,
                                               result.getMetaData().getColumnName(b==0 ? 2 : 3)));
                }
                Unit unit;
                if (b==0 && c==0) {
                    unit = Unit.DMS;
                } else if (type.equalsIgnoreCase("length")) {
                    // In the UOM table, all length are based on the metre.
                    unit = Unit.METRE;
                } else if (type.equalsIgnoreCase("angle")) {
                    // In the UOM table, all angles are based on the radian.
                    unit = Unit.RADIAN;
                } else if (type.equalsIgnoreCase("scale")) {
                    unit = Unit.DIMENSIONLESS;
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                /*
                 * Now that we have built an unit, scale it and
                 * compare it with the previous one (if any).
                 */
                if (b!=0 && c!=0) {
                    unit = unit.scale(b/c);
                }
                returnValue = (Unit) ensureSingleton(unit, returnValue, code);
            }
            result.close();
        }
        catch (SQLException exception) {
            throw new FactoryException(code, exception);
        } if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a prime meridian, relative to Greenwich.
     *
     * @param  code Value allocated by authority.
     * @return The prime meridian object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized PrimeMeridian createPrimeMeridian(final String code)
            throws FactoryException
    {
        PrimeMeridian returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("PrimeMeridian", "select PRIME_MERIDIAN_NAME,"
                                                     + " GREENWICH_LONGITUDE,"
                                                     + " UOM_CODE"
                                                     + " from Prime_Meridian"
                                                     + " where PRIME_MERIDIAN_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */ 
            while (result.next()) {
                final String name      = getString(result, 1);
                final double longitude = getDouble(result, 2);
                final String unit_code = getString(result, 3);
                final Unit unit        = createUnit(unit_code);
                PrimeMeridian primeMeridian = factory.createPrimeMeridian(name, unit, longitude);
                returnValue = (PrimeMeridian) ensureSingleton(primeMeridian, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        } if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Returns a datum from a code. This method may
     * returns a vertical, horizontal or local datum.
     *
     * @param  code Value allocated by authority.
     * @return The datum object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     *
     * @task REVISIT: Current implementation maps all "vertical" datum to
     *                {@link DatumType#ELLIPSOIDAL} and all "horizontal"
     *                datum to {@link DatumType#GEOCENTRIC}. At the time
     *                of writting, it was not clear how to maps the exact
     *                datum type from the EPSG database.
     *
     * @task REVISIT: The creation of horizontal datum use only the first
     *                {@link WGS84ConversionInfo} object, because current
     *                version of {@link CoordinateSystemFactory} do not
     *                allows more than one conversion info. We should fix
     *                that.
     *
     * @task TODO:    Datum "engineering" is currently not supported.
     */
    public Datum createDatum(final String code) throws FactoryException {
        Datum returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("Datum", "select DATUM_NAME,"
                                             + " DATUM_TYPE,"
                                             + " ELLIPSOID_CODE"
                                             + " from Datum"
                                             + " where DATUM_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final String name = getString(result, 1);
                final String type = getString(result, 2);
                final Datum datum;
                if (type.equalsIgnoreCase("vertical")) {
                    /*
                     * Vertical datum type. Maps to "ELLIPSOIDAL".
                     */
                    final DatumType.Vertical dtype = DatumType.Vertical.ELLIPSOIDAL; // TODO
                    datum = factory.createVerticalDatum(name, dtype);
                } else if (type.equalsIgnoreCase("geodetic")) {
                    /*
                     * Horizontal datum type. Maps to "GEOCENTRIC".
                     */
                    final Ellipsoid         ellipsoid = createEllipsoid(getString(result, 3));
                    final WGS84ConversionInfo[] infos = createWGS84ConversionInfo(code);
                    final WGS84ConversionInfo mainInf = (infos.length!=0) ? infos[0] : null;
                    final DatumType.Horizontal  dtype = DatumType.Horizontal.GEOCENTRIC; // TODO
                    // TODO: on utilise la premiere info seulement pour le moment.
                    datum = factory.createHorizontalDatum(name, dtype, ellipsoid, mainInf);
                } else if (type.equalsIgnoreCase("engineering")) {
                    /*
                     * Local datum type.
                     */
                    // TODO
                    //return factory.createLocalDatum(name, new DatumType.Local("bidon",0,0));
                    result.close();
                    throw new UnsupportedOperationException("DatumType.Local not supported.");
                } else {
                    result.close();
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, type));
                }
                returnValue = (Datum) ensureSingleton(datum, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        } if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /** 
     * Returns the differents WGS84 Conversion Informations
     * for a {@link HorizontalDatum}. If the specified datum
     * has no WGS84 conversion informations, then this method
     * will returns an empty array.
     *  
     * @param  code the EPSG code of the {@link HorizontalDatum}.
     * @return an array of {@link WGS84ConversionInfo}, which may
     *         be empty.
     */
    private WGS84ConversionInfo[] createWGS84ConversionInfo(final String code)
            throws FactoryException
    {
        final List list = new ArrayList();
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("WGS84ConversionInfo", "SELECT co.COORD_OP_CODE,"
                                               + " area.AREA_OF_USE,"
                                               + " co.COORD_OP_METHOD_CODE"
                                               + " FROM Coordinate_Operation AS co,"
                                               + " Coordinate_Reference_System AS crs,"
                                               + " Area AS area"
                                               + " WHERE crs.DATUM_CODE = ?"
                                               + " AND co.SOURCE_CRS_CODE = crs.COORD_REF_SYS_CODE"
                                               + " AND co.TARGET_CRS_CODE = 4326"
                                               + " AND area.AREA_CODE = co.AREA_OF_USE_CODE"
                                               + " ORDER BY co.COORD_OP_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final WGS84ConversionInfo info = new WGS84ConversionInfo();
                final Parameter[] param = getParameter(getString(result, 1));
                if ((param != null) && (param.length != 0)) {
                    String method_op_code = getString(result, 3);
                    // Value could be something else, but I don't know what to do when
                    // it is the case (for example 9618, with a radian Unit).
                    // So limiting to 9603 and 9606 cases for the moment.
                    if (method_op_code.equals("9603") || method_op_code.equals("9606")) {
                        // First we get the description of the area of use
                        info.areaOfUse = result.getString(2);

                        // Then we get the coordinates. For each one we convert the unit in meter
                        info.dx = Unit.METRE.convert(param[0].value, param[0].unit);
                        info.dy = Unit.METRE.convert(param[1].value, param[1].unit);
                        info.dz = Unit.METRE.convert(param[2].value, param[2].unit);

                        if (getString(result, 3).equals("9606")) {
                            // Here we know that the database provides four more informations
                            // for WGS84 conversion : ex, ey, ez and ppm
                            info.ex  = Unit.ARC_SECOND.convert(param[3].value, param[3].unit);
                            info.ey  = Unit.ARC_SECOND.convert(param[4].value, param[4].unit);
                            info.ez  = Unit.ARC_SECOND.convert(param[5].value, param[5].unit);
                            info.ppm = param[6].value; // Parts per million, no conversion needed
                        }
                        list.add(info);
                    }
                }
            }            
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        return (WGS84ConversionInfo[]) list.toArray(new WGS84ConversionInfo[list.size()]);
    }
    
    /**
     * Returns a coordinate system from a code.
     *
     * @param  code Value allocated by authority.
     * @return The coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized CoordinateSystem createCoordinateSystem(final String code)
            throws FactoryException
    {
        final String type;
        try {
            type = getCoordinateSystemType(code);
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (type.equalsIgnoreCase("compound")) {
            return createCompoundCoordinateSystem(code);
        } else if (type.equalsIgnoreCase("vertical")) {
            return createVerticalCoordinateSystem(code);
        } else if (type.equalsIgnoreCase("geographic 2D")) {
            return createGeographicCoordinateSystem(code);
        } else if (type.equalsIgnoreCase("projected")) {
            return createProjectedCoordinateSystem(code);
        } else {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
        }
    }
    
    /**
     * Returns a geographic coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The geographic coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     *
     */
    public synchronized GeographicCoordinateSystem createGeographicCoordinateSystem(final String code)
            throws FactoryException
    {
        GeographicCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("GeographicCoordinateSystem", "select DIMENSION,"
                                               + " cs.COORD_SYS_CODE,"
                                               + " COORD_REF_SYS_NAME,"
                                               + " PRIME_MERIDIAN_CODE,"
                                               + " datum.DATUM_CODE"
                                               + " from Coordinate_Reference_System AS crs,"
                                               + " Coordinate_System AS cs,"
                                               + " Datum AS datum"
                                               + " where COORD_REF_SYS_CODE = ?"
                                               + " and cs.COORD_SYS_CODE = crs.COORD_SYS_CODE"
                                               + " and datum.DATUM_CODE = crs.DATUM_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int        dimension = getInt   (result, 1);
                final String  coordSysCode = getString(result, 2);
                final String          name = getString(result, 3);
                final String primeMeridian = getString(result, 4);
                final String         datum = getString(result, 5);
                final AxisInfo[] axisInfos = getAxisInfo(coordSysCode, dimension);
                final CoordinateSystem coordSys;
                coordSys = factory.createGeographicCoordinateSystem(name,createUnit2D(coordSysCode),
                                            (HorizontalDatum) createDatum(datum),
                                            createPrimeMeridian(primeMeridian),
                                            axisInfos[0], axisInfos[1]);
                returnValue = (GeographicCoordinateSystem) ensureSingleton(coordSys, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        } if (returnValue==null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }
    
    /**
     * Returns a projected coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The projected coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized ProjectedCoordinateSystem createProjectedCoordinateSystem(final String code)
            throws FactoryException
    {
        CoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("ProjectedCoordinateSystem", "select DIMENSION,"
                                       + " cs.COORD_SYS_CODE,"
                                       + " COORD_REF_SYS_NAME,"
                                       + " crs.SOURCE_GEOGCRS_CODE,"
                                       + " co.COORD_OP_NAME,"
                                       + " com.COORD_OP_METHOD_NAME,"
                                       + " crs.PROJECTION_CONV_CODE"
                                       + " from Coordinate_Reference_System AS crs,"
                                       + " Coordinate_System AS cs,"
                                       + " Coordinate_Operation AS co,"
                                       + " Coordinate_Operation_Method AS com"
                                       + " where COORD_REF_SYS_CODE = ?"
                                       + " and cs.COORD_SYS_CODE = crs.COORD_SYS_CODE"
                                       + " and co.COORD_OP_CODE = crs.PROJECTION_CONV_CODE"
                                       + " and com.COORD_OP_METHOD_CODE = co.COORD_OP_METHOD_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int          dimension =               getInt   (result, 1);
                final String    coordSysCode =               getString(result, 2);
                final String            name =               getString(result, 3);
                final String     geoCoordSys =               getString(result, 4);
                final String   operationName =               getString(result, 5);
                final String  classification = fromEPSGtoOGC(getString(result, 6));
                final Parameter[] parameters = getParameter (getString(result, 7));
                final AxisInfo[]   axisInfos = getAxisInfo(coordSysCode, dimension);
                final ParameterList list = factory.createProjectionParameterList(classification);
                for (int i=0; i<parameters.length; i++) {
                    list.setParameter(parameters[i].name, parameters[i].value);
                }
                final GeographicCoordinateSystem gcs;
                gcs = (GeographicCoordinateSystem) createCoordinateSystem(geoCoordSys);
                final Ellipsoid e = gcs.getHorizontalDatum().getEllipsoid();
                if (e != null) {
                    final Unit unit = e.getAxisUnit();
                    list.setParameter("semi_major", Unit.METRE.convert(e.getSemiMajorAxis(), unit));
                    list.setParameter("semi_minor", Unit.METRE.convert(e.getSemiMinorAxis(), unit));
                }
                final Projection projection = factory.createProjection(operationName,
                                                                       classification, list);
                final Unit unit = createUnit2D(coordSysCode);
                final CoordinateSystem coordSys;
                coordSys = factory.createProjectedCoordinateSystem(name, gcs, projection, unit,
                                                                   axisInfos[0], axisInfos[1]);
                returnValue = (CoordinateSystem) ensureSingleton(coordSys, returnValue, code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue == null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return (ProjectedCoordinateSystem) returnValue;
    }
    
    /**
     * Returns a vertical coordinate system from an EPSG code.
     *
     * @param  code Value allocated by authority.
     * @return The vertical coordinate system object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized VerticalCoordinateSystem createVerticalCoordinateSystem(final String code)
            throws FactoryException
    {
        VerticalCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("VerticalCoordinateSystem", "select DIMENSION,"
                                               + " cs.COORD_SYS_CODE,"
                                               + " COORD_REF_SYS_NAME,"
                                               + " DATUM_CODE"
                                               + " from Coordinate_Reference_System AS crs,"
                                               + " Coordinate_System AS cs"
                                               + " where COORD_REF_SYS_CODE = ?"
                                               + " and cs.COORD_SYS_CODE = crs.COORD_SYS_CODE");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            /*
             * If the supplied code exists in the database, then we
             * should find only one record.   However, we will do a
             * paranoiac check and verify if there is more records.
             */
            while (result.next()) {
                final int        dimension = getInt   (result, 1);
                final String  coordSysCode = getString(result, 2);
                final String          name = getString(result, 3);
                final String         datum = getString(result, 4);
                final AxisInfo[] axisInfos = getAxisInfo(coordSysCode, dimension);
                final CoordinateSystem  coordSys;
                coordSys = factory.createVerticalCoordinateSystem(name,
                                        (VerticalDatum) createDatum(datum),
                                        createUnit2D(coordSysCode), axisInfos[0]);
                returnValue = (VerticalCoordinateSystem)ensureSingleton(coordSys,returnValue,code);
            }
            result.close();
        } catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue==null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }
    
    /**
     * Create a compound coordinate system from the EPSG code.
     *
     * @param code the EPSG code for the CS.
     * @return the compound CS which value was given.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occured in the backing
     *         store. This exception usually have {@link SQLException} as its cause.
     */
    public synchronized CompoundCoordinateSystem createCompoundCoordinateSystem(final String code)
            throws FactoryException
    {
        CompoundCoordinateSystem returnValue = null;
        try {
            final PreparedStatement stmt;
            stmt = prepareStatement("CompoundCoordinateSystem", "select COORD_REF_SYS_NAME,"
                                               + " COORD_REF_SYS_KIND,"
                                               + " CMPD_HORIZCRS_CODE,"
                                               + " CMPD_VERTCRS_CODE"
                                               + " from Coordinate_Reference_System"
                                               + " where COORD_REF_SYS_CODE = ?");
            stmt.setString(1, code);
            final ResultSet result = stmt.executeQuery();
            while (result.next()) {
                final String name = getString(result, 1);
                final String type = getString(result, 2);
                if (!type.equalsIgnoreCase("compound")) {
                    throw new FactoryException(Resources.format(
                                               ResourceKeys.ERROR_UNKNOW_TYPE_$1, code));
                }
                final CoordinateSystem  cs1 = createCoordinateSystem(getString(result, 3));
                final CoordinateSystem  cs2 = createCoordinateSystem(getString(result, 4));
                CompoundCoordinateSystem cs = factory.createCompoundCoordinateSystem(name, cs1,cs2);
                returnValue = (CompoundCoordinateSystem) ensureSingleton(cs, returnValue, code);
            }
            result.close();
        }
        catch (SQLException exception) {
            throw new FactoryException(code, exception);
        }
        if (returnValue==null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue; 
    }
    
    /**
     * Return the type of a coordinate reference system.
     *
     * @param the EPSG code of the system.
     * @return The string that gives the type of the system.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if the code has not been found.
     */
    private String getCoordinateSystemType(final String code) throws SQLException, FactoryException
    {
        String returnValue = null;
        final PreparedStatement stmt;
        stmt = prepareStatement("CoordinateSystemType", "select COORD_REF_SYS_KIND"
                                                        + " from Coordinate_Reference_System"
                                                        + " where COORD_REF_SYS_CODE = ?");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final String type = getString(result, 1);
            returnValue = (String) ensureSingleton(type, returnValue, code);
        }
        if (returnValue==null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }
    
    /**
     * Returns the {@link AxisInfo}s from an
     * EPSG code for a {@link CoordinateSystem}.
     *
     * @param  code the EPSG code.
     * @param  dimension of the coordinate system, which is also the
     *         size of the returned Array.
     * @return an array of AxisInfo.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if the code has not been found.
     *
     * @task HACK: WARNING!! The EPSG database use "ORDER" as a column name.
     *             This is tolerated by Access, but MySQL doesn't accept this name.
     */
    private AxisInfo[] getAxisInfo(final String code, final int dimension)
            throws SQLException, FactoryException
    {
        final AxisInfo[] axis = new AxisInfo[dimension];
        final PreparedStatement stmt;
        stmt = prepareStatement("AxisInfo", "select COORD_AXIS_NAME,"
                                           + " COORD_AXIS_ORIENTATION"
                                           + " from Coordinate_Axis AS ca,"
                                           + " Coordinate_Axis_Name AS can"
                                           + " where COORD_SYS_CODE = ?"
                                           + " and ca.COORD_AXIS_NAME_CODE = can.COORD_AXIS_NAME_CODE"
                                           // WARNING: Be careful about the table name :
                                           //          MySQL refuse ORDER as a column name !!!
                                           + " order by ORDER");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        int i = 0;
        while (result.next()) {
            final String name = getString(result, 1);
            final AxisOrientation enum;
            try {
                enum = AxisOrientation.getEnum(getString(result, 2));
            } catch (NoSuchElementException exception) {
                throw new FactoryException(Resources.format(
                                           ResourceKeys.ERROR_UNKNOW_TYPE_$1, name), exception);
            }
            if (i < axis.length) {
                axis[i++] = new AxisInfo(name, enum);
            }
        }
        result.close();
        if (i != axis.length) {
            throw new FactoryException(Resources.format(ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                                       new Integer(axis.length), new Integer(i)));
        }
        return axis;
    }
    
    /**
     * Returns the Unit for 1D and 2D coordinate system.
     *
     * @param  code The requested code.
     * @return The unit.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if some other errors has occured.
     */
    private Unit createUnit2D(String code) throws SQLException, FactoryException
    {
        Unit returnValue = null;
        final PreparedStatement stmt;
        // Note: can't use "Unit" key, because it is already used by "createUnit".
        stmt = prepareStatement("Unit2D", "select UOM_CODE"
                                          + " from Coordinate_Axis AS ca"
                                          + " where COORD_SYS_CODE = ?");
        stmt.setString(1, code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final Unit unit = createUnit(getString(result, 1));
            returnValue = (Unit) ensureSingleton(unit, returnValue, code);
        }
        result.close();
        if (returnValue==null) {
             throw new NoSuchAuthorityCodeException(code);
        }
        return returnValue;
    }

    /**
     * Close the database connection and dispose any resources
     * hold by this object.
     *
     * @throws FactoryException if an error occured while closing the connection.
     */
    public void dispose() throws FactoryException {
        try {
            close();
        } catch (SQLException exception) {
            throw new FactoryException(null, exception);
        }
    }

    /**
     * Close the database connection and dispose any resources
     * hold by this object.
     *
     * @throws SQLException if an error occured while closing the connection.
     */
    private synchronized void close() throws SQLException {
        for (final Iterator it=statements.values().iterator(); it.hasNext();) {
            final PreparedStatement stmt = (PreparedStatement) it.next();
            stmt.close();
        }
        statements.clear();
        connection.close();
    }

    /**
     * Returns the parameter list for an operation method code.
     *
     * @param  op_code The operation code.
     * @return Parameters.
     * @throws SQLException if an error occured during database access.
     * @throws FactoryException if some other errors has occured.
     */
    private Parameter[] getParameter(final String op_code) throws SQLException, FactoryException {
        final List list = new ArrayList();
        final PreparedStatement stmt;
        stmt = prepareStatement("Parameter", "select copu.PARAMETER_CODE,"
                                       + " cop.PARAMETER_NAME,"
                                       + " copv.PARAMETER_VALUE,"
                                       + " copv.UOM_CODE"
                                       + " from Coordinate_Operation_Parameter_Usage AS copu,"
                                       + " Coordinate_Operation AS co,"
                                       + " Coordinate_Operation_Parameter AS cop,"
                                       + " Coordinate_Operation_Parameter_Value AS copv"
                                       + " where co.COORD_OP_CODE = ?"
                                       + " and co.COORD_OP_METHOD_CODE = copu.COORD_OP_METHOD_CODE"
                                       + " and cop.PARAMETER_CODE = copu.PARAMETER_CODE"
                                       + " and copv.PARAMETER_CODE = copu.PARAMETER_CODE"
                                       + " and copv.COORD_OP_CODE = ?"
                                       + " order by copu.SORT_ORDER");
        stmt.setString(1, op_code);
        stmt.setString(2, op_code);
        final ResultSet result = stmt.executeQuery();
        while (result.next()) {
            final String  code = getString(result, 1);
            final String  name = getString(result, 2);
            final double value = result.getDouble(3);
            if (result.wasNull()) {
                /*
                 * This a temporary hack because sometimes PARAMETER_VALUE is not
                 * defined, it is replaced by PARAMETER_VALUE_FILE_RE but I don't
                 * know what to do with this one !
                 */
                return null;
            }
            final String  unit = getString(result, 4);
            list.add(new Parameter(code, getOGCParamName(name), value, createUnit(unit)));
        }
        result.close();
        return (Parameter[]) list.toArray(new Parameter[list.size()]);
    }
    
    /**
     * An internal class for Operations Parameters informations
     */
    private static final class Parameter {
        /**
         * The EPSG code for this Parameter
         */
        public final String code;
        
        /**
         * The name of the parameter.
         */
        public final String name;
        
        /**
         * The value of the parameter.
         */
        public final double value;
        
        /**
         * The Unit for this parameter.
         */
        public final Unit unit;
        
        /**
         * Main class constructor.
         */
        private Parameter(final String code, final String name, final double value, final Unit unit)
        {
            this.code  = code;
            this.name  = name;
            this.value = value;
            this.unit  = unit;
        }

        /**
         * Returns a string representation for debugging purpose.
         */
        public String toString()
        {
            StringBuffer str = new StringBuffer();
            str.append("Parameter[\"");
            str.append(name);
            str.append("\"," + code);
            str.append("," + value);
            str.append("," + unit + "]");
            return str.toString();
        }
    }
}
