/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.cs;

// J2SE dependencies
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.NoSuchElementException;

// JAI dependencies
import javax.media.jai.ParameterList;

// Parsing
import java.util.Locale;
import java.text.ParsePosition;
import java.text.ParseException;

// Logging
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Parser for <cite>Well Know Text</cite> (WKT).
 * Instances of this class are thread-safe.
 *
 * @version $Id: WKTParser.java,v 1.1 2002/09/02 17:55:39 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
final class WKTParser extends WKTFormat {
    /**
     * The factory to use for creating coordinate systems.
     */                    
    private CoordinateSystemFactory factory;
    
    /**
     * Construct a parser for the specified locale.
     *
     * @param local   The locale for parsing and formatting numbers.
     * @param factory The factory for constructing coordinate systems.
     */
    public WKTParser(final Locale locale, final CoordinateSystemFactory factory) {
        super(locale);
        this.factory = factory;
    }
    
    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     */
    public Object parseObject(final String source, final ParsePosition pos) {
        return null;
    }

    /**
     * Parse an <strong>optional</strong> "AUTHORITY" element.
     * This element has the following pattern:
     *
     * <blockquote><pre>
     * AUTHORITY["<name>", "<code>"]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @param  parentName The name of the parent object being parsed.
     * @return The name with the autority code, or <code>parent</code>
     *         if no "AUTHORITY" element has been found. Never null.
     */
    private static CharSequence parseAuthority(final WKTElement parent, final CharSequence parentName)
        throws ParseException 
    {
        final WKTElement authority = parent.pullElement("AUTHORITY");
        if (authority == null) {
            return parentName;
        }
        final String name = authority.pullString("name");        
        final String code = authority.pullString("code");
        final InfoProperties.Named info = new InfoProperties.Named(parentName.toString());
        info.put("authority",     name);
        info.put("authorityCode", code);
        authority.close();
        return info;
    }

    /**
     * Parse an "UNIT" element.
     * This element has the following pattern:
     *
     * <blockquote><pre>
     * UNIT["<name>", <conversion factor> {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit. Usually {@link Unit#DEGREE} or {@link Unit#METRE}.
     * @return The "UNIT" element as an {@link Unit} object.
     * @throws ParseException if the "UNIT" can't be parsed.
     */
    private static Unit parseUnit(final WKTElement parent, final Unit unit)
        throws ParseException
    {
        WKTElement element = parent.pullElement("UNIT");
        CharSequence  name = element.pullString("name");
        double      factor = element.pullDouble("factor");
        name = parseAuthority(parent, name);        
        element.close();
        return unit.scale(factor);
    }

    /**
     * Parse an "AXIS" element.
     * This element has the following pattern:
     *
     * <blockquote><pre>
     * AXIS["<name>", NORTH | SOUTH | EAST | WEST | UP | DOWN | OTHER]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @param  required <code>true</code> if an axis is required, or <code>false</code> otherwise.
     * @return The "AXIS" element as a {@link AxisInfo} object, or <code>null</code> if the axis
     *         was not required and there is no axis object.
     * @throws ParseException if the "AXIS" element can't be parsed.
     */
    private AxisInfo parseAxis(final WKTElement parent, final boolean required)
        throws ParseException 
    {
        final WKTElement element;
        if (required) {
            element = parent.pullElement("AXIS");
        } else {
            element = parent.pullOptionalElement("AXIS");
            if (element == null) {
                return null;
            }
        }
        final String            name = element.pullString     ("name");
        final WKTElement orientation = element.pullVoidElement("orientation");
        element.close();
        try {
            return new AxisInfo(name, AxisOrientation.getEnum(orientation.toString(), locale));
        } catch (NoSuchElementException exception) {
            final ParseException e = new ParseException(Resources.format(
                    ResourceKeys.ERROR_UNKNOW_TYPE_$1, orientation), orientation.offset);
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Parse a "PRIMEM" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * PRIMEM["<name>", <longitude> {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit.
     * @return The "PRIMEM" element as a {@link PrimeMeridian} object.
     * @throws ParseException if the "PRIMEM" element can't be parsed.
     * @throws FactoryException if the {@link PrimeMeridian} object can't be created.
     */
    private PrimeMeridian parsePrimem(final WKTElement parent, final Unit unit)
        throws ParseException, FactoryException
    {
        WKTElement element = parent.pullElement("PRIMEM");
        CharSequence  name = element.pullString("name");
        double   longitude = element.pullDouble("longitude");
        name = parseAuthority(element, name);
        element.close();
        return factory.createPrimeMeridian(name, unit, longitude);
    }

    /**
     * Parse an <strong>optional</strong> "TOWGS84" element.
     * This element has the following pattern:
     *
     * <blockquote><pre>
     * TOWGS84[<dx>, <dy>, <dz>, <ex>, <ey>, <ez>, <ppm>]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "TOWGS84" element as a {@link WGS84ConversionInfo} object,
     *         or <code>null</code> if no "TOWGS84" has been found.
     * @throws ParseException if the "TOWGS84" can't be parsed.
     */
    private static WGS84ConversionInfo parseToWGS84(final WKTElement parent)
        throws ParseException 
    {          
        final WKTElement element = parent.pullOptionalElement("TOWGS84");
        if (element == null) {
            return null;
        }
        final WGS84ConversionInfo info = new WGS84ConversionInfo();
        info.dx  = element.pullDouble("dx");
        info.dy  = element.pullDouble("dy");
        info.dz  = element.pullDouble("dz");
        info.ex  = element.pullDouble("ex");
        info.ey  = element.pullDouble("ey");
        info.ez  = element.pullDouble("ez");
        info.ppm = element.pullDouble("ppm");
        element.close();
        return info;
    }

    /**
     * Parse a "SPHEROID" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * SPHEROID["<name>", <semi-major axis>, <inverse flattening> {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "SPHEROID" element as an {@link Ellipsoid} object.
     * @throws ParseException if the "SPHEROID" element can't be parsed.
     * @throws FactoryException if the {@link Ellipsoid} object can't be created.
     */
    private Ellipsoid parseSpheroid(final WKTElement parent)
        throws ParseException, FactoryException
    {       
        WKTElement       element = parent.pullElement("SPHEROID");
        CharSequence        name = element.pullString("name");
        double     semiMajorAxis = element.pullDouble("semiMajorAxis");
        double inverseFlattening = element.pullDouble("inverseFlattening");
        name = parseAuthority(element, name);
        element.close();
        return factory.createFlattenedSphere(name, semiMajorAxis, inverseFlattening, Unit.METRE);
    }

    /**
     * Parse a "DATUM" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * DATUM["<name>", <spheroid> {,<to wgs84>} {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "DATUM" element as a {@link HorizontalDatum} object.
     * @throws ParseException if the "DATUM" element can't be parsed.
     * @throws FactoryException if the {@link HorizontalDatum} object can't be created.
     */
    private HorizontalDatum parseDatum(final WKTElement parent)
        throws ParseException, FactoryException
    {
        WKTElement          element = parent.pullElement("DATUM");
        CharSequence           name = element.pullString("name");
        Ellipsoid         ellipsoid = parseSpheroid(element);
        WGS84ConversionInfo toWGS84 = parseToWGS84(element); // Optional; may be null.
        name = parseAuthority(element, name);
        element.close();
        return factory.createHorizontalDatum(name, DatumType.GEOCENTRIC, ellipsoid, toWGS84);
    }        

    /**
     * Parse a "VERT_DATUM" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * VERT_DATUM["<name>", <datum type> {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_DATUM" element as a {@link VerticalDatum} object.
     * @throws ParseException if the "VERT_DATUM" element can't be parsed.
     * @throws FactoryException if the {@link VerticalDatum} object can't be created.
     *
     * @task HACK: Must implement DatumType.getEnum(String) and use it here.
     */
    private VerticalDatum parseVertDatum(final WKTElement parent)
        throws ParseException, FactoryException
    {        
        WKTElement element = parent.pullElement("VERT_DATUM");
        CharSequence  name = element.pullString("name");
        final String datum = element.pullString("datum");
        name = parseAuthority(element, name);
        element.close();
        if (!warningVerticalDatum) {
            warningVerticalDatum = true;
            Logger.getLogger("org.geotools.cs").warning("Verticlm datum parsing not yet implemented");
        }
        return factory.createVerticalDatum(name, /*DatumType.getEnum(datum)*/DatumType.ELLIPSOIDAL);
    }
    /** Temporary patch */ private static boolean warningVerticalDatum;
    
    /**
     * Parse a "LOCAL_DATUM" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * LOCAL_DATUM["<name>", <datum type> {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_DATUM" element as a {@link LocalDatum} object.
     * @throws ParseException if the "LOCAL_DATUM" element can't be parsed.
     * @throws FactoryException if the {@link LocalDatum} object can't be created.
     *
     * @task HACK: Must implement DatumType.getEnum(String) and use it here.
     */
    private LocalDatum parseLocalDatum(final WKTElement parent)
        throws ParseException, FactoryException
    {
        WKTElement element = parent.pullElement("LOCAL_DATUM");
        CharSequence  name = element.pullString("name");
        String       datum = element.pullString("datum");
        name = parseAuthority(element, name);
        element.close();
        if (!warningLocalDatum) {
            warningLocalDatum = true;
            Logger.getLogger("org.geotools.cs").warning("Verticlm datum parsing not yet implemented");
        }
        return factory.createLocalDatum(name, /*(DatumType.Local)DatumType.getEnum(datum)*/
                             (DatumType.Local) DatumType.getEnum(DatumType.Local.MINIMUM));
    }
    /** Temporary patch */ private static boolean warningLocalDatum;

    /**
     * Parse a "VERT_CS" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * VERT_CS["<name>", <vert datum>, <linear unit>, {<axis>,} {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_CS" element as a {@link VerticalCoordinateSystem} object.
     * @throws ParseException if the "VERT_CS" element can't be parsed.
     * @throws FactoryException if the {@link VerticalCoordinateSystem} object can't be created.
     */
    private VerticalCoordinateSystem parseVertCS(final WKTElement parent)
        throws ParseException, FactoryException
    { 
        WKTElement  element = parent.pullElement("VERT_CS");
        CharSequence   name = element.pullString("name");
        VerticalDatum datum = parseVertDatum(element);
        Unit           unit = parseUnit(element, Unit.METRE);
        AxisInfo       axis = parseAxis(element, false);
        name = parseAuthority(element, name);
        element.close();
        if (axis == null) {
            axis = AxisInfo.ALTITUDE;
        }
        return factory.createVerticalCoordinateSystem(name, datum, unit, axis);
    }

    /**
     * Parse a "LOCAL_CS" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * LOCAL_CS["<name>", <local datum>, <unit>, <axis>, {,<axis>}* {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_CS" element as a {@link LocalCoordinateSystem} object.
     * @throws ParseException if the "LOCAL_CS" element can't be parsed.
     * @throws FactoryException if the {@link LocalCoordinateSystem} object can't be created.
     */
    private LocalCoordinateSystem parseLocalCS(final WKTElement parent)
        throws ParseException, FactoryException
    {        
        WKTElement element = parent.pullElement("LOCAL_CS");
        CharSequence  name = element.pullString("name");
        LocalDatum   datum = parseLocalDatum(element);
        Unit          unit = parseUnit(element, Unit.METRE);
        AxisInfo      axis = parseAxis(element, true);
        List          list = new ArrayList();  
        AxisInfo candidate;
        while ((candidate=parseAxis(element, false)) != null) {
            list.add(candidate);
        }
        name = parseAuthority(element, name);
        element.close();
        AxisInfo[] array = (AxisInfo[]) list.toArray(new AxisInfo[list.size()]);
        return factory.createLocalCoordinateSystem(name, datum, unit, array);
    }        

    /**
     * Parse a "GEOGCS" element. This element has the following pattern:
     *
     * <blockquote><pre>
     * GEOGCS["<name>", <datum>, <prime meridian>, <angular unit>  {,<twin axes>} {,<authority>}]
     * </pre></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOGCS" element as a {@link GeographicCoordinateSystem} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     * @throws FactoryException if the {@link GeographicCoordinateSystem} object can't be created.
     */
    private GeographicCoordinateSystem parseGeoGCS(final WKTElement parent)
        throws ParseException, FactoryException
    {
        WKTElement     element = parent.pullElement("GEOGCS");
        CharSequence      name = element.pullString("name");
        HorizontalDatum  datum = parseDatum(element);
        Unit              unit = parseUnit(element, Unit.RADIAN);
        PrimeMeridian meridian = parsePrimem(element, unit);

        AxisInfo axis0;
        AxisInfo axis1;
        axis0 = parseAxis(element, false);
        if (axis0 != null) {
            axis1 = parseAxis(element, true);
        } else {
            axis0 = AxisInfo.LONGITUDE;
            axis1 = AxisInfo.LATITUDE;
        }
        name = parseAuthority(element, name);
        element.close();
        return factory.createGeographicCoordinateSystem(name, unit, datum, meridian, axis0, axis1);
    }        
}
