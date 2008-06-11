/*
 * GeoTools - The Open Source Java GIS Tookit
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.coverage.io;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.units.Unit;
import javax.units.SI;
import javax.units.NonSI;

import javax.imageio.IIOException;
import javax.media.jai.DeferredData;
import javax.media.jai.DeferredProperty;
import javax.media.jai.PropertySource;

import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.geometry.Envelope;
import org.opengis.util.Cloneable;

import org.geotools.io.TableWriter;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.geotools.referencing.wkt.UnformattableObjectException;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.geometry.GeneralEnvelope;


/**
 * Helper class for creating OpenGIS's object from a set of metadata. Metadata are
 * <cite>key-value</cite> pairs, for example {@code "Units=meters"}. There is a wide
 * variety of ways to contruct OpenGIS's objects from <cite>key-value</cite> pairs, and
 * supporting them is not always straightforward. The {@code MetadataBuilder} class
 * tries to make the work easier. It defines a set of format-neutral keys (i.e. keys not
 * related to any specific file format). Before parsing a file, the mapping between
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
 * Before to be used for parsing such informations, a {@code MetadataBuilder} object
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
 * Once the mapping is etablished, {@code MetadataBuilder} provides a set of {@code getXXX()}
 * methods for constructing various objects from those informations. For example, the
 * {@link #getCoordinateReferenceSystem} method constructs a {@link CoordinateReferenceSystem}
 * object using available informations.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 *
 * @since 2.2
 */
public class MetadataBuilder {
    /**
     * Set of commonly used symbols for "metres".
     *
     * @todo Needs a more general way to set unit symbols once the Unit API is completed.
     */
    private static final String[] METRES = {
        "meter", "meters", "metre", "metres", "m"
    };

    /**
     * Set of commonly used symbols for "degrees".
     *
     * @todo Needs a more general way to set unit symbols once the Unit API is completed.
     */
    private static final String[] DEGREES = {
        "degree", "degrees", "deg", "°"
    };

    /**
     * Small tolerance factor when checking metadata for consistency.
     */
    private static final double EPS = 1E-6;

    /**
     * Key for the {@linkplain CoordinateReferenceSystem coordinate reference system}.
     * The {@link #getCoordinateReferenceSystem} method looks for this metadata.
     *
     * @see #UNITS
     * @see #DATUM
     * @see #PROJECTION
     */
    public static final Key<CoordinateReferenceSystem> COORDINATE_REFERENCE_SYSTEM =
            new Key<CoordinateReferenceSystem>("CoordinateReferenceSystem")
    {
        @Override
        public CoordinateReferenceSystem getValue(final GridCoverage coverage) {
            return coverage.getCoordinateReferenceSystem();
        }
    };

    /**
     * Key for the {@linkplain CoordinateSystemAxis coordinate system axis} units.
     * The {@link #getUnit} method looks for this metadata. The following heuristic
     * rule may be applied in order to infer the CRS from the units:
     * <p>
     * <ul>
     *   <li>If the unit is compatible with {@linkplain NonSI#DEGREE_ANGLE degrees},
     *       then a {@linkplain GeographicCRS geographic CRS} is assumed.</li>
     *   <li>Otherwise, if this unit is compatible with {@linkplain SI#METER metres},
     *       then a {@linkplain ProjectedCRS projected CRS} is assumed.</li>
     * </ul>
     *
     * @see #ELLIPSOID
     * @see #DATUM
     * @see #PROJECTION
     * @see #COORDINATE_REFERENCE_SYSTEM
     */
    public static final Key<Unit> UNITS = new Key<Unit>("Unit") {
        @Override
        public Unit getValue(final GridCoverage coverage) {
            Unit unit = null;
            final CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
            if (crs != null) {
                final CoordinateSystem cs = crs.getCoordinateSystem();
                if (cs != null) {
                    for (int i=cs.getDimension(); --i>=0;) {
                        final Unit candidate = cs.getAxis(i).getUnit();
                        if (candidate != null) {
                            if (unit == null) {
                                unit = candidate;
                            } else if (!unit.equals(candidate)) {
                                return null;
                            }
                        }
                    }
                }
            }
            return unit;
        }
    };

    /**
     * Key for the coordinate reference system's {@linkplain Datum datum}.
     * The {@link #getGeodeticDatum} method looks for this metadata.
     *
     * @see #UNITS
     * @see #ELLIPSOID
     * @see #PROJECTION
     * @see #COORDINATE_REFERENCE_SYSTEM
     */
    public static final Key<Datum> DATUM = new Key<Datum>("Datum") {
        @Override
        public Datum getValue(final GridCoverage coverage) {
            return CRSUtilities.getDatum(coverage.getCoordinateReferenceSystem());
        }
    };

    /**
     * Key for the coordinate reference system {@linkplain Ellipsoid ellipsoid}.
     * The {@link #getEllipsoid} method looks for this metadata.
     *
     * @see #UNITS
     * @see #DATUM
     * @see #PROJECTION
     * @see #COORDINATE_REFERENCE_SYSTEM
     */
    public static final Key<Ellipsoid> ELLIPSOID = new Key<Ellipsoid>("Ellipsoid") {
        @Override
        public Ellipsoid getValue(final GridCoverage coverage) {
            return CRS.getEllipsoid(coverage.getCoordinateReferenceSystem());
        }
    };

    /**
     * Key for the {@linkplain OperationMethod operation method}. The {@link #getProjection}
     * method looks for this metadata. The operation method name determines the {@linkplain
     * MathTransformFactory#getDefaultParameters math transform implementation and its list
     * of parameters}. This name is the projection <cite>classification</cite>.
     * <p>
     * If this metadata is not defined, then the operation name is inferred from the
     * {@linkplain #PROJECTION projection name}.
     *
     * @see #PROJECTION
     * @see #COORDINATE_REFERENCE_SYSTEM
     */
    public static final Key<String> OPERATION_METHOD = new Key<String>("OperationMethod") {
        @Override
        public String getValue(final GridCoverage coverage) {
            final Projection projection = (Projection) PROJECTION.getValue(coverage);
            return (projection!=null) ? projection.getName().getCode() : null;
        }
    };

    /**
     * Key for the {@linkplain Projection projection}. The {@link #getProjection} method looks
     * for this metadata. If the metadata is not defined, then the projection name is assumed
     * the same than the {@linkplain #OPERATION_METHOD operation method} name.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     */
    public static final Key<Projection> PROJECTION = new Key<Projection>("Projection") {
        @Override
        public Projection getValue(final GridCoverage coverage) {
            final ProjectedCRS crs;
            crs = CRS.getProjectedCRS(coverage.getCoordinateReferenceSystem());
            return (crs!=null) ? crs.getConversionFromBase() : null;
        }
    };

    /**
     * Key for the {@code "semi_major"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key<Number> SEMI_MAJOR = new ProjectionKey("semi_major");

    /**
     * Key for the {@code "semi_minor"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key<Number> SEMI_MINOR = new ProjectionKey("semi_minor");

    /**
     * Key for the {@code "latitude_of_origin"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key<Number> LATITUDE_OF_ORIGIN = new ProjectionKey("latitude_of_origin");

    /**
     * Key for the {@code "central_meridian"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key<Number> CENTRAL_MERIDIAN = new ProjectionKey("central_meridian");

    /**
     * Key for the {@code "false_easting"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_NORTHING
     * @see #PROJECTION
     */
    public static final Key<Number> FALSE_EASTING = new ProjectionKey("false_easting");

    /**
     * Key for the {@code "false_northing"} projection parameter. There is no specific method
     * for this key. However, this key may be queried indirectly by {@link #getProjection}.
     *
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #PROJECTION
     */
    public static final Key<Number> FALSE_NORTHING = new ProjectionKey("false_northing");

    /**
     * Key for the minimal <var>x</var> value (western limit).
     * This is usually the longitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this metadata in order to set the
     * {@linkplain Envelope#getMinimum minimal coordinate} for dimension <strong>0</strong>.
     *
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> X_MINIMUM = new EnvelopeKey("XMinimum",
            (byte)0, EnvelopeKey.MINIMUM);

    /**
     * Key for the minimal <var>y</var> value (southern limit).
     * This is usually the latitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this metadata. in order to set the
     * {@linkplain Envelope#getMinimum minimal coordinate} for dimension <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> Y_MINIMUM = new EnvelopeKey("YMinimum",
            (byte)1, EnvelopeKey.MINIMUM);

    /**
     * Key for the minimal <var>z</var> value. This is usually the minimal altitude.
     * The {@link #getEnvelope} method looks for this metadata in order to set the
     * {@linkplain Envelope#getMinimum minimal coordinate} for dimension <strong>2</strong>.
     *
     * @see #Z_MAXIMUM
     * @see #Z_RESOLUTION
     * @see #DEPTH
     */
    public static final Key<Comparable<?>> Z_MINIMUM = new EnvelopeKey("ZMinimum",
            (byte)2, EnvelopeKey.MINIMUM);

    /**
     * Key for the maximal <var>x</var> value (eastern limit).
     * This is usually the longitude coordinate of the <em>bottom right</em> corner.
     * The {@link #getEnvelope} method looks for this metadata in order to set the
     * {@linkplain Envelope#getMaximum maximal coordinate} for dimension <strong>0</strong>.
     *
     * @see #X_MINIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> X_MAXIMUM = new EnvelopeKey("XMaximum",
            (byte)0, EnvelopeKey.MAXIMUM);

    /**
     * Key for the maximal <var>y</var> value (northern limit).
     * This is usually the latitude coordinate of the <em>upper left</em> corner.
     * The {@link #getEnvelope} method looks for this metadata in order to set the
     * {@linkplain Envelope#getMaximum maximal coordinate} for dimension <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> Y_MAXIMUM = new EnvelopeKey("YMaximum",
            (byte)1, EnvelopeKey.MAXIMUM);

    /**
     * Key for the maximal <var>z</var> value. This is usually the maximal altitude.
     * The {@link #getEnvelope} method looks for this metadata in order to set the
     * {@linkplain Envelope#getMaximum maximal coordinate} for dimension <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_RESOLUTION
     * @see #DEPTH
     */
    public static final Key<Comparable<?>> Z_MAXIMUM = new EnvelopeKey("ZMaximum", (byte)2,
            EnvelopeKey.MAXIMUM);

    /**
     * Key for the resolution among the <var>x</var> axis. The {@link #getEnvelope} method looks
     * for this metadata in order to infer the coordinates for dimension <strong>0</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> X_RESOLUTION = new EnvelopeKey("XResolution",
            (byte)0, EnvelopeKey.RESOLUTION);

    /**
     * Key for the resolution among the <var>y</var> axis. The {@link #getEnvelope} method looks
     * for this metadata in order to infer the coordinates for dimension <strong>1</strong>.
     *
     * @see #X_MINIMUM
     * @see #X_MAXIMUM
     * @see #Y_MINIMUM
     * @see #Y_MAXIMUM
     * @see #X_RESOLUTION
     * @see #WIDTH
     * @see #HEIGHT
     */
    public static final Key<Comparable<?>> Y_RESOLUTION = new EnvelopeKey("YResolution",
            (byte)1, EnvelopeKey.RESOLUTION);

    /**
     * Key for the resolution among the <var>z</var> axis. The {@link #getEnvelope} method looks
     * for this metadata in order to infer the coordinates for dimension <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_MAXIMUM
     * @see #DEPTH
     */
    public static final Key<Comparable<?>> Z_RESOLUTION = new EnvelopeKey("ZResolution",
            (byte)2, EnvelopeKey.RESOLUTION);

    /**
     * Key for the image's width in pixels. The {@link #getGridRange} method looks for this
     * metadata in order to infer the {@linkplain GridRange#getLength grid size} along the
     * dimension <strong>0</strong>.
     *
     * @see #HEIGHT
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> WIDTH = new EnvelopeKey("Width",
            (byte)0, EnvelopeKey.SIZE);

    /**
     * Key for the image's height in pixels. The {@link #getGridRange} method looks for this
     * metadata in order to infer the {@linkplain GridRange#getLength grid size} along the
     * dimension <strong>1</strong>.
     *
     * @see #WIDTH
     * @see #X_RESOLUTION
     * @see #Y_RESOLUTION
     */
    public static final Key<Comparable<?>> HEIGHT = new EnvelopeKey("Height",
            (byte)1, EnvelopeKey.SIZE);

    /**
     * Key for the image's "depth" in pixels. This metadata may exists for 3D images,
     * but some implementations accept at most 1 pixel depth among the third dimension.
     * The {@link #getGridRange} method looks for this metadata in order to infer the
     * {@linkplain GridRange#getLength grid size} along the dimension <strong>2</strong>.
     *
     * @see #Z_MINIMUM
     * @see #Z_MAXIMUM
     * @see #Z_RESOLUTION
     */
    public static final Key<Comparable<?>> DEPTH = new EnvelopeKey("Depth",
            (byte)2, EnvelopeKey.SIZE);

    /**
     * The source (the file path or the URL) specified during the last call to a {@code load(...)}
     * method.
     *
     * @see #load(File)
     * @see #load(URL)
     * @see #load(BufferedReader)
     */
    private String source;

    /**
     * The symbol to use as a separator. The full version ({@code separator}) will be used for
     * formatting with {@link #listMetadata}, while the trimed version ({@code trimSeparator})
     * will be used for parsing with {@link #parseLine}.
     *
     * @see #getSeparator
     * @see #setSeparator
     */
    private String separator = " = ", trimSeparator = "=";

    /**
     * The non-localized pattern for formatting numbers (as floating point or as integer)
     * and dates. If {@code null}, then the default pattern is used.
     */
    private String numberPattern, datePattern;

    /**
     * The metadata, or {@code null} if none. Keys are the caseless metadata names
     * and values are arbitrary objects (usually {@link String}s). This map will be
     * constructed only when first needed.
     */
    private Map<Key<?>,Object> metadata;

    /**
     * The mapping between keys and alias, or {@code null} if there is no alias.
     * This mapping is used for two purpose:
     * <ul>
     *   <li>If the key is a {@link Key} object, then the value is the set of alias (as
     *       {@code AliasKey} objects) for this key. This set is used by {@code getXXX()}
     *       methods.</li>
     *   <li>If the key is an {@code AliasKey} object, then the value if the set of {@link Key}
     *       which have this alias. This set is used by {@code add(...)} methods in order to check
     *       for ambiguity when adding a new metadata.</li>
     * </ul>
     */
    private Map<Key<?>, Set<Key<?>>> naming;

    /**
     * The alias used in the last {@link #getOptional} invocation. This field is for information
     * purpose only. It is used when constructing an exception for an operation failure.
     */
    private transient String lastAlias;

    /**
     * Map of objects already created. Some objects may be expensive to construct and required
     * many times. For example, {@link #getCoordinateReferenceSystem} is required by some other
     * methods like {@link #getRange}. Caching objects after their construction allow for faster
     * execution. Keys are object names (e.g. "CoordinateReferenceSystem"), and value are the
     * actual objects.
     */
    private transient Map<String,Object> cache;

    /**
     * The factories to use for constructing ellipsoids, projections, coordinate reference systems...
     */
    private final ReferencingFactoryContainer factories;

    /**
     * The locale to use for formatting messages, or {@code null} for a default locale.
     * This is <strong>not</strong> the local to use for parsing the file. This later locale
     * is specified by {@link #getLocale}.
     */
    private Locale userLocale;

    /**
     * Constructs a new {@code MetadataBuilder} using default factories.
     */
    public MetadataBuilder() {
        this(ReferencingFactoryContainer.instance(null));
    }

    /**
     * Constructs a new {@code MetadataBuilder} using the specified factories.
     */
    public MetadataBuilder(final ReferencingFactoryContainer factories) {
        this.factories = factories;
    }

    /**
     * Returns the characters to use as separator between keys and values. Leading and trailing
     * spaces will be keept when formatting with {@link #listMetadata}, but will be ignored
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
     * The type should be either {@code Number.class} or {@code Date.class}.
     * <p>
     * <ul>
     *   <li>if {@code type} is assignable to {@code Number.class}, then this method
     *       returns the number pattern as specified by {@link DecimalFormat}.</li>
     *   <li>Otherwise, if {@code type} is assignable to {@code Date.class}, then this method
     *       returns the date pattern as specified by {@link SimpleDateFormat}.</li>
     * </ul>
     * <p>
     * In any case, this method returns {@code null} if this object should use the default
     * pattern for the {@linkplain #getLocale data locale}.
     *
     * @param  type The data type ({@code Number.class} or {@code Date.class}).
     * @return The format pattern for the specified data type, or {@code null} for
     *         the default locale-dependent pattern.
     * @throws IllegalArgumentException if {@code type} is not valid.
     */
    public String getFormatPattern(final Class<?> type) {
        if (Date.class.isAssignableFrom(type)) {
            return datePattern;
        }
        if (Number.class.isAssignableFrom(type)) {
            return numberPattern;
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, type));
    }

    /**
     * Set the pattern to use for parsing and formatting values of the specified type.
     * The type should be either {@code Number.class} or {@code Date.class}.
     *
     * <ul>
     *   <li>If {@code type} is assignable to <code>{@linkplain java.lang.Number}.class</code>,
     *       then {@code pattern} should be a {@link DecimalFormat} pattern (example:
     *       {@code "#0.###"}).</li>
     *   <li>If {@code type} is assignable to <code>{@linkplain Date}.class</code>,
     *       then {@code pattern} should be a {@link SimpleDateFormat} pattern
     *       (example: {@code "yyyy/MM/dd HH:mm"}).</li>
     * </ul>
     *
     * @param  type The data type ({@code Number.class} or {@code Date.class}).
     * @param  pattern The format pattern for the specified data type, or {@code null}
     *         for the default locale-dependent pattern.
     * @throws IllegalArgumentException if {@code type} is not valid.
     */
    public synchronized void setFormatPattern(final Class<?> type, final String pattern) {
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
        throw new IllegalArgumentException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, type));
    }

    /**
     * Clears this metadata set. If the same {@code MetadataBuilder} object is used for parsing
     * many files, then {@code clear()} should be invoked prior any {@code load(...)} method.
     * Note that {@code clear()} do not remove any alias, so this {@code MetadataBuilder} can
     * been immediately reused for parsing new files of the same kind.
     */
    public synchronized void clear() {
        source   = null;
        metadata = null;
        cache    = null;
    }

    /**
     * Reads all metadata from a text file. The default implementation invokes
     * {@link #load(BufferedReader)}. Note that this method do not invokes {@link #clear}
     * prior the loading. Consequently, the loaded metadata will be added to the set of
     * existing metadata.
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
     * Reads all metadata from an URL. The default implementation invokes
     * {@link #load(BufferedReader)}. Note that this method do not invokes {@link #clear}
     * prior the loading. Consequently, the loaded metadata will be added to the set of
     * existing metadata.
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
     * Reads all metadata from a stream. The default implementation invokes
     * {@link #parseLine} for each non-empty line found in the stream. Notes:
     * <p>
     * <ul>
     *   <li>This method is not public because it has no way to know how
     *       to set the {@link #getSource source} metadata.</li>
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
        assert Thread.holdsLock(this);
        final Set<String> previousComments = new HashSet<String>();
        final StringBuilder comments = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator", "\n");
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
     * Parses a line and add the key-value pair to this metadata set. The default implementation
     * takes the substring on the left side of the first occurence of the {@linkplain #getSeparator
     * separator} (usually the '=' character) as the key, and the substring on the right side of
     * the separator as the value. For example, if {@code line} has the following value:
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
     * This method returns {@code true} if it has consumed the line, or {@code false} otherwise.
     * A line is "consumed" if {@code parseLine(...)} has either added the key-value pair (using
     * {@link #add}), or determined that the line must be ignored (for example because
     * {@code parseLine(...)} detected a character announcing a comment line). A "consumed" line
     * will not receive any further treatment. The line is not consumed (i.e. this method returns
     * {@code false}) if {@code parseLine(...)} don't know what to do with it. Non-consumed line
     * will typically go up in a chain of {@code parseLine(...)} methods (if {@code MetadataBuilder}
     * has been subclassed) until someone consume it.
     *
     * @param  line The line to parse.
     * @return {@code true} if this method has consumed the line.
     * @throws IIOException if the line is badly formatted.
     * @throws AmbiguousMetadataException if a different value was already defined for the same
     *         metadata name.
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
     * Add all metadata from the specified grid coverage. This method can be used together with
     * {@link #listMetadata} as a way to format the metadata for an arbitrary grid coverage.
     * The default implementation performs the following step:
     * <p>
     * <ul>
     *   <li>For each {@code key} declared with
     *       <code>{@linkplain #addAlias addAlias}(<strong>key</strong>, alias)</code>, fetchs
     *       a value with <code>key.{linkplain Key#getValue getValue}(coverage)</code>.</li>
     *   <li>For each value found, {@linkplain #add(String, Object) add} the value under the
     *       name of the first alias found for the {@code key}.</li>
     *
     * @param coverage The grid coverage with metadata to add to this {@code MetadataBuilder}.
     * @throws AmbiguousMetadataException if a metadata is defined twice.
     *
     * @see #add(RenderedImage)
     * @see #add(PropertySource,String)
     * @see #add(String,Object)
     * @see #listMetadata
     */
    public synchronized void add(final GridCoverage coverage) throws AmbiguousMetadataException {
        if (naming == null) {
            return;
        }
        for (final Map.Entry<Key<?>, Set<Key<?>>> entry : naming.entrySet()) {
            final Key<?> key = entry.getKey();
            if (key instanceof AliasKey) {
                continue;
            }
            final Set<Key<?>> alias = entry.getValue();
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
     * Add all metadata from the specified image.
     *
     * @param  image The image with metadata to add to this {@code MetadataBuilder}.
     * @throws AmbiguousMetadataException if a metadata is defined twice.
     *
     * @see #add(GridCoverage)
     * @see #add(PropertySource,String)
     * @see #add(String,Object)
     */
    public synchronized void add(final RenderedImage image) throws AmbiguousMetadataException {
        if (image instanceof PropertySource) {
            // This version allow the use of deferred properties.
            add((PropertySource) image, null);
        } else {
            final String[] names = image.getPropertyNames();
            if (names != null) {
                for (int i=0; i<names.length; i++) {
                    final String name = names[i];
                    add(name, image.getProperty(name));
                }
            }
        }
    }

    /**
     * Add metadata from the specified property source.
     *
     * @param  properties The properties source.
     * @param  prefix The prefix for properties to add, of {@code null} to add
     *         all properties. If non-null, only properties begining with this prefix
     *         will be added.
     * @throws AmbiguousMetadataException if a metadata is defined twice.
     *
     * @see #add(GridCoverage)
     * @see #add(RenderedImage)
     * @see #add(String,Object)
     */
    public synchronized void add(final PropertySource properties, final String prefix)
            throws AmbiguousMetadataException
    {
        final String[] names = (prefix!=null) ? properties.getPropertyNames(prefix) :
                                                properties.getPropertyNames();
        if (names != null) {
            for (int i=0; i<names.length; i++) {
                final String  name = names[i];
                final Class<?> classe = properties.getPropertyClass(name);
                add(name, new DeferredProperty(properties, name, classe));
            }
        }
    }

    /**
     * Add a metadata for the specified key. Keys are case-insensitive, ignore leading and
     * trailing whitespaces and consider any other whitespace sequences as equal to a single
     * {@code '_'} character.
     *
     * @param  alias The key for the metadata to add. This is usually the name found in the
     *         file to be parsed (this is different from {@link Key} objects, which are keys
     *         in a format neutral way). This key is usually, but not always, one of the alias
     *         defined with {@link #addAlias}.
     * @param  value The value for the metadata to add. If {@code null} or
     *         {@link Image#UndefinedProperty}, then this method do nothing.
     * @throws AmbiguousMetadataException if a different value already exists for the specified
     *         alias, or for an other alias bound to the same {@link Key}.
     *
     * @see #add(GridCoverage)
     * @see #add(RenderedImage)
     * @see #add(PropertySource,String)
     * @see #parseLine
     */
    public synchronized void add(String alias, final Object value) throws AmbiguousMetadataException
    {
        final AliasKey aliasAsKey;
        if (alias != null) {
            alias = alias.trim();
            aliasAsKey = new AliasKey(alias);
        }
        else {
            aliasAsKey = null;
        }
        add(aliasAsKey, value);
    }

    /**
     * Implementation of the {@link #add(String, Object)} method. This method is invoked by
     * {@link #add(GridCoverage)}, which iterates through each {@link AliasKey} declared in
     * {@link #naming}.
     */
    private void add(final AliasKey aliasAsKey, Object value) throws AmbiguousMetadataException {
        assert isValid();
        if (value==null || value==Image.UndefinedProperty) {
            return;
        }
        if (value instanceof CharSequence) {
            final String text = trim(value.toString().trim(), " ");
            if (text.length() == 0) return;
            value = text;
        }
        if (metadata == null) {
            metadata = new LinkedHashMap<Key<?>,Object>();
        }
        /*
         * Consistency check:
         *
         *    - First, compare the value with any older values defined for the
         *      same alias. This value is fetched only once with 'getMetadata'.
         *    - Next, compare the value with any values defined with any other
         *      alias bound to the same key. Those values are fetched in a loop
         *      with 'getOptional'.
         */
        Object           oldValue = getMetadata(aliasAsKey);
        Key<?>           checkKey = null;
        Iterator<Key<?>> iterator = null;
        while (true) {
            if (oldValue!=null && !oldValue.equals(value)) {
                final String alias = aliasAsKey.toString();
                throw new AmbiguousMetadataException(Errors.getResources(userLocale).
                          getString(ErrorKeys.INCONSISTENT_PROPERTY_$1, alias), checkKey, alias);
            }
            if (iterator == null) {
                if (naming == null) break;
                final Set<Key<?>> keySet = naming.get(aliasAsKey);
                if (keySet == null) break;
                iterator = keySet.iterator();
            }
            if (!iterator.hasNext()) break;
            checkKey = iterator.next();
            oldValue = getOptional(checkKey);
        }
        /*
         * All tests are okay. Now add the metadata.
         */
        cache = null;
        metadata.put(aliasAsKey, value);
    }

    /**
     * Add an alias to a key. After this method has been invoked, calls to
     * <code>{@link #get get}(key)</code> will really looks for metadata named {@code alias}.
     * Alias are mandatory in order to get various {@code getXXX()} methods to work for a
     * particular file format.
     * <p>
     * For example if the file to be parsed uses the names {@code "ULX"} and {@code "ULY"} for the
     * coordinate of the upper left corner, then the {@link #getEnvelope} method will not work
     * unless the following alias are set:
     *
     * <blockquote><pre>
     * addAlias({@linkplain #X_MINIMUM}, "ULX");
     * addAlias({@linkplain #Y_MAXIMUM}, "ULY");
     * </pre></blockquote>
     *
     * An arbitrary number of alias can be set for the same key. For example,
     * <code>addAlias(Y_MAXIMUM,&nbsp;...)</code> could be invoked twice with {@code "ULY"} and
     * {@code "Limit North"} alias. The {@code getXXX()} methods will try alias in the order they
     * were added and use the first value found.
     * <p>
     * The same alias can also be set to more than one key. For example, the following code is
     * legal. It means that pixel are square with the same horizontal and vertical resolution:
     *
     * <blockquote><pre>
     * addAlias({@linkplain #X_RESOLUTION}, "Resolution");
     * addAlias({@linkplain #Y_RESOLUTION}, "Resolution");
     * </pre></blockquote>
     *
     * @param  key The key to add an alias. This key is format neutral.
     * @param  alias The alias to add. This is the name actually used in the file to be parsed.
     *         Alias are case insensitive and ignore multiple whitespace, like keys. If
     *         this alias is already bound to the specified key, then this method do nothing.
     * @throws AmbiguousMetadataException if the addition of the supplied alias
     *         would introduce an ambiguity in the current set of metadata.
     *         This occurs if the key has already an alias mapping to a different value.
     *
     * @see #getAlias
     * @see #contains
     * @see #get
     */
    public synchronized void addAlias(final Key<?> key, String alias)
            throws AmbiguousMetadataException
    {
        alias = trim(alias.trim(), " ");
        final AliasKey aliasAsKey = new AliasKey(alias);
        final Object   metadata   = getMetadata(aliasAsKey);
        if (metadata != null) {
            final Object value = getOptional(key); // Checks also alias
            if (value!=null && !value.equals(metadata)) {
                throw new AmbiguousMetadataException(Errors.getResources(userLocale).
                          getString(ErrorKeys.INCONSISTENT_PROPERTY_$1, alias), key, alias);
            }
        }
        if (naming == null) {
            naming = new LinkedHashMap<Key<?>,Set<Key<?>>>();
        }
        cache = null;
        // Add the alias for the specified key. This is the information
        // used by 'get' methods for fetching a metadata from a key.
        Set<Key<?>> set = naming.get(key);
        if (set == null) {
            set = new LinkedHashSet<Key<?>>(4);
            naming.put(key, set);
        }
        set.add(aliasAsKey);
        // Add the key for the specified alias. This is the information used by
        // 'add' to check against ambiguities. Set's order doesn't matter here,
        // but we use LinkedHashSet anyway for faster iteration in key set.
        set = naming.get(aliasAsKey);
        if (set == null) {
            set = new LinkedHashSet<Key<?>>(4);
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
            for (final Set<Key<?>> keys : naming.values()) {
                if (!naming.keySet().containsAll(keys)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the specified value as a string.
     *
     * @param  value The value to cast.
     * @param  key The key, for formatting error message if needed.
     * @param  alias The alias, for formatting error message if needed.
     * @return The value as a string.
     * @throws MetadataException if the value can't be cast to a string.
     */
    private String toString(final Object value, final Key<?> key, final String alias)
            throws MetadataException
    {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence) {
            return value.toString();
        }
        if (value instanceof IdentifiedObject) {
            return ((IdentifiedObject) value).getName().getCode();
        }
        throw new MetadataException(Errors.getResources(userLocale).getString(
              ErrorKeys.CANT_CONVERT_FROM_TYPE_$1, Classes.getClass(value)), key, alias);
    }

    /**
     * Returns the metadata value for the specified alias. No other alias than the specified
     * one is examined. This method is used for the implementation of {@link #getOptional(Key)}.
     * This method is also invoked by {@link #add(String,Object)} in order to check if an
     * incompatible value is already set for a given alias.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive and
     *         can be any of the alias defined with {@link #addAlias}.
     * @return The metadata for the specified alias, or {@code null} if none.
     */
    private Object getMetadata(final AliasKey alias) {
        assert Thread.holdsLock(this);
        if (metadata != null) {
            Object value = metadata.get(alias);
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
     * Returns the metadata for the specified key, or {@code null} if the metadata is not
     * found. This method expects a format neutral, case insensitive {@link Key} argument. In
     * order to maps the key to the actual name used in the underlying metadata file, the method
     * {@link #addAlias} <strong>must</strong> have been invoked prior to any {@code get} method.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive and
     *         can be any of the alias defined with {@link #addAlias}.
     * @return The metadata for the specified key, or {@code null} if none.
     */
    private Object getOptional(final Key<?> key) {
        assert Thread.holdsLock(this);
        lastAlias = null;
        if (naming != null) {
            final Set<Key<?>> alias = naming.get(key);
            if (alias != null) {
                for (final Key<?> aliasAsKey : alias) {
                    final Object value = getMetadata((AliasKey) aliasAsKey);
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
     * Checks if this {@code MetadataBuilder} contains a value for the specified key.
     * Invoking {@link #get} will thrown a {@link MissingMetadataException} if and only
     * if {@link #contains} returns {@code false} for the same key.
     *
     * @param  key The key to test for inclusion in this {@code MetadataBuilder}.
     * @return {@code true} if the given key was found.
     *
     * @see #get
     * @see #addAlias
     */
    public synchronized boolean contains(final Key<?> key) {
        return getOptional(key) != null;
    }

    /**
     * Returns the metadata for the specified key. This method expect a format neutral, case
     * insensitive {@link Key} argument. In order to maps the key to the actual name used in
     * the underlying metadata file, the method {@link #addAlias} <strong>must</strong> have
     * been invoked prior to any {@code get} method.
     *
     * @param  key The key of the desired metadata. Keys are case insensitive and format neutral.
     * @return Value for the specified key (never {@code null}).
     * @throws MissingMetadataException if no value exists for the specified key.
     *
     * @see #getAsDouble
     * @see #getAsInt
     * @see #contains
     * @see #addAlias
     */
    public synchronized Object get(final Key<?> key) throws MissingMetadataException {
        final Object value = getOptional(key);
        if (value!=null && value!=Image.UndefinedProperty) {
            return value;
        }
        throw new MissingMetadataException(Errors.getResources(userLocale).
                  getString(ErrorKeys.UNDEFINED_PROPERTY_$1, key), key, lastAlias);
    }

    /**
     * Returns a metadata as a {@code double} value. The default implementation invokes
     * {@link #getAsDouble(Key)} or {@link #getAsDate(Key)} according the metadata type:
     * the metadata is assumed to be a number, except if {@code crs} is a {@link TemporalCRS}.
     * In this later case, the metadata is assumed to be a {@link Date}.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive.
     * @param  crs The coordinate reference system for the dimension of the key to be queried,
     *             or {@code null} if unknow.
     * @return Value for the specified key as a {@code double}.
     * @throws MissingMetadataException if no value exists for the specified key.
     * @throws MetadataException if the value can't be parsed as a {@code double}.
     */
    private double getAsDouble(final Key<?> key, final CoordinateReferenceSystem crs)
            throws MetadataException
    {
        if (crs instanceof TemporalCRS) {
            return DefaultTemporalCRS.wrap((TemporalCRS) crs).toValue(getAsDate(key));
        } else {
            return getAsDouble(key);
        }
    }

    /**
     * Returns a metadata as a {@code double} value. The default implementation
     * invokes <code>{@link #get get}(key)</code> and parse the resulting value with
     * {@link NumberFormat#parse(String)} for the {@linkplain #getLocale current locale}.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive.
     * @return Value for the specified key as a {@code double}.
     * @throws MissingMetadataException if no value exists for the specified key.
     * @throws MetadataException if the value can't be parsed as a {@code double}.
     *
     * @see #getAsInt
     * @see #get
     * @see #contains
     * @see #addAlias
     */
    public synchronized double getAsDouble(final Key<?> key) throws MetadataException {
        final Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return getNumberFormat().parse(toString(value, key, lastAlias)).doubleValue();
        } catch (ParseException exception) {
            throw new MetadataException(exception, key, lastAlias);
        }
    }

    /**
     * Returns a metadata as a {@code int} value. The default implementation
     * invokes <code>{@link #getAsDouble getAsDouble}(key)</code> and make sure
     * that the resulting value is an integer.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive.
     * @return Value for the specified key as an {@code int}.
     * @throws MissingMetadataException if no value exists for the specified key.
     * @throws MetadataException if the value can't be parsed as an {@code int}.
     *
     * @see #getAsDouble
     * @see #get
     * @see #contains
     * @see #addAlias
     */
    public synchronized int getAsInt(final Key<?> key) throws MetadataException {
        final double value = getAsDouble(key);
        final int  integer = (int) value;
        if (value != integer) {
            throw new MetadataException(Errors.getResources(userLocale).getString(
                      ErrorKeys.BAD_PARAMETER_$2, lastAlias, value), key, lastAlias);
        }
        return integer;
    }

    /**
     * Returns a metadata as a {@link Date} value. The default implementation
     * invokes <code>{@link #get get}(key)</code> and parse the resulting value with
     * {@link DateFormat#parse(String)} for the {@linkplain #getLocale current locale}.
     *
     * @param  key The key of the desired metadata. Keys are case-insensitive.
     * @return Value for the specified key as a {@link Date}.
     * @throws MissingMetadataException if no value exists for the specified key.
     * @throws MetadataException if the value can't be parsed as a date.
     */
    public synchronized Date getAsDate(final Key<?> key) throws MetadataException {
        final Object value = get(key);
        if (value instanceof Date) {
            return (Date) (((Date) value).clone());
        }
        try {
            return getDateFormat().parse(toString(value, key, lastAlias));
        } catch (ParseException exception) {
            throw new MetadataException(exception, key, lastAlias);
        }
    }

    /**
     * Gets the object to use for parsing numbers.
     */
    private NumberFormat getNumberFormat() throws MetadataException {
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
     * Gets the object to use for parsing dates.
     */
    private DateFormat getDateFormat() throws MetadataException {
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
            cache = new HashMap<String,Object>();
        }
        cache.put(key, object);
    }

    /**
     * Returns the list of alias for the specified key, or {@code null}
     * if the key has no alias. Alias are the names used in the underlying
     * metadata file, and are format dependent.
     *
     * @param  key The format neutral key.
     * @return The alias for the specified key, or {@code null} if none.
     *
     * @see #addAlias
     */
    public synchronized String[] getAlias(final Key<?> key) {
        assert isValid();
        if (naming != null) {
            final Set<Key<?>> alias = naming.get(key);
            if (alias != null) {
                int index = 0;
                final String[] list = new String[alias.size()];
                for (final Key<?> aliasAsKey : alias) {
                    list[index++] = aliasAsKey.toString();
                }
                assert index == list.length;
                return list;
            }
        }
        return null;
    }

    /**
     * Returns the source file name or URL. This is the path specified
     * during the last call to a {@code load(...)} method.
     *
     * @return The source file name or URL.
     * @throws MetadataException if this information can't be fetched.
     *
     * @link #load(File)
     * @link #load(URL)
     */
    public String getSource() throws MetadataException {
        return source;
    }

    /**
     * Returns the locale to use when parsing metadata values as numbers, angles or dates.
     * This is <strong>not</strong> the locale used for formatting error messages, if any.
     * The default implementation returns {@link Locale#US}, since it is the format used
     * in most data file.
     *
     * @return The locale to use for parsing metadata values.
     * @throws MetadataException if this information can't be fetched.
     *
     * @see #getAsDouble
     * @see #getAsInt
     * @see #getAsDate
     */
    public Locale getLocale() throws MetadataException {
        return Locale.US;
    }

    /**
     * Returns the units, or the specified value if no units is found.
     * The default value may be {@code null}.
     */
    private Unit getUnit(final Unit defaultValue) {
        try {
            return getUnit();
        } catch (MetadataException exception) {
            return defaultValue;
        }
    }

    /**
     * Returns the units. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #UNITS})</code>
     * and transform the resulting string into an {@link Unit} object.
     *
     * @throws MissingMetadataException if no value exists for the {@link #UNITS} key.
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getCoordinateReferenceSystem
     */
    public synchronized Unit getUnit() throws MetadataException {
        final Object value = get(UNITS);
        if (value instanceof Unit) {
            return (Unit) value;
        }
        final String text = toString(value, UNITS, lastAlias);
        if (contains(text, METRES)) {
            return SI.METER;
        } else if (contains(text, DEGREES)) {
            return NonSI.DEGREE_ANGLE;
        } else {
            throw new MetadataException("Unknow unit: "+text, UNITS, lastAlias);
        }
    }

    /**
     * Check if {@code toSearch} appears in the {@code list} array.
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
     * Returns the geodetic datum. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #DATUM})</code>
     * and transform the resulting string into a {@link GeodeticDatum} object.
     *
     * @throws MissingMetadataException if no value exists for the {@link #DATUM} key.
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getCoordinateReferenceSystem
     * @see #getEllipsoid
     */
    public synchronized GeodeticDatum getGeodeticDatum() throws MetadataException {
        final Object value = get(DATUM);
        if (value instanceof GeodeticDatum) {
            return (GeodeticDatum) value;
        }
        final String text = toString(value, DATUM, lastAlias);
        /*
         * TODO: parse 'text' when DatumAuthorityFactory will be fully implemented.
         */
        checkEllipsoid(text, "getGeodeticDatum");
        return DefaultGeodeticDatum.WGS84;
    }

    /**
     * Returns the ellipsoid. The default implementation invokes
     * <code>{@linkplain #get get}({@linkplain #ELLIPSOID})</code>
     * and transform the resulting string into an {@link Ellipsoid} object.
     *
     * @throws MissingMetadataException if no value exists for the {@link #ELLIPSOID} key.
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getCoordinateReferenceSystem
     * @see #getGeodeticDatum
     */
    public synchronized Ellipsoid getEllipsoid() throws MetadataException {
        final Object value = get(ELLIPSOID);
        if (value instanceof Ellipsoid) {
            return (Ellipsoid) value;
        }
        final String text = toString(value, ELLIPSOID, lastAlias);
        /*
         * TODO: parse 'text' when DatumAuthorityFactory will be fully implemented.
         */
        checkEllipsoid(text, "getEllipsoid");
        return org.geotools.referencing.datum.DefaultEllipsoid.WGS84;
    }

    /**
     * Check if the supplied ellipsoid is WGS 1984.
     * This is a temporary patch.
     *
     * @todo parse the datum and ellipsoid names when DatumAuthorityFactory
     *       will be implemented. The current EPSG factory implementation may not be enough.
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
                record.setSourceClassName(MetadataBuilder.class.getName());
                AbstractGridCoverageReader.LOGGER.log(record);
            }
        }
    }

    /** Temporary flag for {@link #checkEllipsoid}. */
    private static boolean emittedWarning;

    /**
     * Set the specified value for the specified parameter. If the specified unit is non-null and
     * compatible with the parameter value, then it will be given to the parameter. Otherwise, the
     * the parameter unit is left unchanged. This heuristic rule may be acceptable only when we
     * don't know for sure on which parameter the unit applies, which explain why it is not part
     * of any public API. This method is for internal usage by {@link #getProjection}.
     */
    private static void setValue(final ParameterValue parameter, final double value, final Unit unit) {
        if (unit != null) {
            final Unit expected = parameter.getDescriptor().getUnit();
            if (expected!=null && unit.isCompatible(expected)) {
                parameter.setValue(value, unit);
                return;
            }
        }
        parameter.setValue(value);
    }

    /**
     * Returns the projection. The default implementation performs the following steps:
     * <p>
     * <ul>
     *   <li>Gets the projection classification with
     *       <code>{@linkplain #get get}({@linkplain #OPERATION_METHOD})</code>, or with
     *       <code>{@linkplain #get get}({@linkplain #PROJECTION})</code> if no value were
     *       defined for the former.</li>
     *
     *   <li>Gets the list of projection parameters for the above classification.</li>
     *
     *   <li>Gets the metadata values for each parameters in the above step. If a parameter is not
     *       defined in this {@code MetadataBuilder}, then it will be left to its (projection
     *       dependent) default value. Parameters are projection dependent, but will typically
     *       include
     *
     *           {@code "semi_major"},
     *           {@code "semi_minor"},
     *           {@code "central_meridian"},
     *           {@code "latitude_of_origin"},
     *           {@code "false_easting"} and
     *           {@code "false_northing"}.
     *
     *       The names actually used in the metadata file to be parsed must be declared as usual,
     *       e.g. <code>{@linkplain #addAlias addAlias}({@linkplain #SEMI_MAJOR}, ...)</code></li>
     *
     *   <li>If no value was defined for {@code "semi-major"} and/or {@code "semi-minor"}
     *       parameters, then invokes {@link #getEllipsoid} and uses its semi-axis length.</li>
     *
     *   <li>If a value exists for the optional key {@link #PROJECTION}, then takes it as
     *       the projection name. The projection name is for documentation purpose only and do
     *       not affect any computation. If there is no value for {@link #PROJECTION}, then
     *       the projection name will be the same than the operation method name (the first step
     *       above).</li>
     * </ul>
     *
     * @return The projection.
     * @throws MissingMetadataException if no value exists for the {@link #PROJECTION} or the
     *         {@link #OPERATION_METHOD} keys.
     * @throws MetadataException if the operation failed for some other reason
     *         (for example if a parameter value can't be parsed as a {@code double}).
     *
     * @see #getCoordinateReferenceSystem
     * @see #SEMI_MAJOR
     * @see #SEMI_MINOR
     * @see #LATITUDE_OF_ORIGIN
     * @see #CENTRAL_MERIDIAN
     * @see #FALSE_EASTING
     * @see #FALSE_NORTHING
     */
    public synchronized Conversion getProjection() throws MetadataException {
        /*
         * First, checks if a Projection object has already been constructed. Since
         * Projection is immutable, it is safe to returns a single instance for all.
         */
        final String CACHE_KEY = "Projection";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof Conversion) {
                return (Conversion) candidate;
            }
        }
        /*
         * No projection is available in the cache. Computes it now and cache it for future use.
         * If the projection is provided, then the operation method is optional. Otherwise, the
         * operation method is mandatory.
         */
        Object projection = getOptional(PROJECTION);
        if (projection instanceof Conversion) {
            return (Conversion) projection;
        }
        String projectionAlias = lastAlias; // Protect from change, except if projection is null.
        Object operationMethod;
        if (projection == null) {
            operationMethod = get(OPERATION_METHOD);
            projection      = operationMethod;
            projectionAlias = lastAlias;
        } else {
            operationMethod = getOptional(OPERATION_METHOD);
            if (operationMethod == null) {
                operationMethod = projection;
            }
        }
        /*
         * We now have the projection name and the projection classification (as operation method
         * name). Now iterates through all expected arguments for this projection, and ask a
         * matching metadata for each of them. If none is found, the projection parameter is left
         * to its default value.
         */
        boolean semiMajorAxisDefined = false;
        boolean semiMinorAxisDefined = false;
        final ParameterValueGroup parameters;
        final MathTransformFactory factory = factories.getMathTransformFactory();
        try {
            parameters = factory.getDefaultParameters(toString(operationMethod, OPERATION_METHOD, lastAlias));
        } catch (NoSuchIdentifierException exception) {
            throw new MetadataException(exception, OPERATION_METHOD, lastAlias);
        }
        final Unit unit = getUnit(null);
        for (final GeneralParameterDescriptor descriptor : parameters.getDescriptor().descriptors()) {
            if (descriptor instanceof ParameterDescriptor) {
                final String          name = descriptor.getName().getCode();
                final ParameterValue param = parameters.parameter(name);
                final double paramValue;
                try {
                    paramValue = getAsDouble(new Key<Number>(name));
                } catch (MissingMetadataException exception) {
                    // Parameter is not defined. Lets it to
                    // its default value and continue...
                    continue;
                }
                setValue(param, paramValue, unit);
                if (name.equalsIgnoreCase("semi_major")) semiMajorAxisDefined=true;
                if (name.equalsIgnoreCase("semi_minor")) semiMinorAxisDefined=true;
            }
        }
        /*
         * After all parameters have been set, ensures that semi major and minor axis are
         * presents. If they were already specified, ensures that their values is consistent
         * with the ellipsoid.
         */
        if (!semiMajorAxisDefined || !semiMinorAxisDefined) {
            final Ellipsoid ellipsoid = getEllipsoid();
            final double semiMajor = ellipsoid.getSemiMajorAxis();
            final double semiMinor = ellipsoid.getSemiMinorAxis();
            final Unit   axisUnit  = ellipsoid.getAxisUnit();
            if ((semiMajorAxisDefined && parameters.parameter("semi_major").doubleValue(axisUnit)!=semiMajor) ||
                (semiMinorAxisDefined && parameters.parameter("semi_minor").doubleValue(axisUnit)!=semiMinor))
            {
                throw new AmbiguousMetadataException(Errors.getResources(userLocale).getString(
                          ErrorKeys.AMBIGIOUS_AXIS_LENGTH), PROJECTION, projectionAlias);
            }
            parameters.parameter("semi_major").setValue(semiMajor, axisUnit);
            parameters.parameter("semi_minor").setValue(semiMinor, axisUnit);
        }
        final Conversion defining = new DefiningConversion(toString(
                projection, PROJECTION, projectionAlias), parameters);
        cache(CACHE_KEY, defining);
        return defining;
    }

    /**
     * Returns the coordinate reference system. The default implementation constructs a CRS
     * from the information provided by {@link #getUnit}, {@link #getGeodeticDatum} and
     * {@link #getProjection}. The coordinate system name (optional) will be fetch from
     * metadata {@link #COORDINATE_REFERENCE_SYSTEM}, if presents as a string.
     *
     * @throws MissingMetadataException if a required value is missing
     *        (e.g. {@link #PROJECTION}, {@link #DATUM}, {@link #UNITS}, etc.).
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getUnit
     * @see #getGeodeticDatum
     * @see #getProjection
     */
    public synchronized CoordinateReferenceSystem getCoordinateReferenceSystem()
            throws MetadataException
    {
        /*
         * First, checks if a CRS object has already been constructed. Since CRS
         * are immutables, it is safe to returns a single instance for all.
         */
        final String CACHE_KEY = "CoordinateReferenceSystem";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof CoordinateReferenceSystem) {
                return (CoordinateReferenceSystem) candidate;
            }
        }
        /*
         * No CoordinateReferenceSystem is available in the cache.
         * Computes it now and cache it for future use.
         */
        Object value = getOptional(COORDINATE_REFERENCE_SYSTEM);
        if (value instanceof CoordinateReferenceSystem) {
            return (CoordinateReferenceSystem) value;
        }
        if (value == null) {
            value = "Generated";
        }
        final String        crsAlias = lastAlias; // Protect from change
        final String         crsName = toString(value, COORDINATE_REFERENCE_SYSTEM, crsAlias);
        final Unit              unit = getUnit();
        final GeodeticDatum    datum = getGeodeticDatum();
        final boolean   isGeographic = NonSI.DEGREE_ANGLE.isCompatible(unit);
        final Unit       angularUnit = isGeographic ? unit : NonSI.DEGREE_ANGLE;
        final Unit        linearUnit = SI.METER.isCompatible(unit) ? unit : SI.METER;
        final EllipsoidalCS    geoCS = DefaultEllipsoidalCS.GEODETIC_2D.usingUnit(angularUnit);
        final Map<String,String> properties =
                Collections.singletonMap(IdentifiedObject.NAME_KEY, crsName);
        final GeographicCRS   geographicCRS;
        final CoordinateReferenceSystem crs;
        try {
            geographicCRS = factories.getCRSFactory().createGeographicCRS(properties, datum, geoCS);
            if (isGeographic) {
                crs = geographicCRS;
            } else {
                final CartesianCS cs = DefaultCartesianCS.PROJECTED.usingUnit(linearUnit);
                final Conversion projection = getProjection();
                crs = factories.createProjectedCRS(properties, geographicCRS, projection, cs);
            }
            cache(CACHE_KEY, crs);
            return crs;
        } catch (FactoryException exception) {
            throw new MetadataException(exception, COORDINATE_REFERENCE_SYSTEM, crsAlias);
        }
    }

    /**
     * Convenience method returning the envelope in geographic coordinate system using WGS
     * 1984 datum.
     *
     * @throws MetadataException if the operation failed. This exception
     *         may contains a {@link TransformException} as its cause.
     *
     * @see #getEnvelope
     * @see #getGridRange
     */
    public synchronized GeographicBoundingBox getGeographicBoundingBox() throws MetadataException {
        /*
         * First, checks if a bounding box object has already been constructed.
         * Since bounding box can be immutable after their construction, there
         * is no need to clone it before to return it.
         */
        final String CACHE_KEY = "GeographicBoundingBox";
        if (cache != null) {
            final Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof GeographicBoundingBox) {
                return (GeographicBoundingBox) candidate;
            }
        }
        /*
         * No bounding box is available in the cache.
         * Computes it now and cache it for future use.
         */
        final GeographicBoundingBoxImpl box;
        try {
            box = new GeographicBoundingBoxImpl(getEnvelope());
        } catch (TransformException exception) {
            throw new MetadataException(exception, null, null);
        }
        box.freeze();
        cache(CACHE_KEY, box);
        return box;
    }

    /**
     * Returns the envelope. The default implementation constructs an envelope
     * using the values from the following keys:
     * <ul>
     *   <li>The horizontal limits with at least one of the following keys:
     *       {@link #X_MINIMUM} and/or {@link #X_MAXIMUM}. If one of those
     *       keys is missing, then {@link #X_RESOLUTION} is required.</li>
     *   <li>The vertical limits with at least one of the following keys:
     *       {@link #Y_MINIMUM} and/or {@link #Y_MAXIMUM}. If one of those
     *       keys is missing, then {@link #Y_RESOLUTION} is required.</li>
     * </ul>
     *
     * @throws MissingMetadataException if a required value is missing.
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getGridRange
     * @see #getGeographicBoundingBox
     */
    @SuppressWarnings("fallthrough")
    public synchronized Envelope getEnvelope() throws MetadataException {
        /*
         * First, checks if an Envelope object has already been constructed.
         * Since Envelope is mutable, we need to clone it before to return it.
         */
        final String CACHE_KEY = "Envelope";
        if (cache != null) {
            Object candidate = cache.get(CACHE_KEY);
            if (candidate instanceof Envelope) {
                if (candidate instanceof Cloneable) {
                    candidate = ((Cloneable) candidate).clone();
                }
                return (Envelope) candidate;
            }
        }
        /*
         * No Envelope is available in the cache.
         * Computes it now and cache it for future use.
         */
        final GridRange               range = getGridRange();
        final CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
        final GeneralEnvelope      envelope = new GeneralEnvelope(crs);
        switch (envelope.getDimension()) {
            default: // TODO: What should we do with other dimensions? Open question...
            case 3: setRange(Z_MINIMUM, Z_MAXIMUM, Z_RESOLUTION, envelope, 2, range, crs); // fall through
            case 2: setRange(Y_MINIMUM, Y_MAXIMUM, Y_RESOLUTION, envelope, 1, range, crs); // fall through
            case 1: setRange(X_MINIMUM, X_MAXIMUM, X_RESOLUTION, envelope, 0, range, crs); // fall through
            case 0: break;
        }
        cache(CACHE_KEY, envelope);
        return envelope.clone();
    }

    /**
     * Set the range for the specified dimension of an envelope. The range will be computed
     * from the "?Minimum" and "?Maximum" metadata, if presents. If only one of those
     * metadata is present, the "?Resolution" metadata will be used.
     *
     * @param minKey    Property name for the minimal value.
     * @param maxKey    Property name for the maximal value.
     * @param resKey    Property name for the resolution.
     * @param envelope  The envelope to set.
     * @param dimension The dimension in the envelope to set.
     * @param gridRange The grid range.
     * @param crs       The coordinate reference system
     * @throws MetadataException if a metadata can't be set, or if an ambiguity has been found.
     */
    private void setRange(final Key<?> minKey, final Key<?> maxKey, final Key<?> resKey,
                          final GeneralEnvelope envelope, final int dimension,
                          final GridRange gridRange, CoordinateReferenceSystem crs)
            throws MetadataException
    {
        assert Thread.holdsLock(this);
        crs = CRSUtilities.getSubCRS(crs, dimension, dimension+1);
        if (!contains(resKey)) {
            envelope.setRange(dimension, getAsDouble(minKey, crs), getAsDouble(maxKey, crs));
            return;
        }
        final double resolution = getAsDouble(resKey, crs);
        final String lastAlias = this.lastAlias; // Protect from change
        final int range = gridRange.getLength(dimension);
        if (!contains(maxKey)) {
            final double min = getAsDouble(minKey, crs);
            envelope.setRange(dimension, min, min + resolution*range);
            return;
        }
        if (!contains(minKey)) {
            final double max = getAsDouble(maxKey, crs);
            envelope.setRange(dimension, max - resolution*range, max);
            return;
        }
        final double min = getAsDouble(minKey, crs);
        final double max = getAsDouble(maxKey, crs);
        envelope.setRange(dimension, min, max);
        if (Math.abs((min-max)/resolution - range) > EPS) {
            throw new AmbiguousMetadataException(Errors.getResources(userLocale).getString(
                      ErrorKeys.INCONSISTENT_PROPERTY_$1, resKey), resKey, lastAlias);
        }
    }

    /**
     * Returns the grid range. Default implementation fetchs the metadata values
     * for keys {@link #WIDTH} and {@link #HEIGHT}, and transform the resulting
     * strings into a {@link GridRange} object.
     *
     * @throws MissingMetadataException if a required value is missing.
     * @throws MetadataException if the operation failed for some other reason.
     *
     * @see #getEnvelope
     * @see #getGeographicBoundingBox
     */
    @SuppressWarnings("fallthrough")
    public synchronized GridRange getGridRange() throws MetadataException {
        /*
         * First, checks if a GridRange object has already been constructed. Since
         * GridRange is immutable, it is safe to returns a single instance for all.
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
        final int dimension = getCoordinateReferenceSystem().getCoordinateSystem().getDimension();
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
        final GridRange range = new GeneralGridRange(lower, upper);
        cache(CACHE_KEY, range);
        return range;
    }

    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
     * to be read. If sample dimensions are not know, then this method returns
     * {@code null}. The default implementation always returns {@code null}.
     *
     * @throws MetadataException if the operation failed.
     */
    public GridSampleDimension[] getSampleDimensions() throws MetadataException {
        return null;
    }

    /**
     * Sets the current {@link Locale} of this {@code MetadataBuilder}
     * to the given value. A value of {@code null} removes any previous
     * setting, and indicates that the parser should localize as it sees fit.
     * <p>
     * <strong>Note:</strong> this is the locale to use for formatting error messages,
     * not the locale to use for parsing the file. The locale for parsing is specified
     * by {@link #getLocale}.
     */
    final synchronized void setUserLocale(final Locale locale) {
        userLocale = locale;
    }

    /**
     * List all metadata to the specified stream. The default implementation list the
     * metadata as <cite>key&nbsp;=&nbsp;value</cite> pairs. Each pair is formatted on
     * its own line, and the caracter <code>'='</code> is inserted between keys and values.
     * A question mark (<code>'?'</code>) is put in front of any unknow name (i.e. any name
     * not specified with {@link #addAlias}).
     *
     * @param  out Stream to write metadata to.
     * @throws IOException if an error occured while listing metadata.
     *
     * @see #add(GridCoverage)
     * @see #toString()
     */
    public synchronized void listMetadata(final Writer out) throws IOException {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final String comments = (String) getMetadata(null);
        if (comments != null) {
            int stop = comments.length();
            while (--stop>=0 && Character.isSpaceChar(comments.charAt(stop)));
            out.write(comments.substring(0, stop+1));
            out.write(lineSeparator);
            out.write(lineSeparator);
        }
        if (metadata != null) {
            int maxLength = 1;
            for (final Object key : metadata.keySet()) {
                if (key != null) {
                    final int length = key.toString().length();
                    if (length > maxLength) maxLength = length;
                }
            }
            for (final Map.Entry<Key<?>,?> entry : metadata.entrySet()) {
                final Key<?> key = entry.getKey();
                if (key != null) {
                    Object value = entry.getValue();
                    if (value instanceof Number) {
                        value = getNumberFormat().format(value);
                    } else if (value instanceof Date) {
                        value = getDateFormat().format(value);
                    } else if (value instanceof Formattable) try {
                        // Format without indentation
                        value = ((Formattable) value).toWKT(0);
                    } catch (UnformattableObjectException exception) {
                        // Ignore; we will use 'toString()' instead.
                    }
                    final boolean isKnow = (naming!=null && naming.containsKey(key));
                    out.write(isKnow ? "  " : "? ");
                    out.write(String.valueOf(key));
                    out.write(Utilities.spaces(maxLength - key.toString().length()));
                    out.write(separator);
                    out.write(String.valueOf(value));
                    out.write(lineSeparator);
                }
            }
        }
    }

    /**
     * Returns a string representation of this metadata set. The default implementation
     * write the class name and the envelope in geographic coordinates, as returned by
     * {@link #getGeographicBoundingBox}. Then, it append the list of all metadata as
     * formatted by {@link #listMetadata}.
     */
    @Override
    public String toString() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringWriter  buffer = new StringWriter();
        if (source != null) {
            buffer.write("[\"");
            buffer.write(source);
            buffer.write("\"]");
        }
        buffer.write(lineSeparator);
        try {
            final GeographicBoundingBox box = getGeographicBoundingBox();
            buffer.write(GeographicBoundingBoxImpl.toString(box, "DD°MM'SS\"", null));
            buffer.write(lineSeparator);
        } catch (MetadataException exception) {
            // Ignore.
        }
        buffer.write('{');
        buffer.write(lineSeparator);
        try {
            final TableWriter table = new TableWriter(buffer, 2);
            table.setMultiLinesCells(true);
            table.nextColumn();
            listMetadata(table);
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
     * @param  str The string to trim (may be {@code null}).
     * @param  separator The separator to insert in place of succession of whitespaces.
     *         Usually "_" for keys and " " for values.
     * @return The trimed string, or {@code null} if <code>str</code> was null.
     */
    static String trim(String str, final String separator) {
        if (str != null) {
            str = str.trim();
            StringBuilder buffer = null;
    loop:       for (int i=str.length(); --i>=0;) {
                if (Character.isSpaceChar(str.charAt(i))) {
                    final int upper = i;
                    do if (--i < 0) break loop;
                    while (Character.isSpaceChar(str.charAt(i)));
                    if (buffer == null) {
                        buffer = new StringBuilder(str);
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
     * A key for fetching metadata in a format independent way. For example, the northern
     * limit of an image way be named <code>"Limit North"</code> is some metadata files,
     * and <code>"ULY"</code> (as <cite>Upper Left Y</cite>) in other metadata files. The
     * {@link MetadataBuilder#Y_MAXIMUM} allows to fetch this metadata without knowledge of
     * the actual name used in the underlying metadata file.
     * <p>
     * Keys are case-insensitive. Furthermore, trailing and leading spaces are ignored.
     * Any succession of one ore more unicode whitespace characters (as of
     * {@link java.lang.Character#isSpaceChar(char)} is understood as equal to a single
     * <code>'_'</code> character. For example, the key <code>"false&nbsp;&nbsp;easting"</code>
     * is considered equals to <code>"false_easting"</code>.
     *
     * @version $Id$
     * @author Martin Desruisseaux (IRD)
     */
    public static class Key<T> implements Serializable {
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
         * For example the key {@link MetadataBuilder#X_MINIMUM} will returns
         * <code>coverage.getEnvelope().getMinimum(0)</code>.
         *
         * @param coverage The grid coverage from which to fetch the value.
         * @return The value, or {@code null} if none.
         */
        public T getValue(final GridCoverage coverage) {
            return null;
        }

        /**
         * Returns the name for this key. This is the name supplied to the constructor
         * (i.e. case and whitespaces are preserved).
         */
        @Override
        public String toString() {
            return name;
        }

        /**
         * Returns a hash code value.
         */
        @Override
        public int hashCode() {
            return key.hashCode();
        }

        /**
         * Compare this key with the supplied key for equality. Comparaison is case-insensitive
         * and considere any sequence of whitespaces as a single <code>'_'</code> character, as
         * specified in this class documentation.
         */
        @Override
        public boolean equals(final Object object) {
            return (object!=null) && object.getClass().equals(getClass()) &&
                    key.equals(((Key) object).key);
        }
    }

    /**
     * A key for metadata derived from {@link Envelope} and/or {@link GridRange}.
     *
     * @version $Id$
     * @author Martin Desruisseaux (IRD)
     */
    private static final class EnvelopeKey extends Key<Comparable<?>> {
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
        @Override
        public Comparable<?> getValue(final GridCoverage coverage) {
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
                case LENGTH : return Double.valueOf(    envelope.getLength (dimension));
                case MINIMUM: return getValue(coverage, envelope.getMinimum(dimension));
                case MAXIMUM: return getValue(coverage, envelope.getMaximum(dimension));
                case SIZE   : return Integer.valueOf(      range.getLength (dimension));
                case LOWER  : return Integer.valueOf(      range.getLower  (dimension));
                case UPPER  : return Integer.valueOf(      range.getUpper  (dimension));
                case RESOLUTION: {
                    return Double.valueOf(envelope.getLength(dimension) / range.getLength(dimension));
                }
            }
        }

        /**
         * Returns the specified value as a {@link Double} or {@link Date} object
         * according the coverage's coordinate system.
         */
        private Comparable<?> getValue(final GridCoverage coverage, final double value) {
            CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
            if (crs != null) {
                crs = CRSUtilities.getSubCRS(crs, dimension, dimension+1);
                if (crs instanceof TemporalCRS) {
                    return DefaultTemporalCRS.wrap((TemporalCRS) crs).toDate(value);
                }
            }
            return Double.valueOf(value);
        }

        /**
         * Compares this key with the supplied key for equality.
         */
        @Override
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
     * A key for metadata derived from {@link Projection}.
     * The key name must be the projection parameter name.
     *
     * @version $Id$
     * @author Martin Desruisseaux (IRD)
     */
    private static final class ProjectionKey extends Key<Number> {
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
        @Override
        public Number getValue(final GridCoverage coverage) {
            final ProjectedCRS crs = CRS.getProjectedCRS(coverage.getCoordinateReferenceSystem());
            if (crs != null) {
                final ParameterValueGroup parameters = crs.getConversionFromBase().getParameterValues();
                try {
                    return (Number) parameters.parameter(toString()).getValue();
                } catch (ParameterNotFoundException exception) {
                    // No value set for the specified parameter.
                    // This is not an error. Just ignore...
                }
            }
            return null;
        }
    }

    /**
     * A case-insensitive key for alias name. We use a different class because the {@code equals}
     * method must returns {@code false} when comparing {@code AliasKey} with ordinary {@code Key}s.
     * This kind of key is for internal use only.
     *
     * @version $Id$
     * @author Martin Desruisseaux (IRD)
     */
    private static final class AliasKey extends Key<Object> {
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
        @Override
        public int hashCode() {
            return ~super.hashCode();
        }
    }
}
