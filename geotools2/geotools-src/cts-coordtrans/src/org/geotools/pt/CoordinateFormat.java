/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.pt;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;
import java.text.Format;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.AxisOrientation;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.cs.TemporalCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.GeocentricCoordinateSystem;
import org.geotools.ct.CannotCreateTransformException;
import org.geotools.resources.CTSUtilities;


/**
 * Format a {@link CoordinatePoint} in an arbitrary {@link CoordinateSystem}. The format for
 * each ordinate is infered from the coordinate system units using the following rules:
 * <ul>
 *   <li>Ordinate values in {@linkplain Unit#DEGREE degrees} are formated as angles
 *       using {@link AngleFormat}.</li>
 *   <li>Ordinate values in any unit compatible with {@linkplain Unit#SECOND seconds}
 *       are formated as dates using {@link DateFormat}.</li>
 *   <li>All other values are formatted as numbers using {@link NumberFormat}.</li>
 * </ul>
 *
 * <strong>Note:</strong> parsing is not yet implemented in this version.
 *
 * @version $Id: CoordinateFormat.java,v 1.3 2003/05/13 10:58:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CoordinateFormat extends Format {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8826750502538985873L;
    
    /**
     * The output coordinate system.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * The formats to uses for formatting. This array's length must be equals
     * to the {@linkplain #getCoordinateSystem coordinate system}'s dimension.
     * This array is never <code>null</code>.
     */
    private Format[] formats;

    /**
     * The type for each value in the <code>formats</code> array.
     * Types are: 0=number, 1=longitude, 2=latitude, 3=other angle,
     * 4=date, 5=ellapsed time.
     * This array is never <code>null</code>.
     */
    private byte[] types;

    /**
     * Constants for the <code>types</code> array.
     */
    private static final byte LONGITUDE=1, LATITUDE=2, ANGLE=3, DATE=4, TIME=5;

    /**
     * The time epochs. Non-null only if at least one ordinate is a date.
     */
    private long[] epochs;

    /**
     * Dummy field position.
     */
    private final FieldPosition dummy = new FieldPosition(0);

    /**
     * The locale for formatting coordinates and numbers.
     */
    private final Locale locale;

    /**
     * Construct a new coordinate format with default locale and
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984} coordinate system.
     */
    public CoordinateFormat() {
        this(Locale.getDefault());
    }

    /**
     * Construct a new coordinate format for the specified locale and
     * {@linkplain GeographicCoordinateSystem#WGS84 WGS 1984} coordinate system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     */
    public CoordinateFormat(final Locale locale) {
        this(locale, GeographicCoordinateSystem.WGS84);
    }

    /**
     * Construct a new coordinate format for the specified locale and coordinate system.
     *
     * @param locale The locale for formatting coordinates and numbers.
     * @param cs     The output coordinate system.
     */
    public CoordinateFormat(final Locale locale, final CoordinateSystem cs) {
        this.locale = locale;
        setCoordinateSystem(cs);
    }

    /**
     * Returns the coordinate system for points to be formatted.
     *
     * @param output The output coordinate system.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the coordinate system for points to be formatted. The number
     * of dimensions must matched the dimension of points to be formatted.
     *
     * @param cs The new coordinate system.
     */
    public void setCoordinateSystem(final CoordinateSystem cs) {
        if (!cs.equals(coordinateSystem, false)) {
            Format numberFormat = null;
            Format  angleFormat = null;
            Format   dateFormat = null;
            /*
             * Reuse existing formats. It is necessary in order to avoid
             * overwritting any setting done with 'setNumberPattern(...)'
             * or 'setAnglePattern(...)'
             */
            if (formats != null) {
                for (int i=formats.length; --i>=0;) {
                    final Format format = formats[i];
                    if (format instanceof NumberFormat) {
                        numberFormat = format;
                    } else if (format instanceof AngleFormat) {
                        angleFormat = format;
                    } else if (format instanceof DateFormat) {
                        dateFormat = format;
                    }
                }
            }
            /*
             * Create a new array of 'Format' objects, one for each dimension.
             * The format subclasses are infered from coordinate system axis.
             */
            epochs  = null;
            formats = new Format[cs.getDimension()];
            types   = new byte[formats.length];
            for (int i=0; i<formats.length; i++) {
                final Unit unit = cs.getUnits(i);
                /////////////////
                ////  Angle  ////
                /////////////////
                if (Unit.DEGREE.equals(unit)) {
                    if (angleFormat == null) {
                        angleFormat = new AngleFormat("DD°MM.m'", locale);
                    }
                    formats[i] = angleFormat;
                    final AxisOrientation axis = cs.getAxis(i).orientation.absolute();
                    if (AxisOrientation.EAST.equals(axis)) {
                        types[i] = LONGITUDE;
                    } else if (AxisOrientation.NORTH.equals(axis)) {
                        types[i] = LATITUDE;
                    } else {
                        types[i] = ANGLE;
                    }
                    continue;
                }
                ////////////////
                ////  Date  ////
                ////////////////
                if (Unit.SECOND.canConvert(unit)) {
                    final CoordinateSystem tcs = CTSUtilities.getSubCoordinateSystem(cs, i, i+1);
                    if (tcs instanceof TemporalCoordinateSystem) {
                        if (epochs == null) {
                            epochs = new long[formats.length];
                        }
                        epochs[i] = ((TemporalCoordinateSystem) tcs).getEpoch().getTime();
                        if (dateFormat == null) {
                            dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                        }
                        formats[i] = dateFormat;
                        types[i] = DATE;
                        continue;
                    }
                    types[i] = TIME;
                    // Fallthrough: formatted as number for now.
                }
                //////////////////
                ////  Number  ////
                //////////////////
                if (numberFormat == null) {
                    numberFormat = NumberFormat.getNumberInstance(locale);
                }
                formats[i] = numberFormat;
                // types[i] default to 0.
            }
        }
        coordinateSystem = cs;
    }

    /**
     * Set the pattern for numbers fields.  If some ordinates are formatted as plain number
     * (for example in {@linkplain GeocentricCoordinateSystem geocentric} coordinate system),
     * then those numbers will be formatted using this pattern.
     *
     * @param pattern The number pattern as specified in {@link DecimalFormat}.
     */
    public void setNumberPattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof DecimalFormat)) {
                ((DecimalFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Set the pattern for angles fields. If some ordinates are formatted as angle (for
     * example in {@linkplain GeographicCoordinateSystem geographic} coordinate system),
     * then those angles will be formatted using this pattern.
     *
     * @param pattern The angle pattern as specified in {@link AngleFormat}.
     */
    public void setAnglePattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof AngleFormat)) {
                ((AngleFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Set the pattern for dates fields. If some ordinates are formatted as date (for
     * example in {@linkplain TemporalCoordinateSystem temporal} coordinate system),
     * then those dates will be formatted using this pattern.
     *
     * @param pattern The date pattern as specified in {@link SimpleDateFormat}.
     */
    public void setDatePattern(final String pattern) {
        Format lastFormat = null;
        for (int i=0; i<formats.length; i++) {
            final Format format = formats[i];
            if (format!=lastFormat && (format instanceof SimpleDateFormat)) {
                ((SimpleDateFormat) format).applyPattern(pattern);
                lastFormat = format;
            }
        }
    }

    /**
     * Formats a coordinate point.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param point      The {@link CoordinatePoint} to format.
     * @return           The formatted coordinate point.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public String format(final CoordinatePoint point) {
        return format(point, new StringBuffer(), null).toString();
    }
    
    /**
     * Formats a coordinate point and appends the resulting text to a given string buffer.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param point      The {@link CoordinatePoint} to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return           The string buffer passed in as <code>toAppendTo</code>, with formatted
     *                   text appended.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final CoordinatePoint point,
                               final StringBuffer    toAppendTo,
                               final FieldPosition   position)
            throws IllegalArgumentException
    {
        point.ensureDimensionMatch(formats.length);
        for (int i=0; i<formats.length; i++) {
            final double value = point.getOrdinate(i);
            final Object object;
            switch (types[i]) {
                default:        object=new Double   (value); break;
                case LONGITUDE: object=new Longitude(value); break;
                case LATITUDE:  object=new Latitude (value); break;
                case ANGLE:     object=new Angle    (value); break;
                case DATE: {
                    long offset = Math.round(Unit.MILLISECOND.convert(value,
                                             coordinateSystem.getUnits(i)));
                    if (AxisOrientation.PAST.equals(coordinateSystem.getAxis(i).orientation)) {
                        offset = -offset;
                    }
                    object = new Date(epochs[i] + offset);
                    break;
                }
            }
            if (i!=0) {
                toAppendTo.append(' ');
            }
            formats[i].format(object, toAppendTo, dummy);
        }
        return toAppendTo;
    }

    /**
     * Formats a coordinate point and appends the resulting text to a given string buffer.
     * The coordinate point dimension must matches the {@linkplain #getCoordinateSystem()
     * coordinate system} dimension.
     *
     * @param object     The {@link CoordinatePoint} to format.
     * @param toAppendTo Where the text is to be appended.
     * @param position   A <code>FieldPosition</code> identifying a field in the formatted text,
     *                   or <code>null</code> if none.
     * @return           The string buffer passed in as <code>toAppendTo</code>, with formatted
     *                   text appended.
     * @throws NullPointerException if <code>toAppendTo</code> is null.
     * @throws IllegalArgumentException if this <code>CoordinateFormat</code>
     *         cannot format the given object.
     */
    public StringBuffer format(final Object        object,
                               final StringBuffer  toAppendTo,
                               final FieldPosition position)
            throws IllegalArgumentException
    {
        if (object instanceof CoordinatePoint) {
            return format((CoordinatePoint) object, toAppendTo, position);
        } else {
            throw new IllegalArgumentException(String.valueOf(object));
        }
    }
    
    /**
     * Not yet implemented.
     */
    public Object parseObject(final String source, final ParsePosition position) {
        throw new UnsupportedOperationException();
    }
}
