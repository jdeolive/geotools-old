/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.cs;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.IllegalPathStateException;
import java.util.ArrayList;
import java.util.List;

// JTS dependencies
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.GeometryFactory;

// Geotools dependencies
import org.geotools.cs.Ellipsoid;
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.pt.CoordinatePoint;
import org.geotools.pt.CoordinateFormat;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Performs geodetic calculations on an ellipsoid. This class encapsulate a generic ellipsoid
 * and calculate the following properties:
 * <ul>
 *   <li>Distance and azimuth between two points.</li>
 *   <li>Point located at a given distance and azimuth from an other point.</li>
 * </ul>
 *
 * <br>
 * Note: This class is not thread-safe. If geodetic calculations are needed in a multi-threads
 * environment, create one instance of <code>GeodeticCalculator</code> for each thread even
 * if the computations are performed with the same ellipsoid.
 *
 * @version $Id: GeodeticCalculator.java,v 1.1 2004/03/08 17:48:27 desruisseaux Exp $
 * @author Daniele Franzoni
 * @author Martin Desruisseaux
 */
public class GeodeticCalculator {
    /**
     * Tolerance factors from the strictest (<code>TOLERANCE_0</CODE>)
     * to the most relax one (<code>TOLERANCE_3</CODE>).
     */
    private static final double TOLERANCE_0 = 5.0e-15,  // tol0
                                TOLERANCE_1 = 5.0e-14,  // tol1
                                TOLERANCE_2 = 5.0e-13,  // tt
                                TOLERANCE_3 = 7.0e-3;   // tol2
    
    /**
     * The encapsulated ellipsoid.
     */
    private final Ellipsoid ellipsoid;

    /*
     * The semi major axis of the refereced ellipsoid.
     */
    private final double semiMajorAxis;

    /*
     * The semi minor axis of the refereced ellipsoid.
     */
    private final double semiMinorAxis;

    /*
     * The eccenticity squared of the refereced ellipsoid.
     */
    private final double eccentricitySquared; 

    /*
     * The maximum orthodromic distance that could be calculated onto the referenced ellipsoid.
     */
    private final double maxOrthodromicDistance; 

    /**
     * GPNARC parameters computed from the ellipsoid.
     */
    private final double A, B, C, D, E, F;

    /**
     * GPNHRI parameters computed from the ellipsoid.
     *
     * <code>f</code> if the flattening of the referenced ellipsoid. <code>f2</code>,
     * <code>f3</code> and <code>f4</code> are <var>f<sup>2</sup></var>,
     * <var>f<sup>3</sup></var> and <var>f<sup>4</sup></var> respectively.
     */
    private final double fo, f, f2, f3, f4;

    /**
     * Parameters computed from the ellipsoid.
     */
    private final double T1, T2, T4, T6;

    /**
     * Parameters computed from the ellipsoid.
     */
    private final double a01, a02, a03, a21, a22, a23, a42, a43, a63;

    /**
     * The (<var>latitude</var>, <var>longitude</var>) coordinate of the first point
     * <strong>in radians</strong>. This point is set by {@link #setAnchorPoint}.
     *
     * @task TODO: rename as 'latitude1' and 'longitude1' when everything else will be finished.
     */
    private double lat1, long1;

    /**
     * The (<var>latitude</var>, <var>longitude</var>) coordinate of the destination point
     * <strong>in radians</strong>. This point is set by {@link #setDestinationPoint}.
     *
     * @task TODO: rename as 'latitude2' and 'longitude2' when everything else will be finished.
     */
    private double lat2, long2;

    /**
     * The distance and azimuth (in radians) from the anchor point
     * ({@link #long1}, {@link #lat1}) to the destination point
     * ({@link #long2}, {@link #lat2}).
     */
    private double distance, azimuth;

    /**
     * Tell if the destination point is valid.
     * <code>false</code> if {@link #long2} and {@link #lat2} need to be computed.
     */
    private boolean destinationValid;

    /**
     * Tell if the azimuth and the distance are valids.
     * <code>false</code> if {@link #distance} and {@link #azimuth} need to be computed.
     */
    private boolean directionValid;

    /**
     * Returns an angle between -{@linkplain Math#PI PI} and {@linkplain Math#PI PI}
     * equivalent to the specified angle in radians.
     *
     * @param  alpha An angle value in radians.
     * @return The angle between between -{@linkplain Math#PI PI} and {@linkplain Math#PI PI}.
     * 
     */
    private static double castToAngleRange(final double alpha) {
        return alpha - (2*Math.PI) * Math.floor(alpha/(2*Math.PI) + 0.5);
    }

    /**
     * Checks the latidude validity. The argument <code>latidude</code> should be
     * greater or equal than -90 degrees and lower or equals than +90 degrees. As
     * a convenience, this method returns the latitude in radians.
     *
     * @param  latitude The latitude value in <strong>degrees</strong>.
     * @return The latitude value in <strong>radians</strong>.
     * @throws IllegalArgumentException if <code>latitude</code> is not between -90 and +90 degrees.
     */
    private static double checkLatitude(final double latitude) throws IllegalArgumentException {
        if (latitude>=Latitude.MIN_VALUE && latitude<=Latitude.MAX_VALUE) {
            return Math.toRadians(latitude);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LATITUDE_OUT_OF_RANGE_$1, new Latitude(latitude)));
    }

    /** 
     * Checks the longitude validity. The argument <code>longitude</code> should be
     * greater or equal than -180 degrees and lower or equals than +180 degrees. As
     * a convenience, this method returns the longitude in radians.
     *
     * @param  longitude The longitude value in <strong>degrees</strong>.
     * @return The longitude value in <strong>radians</strong>.
     * @throws IllegalArgumentException if <code>longitude</code> is not
     *                                  between -180 and +180 degrees.
     */
    private static double checkLongitude(final double longitude) throws IllegalArgumentException {
        if (longitude>=Longitude.MIN_VALUE && longitude<=Longitude.MAX_VALUE) {
            return Math.toRadians(longitude);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_LONGITUDE_OUT_OF_RANGE_$1, new Longitude(longitude)));
    }

    /** 
     * Checks the azimuth validity. The argument <code>azimuth</code>  should be
     * greater or equal than -180 degrees and lower or equals than +180 degrees.
     * As a convenience, this method returns the azimuth in radians.
     *
     * @param  azimuth The azimuth value in <strong>degrees</strong>.
     * @return The azimuth value in <strong>radians</strong>.
     * @throws IllegalArgumentException if <code>azimuth</code> is not
     *                                  between -180 and +180 degrees.
     */
    private static double checkAzimuth(final double azimuth) throws IllegalArgumentException {
        if (azimuth>=-180.0 && azimuth<=180.0) {
            return Math.toRadians(azimuth);
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_AZIMUTH_OUT_OF_RANGE_$1, new Longitude(azimuth)));
    }

    /** 
     * Checks the orthodromic distance validity. Arguments <code>orthodromicDistance</code>  
     * should be greater or equal than 0 and lower or equals than the maximum orthodromic distance.
     *
     * @param  distance The orthodromic distance value.
     * @throws IllegalArgumentException if <code>orthodromic distance</code> is not between
     *                                  0 and the maximum orthodromic distance.
     */
    private void checkOrthodromicDistance(final double distance)
            throws IllegalArgumentException
    {
        if (!(distance>=0.0 && distance<=maxOrthodromicDistance)) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_DISTANCE_OUT_OF_RANGE_$4,
                    new Double(distance), new Double(0), new Double(maxOrthodromicDistance),
                    ellipsoid.getAxisUnit()));
        }
    }

    /** 
     * Checks the number of verteces in a curve. Arguments <code>numberOfPoints</code>  
     * should be not negative.
     *
     * @param  numberOfPonits The number of verteces in a curve.
     * @throws IllegalArgumentException if <code>numberOfVerteces</code> is negative.
     */
    private static void checkNumberOfPoints(final int numberOfPoints)
            throws IllegalArgumentException
    {
        if (!(numberOfPoints >= 0)) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2,
                        "numberOfPoints", new Integer(numberOfPoints)));
        }
    }

    /**
     * Returns a localized "No convergence" error message. The error message
     * includes informations about anchor and destination points.
     */
    private String getNoConvergenceErrorMessage() {
        final CoordinateFormat format = new CoordinateFormat();
        return Resources.format(ResourceKeys.ERROR_NO_CONVERGENCE_$2,
               format.format(new CoordinatePoint(Math.toDegrees(long1), Math.toDegrees(lat1))),
               format.format(new CoordinatePoint(Math.toDegrees(long2), Math.toDegrees(lat2))));
    }

    /**
     * Constructs a new geodetic calculator associated with the WGS84 ellipsoid.
     */
    public GeodeticCalculator() {
        this(Ellipsoid.WGS84);
    }

    /**
     * Constructs a new geodetic calculator associated with the specified ellipsoid.
     * All calculations done by the new instance are referenced to the ellipsoid specified.
     *
     * @param ellipsoid The reference to the ellipsoid onto which calculates distances and azimuths.
     */
    public GeodeticCalculator(final Ellipsoid ellipsoid) {
        this.ellipsoid     = ellipsoid;
        this.semiMajorAxis = ellipsoid.getSemiMajorAxis();
        this.semiMinorAxis = ellipsoid.getSemiMinorAxis();

        /* calculation of GPNHRI parameters*/
        f  = (semiMajorAxis-semiMinorAxis) / semiMajorAxis;
        fo = 1.0 - f;
        f2 = f*f;
        f3 = f*f2;
        f4 = f*f3;
        eccentricitySquared = f * (2.0-f);

        /* Calculation of GNPARC parameters */
        final double E2 = eccentricitySquared;
        final double E4 = E2*E2;
        final double E6 = E4*E2;
        final double E8 = E6*E2;
        final double EX = E8*E2;

        A =  1.0+0.75*E2+0.703125*E4+0.68359375 *E6+0.67291259765625*E8+0.6661834716796875 *EX;
        B =      0.75*E2+0.9375  *E4+1.025390625*E6+1.07666015625   *E8+1.1103057861328125 *EX;
        C =              0.234375*E4+0.41015625 *E6+0.538330078125  *E8+0.63446044921875   *EX;
        D =                          0.068359375*E6+0.15380859375   *E8+0.23792266845703125*EX;
        E =                                         0.01922607421875*E8+0.0528717041015625 *EX;
        F =                                                             0.00528717041015625*EX;

        maxOrthodromicDistance = semiMajorAxis * (1.0-E2) * Math.PI * A - 1.0;

        T1 = 1.0;
        T2 = -0.25*f*(1.0 + f + f2);
        T4 = 0.1875 * f2 * (1.0+2.25*f);
        T6 = 0.1953125 * f3;

        final double a = f3*(1.0+2.25*f);
        a01 = -f2*(1.0+f+f2)/4.0;
        a02 = 0.1875*a;
        a03 = -0.1953125*f4;
        a21 = -a01;
        a22 = -0.25*a;
        a23 = 0.29296875*f4;
        a42 = 0.03125*a;
        a43 = 0.05859375*f4;
        a63 = 5.0*f4/768.0;
    }

    /**
     * Return the referenced ellipsoid.
     *
     * @return The referenced ellipsoid.
     */
    public Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * Set the anchor point.
     * The {@linkplain #getAzimuth() azimuth},
     * the {@linkplain #getDistance() distance} and
     * the {@linkplain #getDestinationPoint() destination point} are discarted.
     * They will need to be specified again.
     *
     * @param  longitude The longitude in degrees between -180 and +180°
     * @param  latitude  The latitude  in degrees between  -90 and  +90°
     * @throws IllegalArgumentException if the longitude or the latitude is out of bounds.
     */
    public void setAnchorPoint(double longitude, double latitude) throws IllegalArgumentException {
        // Check first in case an exception is raised
        // (in other words, we change all or nothing).
        longitude = checkLongitude(longitude);
        latitude  = checkLatitude (latitude);
        // Check passed. Now performs the changes in this object.
        long1 = longitude;
        lat1  = latitude;
        destinationValid = false;
        directionValid   = false;
    }

    /**
     * Set the anchor point. The <var>x</var> and <var>y</var> coordinates
     * must be the longitude and latitude in degrees, respectively.
     *
     * This is a convenience method for
     * <code>{@linkplain #setAnchorPoint(double,double) setAnchorPoint}(x,y)</code>.
     *
     * @param  point The anchor point.
     * @throws IllegalArgumentException if the longitude or the latitude is out of bounds.
     */
    public void setAnchorPoint(final Point2D point) throws IllegalArgumentException {
        setAnchorPoint(point.getX(), point.getY());
    }

    /**
     * Returns the anchor point. The <var>x</var> and <var>y</var> coordinates
     * are the longitude and latitude in degrees, respectively. If the anchor
     * point has never been set, then the default value is (0,0).
     *
     * @return The anchor point.
     */
    public Point2D getAnchorPoint() {
        return new Point2D.Double(Math.toDegrees(long1), Math.toDegrees(lat1));
    }

    /**
     * Set the destination point. The azimuth and distance values will be updated as a side
     * effect of this call. They will be recomputed the next time {@link #getAzimuth()} or
     * {@link #getDistance()} are invoked.
     *
     * @param  longitude The longitude in degrees between -180 and +180°
     * @param  latitude  The latgitude in degrees between  -90 and  +90°
     * @throws IllegalArgumentException if the longitude or the latitude is out of bounds.
     */
    public void setDestinationPoint(double longitude, double latitude) throws IllegalArgumentException {
        // Check first in case an exception is raised
        // (in other words, we change all or nothing).
        longitude = checkLongitude(longitude);
        latitude  = checkLatitude (latitude);
        // Check passed. Now performs the changes in this object.
        long2 = longitude;
        lat2  = latitude;
        destinationValid = true;
        directionValid   = false;
    }

    /**
     * Set the destination point. The <var>x</var> and <var>y</var> coordinates
     * must be the longitude and latitude in degrees, respectively.
     *
     * This is a convenience method for
     * <code>{@linkplain #setDestinationPoint(double,double) setDestinationPoint}(x,y)</code>.
     *
     * @param  point The destination point.
     * @throws IllegalArgumentException if the longitude or the latitude is out of bounds.
     */
    public void setDestinationPoint(final Point2D point) throws IllegalArgumentException {
        setDestinationPoint(point.getX(), point.getY());
    }

    /**
     * Returns the destination point. This method returns the point set by the last call to a
     * <code>{@linkplain #setDestinationPoint(double,double) setDestinationPoint}(...)</code>
     * method, <strong>except</strong> if
     * <code>{@linkplain #setDirection(double,double) setDirection}(...)</code> has been
     * invoked after. In this later case, the destination point will be computed from the
     * {@linkplain #getAnchorPoint anchor point} to the azimuth and distance specified.
     *
     * @return The destination point. The <var>x</var> and <var>y</var> coordinates
     *         are the longitude and latitude in degrees, respectively.
     * @throws IllegalStateException if the azimuth and the distance have not been set.
     */
    public Point2D getDestinationPoint() throws IllegalStateException {
        if (!destinationValid) {
            computeDestinationPoint();
        }
        return new Point2D.Double(Math.toDegrees(long2), Math.toDegrees(lat2));
    }

    /**
     * Set the azimuth and the distance from the {@linkplain #getAnchorPoint anchor point}.
     * The destination point will be updated as a side effect of this call. It will be
     * recomputed the next time {@link #getDestinationPoint()} is invoked.
     *
     * @param  azimuth The azimuth in degrees from -180° to 180°.
     * @param  distance The orthodromic distance in the same units as the
     *         {@linkplain #getEllipsoid ellipsoid} axis.
     * @throws IllegalArgumentException if the azimuth or the distance is out of bounds.
     *
     * @see #getAzimuth
     * @see #getOrthodromicDistance
     */
    public void setDirection(double azimuth, final double distance) throws IllegalArgumentException {
        // Check first in case an exception is raised
        // (in other words, we change all or nothing).
        azimuth = checkAzimuth(azimuth);
        checkOrthodromicDistance(distance);
        // Check passed. Now performs the changes in this object.
        this.azimuth  = azimuth;
        this.distance = distance;
        destinationValid = false;
        directionValid   = true;
    }

    /**
     * Returns the azimuth. This method returns the value set by the last call to
     * <code>{@linkplain #setDirection(double,double) setDirection}(azimuth,distance)</code>,
     * <strong>except</strong> if
     * <code>{@linkplain #setDestinationPoint(double,double) setDestinationPoint}(...)</code>
     * has been invoked after. In this later case, the azimuth will be computed from the
     * {@linkplain #getAnchorPoint anchor point} to the destination point.
     *
     * @return The azimuth, in degrees from -180° to +180°.
     * @throws IllegalStateException if the destination point has not been set.
     */
    public double getAzimuth() throws IllegalStateException {
        if (!directionValid) {
            computeDirection();
        }
        return Math.toDegrees(azimuth);
    }

    /**
     * Returns the orthodromic distance. This method returns the value set by the last call to
     * <code>{@linkplain #setDirection(double,double) setDirection}(azimuth,distance)</code>,
     * <strong>except</strong> if
     * <code>{@linkplain #setDestinationPoint(double,double) setDestinationPoint}(...)</code>
     * has been invoked after. In this later case, the distance will be computed from the
     * {@linkplain #getAnchorPoint anchor point} to the destination point.
     *
     * @return The orthodromic distance, in the same units as the
     *         {@linkplain #getEllipsoid ellipsoid} axis.
     * @throws IllegalStateException if the destination point has not been set.
     */
    public double getOrthodromicDistance() throws IllegalStateException {
        if (!directionValid) {
            computeDirection();
            final double check;
            assert (check = Math.abs(ellipsoid.orthodromicDistance(
                                     Math.toDegrees(long1), Math.toDegrees(lat1),
                                     Math.toDegrees(long2), Math.toDegrees(lat2)) - distance))
                   <= semiMajorAxis*TOLERANCE_2 : check;
        }
        return distance;
    }

    /**
     * Compute the destination point from the {@linkplain #getAnchorPoint anchor point},
     * the {@linkplain #getAzimuth azimuth} and the {@linkplain #getDistance distance}.
     *
     * @throws IllegalStateException if the azimuth and the distance have not been set.
     *
     * @see #getDestinationPoint
     */
    private void computeDestinationPoint() throws IllegalStateException {
        if (!directionValid) {
            // TODO: localize this message.
            throw new IllegalStateException("Direction not set");
        }
        // Protect internal variables from changes
        final double lat1     = this.lat1;
        final double long1    = this.long1;
        final double azimuth  = this.azimuth;
        final double distance = this.distance;
        /*
         * Solution of the geodetic direct problem after T.Vincenty.
         * Modified Rainsford's method with Helmert's elliptical terms.
         * Effective in any azimuth and at any distance short of antipodal.
         *
         * Latitudes and longitudes in radians positive North and East.
         * Forward azimuths at both points returned in radians from North.
         *
         * Programmed for CDC-6600 by LCDR L.Pfeifer NGS ROCKVILLE MD 18FEB75
         * Modified for IBM SYSTEM 360 by John G.Gergen NGS ROCKVILLE MD 7507
         * Ported from Fortran to Java by Daniele Franzoni.
         *
         * Source: ftp://ftp.ngs.noaa.gov/pub/pcsoft/for_inv.3d/source/forward.for
         *         subroutine DIRECT1
         */
        double TU  = fo*Math.sin(lat1) / Math.cos(lat1);
        double SF  = Math.sin(azimuth);
        double CF  = Math.cos(azimuth);
        double BAZ = (CF!=0) ? Math.atan2(TU,CF)*2.0 : 0;
        double CU  = 1/Math.sqrt(TU*TU + 1.0);
        double SU  = TU*CU;
        double SA  = CU*SF;
        double C2A = 1.0 - SA*SA;
        double X   = Math.sqrt((1.0/fo/fo-1)*C2A+1.0) + 1.0;
        X   = (X-2.0)/X;
        double C   = 1.0-X;
        C   = (X*X/4.0+1.0)/C;
        double D   = (0.375*X*X-1.0)*X;
        TU   = distance / fo / semiMajorAxis / C;
        double Y   = TU;
        double SY, CY, CZ, E;
        do {
            SY = Math.sin(Y);
            CY = Math.cos(Y);
            CZ = Math.cos(BAZ+Y);
            E  = CZ*CZ*2.0-1.0;
            C  = Y;
            X  = E*CY;
            Y  = E+E-1.0;
            Y  = (((SY*SY*4.0-3.0)*Y*CZ*D/6.0+X)*D/4.0-CZ)*SY*D+TU;
        } while (Math.abs(Y-C) > TOLERANCE_1);
        BAZ  = CU*CY*CF - SU*SY;
        C    = fo*Math.sqrt(SA*SA+BAZ*BAZ);
        D    = SU*CY + CU*SY*CF;
        lat2 = Math.atan2(D,C);
        C    = CU*CY-SU*SY*CF;
        X    = Math.atan2(SY*SF,C);
        C    = ((-3.0*C2A+4.0)*f+4.0)*C2A*f/16.0;
        D    = ((E*CY*C+CZ)*SY*C+Y)*SA;
        long2 = long1+X - (1.0-C)*D*f;
        long2 = castToAngleRange(long2);
        destinationValid = true;
    }

    /**
     * Calculate the meridian arc length between two points in the same meridian 
     * in the referenced ellipsoid.
     *
     * @param  latitude1 The latitude of the first  point (in degrees).
     * @param  latitude2 The latitude of the second point (in degrees).
     * @return Returned the meridian arc length between latitude1 and latitude2 
     */
    public double getMeridianArcLength(final double latitude1, final double latitude2) {
        return getMeridianArcLengthRadians(checkLatitude(latitude1), checkLatitude(latitude2));
    }

    /**
     * Calculate the meridian arc length between two points in the same meridian 
     * in the referenced ellipsoid.
     *
     * @param  P1 The latitude of the first  point (in radians).
     * @param  P2 The latitude of the second point (in radians).
     * @return Returned the meridian arc length between P1 and P2 
     */
    private double getMeridianArcLengthRadians(final double P1, final double P2) {
        /*
         * Latitudes P1 and P2 in radians positive North and East.
         * Forward azimuths at both points returned in radians from North.
         *
         * Source: ftp://ftp.ngs.noaa.gov/pub/pcsoft/for_inv.3d/source/inverse.for
         *         subroutine GPNARC
         *         version    200005.26
         *         written by Robert (Sid) Safford
         * 
         * Ported from Fortran to Java by Daniele Franzoni.
         */		
        double S1 = Math.abs(P1);
        double S2 = Math.abs(P2);
        double DA = (P2-P1);
        // Check for a 90 degree lookup
        if (S1>TOLERANCE_0 || S2<=(Math.PI/2-TOLERANCE_0) || S2>=(Math.PI/2+TOLERANCE_0)) {
            final double DB = Math.sin(P2* 2.0) - Math.sin(P1* 2.0);
            final double DC = Math.sin(P2* 4.0) - Math.sin(P1* 4.0);
            final double DD = Math.sin(P2* 6.0) - Math.sin(P1* 6.0);
            final double DE = Math.sin(P2* 8.0) - Math.sin(P1* 8.0);
            final double DF = Math.sin(P2*10.0) - Math.sin(P1*10.0);
            // Compute the S2 part of the series expansion
            S2 = -DB*B/2.0 + DC*C/4.0 - DD*D/6.0 + DE*E/8.0 - DF*F/10.0;
        }
        // Compute the S1 part of the series expansion
        S1 = DA*A;
        // Compute the arc length
        return Math.abs(semiMajorAxis * (1.0-eccentricitySquared) * (S1+S2));
    }

    /**
     * Compute the azimuth and orthodromic distance from the
     * {@linkplain #getAnchorPoint anchor point} and the
     * {@linkplain #getDestinationPoint destination point}.
     *
     * @throws IllegalStateException if the destination point has not been set.
     *
     * @see #getAzimuth
     * @see #getOrthodromicDistance
     */
    private void computeDirection() throws IllegalStateException {
        if (!destinationValid) {
            // TODO: localize this message.
            throw new IllegalStateException("Destination not set");
        }
        // Protect internal variables from change.
        final double long1 = this.long1;
        final double lat1  = this.lat1;
        final double long2 = this.long2;
        final double lat2  = this.lat2;
        /*
         * Solution of the geodetic inverse problem after T.Vincenty.
         * Modified Rainsford's method with Helmert's elliptical terms.
         * Effective in any azimuth and at any distance short of antipodal.
         *
         * Latitudes and longitudes in radians positive North and East.
         * Forward azimuths at both points returned in radians from North.
         *
         * Programmed for CDC-6600 by LCDR L.Pfeifer NGS ROCKVILLE MD 18FEB75
         * Modified for IBM SYSTEM 360 by John G.Gergen NGS ROCKVILLE MD 7507
         * Ported from Fortran to Java by Daniele Franzoni.
         *
         * Source: ftp://ftp.ngs.noaa.gov/pub/pcsoft/for_inv.3d/source/inverse.for
         *         subroutine GPNHRI
         *         version    200208.09
         *         written by robert (sid) safford 
         */
        final double dlon = castToAngleRange(long2-long1);
        final double ss = Math.abs(dlon);
        if (ss < TOLERANCE_1) {
            distance = getMeridianArcLengthRadians(lat1, lat2);
            azimuth = (lat2>lat1) ? 0.0 : Math.PI;
            directionValid = true;
            return;
        }
        /*
         * Compute the limit in longitude (alimit), it is equal 
         * to twice  the distance from the equator to the pole,
         * as measured along the equator
         */
        //test for antinodal difference
        final double ESQP = eccentricitySquared / (1.0-eccentricitySquared);
        final double alimit = Math.PI*fo;
        if (ss>=alimit &&
            lat1<TOLERANCE_3 && lat1>-TOLERANCE_3 &&
            lat2<TOLERANCE_3 && lat2>-TOLERANCE_3)
        {
            // Compute an approximate AZ
            final double CONS = (Math.PI-ss)/(Math.PI*f);
            double AZ = Math.asin(CONS);
            double AZ_TEMP, S, AO;
            int iter = 0;
            do {
                if (++iter > 8) {
                    throw new ArithmeticException(getNoConvergenceErrorMessage());
                }
                S = Math.cos(AZ);
                final double C2 = S*S;
                // Compute new AO
                AO = T1 + T2*C2 + T4*C2*C2 + T6*C2*C2*C2;
                final double CS = CONS/AO;
                S = Math.asin(CS);
                AZ_TEMP = AZ;
                AZ = S;
            } while (Math.abs(S-AZ_TEMP) >= TOLERANCE_2);

            final double AZ1 = (dlon<0.0) ? 2.0*Math.PI - S : S;
            azimuth = castToAngleRange(AZ1);
            final double AZ2 = 2.0*Math.PI - AZ1;
            S = Math.cos(AZ1);

            // Equatorial - geodesic(S-s) SMS
            final double U2 = ESQP*S*S;
            final double U4 = U2*U2;
            final double U6 = U4*U2;
            final double U8 = U6*U2;
            final double BO =  1.0                 +
                               0.25            *U2 +
                               0.046875        *U4 +
                               0.01953125      *U6 +
                              -0.01068115234375*U8;
            S = Math.sin(AZ1);
            final double SMS = semiMajorAxis*Math.PI*(1.0 - f*Math.abs(S)*AO - BO*fo);
            distance = semiMajorAxis*ss - SMS;
            directionValid = true;
            return;
        }

        // the reduced latitudes
        final double  u1 = Math.atan(fo*Math.sin(lat1)/Math.cos(lat1));
        final double  u2 = Math.atan(fo*Math.sin(lat2)/Math.cos(lat2));
        final double su1 = Math.sin(u1);
        final double cu1 = Math.cos(u1);
        final double su2 = Math.sin(u2);
        final double cu2 = Math.cos(u2);
        double xy, w, q2, q4, q6, r2, r3, sig, ssig, slon, clon, sinalf, ab=dlon;
        int kcount = 0;
        do {
            if (++kcount > 8) {
                throw new ArithmeticException(getNoConvergenceErrorMessage());
            }
            clon = Math.cos(ab);
            slon = Math.sin(ab);
            final double csig = su1*su2 + cu1*cu2*clon;
            ssig = Math.sqrt(slon*cu2*slon*cu2 + (su2*cu1-su1*cu2*clon)*(su2*cu1-su1*cu2*clon));
            sig  = Math.atan2(ssig, csig);
            sinalf = cu1*cu2*slon/ssig;
            w = (1.0 - sinalf*sinalf);
            final double t4 = w*w;
            final double t6 = w*t4;

            // the coefficents of type a
            final double ao = f+a01*w+a02*t4+a03*t6;
            final double a2 =   a21*w+a22*t4+a23*t6;
            final double a4 =         a42*t4+a43*t6;
            final double a6 =                a63*t6;

            // the multiple angle functions
            double qo  = 0.0;
            if (w > TOLERANCE_0) {
                qo = -2.0*su1*su2/w;
            }
            q2 = csig + qo;
            q4 = 2.0*q2*q2 - 1.0;
            q6 = q2*(4.0*q2*q2 - 3.0);
            r2 = 2.0*ssig*csig;
            r3 = ssig*(3.0 - 4.0*ssig*ssig);

            // the longitude difference
            final double s = sinalf*(ao*sig + a2*ssig*q2 + a4*r2*q4 + a6*r3*q6);
            double xz = dlon+s;
            xy = Math.abs(xz-ab);
            ab = dlon+s;
        } while (xy >= TOLERANCE_1);

        final double z  = ESQP*w;
        final double bo = 1.0 + z*( 1.0/4.0 + z*(-3.0/  64.0 + z*(  5.0/256.0 - z*(175.0/16384.0))));
        final double b2 =       z*(-1.0/4.0 + z*( 1.0/  16.0 + z*(-15.0/512.0 + z*( 35.0/ 2048.0))));
        final double b4 =                   z*z*(-1.0/ 128.0 + z*(  3.0/512.0 - z*( 35.0/ 8192.0)));
        final double b6 =                                  z*z*z*(-1.0/1536.0 + z*(  5.0/ 6144.0));

        // The distance in ellispoid axis units.
        distance = semiMinorAxis * (bo*sig + b2*ssig*q2 + b4*r2*q4 + b6*r3*q6);
        double az1 = (dlon<0) ? Math.PI*(3/2) : Math.PI/2;

        // now compute the az1 & az2 for latitudes not on the equator
        if ((Math.abs(su1)>=TOLERANCE_0) || (Math.abs(su2)>=TOLERANCE_0)) {
            final double tana1 = slon*cu2 / (su2*cu1 - clon*su1*cu2);  
            final double sina1 = sinalf/cu1;

            // azimuths from north,longitudes positive east  
            az1 = Math.atan2(sina1, sina1/tana1);
        }   
        azimuth = castToAngleRange(az1);
        directionValid = true;
        return;
    }

    /**
     * Calculates the geodetic curve between two points in the referenced ellipsoid.
     * A curve in the ellipsoid is a path which points contain the longitude and latitude
     * of the points in the geodetic curve. The geodetic curve is computed from the
     * {@linkplain #getAnchorPoint anchor point} to the {@linkplain #getDestinationPoint
     * destination point}.
     *
     * @param  numberOfPoints The number of vertex in the geodetic curve.
     *         <strong>NOTE:</strong> This argument is only a hint and may be ignored
     *         in future version (if we compute a real curve rather than a list of line
     *         segments).
     * @return The path that represents the geodetic curve from the
     *         {@linkplain #getAnchorPoint anchor point} to the
     *         {@linkplain #getDestinationPoint destination point}.
     *
     * @task TODO: We should check for cases where the path cross the
     *             90°N, 90°S, 90°E or 90°W boundaries.
     */
    public Shape getGeodeticCurve(final int numberOfPoints) {
        checkNumberOfPoints(numberOfPoints);
        if (!directionValid) {
            computeDirection();
        }
        if (!destinationValid) {
            computeDestinationPoint();
        }
        final double         long2 = this.long2;
        final double          lat2 = this.lat2;
        final double      distance = this.distance;
        final double deltaDistance = distance / (numberOfPoints+1);
        final GeneralPath     path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, numberOfPoints+1);
        path.moveTo((float)Math.toDegrees(long1),
                    (float)Math.toDegrees(lat1));
        for (int i=1; i<numberOfPoints; i++) {
            this.distance = i*deltaDistance;
            computeDestinationPoint();
            path.lineTo((float)Math.toDegrees(this.long2),
                        (float)Math.toDegrees(this.lat2));
        }
        this.long2    = long2;
        this.lat2     = lat2;
        this.distance = distance;
        path.lineTo((float)Math.toDegrees(long2),
                    (float)Math.toDegrees(lat2));
        return path;
    }

    /**
     * Calculates the geodetic curve between two points in the referenced ellipsoid.
     * A curve in the ellipsoid is a path which points contain the longitude and latitude
     * of the points in the geodetic curve. The geodetic curve is computed from the
     * {@linkplain #getAnchorPoint anchor point} to the {@linkplain #getDestinationPoint
     * destination point}.
     *
     * @return The path that represents the geodetic curve from the
     *         {@linkplain #getAnchorPoint anchor point} to the
     *         {@linkplain #getDestinationPoint destination point}.
     */
    public Shape getGeodeticCurve() {
        return getGeodeticCurve(10);
    }

    /**
     * Calculates the loxodromic curve between two points in the referenced ellipsoid.
     * The loxodromic curve between two points is a path that links together the two
     * points with a constant azimuth. The azimuth from every points of the loxodromic
     * curve and the second point is constant.
     *
     * @return The path that represents the loxodromic curve from the
     *         {@linkplain #getAnchorPoint anchor point} to the
     *         {@linkplain #getDestinationPoint destination point}.
     */
    private Shape getLoxodromicCurve() {
        if (true) {
            throw new UnsupportedOperationException();
        }
        /*************************************************************************************
        ** THE FOLLOWING IS CHECKED FOR COMPILER ERROR, BUT EXCLUDED FROM THE .class FILE.  **
        ** THIS CODE IS WRONG: LOXODROMIC CURVES ARE STRAIGHT LINES IN MERCATOR PROJECTION, **
        ** NOT IT PLAIN (longitude,latitude) SPACE. FURTHERMORE, THE "OUT OF BOUNDS" CHECK  **
        ** IS UNFINISHED: WHEN THE PATH CROSS THE 180° LONGITUDE, A +360° ADDITION NEED TO  **
        ** BE PERFORMED ON ONE OF THE SOURCE OR TARGET POINT  BEFORE TO COMPUTE THE LINEAR  **
        ** INTERPOLATION (OTHERWISE, THE SLOPE VALUE IS WRONG). FORMULAS FOR COMPUTING MID- **
        ** POINT ON A LOXODROMIC CURVE ARE AVAILABLE THERE:                                 **
        **                                                                                  **
        **              http://mathforum.org/discuss/sci.math/a/t/180912                    **
        **                                                                                  **
        ** LatM = (Lat0+Lat1)/2                                                             **
        **                                                                                  **
        **        (Lon1-Lon0)log(f(LatM)) + Lon0 log(f(Lat1)) - Lon1 log(f(Lat0))           **
        ** LonM = ---------------------------------------------------------------           **
        **                             log(f(Lat1)/f(Lat0))                                 **
        **                                                                                  **
        ** where log(f(x)) == log(sec(x)+tan(x)) is the inverse Gudermannian function.      **
        *************************************************************************************/
        if (!directionValid) {
            computeDirection();
        }
        if (!destinationValid) {
            computeDestinationPoint();
        }
        final double x1 = Math.toDegrees(long1);
        final double y1 = Math.toDegrees( lat1);
        final double x2 = Math.toDegrees(long2);
        final double y2 = Math.toDegrees( lat2);
        /*
         * Check if the azimuth is heading from P1 to P2 (TRUE) or in the opposite direction
         * (FALSE). Horizontal (X) and vertical (Y) components are checked separatly. A null
         * value means "don't know", because the path is perfectly vertical or horizontal or
         * because a coordinate is NaN.  If both components are not null (unknow), then they
         * must be consistent.
         */
        final Boolean xDirect = (x2>x1) ? Boolean.valueOf(azimuth >= 0) :
                                (x2<x1) ? Boolean.valueOf(azimuth <= 0) : null;
        final Boolean yDirect = (y2>y1) ? Boolean.valueOf(azimuth >= -90 && azimuth <= +90) :
                                (y2<y1) ? Boolean.valueOf(azimuth <= -90 || azimuth >= +90) : null;
        assert xDirect==null || yDirect==null || xDirect.equals(yDirect) : azimuth;
        if (!Boolean.FALSE.equals(xDirect) && !Boolean.FALSE.equals(yDirect)) {
            return new Line2D.Double(x1, y1, x2, y2);
        }
        if (Boolean.FALSE.equals(yDirect)) {
            /*
             * Crossing North or South pole is more complicated than what we do for now: If we
             * follow the 0° longitude toward North, then we have to follow the 180° longitude
             * from North to South pole and follow the 0° longitude again toward North up to
             * the destination point.
             */
            throw new UnsupportedOperationException("Crossing pole is not yet implemented");
        }
        /*
         * The azimuth is heading in the opposite direction of the path from P1 to P2. Computes
         * the intersection points at the 90°N / 90°S boundaries, or the 180°E / 180°W boundaries.
         * (xout,yout) is the point where the path goes out (initialized to the corner where the
         * azimuth is heading); (xin,yin) is the point where the path come back in the opposite
         * hemisphere.
         */
        double xout = (x2 >= x1) ? -180 : +180;
        double yout = (y2 >= y1) ?  -90 :  +90;
        double xin  = -xout;
        double yin  = -yout;
        final double dx = x2-x1;
        final double dy = y2-y1;
        if (dx == 0) {
            xin = xout = x1;  // Vertical line.
        } else if (dy == 0) {
            yin = yout = y1;  // Horizontal line.
        } else {
            /*
             * The path is diagonal (neither horizontal or vertical). The following loop
             * is executed exactly twice:  the first pass computes the "out" point,  and
             * the second pass computes the "in" point.  Each pass computes actually two
             * points: the intersection point against the 180°W or 180°E boundary, and
             * the intersection point against the 90°N or 90°S boundary. Usually one of
             * those points will be out of range and the other one is selected.
             */
            boolean in = false;
            do {
                final double meridX, meridY; // The point where the path cross the +/-180° meridian.
                final double zonalX, zonalY; // The point where the path cross the +/- 90° parallel.
                meridX = in ? xin : xout;    meridY = dy/dx * (meridX-x1) + y1;
                zonalY = in ? yin : yout;    zonalX = dx/dy * (zonalY-y1) + x1;
                if (Math.abs(meridY) < Math.abs(zonalX)*0.5) {
                    if (in) {
                        xin = meridX;
                        yin = meridY;
                    } else {
                        xout = meridX;
                        yout = meridY;
                    }
                } else {
                    if (in) {
                        xin = zonalX;
                        yin = zonalY;
                    } else {
                        xout = zonalX;
                        yout = zonalY;
                    }
                }
            } while ((in = !in) == false);
        }
        final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
        path.moveTo((float)x1  , (float)y1  );
        path.lineTo((float)xout, (float)yout);
        path.moveTo((float)xin , (float)yin );
        path.lineTo((float)x2  , (float)y2  );
        return path;
    }

    /**
     * Convert an arbitrary Java2D shape into a JTS geometry. The created JTS geometry
     * may be any of {@link LineString}, {@link LinearRing} or {@link MultiLineString}.
     *
     * @param  shape    The Java2D shape to create.
     * @param  factory  The JTS factory to use for creating geometry.
     * @return The JTS geometry.
     *
     * @task REVISIT: Maybe we should move this method somewhere else (in some utility class).
     */
    public static Geometry shapeToGeometry(final Shape shape, final GeometryFactory factory) {
        final PathIterator iterator = shape.getPathIterator(null,
                org.geotools.resources.Geometry.getFlatness(shape));
        final double[] buffer = new double[6];
        final List     coords = new ArrayList();
        final List     lines  = new ArrayList();
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(buffer)) {
                /*
                 * Close the polygon: the last point is equal to
                 * the first point, and a LinearRing is created.
                 */
                case PathIterator.SEG_CLOSE: {
                    if (!coords.isEmpty()) {
                        coords.add((Coordinate[]) coords.get(0));
                        lines.add(factory.createLinearRing((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    break;
                }
                /*
                 * General case: A LineString is created from previous
                 * points, and a new LineString begin for next points.
                 */
                case PathIterator.SEG_MOVETO: {
                    if (!coords.isEmpty()) {
                        lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                                        new Coordinate[coords.size()])));
                        coords.clear();
                    }
                    // Fall through
                }
                case PathIterator.SEG_LINETO: {
                    coords.add(new Coordinate(buffer[0], buffer[1]));
                    break;
                }
                default: {
                    throw new IllegalPathStateException();
                }
            }
            iterator.next();
        }
        /*
         * End of loops: create the last LineString if any, then create the MultiLineString.
         */
        if (!coords.isEmpty()) {
            lines.add(factory.createLineString((Coordinate[]) coords.toArray(
                                            new Coordinate[coords.size()])));
        }
        switch (lines.size()) {
            case 0: return null;
            case 1: return (LineString) lines.get(0);
            default: {
                return factory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
            }
        }
    }
}
