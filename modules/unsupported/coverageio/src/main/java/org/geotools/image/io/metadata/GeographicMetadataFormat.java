/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.image.io.metadata;

import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

import org.opengis.geometry.Envelope;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.resources.UnmodifiableArrayList;


/**
 * Describes the structure of {@linkplain GeographicMetadata geographic metadata}.
 * The following formatting rules apply:
 * <p>
 * <ul>
 *   <li>Numbers must be formatted as in the {@linkplain Locale#US US locale}, i.e.
 *       as {@link Integer#toString(int)} or {@link Double#toString(double)}.</li>
 *   <li>Dates must be formatted with the {@code "yyyy-MM-dd HH:mm:ss"}
 *       {@linkplain SimpleDateFormat pattern} in UTC {@linkplain TimeZone timezone}.</li>
 * </ul>
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicMetadataFormat extends IIOMetadataFormatImpl {
    /**
     * The metadata format name.
     */
    public static final String FORMAT_NAME = "geotools_coverage_1.0";

    /**
     * The maximum number of dimension allowed for the image coordinate system. Images must be
     * at least two-dimensional. Some plugins consider the set of bands as the third dimension
     * (for example slices at different depths). An additional "1 pixel large" temporal dimension
     * is sometime used for storing the image timestamp.
     */
    private static final int MAXIMUM_DIMENSIONS = 4;

    /**
     * The maximum number of bands allowed. This is a somewhat arbitrary value since there is
     * no reason (except memory or disk space constraints) to restrict the number of bands in
     * the image stream. The number of bands actually read is usually much smaller.
     */
    private static final int MAXIMUM_BANDS = Short.MAX_VALUE;

    /**
     * The geographic {@linkplain CoordinateReferenceSystem coordinate reference system} type.
     * This is often used together with the {@linkplain #ELLIPSOIDAL ellipsoidal} coordinate
     * system type.
     *
     * @see #setCoordinateReferenceSystem
     */
    public static final String GEOGRAPHIC = "geographic";

    /**
     * The geographic {@linkplain CoordinateReferenceSystem coordinate reference system} type
     * with a vertical axis. This is often used together with a three-dimensional {@linkplain
     * #ELLIPSOIDAL ellipsoidal} coordinate system type.
     * <p>
     * If the coordinate reference system has no vertical axis, or has additional axis of
     * other kind than vertical (for example only a temporal axis), then the type should be
     * the plain {@value #GEOGRAPHIC}. This is because such CRS are usually constructed as
     * {@linkplain org.opengis.referencing.crs.CompoundCRS compound CRS} rather than a CRS
     * with a three-dimensional coordinate system.
     * <p>
     * To be strict, a 3D CRS should be allowed only if the vertical axis is of the kind
     * "height above the ellipsoid" (as opposed to "height above the geoid" for example),
     * otherwise we have a compound CRS. But many datafile don't make this distinction.
     *
     * @see #setCoordinateReferenceSystem
     */
    public static final String GEOGRAPHIC_3D = "geographic3D";

    /**
     * The projected {@linkplain CoordinateReferenceSystem coordinate reference system} type.
     * This is often used together with the {@linkplain #CARTESIAN cartesian} coordinate
     * system type.
     *
     * @see #setCoordinateReferenceSystem
     */
    public static final String PROJECTED = "projected";

    /**
     * The projected {@linkplain CoordinateReferenceSystem coordinate reference system} type
     * with a vertical axis. This is often used together with a three-dimensional {@linkplain
     * #CARTESIAN cartesian} coordinate system type.
     * <p>
     * If the coordinate reference system has no vertical axis, or has additional axis of
     * other kind than vertical (for example only a temporal axis), then the type should be
     * the plain {@value #PROJECTED}. This is because such CRS are usually constructed as
     * {@linkplain org.opengis.referencing.crs.CompoundCRS compound CRS} rather than a CRS
     * with a three-dimensional coordinate system.
     * <p>
     * To be strict, a 3D CRS should be allowed only if the vertical axis is of the kind
     * "height above the ellipsoid" (as opposed to "height above the geoid" for example),
     * otherwise we have a compound CRS. But many datafile don't make this distinction.
     *
     * @see #setCoordinateReferenceSystem
     */
    public static final String PROJECTED_3D = "projected3D";

    /**
     * The ellipsoidal {@linkplain CoordinateSystem coordinate system} type.
     *
     * @see #setCoordinateSystem
     */
    public static final String ELLIPSOIDAL = "ellipsoidal";

    /**
     * The cartesian {@linkplain CoordinateSystem coordinate system} type.
     *
     * @see #setCoordinateSystem
     */
    public static final String CARTESIAN = "cartesian";

    /**
     * The geophysics {@linkplain SampleDimension sample dimension} type.
     * Pixels in the {@linkplain java.awt.image.RenderedImage rendered image} produced by
     * the image reader contain directly geophysics values like temperature or elevation.
     * Sample type is typically {@code float} or {@code double} and missing value, if any,
     * <strong>must</strong> be one of {@linkplain Float#isNaN NaN values}.
     */
    public static final String GEOPHYSICS = "geophysics";

    /**
     * The packed {@linkplain SampleDimension sample dimension} type.
     * Pixels in the {@linkplain java.awt.image.RenderedImage rendered image} produced by
     * the image reader contain packed data, typically as {@code byte} or {@code short}
     * integer type. Conversions to geophysics values are performed by the application of
     * a scale and offset. Some special values are typically used for missing values.
     */
    public static final String PACKED = "packed";

    /**
     * Enumeration of valid coordinate reference system types.
     */
    static final List<String> CRS_TYPES = UnmodifiableArrayList.wrap(new String[] {
        GEOGRAPHIC, PROJECTED
    });

    /**
     * Enumeration of valid coordinate system types.
     */
    static final List<String> CS_TYPES = UnmodifiableArrayList.wrap(new String[] {
        ELLIPSOIDAL, CARTESIAN
    });

    /**
     * Enumeration of valid axis directions. We do not declare {@link String} constants for them
     * since they are already available as {@linkplain org.opengis.referencing.cs.AxisDirection
     * axis direction} code list.
     */
    static final List<String> DIRECTIONS = UnmodifiableArrayList.wrap(new String[] {
        "north", "east", "south", "west", "up", "down"
    });

    /**
     * Enumeration of valid pixel orientation. We do not declare {@link String} constants for them
     * since they are already available as {@linkplain org.opengis.metadata.spatial.PixelOrientation
     * pixel orientation} code list.
     */
    static final List<String> PIXEL_ORIENTATIONS = UnmodifiableArrayList.wrap(new String[] {
        "center", "lower left", "lower right", "upper right", "upper left"
    });

    /**
     * Enumeration of valid sample dimention types.
     */
    static final List<String> SAMPLE_TYPES = UnmodifiableArrayList.wrap(new String[] {
        GEOPHYSICS, PACKED
    });

    /**
     * The default instance. Will be created only when first needed.
     *
     * @see #getInstance
     */
    private static GeographicMetadataFormat DEFAULT;

    /**
     * Creates a default metadata format.
     */
    private GeographicMetadataFormat() {
        this(FORMAT_NAME, MAXIMUM_DIMENSIONS, MAXIMUM_BANDS);
    }

    /**
     * Creates a metadata format of the given name. Subclasses should invoke the various
     * {@link #addElement(String,String,int) addElement} or {@link #addAttribute addAttribute}
     * methods for adding new elements compared to the {@linkplain #getInstance default instance}.
     *
     * @param rootName the name of the root element.
     * @param maximumDimensions The maximum number of dimensions allowed for coordinate systems.
     * @param maximumBands The maximum number of sample dimensions allowed for images.
     */
    protected GeographicMetadataFormat(final String rootName,
            final int maximumDimensions, final int maximumBands)
    {
        super(rootName, CHILD_POLICY_SOME);
        /*
         * root
         *   +-- CoordinateReferenceSystem (name, type, WKT)
         *   |     +-- Datum (name)
         *   |     +-- CoordinateSystem (name, type)
         *   |           +-- Axis[0] (name, direction, units, origin)
         *   |           +-- Axis[1] (name, direction, units, origin)
         *   |           +-- ...etc...
         *   +-- GridGeometry (pixelOrientation)
         *         +-- GridRange
         *         |     +-- IndexRange[0] (minimum, maximum)
         *         |     +-- IndexRange[1] (minimum, maximum)
         *         |     +-- ...etc...
         *         +-- Envelope
         *         |     +-- CoordinateValues[0] (minimum, maximum)
         *         |     +-- CoordinateValues[1] (minimum, maximum)
         *         |     +-- ...etc...
         *         +-- AffineTransform (elements[6..n])
         */
        addElement  ("CoordinateReferenceSystem", rootName,    CHILD_POLICY_SOME);
        addAttribute("CoordinateReferenceSystem", "name",      DATATYPE_STRING);
        addAttribute("CoordinateReferenceSystem", "type",      DATATYPE_STRING, false, null, CRS_TYPES);
        addAttribute("CoordinateReferenceSystem", "WKT",       DATATYPE_STRING);
        addElement  ("Datum", "CoordinateReferenceSystem",     CHILD_POLICY_EMPTY);
        addAttribute("Datum",             "name",              DATATYPE_STRING, true,  null);
        addElement  ("CoordinateSystem",  "CoordinateReferenceSystem", 2, maximumDimensions);
        addAttribute("CoordinateSystem",  "name",              DATATYPE_STRING);
        addAttribute("CoordinateSystem",  "type",              DATATYPE_STRING, false, null, CS_TYPES);
        addElement  ("Axis",              "CoordinateSystem",  CHILD_POLICY_EMPTY);
        addAttribute("Axis",              "name",              DATATYPE_STRING);
        addAttribute("Axis",              "direction",         DATATYPE_STRING, false, null, DIRECTIONS);
        addAttribute("Axis",              "units",             DATATYPE_STRING);
        addAttribute("Axis",              "origin",            DATATYPE_STRING);
        addElement  ("GridGeometry",       rootName,           CHILD_POLICY_SOME);
        addAttribute("GridGeometry",      "pixelOrientation",  DATATYPE_STRING, false, "center", PIXEL_ORIENTATIONS);
        addElement  ("GridRange",         "GridGeometry",      CHILD_POLICY_SEQUENCE);
        addElement  ("IndexRange",        "GridRange",         CHILD_POLICY_EMPTY);
        addAttribute("IndexRange",        "minimum",           DATATYPE_INTEGER, true, "0");
        addAttribute("IndexRange",        "maximum",           DATATYPE_INTEGER, true, null); // inclusive
        addElement  ("Envelope",          "GridGeometry",      CHILD_POLICY_SEQUENCE);
        addElement  ("CoordinateValues",  "Envelope",          CHILD_POLICY_EMPTY);
        addAttribute("CoordinateValues",  "minimum",           DATATYPE_DOUBLE, true, null);
        addAttribute("CoordinateValues",  "maximum",           DATATYPE_DOUBLE, true, null); // inclusive
        addElement  ("AffineTransform",   "GridGeometry",      CHILD_POLICY_EMPTY);
        addAttribute("AffineTransform",   "elements",          DATATYPE_DOUBLE, true,
                6, maximumDimensions * (maximumDimensions - 1));
        /*
         * root
         *   +-- SampleDimensions (type)
         *         +-- SampleDimension[0] (name, scale, offset, minValue, maxValue, fillValues)
         *         +-- SampleDimension[1] (name, scale, offset, minValue, maxValue, fillValues)
         *         +-- ...etc...
         */
        addElement  ("SampleDimensions",  rootName,          1, maximumBands);
        addAttribute("SampleDimensions", "type",             DATATYPE_STRING, false, null, SAMPLE_TYPES);
        addElement  ("SampleDimension",  "SampleDimensions", CHILD_POLICY_SOME);
        addAttribute("SampleDimension",  "name",             DATATYPE_STRING);
        addAttribute("SampleDimension",  "scale",            DATATYPE_DOUBLE);
        addAttribute("SampleDimension",  "offset",           DATATYPE_DOUBLE);
        addAttribute("SampleDimension",  "minValue",         DATATYPE_DOUBLE);
        addAttribute("SampleDimension",  "maxValue",         DATATYPE_DOUBLE);
        addAttribute("SampleDimension",  "fillValues",       DATATYPE_DOUBLE, false, 0, Short.MAX_VALUE);
        /*
         * Allow users to specify fully-constructed GeoAPI objects.
         */
        addObjectValue("CoordinateReferenceSystem", CoordinateReferenceSystem.class);
        addObjectValue("Datum",                     Datum.class);
        addObjectValue("CoordinateSystem",          CoordinateSystem.class);
        addObjectValue("Axis",                      CoordinateSystemAxis.class);
        addObjectValue("GridGeometry",              GridGeometry.class);
        addObjectValue("Envelope",                  Envelope.class);
        addObjectValue("AffineTransform",           MathTransform.class);
        addObjectValue("SampleDimension",           SampleDimension.class);
    }

    /**
     * Adds an optional attribute of the specified data type.
     */
    private void addAttribute(final String elementName, final String attrName, final int dataType) {
        addAttribute(elementName, attrName, dataType, false, null);
    }

    /**
     * Adds an optional object value of the specified class.
     */
    private void addObjectValue(final String elementName, final Class<?> classType) {
        addObjectValue(elementName, classType, false, null);
    }

    /**
     * Returns {@code true} if the element (and the subtree below it) is allowed to appear
     * in a metadata document for an image of the given type. The default implementation
     * always returns {@code true}.
     */
    public boolean canNodeAppear(final String elementName, final ImageTypeSpecifier imageType) {
        return true;
    }

    /**
     * Returns the default geographic metadata format instance.
     */
    public static synchronized GeographicMetadataFormat getInstance() {
        if (DEFAULT == null) {
            DEFAULT = new GeographicMetadataFormat();
        }
        return DEFAULT;
    }
}
