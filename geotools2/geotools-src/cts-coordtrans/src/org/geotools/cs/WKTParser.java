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
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.WKTFormat;
import org.geotools.resources.WKTElement;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Parser for <cite>Well Know Text</cite> (WKT).
 * Instances of this class are thread-safe.
 *
 * @version $Id: WKTParser.java,v 1.3 2002/09/03 17:53:00 desruisseaux Exp $
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
     * Parses an <strong>optional</strong> "AUTHORITY" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AUTHORITY["<name>", "<code>"]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  parentName The name of the parent object being parsed.
     * @return The name with the autority code, or <code>parent</code>
     *         if no "AUTHORITY" element has been found. Never null.
     */
    private static CharSequence parseAuthority(final WKTElement parent, final CharSequence parentName)
        throws ParseException 
    {
        final WKTElement element = parent.pullOptionalElement("AUTHORITY");
        if (element == null) {
            return parentName;
        }
        final String name = element.pullString("name");        
        final String code = element.pullString("code");
        final InfoProperties.Named info = new InfoProperties.Named(parentName.toString());
        info.put("authority",     name);
        info.put("authorityCode", code);
        element.close();
        return info;
    }

    /**
     * Parses an "UNIT" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * UNIT["<name>", <conversion factor> {,<authority>}]
     * </code></blockquote>
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
        name = parseAuthority(element, name);
        element.close();
        return unit.scale(factor);
    }

    /**
     * Parses an "AXIS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * AXIS["<name>", NORTH | SOUTH | EAST | WEST | UP | DOWN | OTHER]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  required <code>true</code> if the axis is mandatory,
     *         or <code>false</code> if it is optional.
     * @return The "AXIS" element as a {@link AxisInfo} object, or <code>null</code>
     *         if the axis was not required and there is no axis object.
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
            return new AxisInfo(name, AxisOrientation.getEnum(orientation.keyword, locale));
        } catch (NoSuchElementException exception) {
            throw element.parseFailed(exception,
                    Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, orientation));
        }
    }

    /**
     * Parses a "PRIMEM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PRIMEM["<name>", <longitude> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  unit The contextual unit.
     * @return The "PRIMEM" element as a {@link PrimeMeridian} object.
     * @throws ParseException if the "PRIMEM" element can't be parsed.
     */
    private PrimeMeridian parsePrimem(final WKTElement parent, Unit unit) throws ParseException {
        WKTElement element = parent.pullElement("PRIMEM");
        CharSequence  name = element.pullString("name");
        double   longitude = element.pullDouble("longitude");
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createPrimeMeridian(name, unit, longitude);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses an <strong>optional</strong> "TOWGS84" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * TOWGS84[<dx>, <dy>, <dz>, <ex>, <ey>, <ez>, <ppm>]
     * </code></blockquote>
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
     * Parses a "SPHEROID" element. This element has the following pattern:
     *
     * <blockquote><code>
     * SPHEROID["<name>", <semi-major axis>, <inverse flattening> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "SPHEROID" element as an {@link Ellipsoid} object.
     * @throws ParseException if the "SPHEROID" element can't be parsed.
     */
    private Ellipsoid parseSpheroid(final WKTElement parent) throws ParseException {       
        WKTElement       element = parent.pullElement("SPHEROID");
        CharSequence        name = element.pullString("name");
        double     semiMajorAxis = element.pullDouble("semiMajorAxis");
        double inverseFlattening = element.pullDouble("inverseFlattening");
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createFlattenedSphere(name, semiMajorAxis, inverseFlattening, Unit.METRE);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "PROJECTION" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PROJECTION["<name>" {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @param  ellipsoid The ellipsoid, or <code>null</code> if none.
     * @return The "PROJECTION" element as a {@link Projection} object.
     * @throws ParseException if the "PROJECTION" element can't be parsed.
     */
    private Projection parseProjection(final WKTElement parent, final Ellipsoid ellipsoid)
        throws ParseException
    {                
        final WKTElement element = parent.pullElement("PROJECTION");
        final String   classname = element.pullString("name");
        final CharSequence  name = parseAuthority(element, classname);
        element.close();
                
        // Set the list of parameters. NOTE: Parameters are defined in
        // the parent WKTElement (usually a "PROJCS" element),  not in
        // this "PROJECTION" element.
        final ParameterList parameters = factory.createProjectionParameterList(classname);
        WKTElement param;
        while ((param=parent.pullOptionalElement("PARAMETER")) != null) {
            final String paramName  = param.pullString("name");
            final double paramValue = param.pullDouble("value");
            parameters.setParameter(paramName, paramValue);
        }
        if (ellipsoid != null) {
            final Unit axisUnit = ellipsoid.getAxisUnit();
            parameters.setParameter("semi_major", Unit.METRE.convert(ellipsoid.getSemiMajorAxis(), axisUnit));
            parameters.setParameter("semi_minor", Unit.METRE.convert(ellipsoid.getSemiMinorAxis(), axisUnit));
        }
        try {
            return factory.createProjection(name, classname, parameters);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * DATUM["<name>", <spheroid> {,<to wgs84>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "DATUM" element as a {@link HorizontalDatum} object.
     * @throws ParseException if the "DATUM" element can't be parsed.
     */
    private HorizontalDatum parseDatum(final WKTElement parent) throws ParseException {
        WKTElement          element = parent.pullElement("DATUM");
        CharSequence           name = element.pullString("name");
        Ellipsoid         ellipsoid = parseSpheroid(element);
        WGS84ConversionInfo toWGS84 = parseToWGS84(element); // Optional; may be null.
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createHorizontalDatum(name, DatumType.GEOCENTRIC, ellipsoid, toWGS84);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "VERT_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_DATUM" element as a {@link VerticalDatum} object.
     * @throws ParseException if the "VERT_DATUM" element can't be parsed.
     */
    private VerticalDatum parseVertDatum(final WKTElement parent) throws ParseException {        
        WKTElement element = parent.pullElement("VERT_DATUM");
        CharSequence  name = element.pullString     ("name");
        WKTElement   datum = element.pullVoidElement("datum");
        name = parseAuthority(element, name);
        element.close();
        final DatumType.Vertical type;
        try {
            type = (DatumType.Vertical) DatumType.getEnum(datum.keyword);
        } catch (RuntimeException exception) {
            // Include 'NoSuchElementException' and 'ClassCastException'
            throw element.parseFailed(exception,
                    Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, datum));
        }
        try {
            return factory.createVerticalDatum(name, type);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }
    
    /**
     * Parses a "LOCAL_DATUM" element. This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_DATUM["<name>", <datum type> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_DATUM" element as a {@link LocalDatum} object.
     * @throws ParseException if the "LOCAL_DATUM" element can't be parsed.
     */
    private LocalDatum parseLocalDatum(final WKTElement parent) throws ParseException {
        WKTElement element = parent.pullElement("LOCAL_DATUM");
        CharSequence  name = element.pullString     ("name");
        WKTElement   datum = element.pullVoidElement("datum");
        name = parseAuthority(element, name);
        element.close();
        final DatumType.Local type;
        try {
            type = (DatumType.Local) DatumType.getEnum(datum.keyword);
        } catch (RuntimeException exception) {
            // Include 'NoSuchElementException' and 'ClassCastException'
            throw element.parseFailed(exception,
                    Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, datum));
        }
        try {
            return factory.createLocalDatum(name, type);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "LOCAL_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * LOCAL_CS["<name>", <local datum>, <unit>, <axis>, {,<axis>}* {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "LOCAL_CS" element as a {@link LocalCoordinateSystem} object.
     * @throws ParseException if the "LOCAL_CS" element can't be parsed.
     */
    private LocalCoordinateSystem parseLocalCS(final WKTElement parent) throws ParseException {        
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
        try {
            return factory.createLocalCoordinateSystem(name, datum, unit, array);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "GEOCCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * GEOCCS["<name>", <datum>, <prime meridian>,  <linear unit>
     *        {,<axis> ,<axis> ,<axis>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOCCS" element as a {@link GeocentricCoordinateSystem} object.
     * @throws ParseException if the "GEOCCS" element can't be parsed.
     */
    private GeocentricCoordinateSystem parseGeoCCS(final WKTElement parent) throws ParseException {        
        WKTElement     element = parent.pullElement("GEOCCS");
        CharSequence      name = element.pullString("name");
        HorizontalDatum  datum = parseDatum (element);
        PrimeMeridian meridian = parsePrimem(element, Unit.DEGREE);
        Unit              unit = parseUnit  (element, Unit.METRE);
        AxisInfo[] axes = new AxisInfo[3];
        axes[0] = parseAxis(element, false);
        if (axes[0] != null) {
            axes[1] = parseAxis(element, true);
            axes[2] = parseAxis(element, true);
        }
        else {
            axes = null;
        }
        name = parseAuthority(element, name);                
        element.close();
        // TODO: There is no createGeocentricCoordinateSystem in CoordinateSystemFactory !!!
        if (axes != null) {
            return new GeocentricCoordinateSystem(name, unit, datum, meridian, axes);
        } else {
            return new GeocentricCoordinateSystem(name, unit, datum, meridian);
        }
    }        

    /**
     * Parses an <strong>optional</strong> "VERT_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * VERT_CS["<name>", <vert datum>, <linear unit>, {<axis>,} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "VERT_CS" element as a {@link VerticalCoordinateSystem} object.
     * @throws ParseException if the "VERT_CS" element can't be parsed.
     */
    private VerticalCoordinateSystem parseVertCS(final WKTElement parent) throws ParseException { 
        final WKTElement element = parent.pullElement("VERT_CS");
        if (element == null) {
            return null;
        }
        CharSequence   name = element.pullString("name");
        VerticalDatum datum = parseVertDatum(element);
        Unit           unit = parseUnit(element, Unit.METRE);
        AxisInfo       axis = parseAxis(element, false);
        name = parseAuthority(element, name);
        element.close();
        if (axis == null) {
            axis = AxisInfo.ALTITUDE;
        }
        try {
            return factory.createVerticalCoordinateSystem(name, datum, unit, axis);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "GEOGCS" element. This element has the following pattern:
     *
     * <blockquote><code>
     * GEOGCS["<name>", <datum>, <prime meridian>, <angular unit>  {,<twin axes>} {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "GEOGCS" element as a {@link GeographicCoordinateSystem} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private GeographicCoordinateSystem parseGeoGCS(final WKTElement parent) throws ParseException {
        WKTElement     element = parent.pullElement("GEOGCS");
        CharSequence      name = element.pullString("name");
        HorizontalDatum  datum = parseDatum(element);
        Unit              unit = parseUnit(element, Unit.RADIAN);
        PrimeMeridian meridian = parsePrimem(element, unit);
        AxisInfo         axis0 = parseAxis(element, false);
        AxisInfo         axis1;
        if (axis0 != null) {
            axis1 = parseAxis(element, true);
        } else {
            axis0 = AxisInfo.LONGITUDE;
            axis1 = AxisInfo.LATITUDE;
        }
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createGeographicCoordinateSystem(name, unit, datum, meridian, axis0, axis1);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }

    /**
     * Parses a "PROJCS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * PROJCS["<name>", <geographic cs>, <projection>, {<parameter>,}*,
     *        <linear unit> {,<twin axes>}{,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PROJCS" element as a {@link ProjectedCoordinateSystem} object.
     * @throws ParseException if the "GEOGCS" element can't be parsed.
     */
    private ProjectedCoordinateSystem parseProjCS(final WKTElement parent) throws ParseException {
        WKTElement             element = parent.pullElement("PROJCS");
        CharSequence              name = element.pullString("name");
        GeographicCoordinateSystem gcs = parseGeoGCS(element);
        Projection          projection = parseProjection(element, gcs.getHorizontalDatum().getEllipsoid());
        Unit                      unit = parseUnit(element, Unit.METRE);
        AxisInfo                 axis0 = parseAxis(element, false);
        AxisInfo                 axis1;
        if (axis0 != null) {
            axis1 = parseAxis(element, true);
        } else {
            axis0 = AxisInfo.X;
            axis1 = AxisInfo.Y;
        }
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createProjectedCoordinateSystem(name, gcs, projection, unit, axis0, axis1);      
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }        

    /**
     * Parses a "COMPD_CS" element.
     * This element has the following pattern:
     *
     * <blockquote><code>
     * COMPD_CS["<name>", <head cs>, <tail cs> {,<authority>}]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "COMPD_CS" element as a {@link CompoundCoordinateSystem} object.
     * @throws ParseException if the "COMPD_CS" element can't be parsed.
     */
    private CompoundCoordinateSystem parseCompdCS(final WKTElement parent) throws ParseException
    {        
        WKTElement      element = parent.pullElement("COMPD_CS");
        CharSequence       name = element.pullString("name");
        CoordinateSystem headCS = parseCoordinateSystem(element);
        CoordinateSystem tailCS = parseCoordinateSystem(element);
        name = parseAuthority(element, name);
        element.close();
        try {
            return factory.createCompoundCoordinateSystem(name, headCS, tailCS);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }
    }
    
    /**
     * Parses a coordinate system element.
     *
     * @param  parent The parent element.
     * @return The next element as a {@link CoordinateSystem} object.
     * @throws ParseException if the next element can't be parsed.
     */
    private CoordinateSystem parseCoordinateSystem(final WKTElement element) throws ParseException
    {
        final Object key = element.peek();
        if (key instanceof WKTElement) {
            final String keyword = ((WKTElement) key).keyword.trim().toUpperCase(locale);
            if (  "GEOGCS".equals(keyword)) return parseGeoGCS (element);
            if (  "PROJCS".equals(keyword)) return parseProjCS (element);
            if (  "GEOCCS".equals(keyword)) return parseGeoCCS (element);
            if ( "VERT_CS".equals(keyword)) return parseVertCS (element);
            if ("LOCAL_CS".equals(keyword)) return parseLocalCS(element);
            if ("COMPD_CS".equals(keyword)) return parseCompdCS(element);
        }
        throw element.parseFailed(null, Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, key));
    }

    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The object.
     * @throws ParseException if the element can't be parsed.
     */
    protected Object parse(final WKTElement element) throws ParseException {
        final Object key = element.peek();
        if (key instanceof WKTElement) {
            final String keyword = ((WKTElement) key).keyword.trim().toUpperCase(locale);
            if (       "AXIS".equals(keyword)) return parseAxis      (element, true);
            if (     "PRIMEM".equals(keyword)) return parsePrimem    (element, Unit.DEGREE);
            if (    "TOWGS84".equals(keyword)) return parseToWGS84   (element);
            if (   "SPHEROID".equals(keyword)) return parseSpheroid  (element);
            if (      "DATUM".equals(keyword)) return parseDatum     (element);
            if ( "VERT_DATUM".equals(keyword)) return parseVertDatum (element);
            if ("LOCAL_DATUM".equals(keyword)) return parseLocalDatum(element);
        }
        return parseCoordinateSystem(element);
    }

    /**
     * Parses a coordinate system element.
     *
     * @param  text The text to be parsed.
     * @return The coordinate system.
     * @throws ParseException if the string can't be parsed.
     */
    public CoordinateSystem parseCoordinateSystem(final String text) throws ParseException {
        final WKTElement element = getTree(text, new ParsePosition(0));
        final CoordinateSystem cs = parseCoordinateSystem(element);
        element.close();
        return cs;
    }

    /**
     * Format the specified object. Current implementation just append {@link Object#toString},
     * since the <code>toString()</code> implementation for most {@link org.geotools.cs.Info}
     * objects is to returns a WKT.
     *
     * @task TODO: Provides pacakge private <code>Info.toString(WKTFormat)</code> implementations.
     *             It would allows us to invoke <code>((Info)obj).toString(this)</code> here.
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(obj);
    }
}
