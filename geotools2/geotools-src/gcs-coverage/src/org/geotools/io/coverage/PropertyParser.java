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
import javax.media.jai.ParameterListDescriptor;

// Input/output
import java.io.*;
import java.net.URL;

// Formatting
import java.text.NumberFormat;
import java.text.ParseException;

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
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Miscellaneous
import java.util.Locale;
import java.awt.geom.Point2D;

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


/**
 * Helper class for creating OpenGIS's object from a set of properties. Properties are
 * <cite>key-value</cite> pairs, for example <code>"Units=meters"</code>. There is a wide
 * variety of ways to contruct OpenGIS's objects from <cite>key-value</cite> pairs, and
 * supporting them is not always straightforward. The <code>PropertyParser</code> class
 * try to make the work easier. It defines a set of format-neutral keys (i.e. keys not
 * related to any file format in particular). Before to parse a file, the mapping between
 * format-neutral keys and "real" keys used in a particuler file format <strong>must</strong>
 * be specified. This mapping is constructed with calls to {@link #addAlias}. For example,
 * one may wants to parse the following informations:
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
 * For example, the {@link #getCoordinateSystem} method constructs a {@link CoordinateSystem}
 * object using available informations. 
 *
 * @version $Id: PropertyParser.java,v 1.7 2002/08/26 16:26:13 desruisseaux Exp $
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
     *   <li>If the unit is compatible with {@link Unit#DEGREE}, then
     *       <code>getCoordinateSystem()</code> will usually returns
     *       a {@link GeographicCoordinateSystem}.</li>
     *   <li>Otherwise, if this unit is compatible with {@link Unit#METRE}, then
     *       <code>getCoordinateSystem()</code> will usually returns
     *       a {@link ProjectedCoordinateSystem}.</li>
     * </ul>
     *
     * @see #DATUM
     * @see #ELLIPSOID
     * @see #PROJECTION
     */
    public static final Key UNITS = new Key("Units");

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

    /**
     * Key for the coordinate system ellipsoid.
     * The {@link #getEllipsoid} method looks for this property.
     * Its return value is used by {@link #getProjection}.
     *
     * @see #UNITS
     * @see #DATUM
     * @see #PROJECTION
     */
    public static final Key ELLIPSOID = new Key("Ellipsoid");

    /**
     * Key for the projection classification. This is the classification name required
     * by {@link CoordinateSystemFactory#createProjection(CharSequence,String,ParameterList)
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
    public static final Key PROJECTION = new Key("Projection");

    /**
     * Optional key for the projection name. The {@link #getProjection} method looks for
     * this property, if presents. The projection name is used for documentation purpose
     * only. If it is not defined, then the projection name will be the same than the
     * classification name.
     *
     * @see #PROJECTION
     * @see #COORDINATE_SYSTEM_NAME
     */
    public static final Key PROJECTION_NAME = new Key("Projection name");

    /**
     * Optional Key for the coordinate system name.
     * The {@link #getCoordinateSystem} method looks for this property.
     *
     * @see #PROJECTION_NAME
     */
    public static final Key COORDINATE_SYSTEM_NAME = new Key("CoordinateSystem name");

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
    public static final Key SEMI_MAJOR = new Key("semi_major");

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
    public static final Key SEMI_MINOR = new Key("semi_minor");

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
    public static final Key LATITUDE_OF_ORIGIN = new Key("latitude_of_origin");

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
    public static final Key CENTRAL_MERIDIAN = new Key("central_meridian");

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
    public static final Key FALSE_EASTING = new Key("false_easting");

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
    public static final Key FALSE_NORTHING = new Key("false_northing");

    /**
     * Key for the minimal <var>x</var> value (western limit).
     * This is usually the longitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key X_MINIMUM = new Key("XMinimum");

    /**
     * Key for the maximal <var>y</var> value (northern limit).
     * This is usually the latitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key Y_MAXIMUM = new Key("YMaximum");

    /**
     * Key for the maximal <var>x</var> value (eastern limit).
     * This is usually the longitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MINIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key X_MAXIMUM = new Key("XMaximum");

    /**
     * Key for the minimal <var>y</var> value (southern limit).
     * This is usually the latitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key Y_MINIMUM = new Key("YMinimum");

    /**
     * Key for the resolution among the <var>x</var> axis.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #Y_RESOLUTION
     */
    public static final Key X_RESOLUTION = new Key("XResolution");

    /**
     * Key for the resolution among the <var>y</var> axis.
     * The {@link #getEnvelope} method looks for this property.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #WIDTH
     * @see #HEIGHT
     */
    public static final Key Y_RESOLUTION = new Key("YResolution");

    /**
     * Key for the image's width in pixels.
     * The {@link #getGridRange} method looks for this property.
     *
     * @see #HEIGHT
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key WIDTH = new Key("Width");

    /**
     * Key for the image's height in pixels.
     * The {@link #getGridRange} method looks for this property.
     *
     * @see #WIDTH
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key HEIGHT = new Key("Height");
    
    /**
     * The source (the file path or the URL) specified
     * during the last call to a <code>load(...)</code>
     * method.
     */
    private String source;
    
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
     * The locale to use for formatting messages,
     * or <code>null</code> for a default locale.
     */
    private Locale locale;
    
    /**
     * Construct a new <code>PropertyParser</code>
     * using the default {@link CoordinateSystemFactory}.
     */
    public PropertyParser() {
        this(CoordinateSystemFactory.getDefault());
    }
    
    /**
     * Construct a new <code>PropertyParser</code> using
     * the specified {@link CoordinateSystemFactory}.
     */
    public PropertyParser(final CoordinateSystemFactory factory) {
        this.factory = factory;
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
     * {@link #parseLine} for each non-empty line found in the stream.
     * Note that this method do not invokes {@link #clear} prior the loading.
     * Consequently, the loaded properties will be added to the set of existing
     * properties.
     *
     * @param  in The file to read until EOF.
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
     * {@link #parseLine} for each non-empty line found in the stream.
     * Note that this method do not invokes {@link #clear} prior the loading.
     * Consequently, the loaded properties will be added to the set of existing
     * properties.
     *
     * @param  in The URL to read until EOF.
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
     * {@link #parseLine} for each non-empty line found in the stream.
     * Note that this method do not invokes {@link #clear} prior the loading.
     * Consequently, the loaded properties will be added to the set of existing
     * properties.
     *
     * This method is not public because it doesn't set the {@link #source} field.
     *
     * @param in The stream to read until EOF. The stream will not be closed.
     * @throws IOException if an error occurs during loading.
     *
     * @see #clear()
     * @see #load(File)
     * @see #load(URL)
     * @see #parseLine
     */
    private void load(final BufferedReader in) throws IOException {
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
     * Parse a line and add the key-value pair to this property set.
     * The default implementation take the substring on the left size
     * of the first '=' character as the key, and the substring on the
     * right size of '=' as the value. For example, if <code>line</code>
     * has the following value:
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
        final int index = line.indexOf('=');
        if (index >= 0) {
            add(line.substring(0, index), line.substring(index+1));
            return true;
        }
        return false;
    }
    
    /**
     * Add all properties from the specified image.
     *
     * @param  Image with properties to add to this <code>PropertyParser</code>.
     * @throws AmbiguousPropertyException if a property is defined twice.
     *
     * @see #add(PropertySource,String)
     * @see #add(String,Object)
     */
    public synchronized void add(final RenderedImage image)
        throws AmbiguousPropertyException
    {
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
     *         {@link Image#UndefinedProperty}, then this method do nothing.
     * @throws AmbiguousPropertyException if a different value already exists for the specified
     *         alias, or for an other alias bound to the same {@link Key}.
     *
     * @see #add(RenderedImage)
     * @see #add(PropertySource,String)
     * @see #parseLine
     */
    public synchronized void add(String alias, Object value)
        throws AmbiguousPropertyException
    {
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
        final AliasKey aliasAsKey;
        if (alias!=null) {
            alias = alias.trim();
            aliasAsKey = new AliasKey(alias);
        }
        else aliasAsKey = null;
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
                throw new AmbiguousPropertyException(Resources.getResources(locale).
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
     * @param  The alias to add. This is the name actually used in the file to be parsed.
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
    public synchronized void addAlias(final Key key, String alias)
        throws AmbiguousPropertyException
    {
        alias = trim(alias.trim(), " ");
        final AliasKey aliasAsKey = new AliasKey(alias);
        final Object   property   = getProperty(aliasAsKey);
        if (property != null) {
            final Object value = getOptional(key); // Checks also alias
            if (value!=null && !value.equals(property)) {
                throw new AmbiguousPropertyException(Resources.getResources(locale).
                          getString(ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, alias),
                          key, alias);
            }
        }
        if (naming == null) {
            naming = new HashMap();
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
        throw new MissingPropertyException(Resources.getResources(locale).
                  getString(ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1, key), key, lastAlias);
    }
    
    /**
     * Returns a property as a <code>double</code> value. The default implementation
     * invokes <code>{@link #get get}(key)</code> and parse the resulting value with
     * {@link NumberFormat#parse(String)} for the {@link #getLocale} locale.
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
            throw new PropertyException(Resources.getResources(locale).getString(
                      ResourceKeys.ERROR_BAD_PARAMETER_$2, lastAlias, new Double(value)), key, lastAlias);
        }
        return integer;
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
     * Returns the locale to use when parsing property values as numbers. The default
     * implementation returns {@link Locale#US}, since it is the locale used in most
     * file formats.
     *
     * @return The locale to use for parsing property values.
     * @throws PropertyException if this information can't be fetched.
     *
     * @see #getAsDouble
     * @see #getAsInt
     */
    public Locale getLocale() throws PropertyException {
        return Locale.US;
    }
    
    /**
     * Returns the units. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #UNITS})</code>
     * and transform the resulting string into an {@link Unit} object.
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
     * and transform the resulting string into a {@link HorizontalDatum} object.
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
     * and transform the resulting string into an {@link Ellipsoid} object.
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
                Logger.getLogger("org.geotools.io.coverage").log(record);
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
     *       <code>{@linkplain CoordinateSystemFactory#createProjectionParameterList
     *       createProjectionParameterList}(classification)</code>.</li>
     *
     *   <li>Gets the property values for each parameters in the above {@link ParameterList}.
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
     *         system is not a {@link ProjectedCoordinateSystem}.
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
                throw new AmbiguousPropertyException(Resources.getResources(locale).
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
     *         may contains a {@link TransformException} as its cause.
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
            throw new PropertyException(Resources.getResources(locale).
                      getString(ResourceKeys.ERROR_CANT_TRANSFORM_ENVELOPE), exception);
        }
    }
    
    /**
     * Returns the envelope. Default implementation construct an {@link Envelope}
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
        final GridRange   range = getGridRange();
        final Envelope envelope = new Envelope(range.getDimension());
        switch (envelope.getDimension()) {
            default: // TODO: What should we do with other dimensions? Open question...
            case 2: setRange(Y_MINIMUM, Y_MAXIMUM, Y_RESOLUTION, envelope, 1, range); // fallthrough
            case 1: setRange(X_MINIMUM, X_MAXIMUM, X_RESOLUTION, envelope, 0, range); // fallthrough
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
     * @throws PropertyException if a property can't be set, or if an ambiguity has been found.
     */
    private void setRange(final Key minKey, final Key maxKey, final Key resKey,
                          final Envelope envelope, final int dimension, final GridRange gridRange)
        throws PropertyException
    {
        assert Thread.holdsLock(this);
        if (!contains(resKey)) {
            envelope.setRange(dimension, getAsDouble(minKey), getAsDouble(maxKey));
            return;
        }
        final double resolution = getAsDouble(resKey);
        final String lastAlias = this.lastAlias; // Protect from change
        final int range = gridRange.getLength(dimension);
        if (!contains(maxKey)) {
            final double min = getAsDouble(minKey);
            envelope.setRange(dimension, min, min + resolution*range);
            return;
        }
        if (!contains(minKey)) {
            final double max = getAsDouble(maxKey);
            envelope.setRange(dimension, max - resolution*range, max);
            return;
        }
        final double min = getAsDouble(minKey);
        final double max = getAsDouble(maxKey);
        envelope.setRange(dimension, min, max);
        if (Math.abs((min-max)/resolution - range) > EPS) {
            throw new AmbiguousPropertyException(Resources.getResources(locale).getString(
                      ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, resKey), resKey, lastAlias);
        }
    }
    
    /**
     * Returns the grid range. Default implementation fetchs the property values
     * for keys {@link #WIDTH} and {@link #HEIGHT}, and transform the resulting
     * strings into a {@link GridRange} object.
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
            case 2:  upper[1] = getAsInt(HEIGHT);  // fall through
            case 1:  upper[0] = getAsInt(WIDTH );  // fall through
            case 0:  break;
        }
        final GridRange range = new GridRange(lower, upper);
        cache(CACHE_KEY, range);
        return range;
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
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
     */
    final synchronized void setUserLocale(final Locale locale) {
        this.locale = locale;
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
                    final boolean isKnow = (naming!=null && naming.containsKey(key));
                    out.write(isKnow ? "  " : "? ");
                    out.write(String.valueOf(key));
                    out.write(Utilities.spaces(maxLength-key.toString().length()));
                    out.write(" = ");
                    out.write(String.valueOf(entry.getValue()));
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
            final AngleFormat format = (locale!=null) ? new AngleFormat(pattern, locale) :
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
     * {@link Character#isSpaceChar(char)} is understood as equal to a single
     * <code>'_'</code> character. For example, the key <code>"false&nbsp;&nbsp;easting"</code>
     * is considered equals to <code>"false_easting"</code>.
     *
     * @version $Id: PropertyParser.java,v 1.7 2002/08/26 16:26:13 desruisseaux Exp $
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
         * Compare this key with the supplied keys for equality. Comparaison is case-insensitive
         * and considere any sequence of whitespaces as a single <code>'_'</code> character, as
         * specified in this class documentation.
         */
        public boolean equals(final Object object) {
            return (object!=null) && object.getClass().equals(getClass()) &&
                    key.equals(((Key) object).key);
        }
    }

    /**
     * A case-insensitive key for alias name. We use a different class because the
     * <code>equals</code> method must returns <code>false</code> when comparing
     * <code>AliasKey</code> with ordinary <code>Key</code>s. This kind of key is
     * for internal use only.
     *
     * @version $Id: PropertyParser.java,v 1.7 2002/08/26 16:26:13 desruisseaux Exp $
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
