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

// Images
import java.awt.Image;
import java.awt.image.RenderedImage;
import javax.imageio.IIOException;

// Properties and parameters
import javax.media.jai.DeferredData;
import javax.media.jai.ParameterList;
import javax.media.jai.PropertySource;
import javax.media.jai.DeferredProperty;

// Input/output
import java.io.*;
import java.net.URL;

// Formatting
import java.util.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// Collections
import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

// Logging
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Miscellaneous
import java.util.Locale;

// Geotools dependencies (CTS and GCS)
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;

// Resources
import org.geotools.units.Unit;
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/*
 * NOTE: For an unknow reason, JavaDoc {@link} and {@see} tags fail to recognize classes if they
 *       are not fully qualified.  For example we have to write {@link org.geotools.pt.Envelope}
 *       instead of {@link Envelope}. This is the only Java source I'm aware of which cause this
 *       failure.
 */


/**
 * Helper class for creating OpenGIS's object from a set of properties. Properties are
 * <cite>key-value</cite> pairs, for example <code>"Units=meters"</code>. There is a wide
 * variety of ways to contruct OpenGIS's objects from <cite>key-value</cite> pairs, and
 * supporting them is not always straightforward. The <code>PropertyParser</code> class
 * try to make the work easier. It defines a set of format-neutral keys (i.e. keys not
 * related to any file format in particular). Before parsing a file, the mapping between
 * format-neutral keys and "real" keys used in a particuler file format <strong>must</strong>
 * be specified. This mapping is constructed with calls to {@link #addAlias}. For example,
 * one may want to parse the following informations:
 *
 * <blockquote><pre>
 * XMinimum           = 217904.31
 * YMaximum           = 5663495.1
 * XResolution        = 1000.0000
 * YResolution        = 1000.0000
 * Units              = meters
 * Projection         = Mercator_1SP
 * Central meridian   = -15.2167
 * Latitude of origin =  28.0667
 * False easting      = 0.00000000
 * False northing     = 0.00000000
 * Ellipsoid          = Clarke 1866
 * Datum              = Clarke 1866
 * </pre></blockquote>
 *
 * Before to be used for parsing such informations, a <code>PropertyParser</code> object
 * must be setup using the following code:
 *
 * <blockquote><pre>
 * addAlias({@link #X_MINIMUM},    "XMinimum");
 * addAlias({@link #Y_MAXIMUM},    "YMaximum");
 * addAlias({@link #X_RESOLUTION}, "XResolution");
 * addAlias({@link #Y_RESOLUTION}, "YResolution");
 * // etc...
 * </pre></blockquote>
 *
 * Once the mapping is etablished, <code>PropertyParser</code> provides a set of
 * <code>getXXX()</code> methods for constructing various objects from those informations.
 * For example, the {@link #getCoordinateSystem} method constructs a
 * {@link org.geotools.cs.CoordinateSystem} object using available informations.
 *
 * @version $Id: PropertyParser.java,v 1.17 2003/11/12 14:13:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PropertyParser {
    /**
     * Set of commonly used symbols for "metres".
     */
    private static final String[] METRES = {
        "meter", "meters", "metre", "metres", "m"
    };

    /**
     * Set of commonly used symbols for "degrees".
     *
     * @task TODO: Need a more general way to set unit symbols once the Unit API is completed.
     */
    private static final String[] DEGREES = {
        "degree", "degrees", "deg", "°"
    };

    /**
     * Small tolerance factor when checking properties for consistency.
     */
    private static final double EPS = 1E-6;

    /**
     * Key for the coordinate system axis units.
     * The {@link #getUnits} method looks for this property.
     * Its return value is used by {@link #getCoordinateSystem} as below:
     * <ul>
     *   <li>If the unit is compatible with {@link org.geotools.units.Unit#DEGREE}, then
     *       <code>getCoordinateSystem()</code> will usually returns a
     *       {@link org.geotools.cs.GeographicCoordinateSystem}.</li>
     *   <li>Otherwise, if this unit is compatible with {@link org.geotools.units.Unit#METRE}, then
     *       <code>getCoordinateSystem()</code> will usually returns a
     *       {@link org.geotools.cs.ProjectedCoordinateSystem}.</li>
     * </ul>
     *
     * @see #DATUM
     * @see #ELLIPSOID
     * @see #PROJECTION
     */
    public static final Key UNITS = new Key("Units") {
        public Object getValue(final GridCoverage coverage) {
            /*
             * TODO: Invokes CoordinateSystem.getUnits() if we
             *       make this method public in a future version.
             */
            Unit unit = null;
            final CoordinateSystem cs = coverage.getCoordinateSystem();
            if (cs != null) {
                for (int i=cs.getDimension(); --i>=0;) {
                    final Unit candidate = cs.getUnits(i);
                    if (candidate != null) {
                        if (unit == null) {
                            unit = candidate;
                        } else if (!unit.equals(candidate)) {
                            return null;
                        }
                    }
                }
            }
            return unit;
        }
    };

    /**
     * Key for the coordinate system datum.
     * The {@link #getDatum} method looks for this property.
     * Its return value is used by {@link #getCoordinateSystem}.
     *
     * @see #UNITS
     * @see #ELLIPSOID
     * @see #PROJECTION
     */
    public static final Key DATUM = new Key("Datum");
    /*
     * TODO: Invokes CoordinateSystem.getDatum() if we
     *       make this method public in a future version.
     */

    /**
     * Key for the coordinate system ellipsoid.
     * The {@link #getEllipsoid} method looks for this property.
     * Its return value is used by {@link #getProjection}.
     *
     * @see #UNITS
     * @see #DATUM
     * @see #PROJECTION
     */
    public static final Key ELLIPSOID = new Key("Ellipsoid") {
        public Object getValue(final GridCoverage coverage) {
            return CTSUtilities.getEllipsoid(coverage.getCoordinateSystem());
        }
    };

    /**
     * Key for the projection classification. This is the classification name required by {@link
     * org.geotools.cs.CoordinateSystemFactory#createProjection(CharSequence,String,ParameterList)
     * CoordinateSystemFactory.createProjection(...)}. The {@link #getProjection} method
     * looks for this property. Its return value is used by {@link #getCoordinateSystem}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     */
    public static final Key PROJECTION = new Key("Projection") {
        public Object getValue(final GridCoverage coverage) {
            return CTSUtilities.getProjection(coverage.getCoordinateSystem());
        }
    };

    /**
     * Optional key for the projection name. The {@link #getProjection} method looks for
     * this property, if presents. The projection name is used for documentation purpose
     * only. If it is not defined, then the projection name will be the same than the
     * classification name.
     *
     * @see #PROJECTION
     * @see #COORDINATE_SYSTEM_NAME
     */
    public static final Key PROJECTION_NAME = new Key("Projection name") {
        public Object getValue(final GridCoverage coverage) {
            final Projection proj = CTSUtilities.getProjection(coverage.getCoordinateSystem());
            return (proj!=null) ? proj.getName(null) : null;
        }
    };

    /**
     * Optional Key for the coordinate system name.
     * The {@link #getCoordinateSystem} method looks for this property.
     *
     * @see #PROJECTION_NAME
     */
    public static final Key COORDINATE_SYSTEM_NAME = new Key("CoordinateSystem name") {
        public Object getValue(final GridCoverage coverage) {
            final CoordinateSystem cs = coverage.getCoordinateSystem();
            return (cs!=null) ? cs.getName(null) : null;
        }
    };

    /**
     * Key for the <code>"semi_major"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key SEMI_MAJOR = new ProjectionKey("semi_major");

    /**
     * Key for the <code>"semi_minor"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key SEMI_MINOR = new ProjectionKey("semi_minor");

    /**
     * Key for the <code>"latitude_of_origin"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key LATITUDE_OF_ORIGIN = new ProjectionKey("latitude_of_origin");

    /**
     * Key for the <code>"central_meridian"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key CENTRAL_MERIDIAN = new ProjectionKey("central_meridian");

    /**
     * Key for the <code>"false_easting"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key FALSE_EASTING = new ProjectionKey("false_easting");

    /**
     * Key for the <code>"false_northing"</code> projection parameter.
     * There is no specific method for this key. However, this
     * key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #PROJECTION
     */
    public static final Key FALSE_NORTHING = new ProjectionKey("false_northing");

    /**
     * Key for the minimal <var>x</var> value (western limit).
     * This is usually the longitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this property in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMinimum minimal coordinate} for dimension
     * <strong>0</strong>.
     *
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key X_MINIMUM = new EnvelopeKey("XMinimum", (byte)0, EnvelopeKey.MINIMUM);

    /**
     * Key for the minimal <var>y</var> value (southern limit).
     * This is usually the latitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this property. in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMinimum minimal coordinate} for dimension
     * <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key Y_MINIMUM = new EnvelopeKey("YMinimum", (byte)1, EnvelopeKey.MINIMUM);

    /**
     * Key for the minimal <var>z</var> value.
     * This is usually the minimal altitude.
     * The {@link #getEnvelope} method looks for this property in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMinimum minimal coordinate} for dimension
     * <strong>2</strong>.
     *
     * @see #Z_MAXIMUM
     * @see #Z_RESOLUTION
     * @see #DEPTH
     */
    public static final Key Z_MINIMUM = new EnvelopeKey("ZMinimum", (byte)2, EnvelopeKey.MINIMUM);

    /**
     * Key for the maximal <var>x</var> value (eastern limit).
     * This is usually the longitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this property in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMaximum maximal coordinate} for dimension
     * <strong>0</strong>.
     *
     * @see #X_MINIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key X_MAXIMUM = new EnvelopeKey("XMaximum", (byte)0, EnvelopeKey.MAXIMUM);

    /**
     * Key for the maximal <var>y</var> value (northern limit).
     * This is usually the latitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this property in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMaximum maximal coordinate} for dimension
     * <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key Y_MAXIMUM = new EnvelopeKey("YMaximum", (byte)1, EnvelopeKey.MAXIMUM);

    /**
     * Key for the maximal <var>z</var> value.
     * This is usually the maximal altitude.
     * The {@link #getEnvelope} method looks for this property in order to set the
     * {@linkplain org.geotools.pt.Envelope#getMaximum maximal coordinate} for dimension
     * <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_RESOLUTION
     * @see #DEPTH
     */
    public static final Key Z_MAXIMUM = new EnvelopeKey("ZMaximum", (byte)2, EnvelopeKey.MAXIMUM);

    /**
     * Key for the resolution among the <var>x</var> axis.
     * The {@link #getEnvelope} method looks for this property in
     * order to infer the coordinates for dimension <strong>0</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #Y_RESOLUTION
     */
    public static final Key X_RESOLUTION = new EnvelopeKey("XResolution", (byte)0,
                                                           EnvelopeKey.RESOLUTION);

    /**
     * Key for the resolution among the <var>y</var> axis.
     * The {@link #getEnvelope} method looks for this property in
     * order to infer the coordinates for dimension <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #WIDTH
     * @see #HEIGHT
     */
    public static final Key Y_RESOLUTION = new EnvelopeKey("YResolution", (byte)1,
                                                           EnvelopeKey.RESOLUTION);

    /**
     * Key for the resolution among the <var>z</var> axis.
     * The {@link #getEnvelope} method looks for this property in
     * order to infer the coordinates for dimension <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_MAXIMUM
     * @see #DEPTH
     */
    public static final Key Z_RESOLUTION = new EnvelopeKey("ZResolution", (byte)2,
                                                           EnvelopeKey.RESOLUTION);

    /**
     * Key for the image's width in pixels.
     * The {@link #getGridRange} method looks for this property in order to infer the
     * {@linkplain org.geotools.gc.GridRange#getLength grid size} along the dimension
     * <strong>0</strong>.
     *
     * @see #HEIGHT
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key WIDTH = new EnvelopeKey("Width", (byte)0, EnvelopeKey.SIZE);

    /**
     * Key for the image's height in pixels.
     * The {@link #getGridRange} method looks for this property in order to infer the
     * {@linkplain org.geotools.gc.GridRange#getLength grid size} along the dimension
     * <strong>1</strong>.
     *
     * @see #WIDTH
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key HEIGHT = new EnvelopeKey("Height", (byte)1, EnvelopeKey.SIZE);

    /**
     * Key for the image's "depth" in pixels. This property may exists for 3D images,
     * but some implementations accept at most 1 pixel depth among the third dimension.
     * The {@link #getGridRange} method looks for this property in order to infer the
     * {@linkplain org.geotools.gc.GridRange#getLength grid size} along the dimension
     * <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_MAXIMUM
     * @see #Z_RESOLUTION
     */
    public static final Key DEPTH = new EnvelopeKey("Depth", (byte)2, EnvelopeKey.SIZE);
    
    /**
     * The source (the file path or the URL) specified during the last call to a
     * <code>load(...)</code> method.
     *
     * @see #load(File)
     * @see #load(URL)
     * @see #load(BufferedReader)
     */
    private String source;

    /**
     * The symbol to use as a separator. The full version (<code>separator</code>)
     * will be used for formatting with {@link #listProperties}, while the trimed
     * version (<code>trimseparator</code>) will be used for parsing with {@link #parseLine}.
     *
     * @see #getSeparator
     * @see #setSeparator
     */
    private String separator = " = ", trimSeparator = "=";

    /**
     * The non-localized pattern for formatting numbers (as floating point or as integer)
     * and dates. If <code>null</code>, then the default pattern is used.
     */
    private String numberPattern, datePattern;
    
    /**
     * The properties, or <code>null</code> if none. Keys are the caseless property names
     * as {@link Key} objects, and values are arbitrary objects (usually {@link String}s).
     * This map will be constructed only when first needed.
     */
    private Map properties;

    /**
     * The mapping between keys and alias, or <code>null</code> if there is no alias.
     * Keys are {@link Key} objects and values are {@link Set} of {@link Key} objects.
     * This mapping is used for two purpose:
     * <ul>
     *   <li>If the key is a {@link Key} object, then the value is the set of alias
     *       (as <code>AliasKey</code> objects) for this key. This set is used by
     *       <code>getXXX()</code> methods.</li>
     *   <li>If the key is an <code>AliasKey</code> object, then the value if the set
     *       of {@link Key} which have this alias. This set is used by <code>add</code>
     *       methods in order to check for ambiguity when adding a new property.</li>
     * </ul>
     */
    private Map naming;

    /**
     * The alias used in the last {@link #getOptional} invocation. This field is for information
     * purpose only. It is used when constructing an exception for an operation failure.
     */
    private transient String lastAlias;

    /**
     * Map of objects already created. Some objects may be expensive to construct and required
     * many times. For example, {@link #getCoordinateSystem} is required by some other methods
     * like {@link #getRange}. Caching objects after their construction allow for faster execution.
     * Keys are object names (e.g. "CoordinateSystem"), and value are the actual objects.
     */
    private transient Map cache;
    
    /**
     * The coordinate system factory to use for constructing
     * ellipsoids, projections, coordinate systems...
     */
    private final CoordinateSystemFactory factory;
    
    /**
     * The locale to use for formatting messages, or <code>null</code> for a default locale.
     * This is <strong>not</strong> the local to use for parsing the file. This later locale
     * is specified by {@link #getLocale}.
     */
    private Locale userLocale;
    
    /**
     * Construct a new <code>PropertyParser</code> using the default
     * {@link org.geotools.cs.CoordinateSystemFactory}.
     */
    public PropertyParser() {
        this(CoordinateSystemFactory.getDefault());
    }
    
    /**
     * Construct a new <code>PropertyParser</code> using the specified
     * {@link org.geotools.cs.CoordinateSystemFactory}.
     */
    public PropertyParser(final CoordinateSystemFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the characters to use as separator between keys and values. Leading and trailing
     * spaces will be keep when formatting with {@link #listProperties}, but will be ignored
     * when parsing with {@link #parseLine}. The default value is <code>"&nbsp;=&nbsp;"</code>.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Set the characters to use as separator between keys and values.
     */
    public synchronized void setSeparator(final String separator) {
        this.trimSeparator = separator.trim();
        this.separator     = separator;
    }

    /**
     * Returns the pattern used for parsing and formatting values of the specified type.
     * The type should be either <code>Number.class</code> or <code>Date.class</code>.
     *
     * if <code>type</code> is assignable to <code>Number.class</code>, then this method
     * returns the number pattern as specified by {@link java.text.DecimalFormat}.
     *
     * Otherwise, if <code>type</code> is assignable to <code>Date.class</code>, then this
     * method returns the date pattern as specified by {@link java.text.SimpleDateFormat}.
     *
     * In any case, this method returns <code>null</code> if this object should use the default
     * pattern for the {@linkplain #getLocale data locale}.
     *
     * @param  type The data type (<code>Number.class</code> or <code>Date.class</code>).
     * @return The format pattern for the specified data type, or <code>null</code> for
     *         the default locale-dependent pattern.
     * @throws IllegalArgumentException if <code>type</code> is not valid.
     */
    public String getFormatPattern(final Class type) {
        if (Date.class.isAssignableFrom(type)) {
            return datePattern;
        }
        if (Number.class.isAssignableFrom(type)) {
            return numberPattern;
        }
        throw new IllegalArgumentException(Utilities.getShortName(type));
    }

    /**
     * Set the pattern to use for parsing and formatting values of the specified type.
     * The type should be either <code>Number.class</code> or <code>Date.class</code>.
     *
     * <ul>
     *   <li>If <code>type</code> is assignable to <code>{@linkplain java.lang.Number}.class</code>,
     *       then <code>pattern</code> should be a {@link java.text.DecimalFormat} pattern (example:
     *       <code>"#0.###"</code>).</li>
     *   <li>If <code>type</code> is assignable to <code>{@linkplain java.util.Date}.class</code>,
     *       then <code>pattern</code> should be a {@link java.text.SimpleDateFormat} pattern
     *       (example: <code>"yyyy/MM/dd HH:mm"</code>).</li>
     * </ul>
     *
     * @param  type The data type (<code>Number.class</code> or <code>Date.class</code>).
     * @param  pattern The format pattern for the specified data type, or <code>null</code>
     *         for the default locale-dependent pattern.
     * @throws IllegalArgumentException if <code>type</code> is not valid.
     */
    public synchronized void setFormatPattern(final Class type, final String pattern) {
        if (Date.class.isAssignableFrom(type)) {
            datePattern = pattern;
            cache = null;
            return;
        }
        if (Number.class.isAssignableFrom(type)) {
            numberPattern = pattern;
            cache = null;
            return;
        }
        throw new IllegalArgumentException(Utilities.getShortName(type));
    }
    
    /**
     * Clear this property set. If the same <code>PropertyParser</code>
     * object is used for parsing many files, then <code>clear()</code>
     * should be invoked prior any <code>load(...)</code> method.  Note
     * that <code>clear()</code> do not remove any alias, so this <code>PropertyParser</code>
     * can been immediately reused for parsing new files of the same kind.
     */
    public synchronized void clear() {
        source     = null;
        properties = null;
        cache      = null;
    }
    
    /**
     * Read all properties from a text file. The default implementation invokes
     * {@link #load(BufferedReader)}. Note that this method do not invokes {@link #clear}
     * prior the loading. Consequently, the loaded properties will be added to the set of
     * existing properties.
     *
     * @param  header The file to read until EOF.
     * @throws IOException if an error occurs during loading.
     *
     * @see #clear()
     * @see #load(URL)
     * @see #parseLine
     * @see #getSource
     */
    public synchronized void load(final File header) throws IOException {
        source = header.getPath();
        final BufferedReader in = new BufferedReader(new FileReader(header));
        load(in);
        in.close();
    }
    
    /**
     * Read all properties from an URL. The default implementation invokes
     * {@link #load(BufferedReader)}. Note that this method do not invokes {@link #clear}
     * prior the loading. Consequently, the loaded properties will be added to the set of
     * existing properties.
     *
     * @param  header The URL to read until EOF.
     * @throws IOException if an error occurs during loading.
     *
     * @see #clear()
     * @see #load(File)
     * @see #parseLine
     * @see #getSource
     */
    public synchronized void load(final URL header) throws IOException {
        source = header.getPath();
        final BufferedReader in = new BufferedReader(new InputStreamReader(header.openStream()));
        load(in);
        in.close();
    }
    
    /**
     * Read all properties from a stream. The default implementation invokes
     * {@link #parseLine} for each non-empty line found in the stream. Notes:
     * <ul>
     *   <li>This method is not yet public because it has no way to know how
     *       to set the {@link #getSource source} property.</li>
     *   <li>This method is not synchronized. Synchronization, if wanted,
             must be done from the public frontend.</li>
     *   <li>This method do not invokes {@link #clear} prior the loading.</li>
     * </ul>
     *
     * @param in The stream to read until EOF. The stream will not be closed.
     * @throws IOException if an error occurs during loading.
     *
     * @see #clear()
     * @see #load(File)
     * @see #load(URL)
     * @see #parseLine
     */
    protected void load(final BufferedReader in) throws IOException {
        final Set  previousComments = new HashSet();
        final StringBuffer comments = new StringBuffer();
        final String  lineSeparator = System.getProperty("line.separator", "\n");
        String line; while ((line=in.readLine())!=null) {
            if (line.trim().length()!=0) {
                if (!parseLine(line)) {
                    if (previousComments.add(line)) {
                        comments.append(line);
                        comments.append(lineSeparator);
                    }
                }
            }
        }
        if (comments.length() != 0) {
            add((String)null, comments.toString());
        }
    }
    
    /**
     * Parse a line and add the key-value pair to this property set. The default implementation
     * take the substring on the left size of the first separator (usually the '=' character) as
     * the key, and the substring on the right size of separator as the value. For example,
     * if <code>line</code> has the following value:
     *
     * <blockquote><pre>
     * Ellipsoid = WGS 1984
     * </pre></blockquote>
     *
     * Then, the default implementation will translate this line in
     * the following call:
     *
     * <blockquote><pre>
     * {@link #add(String,Object) add}("Ellipsoid", "WGS 1984");
     * </pre></blockquote>
     *
     * This method returns <code>true</code> if it has consumed the line, or
     * <code>false</code> otherwise. A line is "consumed" if <code>parseLine</code>
     * has either added the key-value pair (using {@link #add}), or determined
     * that the line must be ignored (for example because <code>parseLine</code>
     * detected a character announcing a comment line). A "consumed" line will
     * not receive any further treatment. The line is not consumed (i.e. this
     * method returns <code>false</code>) if <code>parseLine</code> don't know
     * what to do with it. Non-consumed line will typically go up in a chain of
     * <code>parseLine</code> methods (if <code>PropertyParser</code> has been
     * subclassed) until someone consume it.
     *
     * @param  line The line to parse.
     * @return <code>true</code> if this method has consumed the line.
     * @throws IIOException if the line is badly formatted.
     * @throws AmbiguousPropertyException if a different value was
     *         already defined for the same property name.
     *
     * @see #load(File)
     * @see #load(URL)
     * @see #add(String,Object)
     */
    protected boolean parseLine(final String line) throws IIOException {
        final int index = line.indexOf(trimSeparator);
        if (index >= 0) {
            add(line.substring(0, index), line.substring(index+1));
            return true;
        }
        return false;
    }
    
    /**
     * Add all properties from the specified grid coverage. This convenience method can be used
     * together with {@link #listProperties} as a way to format the properties for an arbitrary
     * grid coverage. The default implementation performs the following step:
     * <ul>
     *   <li>For each <code>key</code> declared with
     *       <code>{@linkplain #addAlias addAlias}(<strong>key</strong>, alias)</code>, fetchs
     *       a value with <code>key.{linkplain Key#getValue getValue}(coverage)</code>.</li>
     *   <li>For each value found, {@linkplain #add(String, Object) add} the value under the
     *       name of the first alias found for the <code>key</code>.</li>
     *
     * @param coverage The grid coverage with properties to add to this <code>PropertyParser</code>.
     * @throws AmbiguousPropertyException if a property is defined twice.
     *
     * @see #add(RenderedImage)
     * @see #add(PropertySource,String)
     * @see #add(String,Object)
     * @see #listProperties
     */
    public synchronized void add(final GridCoverage coverage) throws AmbiguousPropertyException {
        if (naming == null) {
            return;
        }
        for (final Iterator it=naming.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Key key = (Key) entry.getKey();
            if (key instanceof AliasKey) {
                continue;
            }
            final Set alias = (Set) entry.getValue();
            if (alias==null || alias.isEmpty()) {
                continue;
            }
            final AliasKey keyAsAlias = (AliasKey) alias.iterator().next();
            /*
             * 'key' is one of the enumerations (X_MINIMUM, WIDTH, ELLIPSOID, etc...).
             * 'keyAsAlias' is the name to use for storing the value for this key.
             */
            add(keyAsAlias, key.getValue(coverage));
        }
    }
    
    /**
     * Add all properties from the specified image.
     *
     * @param  image The image with properties to add to this <code>PropertyParser</code>.
     * @throws AmbiguousPropertyException if a property is defined twice.
     *
     * @see #add(GridCoverage)
     * @see #add(PropertySource,String)
     * @see #add(String,Object)
     */
    public synchronized void add(final RenderedImage image) throws AmbiguousPropertyException {
        if (image instanceof PropertySource) {
            // This version allow the use of deferred properties.
            add((PropertySource) image, null);
        } else {
            final String[] names = image.getPropertyNames();
            if (names!=null) {
                for (int i=0; i<names.length; i++) {
                    final String name = names[i];
                    add(name, image.getProperty(name));
                }
            }
        }
    }

    /**
     * Add properties from the specified property source.
     *
     * @param  properties The properties source.
     * @param  prefix The prefix for properties to add, of <code>null</code> to add
     *         all properties. If non-null, only properties begining with this prefix
     *         will be added.
     * @throws AmbiguousPropertyException if a property is defined twice.
     *
     * @see #add(GridCoverage)
     * @see #add(RenderedImage)
     * @see #add(String,Object)
     */
    public synchronized void add(final PropertySource properties, final String prefix)
        throws AmbiguousPropertyException
    {
        final String[] names = (prefix!=null) ? properties.getPropertyNames(prefix) :
                                                properties.getPropertyNames();
        if (names != null) {
            for (int i=0; i<names.length; i++) {
                final String  name = names[i];
                final Class classe = properties.getPropertyClass(name);
                add(name, new DeferredProperty(properties, name, classe));
            }
        }
    }
    
    /**
     * Add a property for the specified key. Keys are case-insensitive, ignore leading and
     * trailing whitespaces and consider any other whitespace sequences as equal to a single
     * <code>'_'</code> character.
     *
     * @param  alias The key for the property to add. This is usually the name found in the
     *         file to be parsed (this is different from {@link Key} objects, which are keys
     *         in a format neutral way). This key is usually, but not always, one of the alias
     *         defined with {@link #addAlias}.
     * @param  value The value for the property to add. If <code>null</code> or
     *         {@link java.awt.Image#UndefinedProperty}, then this method do nothing.
     * @throws AmbiguousPropertyException if a different value already exists for the specified
     *         alias, or for an other alias bound to the same {@link Key}.
     *
     * @see #add(GridCoverage)
     * @see #add(RenderedImage)
     * @see #add(PropertySource,String)
     * @see #parseLine
     */
    public synchronized void add(String alias, final Object value) throws AmbiguousPropertyException
    {
        final AliasKey aliasAsKey;
        if (alias != null) {
            alias = alias.trim();
            aliasAsKey = new AliasKey(alias);
        }
        else aliasAsKey = null;
        add(aliasAsKey, value);
    }

    /**
     * Implementation of the {@link #add(String, Object)} method. This method is invoked by
     * {@link #add(GridCoverage)}, which iterates through each {@link AliasKey} declared in
     * {@link #naming}.
     */
    private void add(final AliasKey aliasAsKey, Object value) throws AmbiguousPropertyException {
        assert isValid();
        if (value==null || value==Image.UndefinedProperty) {
            return;
        }
        if (value instanceof CharSequence) {
            final String text = trim(value.toString().trim(), " ");
            if (text.length() == 0) return;
            value = text;
        }
        if (properties==null) {
            properties = new LinkedHashMap();
        }
        /*
         * Consistency check:
         *
         *    - First, compare the value with any older values defined for the
         *      same alias. This value is fetched only once with 'getProperty'.
         *    - Next, compare the value with any values defined with any other
         *      alias bound to the same key. Those values are fetched in a loop
         *      with 'getOptional'.
         */
        Object   oldValue = getProperty(aliasAsKey);
        Key      checkKey = null;
        Iterator iterator = null;
        while (true) {
            if (oldValue!=null && !oldValue.equals(value)) {
                final String alias = aliasAsKey.toString();
                throw new AmbiguousPropertyException(Resources.getResources(userLocale).
                          getString(ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, alias),
                          checkKey, alias);
            }
            if (iterator == null) {
                if (naming == null) break;
                final Set keySet = (Set) naming.get(aliasAsKey);
                if (keySet == null) break;
                iterator = keySet.iterator();
            }
            if (!iterator.hasNext()) break;
            checkKey = (Key) iterator.next();
            oldValue = getOptional(checkKey);
        }
        /*
         * All tests are okay. Now add the property.
         */
        cache = null;
        properties.put(aliasAsKey, value);
    }

    /**
     * Add an alias to a key. After this method has been invoked, calls
     * to <code>{@link #get get}(key)</code> will really looks for property
     * named <code>alias</code>. Alias are mandatory in order to get various
     * <code>getXXX()</code> methods to work for a particular file format.
     *
     * For example if the file to be parsed uses the names <code>"ULX"</code>
     * and <code>"ULY"</code> for the coordinate of the upper left corner,
     * then the {@link #getEnvelope} method will not work unless the following
     * alias are set:
     *
     * <blockquote><pre>
     * addAlias({@link #X_MINIMUM}, "ULX");
     * addAlias({@link #Y_MAXIMUM}, "ULY");
     * </pre></blockquote>
     *
     * An arbitrary number of alias can be set for the same key. For example,
     * <code>addAlias(Y_MAXIMUM,&nbsp;...)</code> could be invoked twice with
     * <code>"ULY"</code> and <code>"Limit North"</code> alias. The <code>getXXX()</code>
     * methods will try alias in the order they were added and use the first
     * value found.
     * <br><br>
     * The same alias can also be set to more than one key. For example, the
     * following code is legal. It said that pixel are square with the same
     * horizontal and vertical resolution:
     *
     * <blockquote><pre>
     * addAlias({@link #X_RESOLUTION}, "Resolution");
     * addAlias({@link #Y_RESOLUTION}, "Resolution");
     * </pre></blockquote>
     *
     * @param  key The key to add an alias. This key is format neutral.
     * @param  alias The alias to add. This is the name actually used in the file to be parsed.
     *         Alias are case insensitive and ignore multiple whitespace, like keys. If
     *         this alias is already bound to the specified key, then this method do nothing.
     * @throws AmbiguousPropertyException if the addition of the supplied alias
     *         would introduce an ambiguity in the current set of properties.
     *         This occurs if the key has already an alias mapping to a different value.
     *
     * @see #getAlias
     * @see #contains
     * @see #get
     */
    public synchronized void addAlias(final Key key, String alias) throws AmbiguousPropertyException
    {
        alias = trim(alias.trim(), " ");
        final AliasKey aliasAsKey = new AliasKey(alias);
        final Object   property   = getProperty(aliasAsKey);
        if (property != null) {
            final Object value = getOptional(key); // Checks also alias
            if (value!=null && !value.equals(property)) {
                throw new AmbiguousPropertyException(Resources.getResources(userLocale).
                          getString(ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, alias),
                          key, alias);
            }
        }
        if (naming == null) {
            naming = new LinkedHashMap();
        }
        cache = null;
        // Add the alias for the specified key. This is the information
        // used by 'get' methods for fetching a property from a key.
        Set set = (Set) naming.get(key);
        if (set == null) {
            set = new LinkedHashSet(4);
            naming.put(key, set);
        }
        set.add(aliasAsKey);
        // Add the key for the specified alias. This is the information used by
        // 'add' to check against ambiguities. Set's order doesn't matter here,
        // but we use LinkedHashSet anyway for faster iteration in key set.
        set = (Set) naming.get(aliasAsKey);
        if (set == null) {
            set = new LinkedHashSet(4);
            naming.put(aliasAsKey, set);
        }
        set.add(key);
        assert isValid();
    }

    /**
     * Checks if this object is in a valid state. {@link #naming} should
     * contains a key for every values in all {@link Set} objects.
     */
    private boolean isValid() {
        assert Thread.holdsLock(this);
        if (naming != null) {
            for (final Iterator it=naming.values().iterator(); it.hasNext();) {
                if (!naming.keySet().containsAll((Set) it.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the property value for the specified alias. No other alias than the specified
     * one is examined. This method is used for the implementation of {@link #getOptional(Key)}.
     * This method is also invoked by {@link #add(String,Object)} in order to check if an
     * incompatible value is already set for a given alias.
     *
     * @param  key The key of the desired property. Keys are case-insensitive and
     *         can be any of the alias defined with {@link #addAlias}.
     * @return The property for the specified alias, or <code>null</code> if none.
     */
    private Object getProperty(final AliasKey alias) {
        assert Thread.holdsLock(this);
        if (properties != null) {
            Object value = properties.get(alias);
            if (value instanceof DeferredData) {
                value = ((DeferredData) value).getData();
            }
            if (value!=null && value!=Image.UndefinedProperty) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the property for the specified key, or <code>null</code> if the property is not
     * found. This method expect a format neutral, case insensitive {@link Key} argument. In
     * order to maps the key to the actual name used in the underlying metadata file, the method
     * {@link #addAlias} <strong>must</strong> have been invoked prior to any <code>get</code>
     * method.
     *
     * @param  key The key of the desired property. Keys are case-insensitive and
     *         can be any of the alias defined with {@link #addAlias}.
     * @return The property for the specified key, or <code>null</code> if none.
     */
    private Object getOptional(final Key key) {
        assert Thread.holdsLock(this);
        lastAlias = null;
        if (naming != null) {
            final Set alias = (Set) naming.get(key);
            if (alias != null) {
                for (final Iterator it=alias.iterator(); it.hasNext();) {
                    final AliasKey aliasAsKey = (AliasKey) it.next();
                    final Object value = getProperty(aliasAsKey);
                    if (value != null) {
                        lastAlias = aliasAsKey.toString();
                        return value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if this <code>PropertyParser</code> contains a value for the specified key.
     * Invoking {@link #get} will thrown a {@link MissingPropertyException} if and only
     * if {@link #contains} returns <code>false</code> for the same key.
     *
     * @param  key The key to test for inclusion in this <code>PropertyParser</code>.
     * @return <code>true</code> if the given key was found.
     *
     * @see #get
     * @see #addAlias
     */
    public synchronized boolean contains(final Key key) {
        return getOptional(key) != null;
    }
    
    /**
     * Returns the property for the specified key. This method expect a format neutral, case
     * insensitive {@link Key} argument. In order to maps the key to the actual name used in
     * the underlying metadata file, the method {@link #addAlias} <strong>must</strong> have
     * been invoked prior to any <code>get</code> method.
     *
     * @param  key The key of the desired property. Keys are case insensitive and format neutral.
     * @return Value for the specified key (never <code>null</code>).
     * @throws MissingPropertyException if no value exists for the specified key.
     *
     * @see #getAsDouble
     * @see #getAsInt
     * @see #contains
     * @see #addAlias
     */
    public synchronized Object get(final Key key) throws MissingPropertyException {
        final Object value = getOptional(key);
        if (value!=null && value!=Image.UndefinedProperty) {
            return value;
        }
        throw new MissingPropertyException(Resources.getResources(userLocale).
                  getString(ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1, key), key, lastAlias);
    }
    
    /**
     * Returns a property as a <code>double</code> value. The default implementation invokes
     * {@link #getAsDouble(Key)} or {@link #getAsDate(Key)} according the property type: the
     * property is assumed to be a number, except if <code>cs</code> is a
     * {@link TemporalCoordinateSystem}. In this later case, the property is assumed to be a
     * {@link Date}.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @param  cs  The coordinate system for the dimension of the key to be queried,
     *             or <code>null</code> if unknow.
     * @return Value for the specified key as a <code>double</code>.
     * @throws MissingPropertyException if no value exists for the specified key.
     * @throws PropertyException if the value can't be parsed as a <code>double</code>.
     */
    private double getAsDouble(final Key key, final CoordinateSystem cs) throws PropertyException {
        if (cs instanceof TemporalCoordinateSystem) {
            final Date time  = getAsDate(key);
            final Date epoch = ((TemporalCoordinateSystem) cs).getEpoch();
            return cs.getUnits(0).convert(time.getTime() - epoch.getTime(), Unit.MILLISECOND);
        } else {
            return getAsDouble(key);
        }
    }
    
    /**
     * Returns a property as a <code>double</code> value. The default implementation
     * invokes <code>{@link #get get}(key)</code> and parse the resulting value with
     * {@link java.text.NumberFormat#parse(String)} for the {@linkplain #getLocale current locale}.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key as a <code>double</code>.
     * @throws MissingPropertyException if no value exists for the specified key.
     * @throws PropertyException if the value can't be parsed as a <code>double</code>.
     *
     * @see #getAsInt
     * @see #get
     * @see #contains
     * @see #addAlias
     */
    public synchronized double getAsDouble(final Key key) throws PropertyException {
        final Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return getNumberFormat().parse(value.toString()).doubleValue();
        } catch (ParseException exception) {
            final PropertyException e;
            e = new PropertyException(exception.getLocalizedMessage(), key, lastAlias);
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Returns a property as a <code>int</code> value. The default implementation
     * invokes <code>{@link #getAsDouble getAsDouble}(key)</code> and make sure
     * that the resulting value is an integer.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key as an <code>int</code>.
     * @throws MissingPropertyException if no value exists for the specified key.
     * @throws PropertyException if the value can't be parsed as an <code>int</code>.
     *
     * @see #getAsDouble
     * @see #get
     * @see #contains
     * @see #addAlias
     */
    public synchronized int getAsInt(final Key key) throws PropertyException {
        final double value = getAsDouble(key);
        final int  integer = (int) value;
        if (value != integer) {
            throw new PropertyException(Resources.getResources(userLocale).getString(
                      ResourceKeys.ERROR_BAD_PARAMETER_$2, lastAlias, new Double(value)), key, lastAlias);
        }
        return integer;
    }
    
    /**
     * Returns a property as a {@link java.util.Date} value. The default implementation
     * invokes <code>{@link #get get}(key)</code> and parse the resulting value with
     * {@link java.text.DateFormat#parse(String)} for the {@linkplain #getLocale current locale}.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key as a {@link java.util.Date}.
     * @throws MissingPropertyException if no value exists for the specified key.
     * @throws PropertyException if the value can't be parsed as a date.
     */
    public synchronized Date getAsDate(final Key key) throws PropertyException {
        final Object value = get(key);
        if (value instanceof Date) {
            return (Date) (((Date) value).clone());
        }
        try {
            return getDateFormat().parse(value.toString());
        } catch (ParseException exception) {
            final PropertyException e;
            e = new PropertyException(exception.getLocalizedMessage(), key, lastAlias);
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Get the object to use for parsing numbers.
     */
    private NumberFormat getNumberFormat() throws PropertyException {
        assert Thread.holdsLock(this);
        final String CACHE_KEY = "NumberFormat";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof NumberFormat) {
                return (NumberFormat) candidate;
            }
        }
        final NumberFormat format = NumberFormat.getNumberInstance(getLocale());
        if (numberPattern!=null && format instanceof DecimalFormat) {
            ((DecimalFormat) format).applyPattern(numberPattern);
        }
        cache(CACHE_KEY, format);
        return format; // Do not clone, since this method is private.
    }

    /**
     * Get the object to use for parsing dates.
     */
    private DateFormat getDateFormat() throws PropertyException {
        assert Thread.holdsLock(this);
        final String CACHE_KEY = "DateFormat";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof DateFormat) {
                return (DateFormat) candidate;
            }
        }
        final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                                 DateFormat.SHORT, getLocale());
        if (datePattern!=null && format instanceof SimpleDateFormat) {
            ((SimpleDateFormat) format).applyPattern(datePattern);
        }
        cache(CACHE_KEY, format);
        return format; // Do not clone, since this method is private.
    }

    /**
     * Add an object in the cache.
     */
    private void cache(final String key, final Object object) {
        assert Thread.holdsLock(this);
        if (cache == null) {
            cache = new HashMap();
        }
        cache.put(key, object);
    }

    /**
     * Returns the list of alias for the specified key, or <code>null</code>
     * if the key has no alias. Alias are the names used in the underlying
     * metadata file, and are format dependent.
     *
     * @param  key The format neutral key.
     * @return The alias for the specified key, or <code>null</code> if none.
     *
     * @see #addAlias
     */
    public synchronized String[] getAlias(final Key key) {
        assert isValid();
        if (naming != null) {
            final Set alias = (Set) naming.get(key);
            if (alias != null) {
                int index = 0;
                final String[] list = new String[alias.size()];
                for (final Iterator it=alias.iterator(); it.hasNext();) {
                    list[index++] = it.next().toString();
                }
                assert index == list.length;
                return list;
            }
        }
        return null;
    }
    
    /**
     * Returns the source file name or URL. This is the path specified
     * during the last call to a <code>load(...)</code> method.
     *
     * @return The source file name or URL.
     * @throws PropertyException if this information can't be fetched.
     *
     * @link #load(File)
     * @link #load(URL)
     */
    public String getSource() throws PropertyException {
        return source;
    }

    /**
     * Returns the locale to use when parsing property values as numbers, angles or dates.
     * This is <strong>not</strong> the locale used for formatting error messages, if any.
     * The default implementation returns {@link java.util.Locale#US}, since it is the format used
     * in most data file.
     *
     * @return The locale to use for parsing property values.
     * @throws PropertyException if this information can't be fetched.
     *
     * @see #getAsDouble
     * @see #getAsInt
     * @see #getAsDate
     */
    public Locale getLocale() throws PropertyException {
        return Locale.US;
    }
    
    /**
     * Returns the units. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #UNITS})</code>
     * and transform the resulting string into an {@link org.geotools.units.Unit} object.
     *
     * @throws MissingPropertyException if no value exists for the {@link #UNITS} key.
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getCoordinateSystem
     */
    public synchronized Unit getUnits() throws PropertyException {
        final Object value = get(UNITS);
        if (value instanceof Unit) {
            return (Unit) value;
        }
        final String text = value.toString();
        if (contains(text, METRES)) {
            return Unit.METRE;
        } else if (contains(text, DEGREES)) {
            return Unit.DEGREE;
        } else {
            throw new PropertyException("Unknow unit: "+text, UNITS, lastAlias);
        }
    }
    
    /**
     * Check if <code>toSearch</code> appears in the <code>list</code> array.
     * Search is case-insensitive. This is a temporary patch (will be removed
     * when the final API for JSR-108: Units specification will be available).
     */
    private static boolean contains(final String toSearch, final String[] list) {
        for (int i=list.length; --i>=0;) {
            if (toSearch.equalsIgnoreCase(list[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the datum. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #DATUM})</code>
     * and transform the resulting string into a {@link org.geotools.cs.HorizontalDatum} object.
     *
     * @throws MissingPropertyException if no value exists for the {@link #DATUM} key.
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getCoordinateSystem
     * @see #getEllipsoid
     */
    public synchronized HorizontalDatum getDatum() throws PropertyException {
        final Object value = get(DATUM);
        if (value instanceof HorizontalDatum) {
            return (HorizontalDatum) value;
        }
        final String text = value.toString();
        /*
         * TODO: parse 'text' when CoordinateSystemAuthorityFactory
         *       will be implemented.
         */
        checkEllipsoid(text, "getDatum");
        return HorizontalDatum.WGS84;
    }
    
    /**
     * Returns the ellipsoid. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #ELLIPSOID})</code>
     * and transform the resulting string into an {@link org.geotools.cs.Ellipsoid} object.
     *
     * @throws MissingPropertyException if no value exists for the {@link #ELLIPSOID} key.
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getCoordinateSystem
     * @see #getDatum
     */
    public synchronized Ellipsoid getEllipsoid() throws PropertyException {
        final Object value = get(ELLIPSOID);
        if (value instanceof Ellipsoid) {
            return (Ellipsoid) value;
        }
        final String text = value.toString();
        /*
         * TODO: parse 'text' when CoordinateSystemAuthorityFactory
         *       will be implemented.
         */
        checkEllipsoid(text, "getEllipsoid");
        return Ellipsoid.WGS84;
    }
    
    /**
     * Check if the supplied ellipsoid is WGS 1984.
     * This is a temporary patch.
     *
     * @task TODO: parse the datum and ellipsoid names when CoordinateSystemAuthorityFactory
     *             will be implemented. The current EPSG factory implementation may not be enough.
     */
    private static synchronized void checkEllipsoid(String text, final String source) {
        text = trim(text, " ").replace('_', ' ');
        if (!text.equalsIgnoreCase("WGS 1984") &&
            !text.equalsIgnoreCase("WGS1984" ) &&
            !text.equalsIgnoreCase("WGS84"   ))
        {
            if (!emittedWarning) {
                emittedWarning = true;
                final String message = '"'+text+"\" ellipsoid not yet implemented. Default to WGS 1984.";
                final LogRecord record = new LogRecord(Level.WARNING, message);
                record.setSourceMethodName(source);
                record.setSourceClassName("PropertyParser");
                GridCoverageReader.LOGGER.log(record);
            }
        }
    }
    
    /** Temporary flag for {@link #checkEllipsoid}. */
    private static boolean emittedWarning;
    
    /**
     * Returns the projection. The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Gets the projection classification with
     *       <code>{@linkplain #get get}({@linkplain #PROJECTION})</code>.</li>
     *
     *   <li>Gets the list of projection parameters for the above classification with
     *       <code>{@linkplain org.geotools.cs.CoordinateSystemFactory#createProjectionParameterList
     *       createProjectionParameterList}(classification)</code>.</li>
     *
     *   <li>Gets the property values for each parameters in the above
     *       {@link javax.media.jai.ParameterList}.
     *       If a parameter is not defined in this <code>PropertyParser</code>, then it will
     *       be left to its (projection dependent) default value. Parameters are projection
     *       dependent, but will typically include
     *
     *           <code>"semi_major"</code>,
     *           <code>"semi_minor"</code>,
     *           <code>"central_meridian"</code>,
     *           <code>"latitude_of_origin"</code>,
     *           <code>"false_easting"</code> and
     *           <code>"false_northing"</code>.
     *
     *       The names actually used in the metadata file to be parsed must be declared as usual,
     *       e.g. <code>{@linkplain #addAlias addAlias}({@linkplain #SEMI_MAJOR}, ...)</code></li>
     *
     *   <li>If no value was defined for <code>"semi-major"</code> and/or <code>"semi-minor"</code>
     *       parameters, then invokes {@link #getEllipsoid} and uses its semi-axis length.</li>
     *
     *   <li>If a value exists for the optional key {@link #PROJECTION_NAME}, then takes it as
     *       the projection name. The projection name is for documentation purpose only and do
     *       not affect any computation. If there is no value for {@link #PROJECTION_NAME}, then
     *       the projection name will be the same than the classification name (the first step
     *       above).</li>
     * </ul>
     *
     * @return The projection, or <code>null</code> if the underlying coordinate
     *         system is not a {@link org.geotools.cs.ProjectedCoordinateSystem}.
     * @throws MissingPropertyException if no value exists for the {@link #PROJECTION} key.
     * @throws PropertyException if the operation failed for some other reason
     *         (for example if a parameter value can't be parsed as a <code>double</code>).
     *
     * @see #getCoordinateSystem
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     */
    public synchronized Projection getProjection() throws PropertyException {
        /*
         * First, checks if a Projection object has already
         * been constructed. Since Projection is immutable,
         * it is safe to returns a single instance for all.
         */
        final String CACHE_KEY = "Projection";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof Projection) {
                return (Projection) candidate;
            }
        }
        /*
         * No projection is available in the cache.
         * Compute it now and cache it for future use.
         */
        Object value = get(PROJECTION);
        final String lastAlias = this.lastAlias; // Protect from change
        if (value instanceof Projection) {
            return (Projection) value;
        }
        final String classification = value.toString();
        value = getOptional(PROJECTION_NAME);
        final String projectionName = (value!=null) ? value.toString() : classification;
        /*
         * Parse parameters.
         */
        boolean semiMajorAxisDefined = false;
        boolean semiMinorAxisDefined = false;
        ParameterList  parameters = factory.createProjectionParameterList(classification);
        final String[] paramNames = parameters.getParameterListDescriptor().getParamNames();
        for (int i=0; i<paramNames.length; i++) {
            final double paramValue;
            final String name = paramNames[i];
            try {
                paramValue = getAsDouble(new Key(name));
            } catch (MissingPropertyException exception) {
                // Parameter is not defined. Lets it to
                // its default value and continue...
                continue;
            }
            parameters = parameters.setParameter(name, paramValue);
            if (name.equalsIgnoreCase("semi_major")) semiMajorAxisDefined=true;
            if (name.equalsIgnoreCase("semi_minor")) semiMinorAxisDefined=true;
        }
        if (!semiMajorAxisDefined || !semiMinorAxisDefined) {
            final Ellipsoid ellipsoid = getEllipsoid();
            final double semiMajor = ellipsoid.getSemiMajorAxis();
            final double semiMinor = ellipsoid.getSemiMinorAxis();
            if ((semiMajorAxisDefined && parameters.getDoubleParameter("semi_major")!=semiMajor) ||
                (semiMinorAxisDefined && parameters.getDoubleParameter("semi_minor")!=semiMinor))
            {
                throw new AmbiguousPropertyException(Resources.getResources(userLocale).
                          getString(ResourceKeys.ERROR_AMBIGIOUS_AXIS_LENGTH), PROJECTION, lastAlias);
            }
            parameters = parameters.setParameter("semi_major", semiMajor)
                                   .setParameter("semi_minor", semiMinor);
        }
        try {
            final Projection projection;
            projection = factory.createProjection(projectionName, classification, parameters);
            cache(CACHE_KEY, projection);
            return projection;
        } catch (FactoryException exception) {
            final PropertyException e;
            e = new PropertyException(exception.getLocalizedMessage(), PROJECTION, lastAlias);
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Returns the coordinate system. The default implementation construct a coordinate
     * system from the information provided by {@link #getUnits}, {@link #getDatum} and
     * {@link #getProjection}. The coordinate system name (optional) will be fetch from
     * property {@link #COORDINATE_SYSTEM_NAME}, if presents.
     *
     * @throws MissingPropertyException if a required value is missing
     *        (e.g. {@link #PROJECTION}, {@link #DATUM}, {@link #UNITS}, etc.).
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getUnits
     * @see #getDatum
     * @see #getProjection
     */
    public synchronized CoordinateSystem getCoordinateSystem() throws PropertyException {
        /*
         * First, checks if a CoordinateSystem object has already
         * been constructed. Since CoordinateSystem is immutable,
         * it is safe to returns a single instance for all.
         */
        final String CACHE_KEY = "CoordinateSystem";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof CoordinateSystem) {
                return (CoordinateSystem) candidate;
            }
        }
        /*
         * No CoordinateSystem is available in the cache.
         * Compute it now and cache it for future use.
         */
        Object value = getOptional(COORDINATE_SYSTEM_NAME);
        final String lastAlias = this.lastAlias; // Protect from change
        if (value == null) {
            value = "Generated";
        } else if (value instanceof CoordinateSystem) {
            return (CoordinateSystem) value;
        }
        final String            text = value.toString();
        final Unit             units = getUnits();
        final HorizontalDatum  datum = getDatum();
        final boolean   isGeographic = Unit.DEGREE.canConvert(units);
        final Unit          geoUnits = isGeographic ? units : Unit.DEGREE;
        final PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        try {
            final CoordinateSystem cs;
            final GeographicCoordinateSystem gcs;
            gcs = factory.createGeographicCoordinateSystem(text, geoUnits, datum, meridian,
                                                    AxisInfo.LONGITUDE, AxisInfo.LATITUDE);
            if (isGeographic) {
                cs = gcs;
            } else {
                cs = factory.createProjectedCoordinateSystem(
                        text, gcs, getProjection(), units, AxisInfo.X, AxisInfo.Y);
            }
            cache(CACHE_KEY, cs);
            return cs;
        } catch (FactoryException exception) {
            final PropertyException e;
            e = new PropertyException(exception.getLocalizedMessage(), null, lastAlias);
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Convenience method returning the envelope
     * in geographic coordinate system using WGS
     * 1984 datum.
     *
     * @throws PropertyException if the operation failed. This exception
     *         may contains a {@link org.geotools.ct.TransformException} as its cause.
     *
     * @see #getEnvelope
     * @see #getGridRange
     */
    public synchronized Envelope getGeographicEnvelope() throws PropertyException {
        /*
         * First, checks if an Envelope object has already
         * been constructed. Since Envelope is mutable,
         * we need to clone it before to return it.
         */
        final String CACHE_KEY = "GeographicEnvelope";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof Envelope) {
                return (Envelope) ((Envelope) candidate).clone();
            }
        }
        /*
         * No Envelope is available in the cache.
         * Compute it now and cache it for future use.
         */
        Envelope         envelope = getEnvelope();
        CoordinateSystem sourceCS = getCoordinateSystem();
        CoordinateSystem targetCS = GeographicCoordinateSystem.WGS84;
        try {
            final CoordinateTransformationFactory factory = CoordinateTransformationFactory.getDefault();
            final CoordinateTransformation transformation = factory.createFromCoordinateSystems(sourceCS, targetCS);
            envelope = CTSUtilities.transform(transformation.getMathTransform(), envelope);
            cache(CACHE_KEY, envelope);
            return (Envelope) envelope.clone();
        } catch (TransformException exception) {
            throw new PropertyException(Resources.getResources(userLocale).
                      getString(ResourceKeys.ERROR_CANT_TRANSFORM_ENVELOPE), exception);
        }
    }
    
    /**
     * Returns the envelope. Default implementation construct an {@link org.geotools.pt.Envelope}
     * object using the values from the following keys:
     * <ul>
     *   <li>The horizontal limits with at least one of the following keys:
     *       {@link #X_MINIMUM} and/or {@link #X_MAXIMUM}. If one of those
     *       keys is missing, then {@link #X_RESOLUTION} is required.</li>
     *   <li>The vertical limits with at least one of the following keys:
     *       {@link #Y_MINIMUM} and/or {@link #Y_MAXIMUM}. If one of those
     *       keys is missing, then {@link #Y_RESOLUTION} is required.</li>
     * </ul>
     *
     * @throws MissingPropertyException if a required value is missing.
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getGridRange
     * @see #getGeographicEnvelope
     */
    public synchronized Envelope getEnvelope() throws PropertyException {
        /*
         * First, checks if an Envelope object has already
         * been constructed. Since Envelope is mutable,
         * we need to clone it before to return it.
         */
        final String CACHE_KEY = "Envelope";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof Envelope) {
                return (Envelope) ((Envelope) candidate).clone();
            }
        }
        /*
         * No Envelope is available in the cache.
         * Compute it now and cache it for future use.
         */
        final GridRange     range = getGridRange();
        final Envelope   envelope = new Envelope(range.getDimension());
        final CoordinateSystem cs = getCoordinateSystem();
        switch (envelope.getDimension()) {
            default: // TODO: What should we do with other dimensions? Open question...
            case 3: setRange(Z_MINIMUM, Z_MAXIMUM, Z_RESOLUTION, envelope, 2, range, cs); // fall t.
            case 2: setRange(Y_MINIMUM, Y_MAXIMUM, Y_RESOLUTION, envelope, 1, range, cs); // fall t.
            case 1: setRange(X_MINIMUM, X_MAXIMUM, X_RESOLUTION, envelope, 0, range, cs); // fall t.
            case 0: break;
        }
        cache(CACHE_KEY, envelope);
        return (Envelope) envelope.clone();
    }

    /**
     * Set the range for the specified dimension of an example. The range will be computed
     * from the "?Minimum" and "?Maximum" properties, if presents. If only one of those
     * property is present, the "?Resolution" property will be used.
     *
     * @param minKey    Property name for the minimal value.
     * @param maxKey    Property name for the maximal value.
     * @param resKey    Property name for the resolution.
     * @param envelope  The envelope to set.
     * @param dimension The dimension in the envelope to set.
     * @param gridRange The grid range.
     * @param cs        The coordinate system
     * @throws PropertyException if a property can't be set, or if an ambiguity has been found.
     */
    private void setRange(final Key minKey, final Key maxKey, final Key resKey,
                          final Envelope envelope,   final int dimension,
                          final GridRange gridRange, CoordinateSystem cs)
        throws PropertyException
    {
        assert Thread.holdsLock(this);
        cs = CTSUtilities.getSubCoordinateSystem(cs, dimension, dimension+1);
        if (!contains(resKey)) {
            envelope.setRange(dimension, getAsDouble(minKey, cs), getAsDouble(maxKey, cs));
            return;
        }
        final double resolution = getAsDouble(resKey, cs);
        final String lastAlias = this.lastAlias; // Protect from change
        final int range = gridRange.getLength(dimension);
        if (!contains(maxKey)) {
            final double min = getAsDouble(minKey, cs);
            envelope.setRange(dimension, min, min + resolution*range);
            return;
        }
        if (!contains(minKey)) {
            final double max = getAsDouble(maxKey, cs);
            envelope.setRange(dimension, max - resolution*range, max);
            return;
        }
        final double min = getAsDouble(minKey, cs);
        final double max = getAsDouble(maxKey, cs);
        envelope.setRange(dimension, min, max);
        if (Math.abs((min-max)/resolution - range) > EPS) {
            throw new AmbiguousPropertyException(Resources.getResources(userLocale).getString(
                      ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, resKey), resKey, lastAlias);
        }
    }
    
    /**
     * Returns the grid range. Default implementation fetchs the property values
     * for keys {@link #WIDTH} and {@link #HEIGHT}, and transform the resulting
     * strings into a {@link org.geotools.gc.GridRange} object.
     *
     * @throws MissingPropertyException if a required value is missing.
     * @throws PropertyException if the operation failed for some other reason.
     *
     * @see #getEnvelope
     * @see #getGeographicEnvelope
     */
    public synchronized GridRange getGridRange() throws PropertyException {
        /*
         * First, checks if a GridRange object has already
         * been constructed. Since GridRange is immutable,
         * it is safe to returns a single instance for all.
         */
        final String CACHE_KEY = "GridRange";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof GridRange) {
                return (GridRange) candidate;
            }
        }
        /*
         * No GridRange is available in the cache.
         * Compute it now and cache it for future use.
         */
        final int dimension = getCoordinateSystem().getDimension();
        final int[]   lower = new int[dimension];
        final int[]   upper = new int[dimension];
        Arrays.fill(upper, 1);
        switch (dimension) {
            default: // fall through
            case 3:  upper[2] = getAsInt(DEPTH );  // fall through
            case 2:  upper[1] = getAsInt(HEIGHT);  // fall through
            case 1:  upper[0] = getAsInt(WIDTH );  // fall through
            case 0:  break;
        }
        final GridRange range = new GridRange(lower, upper);
        cache(CACHE_KEY, range);
        return range;
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link org.geotools.gc.GridCoverage}
     * to be read. If sample dimensions are not know, then this method returns
     * <code>null</code>. The default implementation always returns <code>null</code>.
     *
     * @throws PropertyException if the operation failed.
     */
    public SampleDimension[] getSampleDimensions() throws PropertyException {
        return null;
    }
    
    /**
     * Sets the current {@link Locale} of this <code>PropertyParser</code>
     * to the given value. A value of <code>null</code> removes any previous
     * setting, and indicates that the parser should localize as it sees fit.
     *
     * Note: this is the locale to use for formatting error messages, not the
     *       locale to use for parsing the file. The locale for parsing is
     *       specified by {@link #getLocale}.
     */
    final synchronized void setUserLocale(final Locale locale) {
        userLocale = locale;
    }
    
    /**
     * List all properties to the specified stream. The default implementation list the
     * properties as <cite>key&nbsp;=&nbsp;value</cite> pairs. Each pair is formatted on
     * its own line, and the caracter <code>'='</code> is inserted between keys and values.
     * A question mark (<code>'?'</code>) is put in front of any unknow name (i.e. any name
     * not specified with {@link #addAlias}).
     *
     * @param  out Stream to write properties to.
     * @throws IOException if an error occured while listing properties.
     *
     * @see #add(GridCoverage)
     * @see #toString
     */
    public synchronized void listProperties(final Writer out) throws IOException {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String comments = (String) getProperty(null);
        if (comments != null) {
            int stop = comments.length();
            while (--stop>=0 && Character.isSpaceChar(comments.charAt(stop)));
            out.write(comments.substring(0, stop+1));
            out.write(lineSeparator);
            out.write(lineSeparator);
        }
        if (properties != null) {
            int maxLength = 1;
            for (final Iterator it=properties.keySet().iterator(); it.hasNext();) {
                final Object key = it.next();
                if (key != null) {
                    final int length = key.toString().length();
                    if (length > maxLength) maxLength = length;
                }
            }
            for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                final Key key = (Key) entry.getKey();
                if (key != null) {
                    Object value = entry.getValue();
                    if (value instanceof Number) {
                        value = getNumberFormat().format(value);
                    } else if (value instanceof Date) {
                        value = getDateFormat().format(value);
                    }
                    final boolean isKnow = (naming!=null && naming.containsKey(key));
                    out.write(isKnow ? "  " : "? ");
                    out.write(String.valueOf(key));
                    out.write(Utilities.spaces(maxLength-key.toString().length()));
                    out.write(separator);
                    out.write(String.valueOf(value));
                    out.write(lineSeparator);
                }
            }
        }
    }
    
    /**
     * Returns a string representation of this properties set. The default implementation
     * write the class name and the envelope in geographic coordinates, as returned by
     * {@link #getGeographicEnvelope}. Then, it append the list of all properties as formatted
     * by {@link #listProperties}.
     */
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringWriter  buffer = new StringWriter();
        buffer.write(Utilities.getShortClassName(this));
        if (source!=null) {
            buffer.write("[\"");
            buffer.write(source);
            buffer.write("\"]");
        }
        buffer.write(lineSeparator);
        try {
            final String     pattern = "DD°MM'SS\"";
            final Envelope  envelope = getGeographicEnvelope();
            final AngleFormat format = (userLocale!=null) ? new AngleFormat(pattern, userLocale) :
                                                            new AngleFormat(pattern);
            buffer.write(format.format(new  Latitude(envelope.getMaximum(1))));
            buffer.write(", ");
            buffer.write(format.format(new Longitude(envelope.getMinimum(0))));
            buffer.write(" - ");
            buffer.write(format.format(new  Latitude(envelope.getMinimum(1))));
            buffer.write(", ");
            buffer.write(format.format(new Longitude(envelope.getMaximum(0))));
            buffer.write(lineSeparator);
        } catch (PropertyException exception) {
            // Ignore.
        }
        buffer.write('{');
        buffer.write(lineSeparator);
        try {
            final TableWriter table = new TableWriter(buffer, 4);
            table.setMultiLinesCells(true);
            table.nextColumn();
            listProperties(table);
            table.flush();
        } catch (IOException exception) {
            buffer.write(exception.getLocalizedMessage());
        }
        buffer.write('}');
        buffer.write(lineSeparator);
        return buffer.toString();
    }

    /**
     * Trim a character string. Leading and trailing spaces are removed. Any succession of
     * one ore more unicode whitespace characters (as of {@link Character#isSpaceChar(char)}
     * are replaced by a single <code>'_'</code> character. Example:
     *
     *                       <pre>"This   is a   test"</pre>
     * will be returned as   <pre>"This_is_a_test"</pre>
     *
     * @param  str The string to trim (may be <code>null</code>).
     * @param  separator The separator to insert in place of succession of whitespaces.
     *         Usually "_" for keys and " " for values.
     * @return The trimed string, or <code>null</code> if <code>str</code> was null.
     */
    static String trim(String str, final String separator) {
        if (str != null) {
            str = str.trim();
            StringBuffer buffer = null;
loop:       for (int i=str.length(); --i>=0;) {
                if (Character.isSpaceChar(str.charAt(i))) {
                    final int upper = i;
                    do if (--i < 0) break loop;
                    while (Character.isSpaceChar(str.charAt(i)));
                    if (buffer == null) {
                        buffer = new StringBuffer(str);
                    }
                    buffer.replace(i+1, upper+1, separator);
                }
            }
            if (buffer != null) {
                return buffer.toString();
            }
        }
        return str;
    }

    /**
     * A key for fetching property in a format independent way. For example, the northern
     * limit of an image way be named <code>"Limit North"</code> is some metadata files,
     * and <code>"ULY"</code> (as <cite>Upper Left Y</cite>) in other metadata files. The
     * {@link PropertyParser#Y_MAXIMUM} allows to fetch this property without knowledge of
     * the actual name used in the underlying metadata file.
     * <br><br>
     * Keys are case-insensitive. Furthermore, trailing and leading spaces are ignored.
     * Any succession of one ore more unicode whitespace characters (as of
     * {@link java.lang.Character#isSpaceChar(char)} is understood as equal to a single
     * <code>'_'</code> character. For example, the key <code>"false&nbsp;&nbsp;easting"</code>
     * is considered equals to <code>"false_easting"</code>.
     *
     * @version $Id: PropertyParser.java,v 1.17 2003/11/12 14:13:52 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static class Key implements Serializable {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -6197070349689520675L;

        /**
         * The original name, as specified by the user.
         */
        private final String name;

        /**
         * The trimed name in lower case. This
         * is the key to use in comparaisons.
         */
        private final String key;

        /**
         * Construct a new key.
         *
         * @param name The key name.
         */
        public Key(String name) {
            name = name.trim();
            this.name = name;
            this.key  = trim(name, "_").toLowerCase();
        }

        /**
         * Returns the value for this key from the specified grid coverage.
         * For example the key {@link PropertyParser#X_MINIMUM} will returns
         * <code>coverage.getEnvelope().getMinimum(0)</code>.
         *
         * @param coverage The grid coverage from which to fetch the value.
         * @return The value, or <code>null</code> if none.
         */
        public Object getValue(final GridCoverage coverage) {
            return null;
        }

        /**
         * Returns the name for this key. This is the name supplied to the constructor
         * (i.e. case and whitespaces are preserved).
         */
        public String toString() {
            return name;
        }

        /**
         * Returns a hash code value.
         */
        public int hashCode() {
            return key.hashCode();
        }

        /**
         * Compare this key with the supplied key for equality. Comparaison is case-insensitive
         * and considere any sequence of whitespaces as a single <code>'_'</code> character, as
         * specified in this class documentation.
         */
        public boolean equals(final Object object) {
            return (object!=null) && object.getClass().equals(getClass()) &&
                    key.equals(((Key) object).key);
        }
    }

    /**
     * A key for properties derived from {@link Envelope} and/or {@link GridRange}.
     *
     * @version $Id: PropertyParser.java,v 1.17 2003/11/12 14:13:52 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class EnvelopeKey extends Key {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -7928870614384957795L;

        /*
         * BitMask  1 = Minimum value
         *          2 = Maximum value
         *          4 = Apply on Envelope
         *          8 = Apply on GridRange
         */
        /** Property for {@link Envelope#getLength}.  */ public static final byte LENGTH     = 4|0;
        /** Property for {@link Envelope#getMinimum}. */ public static final byte MINIMUM    = 4|1;
        /** Property for {@link Envelope#getMaximum}. */ public static final byte MAXIMUM    = 4|2;
        /** Property for {@link GridRange#getLength}. */ public static final byte SIZE       = 8|0;
        /** Property for {@link GridRange#getLower}.  */ public static final byte LOWER      = 8|1;
        /** Property for {@link GridRange#getUpper}.  */ public static final byte UPPER      = 8|2;
        /** Property for the resolution.              */ public static final byte RESOLUTION = 4|8;

        /**
         * The dimension from which to fetch the value.
         */
        private final byte dimension;

        /**
         * The method to use for fetching the value. Should be one of {@link #MINIMUM},
         * {@link #MAXIMUM}, {@link #LOWER}, {@link #UPPER} or  {@link #RESOLUTION}.
         */
        private final byte method;

        /**
         * Construct a key with the specified name.
         */
        public EnvelopeKey(final String name, final byte dimension, final byte method) {
            super(name);
            this.dimension = dimension;
            this.method    = method;
        }

        /**
         * Returns the value for this key from the specified grid coverage.
         */
        public Object getValue(final GridCoverage coverage) {
            Envelope envelope = null;
            GridRange   range = null;
            if ((method & 4) != 0) {
                envelope = coverage.getEnvelope();
                if (envelope==null || envelope.getDimension()<=dimension) {
                    return null;
                }
            }
            if ((method & 8) != 0) {
                range = coverage.getGridGeometry().getGridRange();
                if (range==null || range.getDimension()<=dimension) {
                    return null;
                }
            }
            switch (method) {
                default     : throw new AssertionError(method);
                case LENGTH : return new Double(        envelope.getLength (dimension));
                case MINIMUM: return getValue(coverage, envelope.getMinimum(dimension));
                case MAXIMUM: return getValue(coverage, envelope.getMaximum(dimension));
                case SIZE   : return new Integer(          range.getLength (dimension));
                case LOWER  : return new Integer(          range.getLower  (dimension));
                case UPPER  : return new Integer(          range.getUpper  (dimension));
                case RESOLUTION: {
                    return new Double(envelope.getLength(dimension)/range.getLength(dimension));
                }
            }
        }

        /**
         * Returns the specified value as a {@link Double} or {@link Date} object
         * according the coverage's coordinate system.
         */
        private Object getValue(final GridCoverage coverage, final double value) {
            CoordinateSystem cs = coverage.getCoordinateSystem();
            if (cs != null) {
                cs = CTSUtilities.getSubCoordinateSystem(cs, dimension, dimension+1);
                if (cs instanceof TemporalCoordinateSystem) {
                    final Date time = ((TemporalCoordinateSystem) cs).getEpoch();
                    time.setTime(time.getTime() +
                                 Math.round(Unit.MILLISECOND.convert(value, cs.getUnits(0))));
                    return time;
                }
            }
            return new Double(value);
        }

        /**
         * Compare this key with the supplied key for equality.
         */
        public boolean equals(final Object object) {
            if (super.equals(object)) {
                final EnvelopeKey that = (EnvelopeKey) object;
                return this.dimension == that.dimension &&
                       this.method    == that.method;
            }
            return false;
        }
    }

    /**
     * A key for properties derived from {@link Projection}.
     * The key name must be the projection parameter name.
     *
     * @version $Id: PropertyParser.java,v 1.17 2003/11/12 14:13:52 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class ProjectionKey extends Key {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -6913177345764406058L;

        /**
         * Construct a key with the specified name.
         */
        public ProjectionKey(final String name) {
            super(name);
        }

        /**
         * Returns the value for this key from the specified grid coverage.
         */
        public Object getValue(final GridCoverage coverage) {
            final Projection projection= CTSUtilities.getProjection(coverage.getCoordinateSystem());
            if (projection != null) try {
                return new Double(projection.getValue(toString()));
            } catch (MissingParameterException exception) {
                // No value set for the specified parameter.
                // This is not an error. Just ignore...
            }
            return null;
        }
    }

    /**
     * A case-insensitive key for alias name. We use a different class because the
     * <code>equals</code> method must returns <code>false</code> when comparing
     * <code>AliasKey</code> with ordinary <code>Key</code>s. This kind of key is
     * for internal use only.
     *
     * @version $Id: PropertyParser.java,v 1.17 2003/11/12 14:13:52 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class AliasKey extends Key {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 4546899841215386795L;

        /**
         * Construct a new key for an alias.
         */
        public AliasKey(final String name) {
            super(name);
        }

        /**
         * Returns a hash code value.
         */
        public int hashCode() {
            return ~super.hashCode();
        }
    }
}
