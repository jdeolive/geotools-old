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
import java.awt.image.RasterFormatException;

// Properties and parameters
import javax.media.jai.DeferredData;
import javax.media.jai.ParameterList;
import javax.media.jai.PropertySource;
import javax.media.jai.DeferredProperty;
import javax.media.jai.ParameterListDescriptor;

// Input/output
import java.io.*;
import java.net.URL;

// Collections
import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import javax.media.jai.util.CaselessStringKey;

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
import org.geotools.cv.CategoryList;

// Resources
import org.geotools.units.Unit;
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Helper class for creating OpenGIS's object from a set of properties.
 * For example, <code>PropertyParser</code> could be used for parsing
 * the following informations:
 *
 * <blockquote><pre>
 * ULX                = 217904.31
 * ULY                = 5663495.1
 * resolution         = 1000.0000
 * units              = meters
 * projection         = Mercator_1SP
 * central_meridian   = -15.2167
 * latitude_of_origin =  28.0667
 * false_easting      = 0.00000000
 * false_northing     = 0.00000000
 * Ellipsoid          = Clarke 1866
 * Datum              = Clarke 1866
 * </pre></blockquote>
 *
 * <code>PropertyParser</code> can build {@link Envelope} and {@link CoordinateSystem}
 * objects from those informations. This class is usually subclassed in order to fit
 * any particular file format. For example, if a file format use different key names
 * (for example <code>"Projection Name"</code> instead of <code>"Projection"</code>),
 * then a subclass could override {@link #get(String,Object)} in order to translate
 * on the fly <code>"Projection"</code> key name into <code>"Projection Name"</code>.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class PropertyParser {
    /**
     * Mapping between some commons projection names
     * and OpenGIS's projection class name.
     */
    private static final String[] PROJECTIONS= {
        "Mercator",            "Mercator_1SP",
        "Geographic (Lat/Lon)", null
    };
    
    /**
     * The source (the file path or the URL) specified
     * during the last call to a <code>load(...)</code>
     * method.
     */
    private String source;
    
    /**
     * The properties, or <code>null</code> if none.
     * This map will be constructed only when first
     * needed.
     */
    private Map properties;
    
    /**
     * The coordinate system, or <code>null</code> if it has not been constructed yet.
     * Coordinate system may be expensive to construct and requested many time; this
     * is why it should be cached the first time {@link #getCoordinateSystem} is invoked.
     */
    private transient CoordinateSystem coordinateSystem;
    
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
     * Clear this property set.
     */
    public synchronized void clear() {
        source           = null;
        properties       = null;
        coordinateSystem = null;
    }
    
    /**
     * Returns the source file name or URL. This is the path specified
     * during the last call to a <code>load(...)</code> method.
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Read all properties from a text file. Default implementation
     * invokes {@link #parseLine} for each non-empty line found in
     * the file.
     *
     * @param  in The file to read until EOF.
     * @throws IOException if an error occurs during loading.
     */
    public synchronized void load(final File header) throws IOException {
        source = header.getPath();
        final BufferedReader in = new BufferedReader(new FileReader(header));
        load(in);
        in.close();
    }
    
    /**
     * Read all properties from an URL. Default implementation
     * invokes {@link #parseLine} for each non-empty line found
     * in the file.
     *
     * @param  in The URL to read until EOF.
     * @throws IOException if an error occurs during loading.
     */
    public synchronized void load(final URL header) throws IOException {
        source = header.getPath();
        final BufferedReader in = new BufferedReader(new InputStreamReader(header.openStream()));
        load(in);
        in.close();
    }
    
    /**
     * Read all properties from a stream. Default implementation
     * invokes {@link #parseLine} for each non-empty line found
     * in the stream.
     *
     * @param in The stream to read until EOF. The stream will not be closed.
     * @throws IOException if an error occurs during loading.
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
        if (comments.length()!=0) {
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
     * {@link #add add}("Ellipsoid", "WGS 1984");
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
     * subclassed) until somebody consume it.
     *
     * @param  line The line to parse.
     * @return <code>true</code> if this method has consumed the line.
     * @throws RasterFormatException if the line is badly formatted,
     *         or if the line contains a property already stored.
     */
    protected boolean parseLine(final String line) throws RasterFormatException {
        final int index = line.indexOf('=');
        if (index>=0) {
            add(line.substring(0, index), line.substring(index+1));
            return true;
        }
        return false;
    }
    
    /**
     * Add all properties from the specified image.
     *
     * @param  Image with properties to add to this <code>PropertyParser</code>.
     * @throws RasterFormatException if a property is defined twice.
     */
    public synchronized void add(final RenderedImage image) throws RasterFormatException {
        if (image instanceof PropertySource) {
            add((PropertySource) image);
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
     * Add all properties from the specified property source.
     *
     * @param  properties Properties to add.
     * @throws RasterFormatException if a property is defined twice.
     */
    public synchronized void add(final PropertySource properties) throws RasterFormatException {
        add(properties, properties.getPropertyNames());
    }
    
    /**
     * Add all properties that beging with the supplied prefix.
     *
     * @param  properties The properties source.
     * @param  prefix The prefix for properties to add.
     * @throws RasterFormatException if a property is defined twice.
     */
    public synchronized void add(final PropertySource properties, final String prefix)
        throws RasterFormatException
    {
        add(properties, properties.getPropertyNames(prefix));
    }
    
    /**
     * Add a set of properties from the specified property source.
     *
     * @param  properties The properties source.
     * @param  names The list of property names to add.
     * @throws RasterFormatException if a property is defined twice.
     */
    private void add(final PropertySource properties, final String[] names)
        throws RasterFormatException
    {
        if (names!=null) {
            for (int i=0; i<names.length; i++) {
                final String name = names[i];
                add(name, new DeferredProperty(properties, name, properties.getPropertyClass(name)));
            }
        }
    }
    
    /**
     * Add a property for the specified key. Keys are case-insensitive.
     * Calling this method with an illegal key-value pair thrown an
     * {@link RasterFormatException} since properties are used for
     * holding raster informations.
     *
     * @param  key   The key for the property to add.
     * @param  value The value for the property to add.
     * @throws RasterFormatException if a different value
     *         already exists for the specified key.
     */
    public synchronized void add(String key, Object value) throws RasterFormatException {
        if (key!=null) {
            key=key.trim();
        }
        if (value==null || value==Image.UndefinedProperty) {
            return;
        }
        if (value instanceof CharSequence) {
            final String text = value.toString().trim();
            if (text.length()==0) return;
            value = text;
        }
        if (properties==null) {
            properties = new LinkedHashMap();
        }
        final CaselessStringKey caselessKey = (key!=null) ? new CaselessStringKey(key) : null;
        final String oldValue = (String) properties.get(caselessKey);
        if (oldValue != null && !oldValue.equals(value)) {
            throw new RasterFormatException(Resources.getResources(locale).
                        getString(ResourceKeys.ERROR_DUPLICATED_PROPERTY_$1, key));
        }
        properties.put(caselessKey, value);
    }
    
    /**
     * Returns the property for the specified key, or a
     * default value if the property was not found.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @param  The default value for <code>key</code>, or <code>null</code>.
     * @return Value for the specified key. Will be <code>null</code> only
     *         if the property was not found and <code>defaultValue</code>
     *         is <code>null</code>.
     */
    public synchronized Object get(String key, final Object defaultValue) {
        if (key!=null) {
            key = key.trim();
        }
        if (properties!=null) {
            final CaselessStringKey caselessKey = (key!=null) ? new CaselessStringKey(key) : null;
            Object value = properties.get(caselessKey);
            if (value instanceof DeferredData) {
                value = ((DeferredData) value).getData();
            }
            if (value!=null && value!=Image.UndefinedProperty) {
                return value;
            }
        }
        return defaultValue;
    }
    
    /**
     * Returns the property for the specified key. This implementation
     * invokes <code>{@link #get(String,Object) get}(key, null)</code>
     * and make sure that the resulting value is not null.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key (never <code>null</code>).
     * @throws NoSuchElementException if no value exists for the specified key.
     */
    public final Object get(final String key) throws NoSuchElementException {
        final Object value = get(key, null);
        if (value!=null && value!=Image.UndefinedProperty) {
            return value;
        }
        throw new NoSuchElementException(Resources.getResources(locale).
                getString(ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1, key));
    }
    
    /**
     * Returns a property as a <code>double</code> value. Default implementation
     * invokes <code>{@link #get(String) get}(key)</code> and parse the resulting
     * value as of {@link Double#parseDouble}.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key as a <code>double</code>.
     * @throws NoSuchElementException if no value exists for the specified key.
     * @throws NumberFormatException if the value can't be parsed as a <code>double</code>.
     */
    public double getAsDouble(final String key)
        throws NoSuchElementException, NumberFormatException
    {
        final Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    /**
     * Returns a property as a <code>int</code> value. Default implementation
     * invokes <code>{@link #get(String) get}(key)</code> and parse the resulting
     * value as of {@link Integer#parseInt}.
     *
     * @param  key The key of the desired property. Keys are case-insensitive.
     * @return Value for the specified key as an <code>int</code>.
     * @throws NoSuchElementException if no value exists for the specified key.
     * @throws NumberFormatException if the value can't be parsed as an <code>int</code>.
     */
    public int getAsInt(final String key)
        throws NoSuchElementException, NumberFormatException
    {
        final Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
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
     * Returns the units.  Default implementation fetchs the property
     * value for key <code>"Units"</code> and transform the resulting
     * string into an {@link Unit} object.
     *
     * @throws NoSuchElementException if no value exists for the "Units" key.
     */
    public synchronized Unit getUnits() throws NoSuchElementException {
        final Object value = get("Units");
        if (value instanceof Unit) {
            return (Unit) value;
        }
        final String text = value.toString();
        if (contains(text, new String[]{"meter","meters","metre","metres","m"})) {
            return Unit.METRE;
        } else if (contains(text, new String[]{"degree","degrees","deg","°"})) {
            return Unit.DEGREE;
        } else {
            throw new NoSuchElementException("Unknow unit: "+text);
        }
    }
    
    /**
     * Returns the datum.  Default implementation fetchs the property
     * value for key <code>"Datum"</code> and transform the resulting
     * string into a {@link HorizontalDatum} object.
     *
     * @throws NoSuchElementException if no value exists for the "Datum" key.
     */
    public synchronized HorizontalDatum getDatum() throws NoSuchElementException {
        final Object value = get("Datum");
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
     * Returns the ellipsoid.  Default implementation fetchs the property
     * value for key <code>"Ellipsoid"</code> and transform the resulting
     * string into an {@link Ellipsoid} object.
     *
     * @throws NoSuchElementException if no value exists for the "Ellipsoid" key.
     */
    public synchronized Ellipsoid getEllipsoid() throws NoSuchElementException {
        final Object value = get("Ellipsoid");
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
     */
    private static synchronized void checkEllipsoid(String text, final String source) {
        text = text.trim().replace('_', ' ');
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
                Logger.getLogger("org.geotools.gcs").log(record);
            }
        }
    }
    
    /** Temporary flag for {@link #checkEllipsoid}. */
    private static boolean emittedWarning;
    
    /**
     * Returns the projection. The default implementation fetchs the property value
     * for key <code>"Projection"</code>,  and then fetch the properties values for
     * every parameters required by the projection. If a parameter is not defined in
     * this <code>PropertyParser</code>, it will be left to its default value (which
     * is projection-dependent). Parameters are projection-dependent, but will typically
     * include
     *
     *     <code>"semi_major"</code>,
     *     <code>"semi_minor"</code>,
     *     <code>"central_meridian"</code>,
     *     <code>"latitude_of_origin"</code>,
     *     <code>"false_easting"</code> and
     *     <code>"false_northing"</code>.
     *
     * @return The projection, or <code>null</code> if the underlying coordinate
     *         system is not a {@link ProjectedCoordinateSystem}.
     * @throws NoSuchElementException if no value exists for the "Projection" key.
     * @throws NumberFormatException if a parameter value can't be parsed as a <code>double</code>.
     */
    public synchronized Projection getProjection() throws NoSuchElementException {
        final Object value = get("Projection");
        if (value instanceof Projection) {
            return (Projection) value;
        }
        final String text = value.toString();
        String classification = text;
        for (int i=PROJECTIONS.length; (i-=2)>=0;) {
            if (PROJECTIONS[i].equalsIgnoreCase(classification)) {
                classification = PROJECTIONS[i+1];
                break;
            }
        }
        if (classification==null) {
            return null;
        }
        boolean semiMajorAxisDefined = false;
        boolean semiMinorAxisDefined = false;
        ParameterList  parameters = factory.createProjectionParameterList(classification);
        final String[] paramNames = parameters.getParameterListDescriptor().getParamNames();
        for (int i=0; i<paramNames.length; i++) {
            final double paramValue;
            final String name = paramNames[i];
            try {
                paramValue = getAsDouble(name);
            } catch (NoSuchElementException exception) {
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
                throw new RasterFormatException(Resources.getResources(locale).
                        getString(ResourceKeys.ERROR_AMBIGIOUS_AXIS_LENGTH));
            }
            parameters = parameters.setParameter("semi_major", semiMajor)
                                   .setParameter("semi_minor", semiMinor);
        }
        try {
            return factory.createProjection(text, classification, parameters);
        } catch (FactoryException exception) {
            NoSuchElementException e = new NoSuchElementException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Returns the coordinate system. The default implementation construct a coordinate
     * system from the information provided by {@link #getUnits}, {@link #getDatum} and
     * {@link #getProjection}.
     *
     * @throws NoSuchElementException if a required value is missing (e.g. "Projection", "Datum", etc.).
     */
    public synchronized CoordinateSystem getCoordinateSystem() throws NoSuchElementException {
        if (coordinateSystem==null) {
            final Object value = get("CoordinateSystem", "Generated");
            if (value instanceof CoordinateSystem) {
                return (CoordinateSystem) value;
            }
            final String            text = value.toString();
            final Unit             units = getUnits();
            final HorizontalDatum  datum = getDatum();
            final boolean   isGeographic = Unit.DEGREE.canConvert(units);
            final Unit          geoUnits = isGeographic ? units : Unit.DEGREE;
            final PrimeMeridian meridian = PrimeMeridian.GREENWICH;
            final GeographicCoordinateSystem gcs;
            try {
                gcs = factory.createGeographicCoordinateSystem(text, geoUnits, datum, meridian,
                                                        AxisInfo.LONGITUDE, AxisInfo.LATITUDE);
                if (isGeographic) {
                    coordinateSystem = gcs;
                } else {
                    coordinateSystem = factory.createProjectedCoordinateSystem(
                            text, gcs, getProjection(), units, AxisInfo.X, AxisInfo.Y);
                }
            } catch (FactoryException exception) {
                NoSuchElementException e = new NoSuchElementException(exception.getLocalizedMessage());
                e.initCause(exception);
                throw e;
            }
        }
        return coordinateSystem;
    }
    
    /**
     * Convenience method returning the envelope
     * in geographic coordinate system using WGS
     * 1984 datum.
     *
     * @throws NoSuchElementException if the operation failed.
     */
    public synchronized Envelope getGeographicEnvelope() throws NoSuchElementException {
        final Envelope         envelope = getEnvelope();
        final CoordinateSystem sourceCS = getCoordinateSystem();
        final CoordinateSystem targetCS = GeographicCoordinateSystem.WGS84;
        try {
            final CoordinateTransformationFactory factory = CoordinateTransformationFactory.getDefault();
            final CoordinateTransformation transformation = factory.createFromCoordinateSystems(sourceCS, targetCS);
            return CTSUtilities.transform(transformation.getMathTransform(), envelope);
        } catch (TransformException exception) {
            NoSuchElementException e = new NoSuchElementException(Resources.getResources(locale).
                                           getString(ResourceKeys.ERROR_CANT_TRANSFORM_ENVELOPE));
            e.initCause(exception);
            throw e;
        }
    }
    
    /**
     * Returns the envelope. Default implementation fetchs the property values
     * for keys <code>"ULX"</code>, <code>"ULY"</code>, <code>"Resolution"</code>
     * and transform the resulting strings into an {@link Envelope} object.
     * <br><br>
     * <STRONG>DO NOT RELY ON THE CURRENT IMPLEMENTATION</STRONG>.
     * This default implementation may be changed in a future version.
     * Always override this method if you want to be safe.
     *
     * @throws NoSuchElementException if a required value is missing.
     */
    public synchronized Envelope getEnvelope() throws NoSuchElementException {
        final double x = getAsDouble("ULX");
        final double y = getAsDouble("ULY");
        final double r = getAsDouble("Resolution");
        final GridRange range = getGridRange();
        final int   dimension = range.getDimension();
        final double[]    min = new double[dimension];
        final double[]    max = new double[dimension];
        min[0] = x; min[1] = y - r*range.getLength(1);
        max[1] = y; max[0] = x + r*range.getLength(0);
        /*
         * TODO: What should we do with other dimensions?
         *       Open question...
         */
        return new Envelope(min, max);
    }
    
    /**
     * Returns the grid range. Default implementation fetchs the property values
     * for keys <code>"Image width"</code> and <code>"Image height"</code>,
     * and transform the resulting strings into a {@link GridRange} object.
     * <br><br>
     * <STRONG>DO NOT RELY ON THE CURRENT IMPLEMENTATION</STRONG>.
     * This default implementation may be changed in a future version.
     * Always override this method if you want to be safe.
     *
     * @throws NoSuchElementException if a required value is missing.
     */
    public synchronized GridRange getGridRange() throws NoSuchElementException {
        final int dimension = getCoordinateSystem().getDimension();
        final int[]   lower = new int[dimension];
        final int[]   upper = new int[dimension];
        Arrays.fill(upper, 1);
        upper[0] = getAsInt("Image width" );
        upper[1] = getAsInt("Image height");
        return new GridRange(lower, upper);
    }
    
    /**
     * Returns the category lists for each band of the {@link GridCoverage}
     * to be read. If there is no category lists, then this method returns
     * <code>null</code>. The default implementation always returns <code>null</code>.
     */
    public CategoryList[] getCategoryLists() {
        return null;
    }
    
    /**
     * Tells if pixel values map directly geophysics values. This method
     * Returns <code>true</code> if pixel values map directly geophysics
     * values, or <code>false</code> if they must be translated first
     * using {@link CategoryList}. The default implementation returns
     * <code>true</code>.
     */
    public boolean isGeophysics() {
        return true;
    }
    
    /**
     * Sets the current {@link Locale} of this <code>PropertyParser</code>
     * to the given value. A value of <code>null</code> removes any previous
     * setting, and indicates that the parser should localize as it sees fit.
     */
    final void setLocale(final Locale locale) {
        this.locale = locale;
    }
    
    /**
     * List all properties to the specified stream.
     *
     * @param  out Stream to write properties to.
     * @throws IOException if an error occured while listing properties.
     */
    public void listProperties(final Writer out) throws IOException {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String comments = (String) get(null,null);
        if (comments!=null) {
            int stop = comments.length();
            while (--stop>=0 && Character.isSpaceChar(comments.charAt(stop)));
            out.write(comments.substring(0, stop+1));
            out.write(lineSeparator);
            out.write(lineSeparator);
        }
        int maxLength = 1;
        for (final Iterator it=properties.keySet().iterator(); it.hasNext();) {
            final Object key = it.next();
            if (key!=null) {
                final int length = key.toString().length();
                if (length > maxLength) maxLength = length;
            }
        }
        for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final Object key = entry.getKey();
            if (key!=null) {
                out.write(String.valueOf(key));
                out.write(Utilities.spaces(maxLength-key.toString().length()));
                out.write(" = ");
                out.write(String.valueOf(entry.getValue()));
                out.write(lineSeparator);
            }
        }
    }
    
    /**
     * Returns a string representation of this properties set.
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
        } catch (RuntimeException exception) {
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
}
