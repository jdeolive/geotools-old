/*
 * Geocentric.java
 *
 * Created on 19 February 2002, 23:41
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class Geocentric {
    private double a,b;
    private double a2 = 40680631590769.0;    /* Square of semi-major axis */
    private double b2 = 40408299984087.05;   /* Square of semi-minor axis */
    private double e2 = 0.0066943799901413800;   /* Eccentricity squared  */
    private double ep2 = 0.00673949675658690300; /* 2nd eccentricity squared */
    
    private static double PI = 3.14159265358979323e0;  //Could use Math.PI but this may make comparisons tricky
    private static double PI_OVER_2  = (PI / 2.0e0);
    
    private static double COS_67P5 =  0.38268343236508977;  /* cosine of 67.5 degrees */
    private static double AD_C     =  1.0026000;            /* Toms region 1 constant */
    
    public Geocentric(double a,double b) throws GeocentricException{
        setGeocentricParameters(a,b);
    }
    
/*
 * The function Set_Geocentric_Parameters receives the ellipsoid parameters
 * as inputs and sets the corresponding state variables.
 *
 *   @param a Semi-major axis, in meters.          (input)
 *   @param b Semi-minor axis, in meters.          (input)
 */
    public void setGeocentricParameters(double a, double b) throws GeocentricException{
        long errorCode = GeocentricException.GEOCENT_NO_ERROR;
        
        if (a <= 0.0)
            errorCode |= GeocentricException.GEOCENT_A_ERROR;
        if (b <= 0.0)
            errorCode |= GeocentricException.GEOCENT_B_ERROR;
        if (a < b)
            errorCode |= GeocentricException.GEOCENT_A_LESS_B_ERROR;
        if (errorCode!=GeocentricException.GEOCENT_NO_ERROR) {
            throw new GeocentricException(errorCode);
        }
        this.a = a;
        this.b = b;
        a2 = a * a;
        b2 = b * b;
        e2 = (a2 - b2) / a2;
        ep2 = (a2 - b2) / b2;
    }
    
/*
 * The function Get_Geocentric_Parameters returns the ellipsoid parameters
 * to be used in geocentric coordinate conversions.
 */
    public double[] getGeocentricParameters(){
        return new double[]{a,b};
    }
    
 /*
  * The function Convert_Geodetic_To_Geocentric converts geodetic coordinates
  * (latitude, longitude, and height) to geocentric coordinates (X, Y, Z),
  * according to the current ellipsoid parameters.
  *
  *    @param latitude  : Geodetic latitude in radians
  *    @param longitude : Geodetic longitude in radians
  *    @param height    : Geodetic height, in meters
  *
  *    @return double[3] x,y,z coordinates in meters
  *
  */
    
    public double[] convertGeodeticToGeocentric(double latitude,double longitude,double height) throws GeocentricException {
        long errorCode = GeocentricException.GEOCENT_NO_ERROR;
        double rn;            /*  Earth radius at location  */
        double sinLat;       /*  sin(latitude)  */
        double sin2Lat;      /*  Square of sin(latitude)  */
        double cosLat;       /*  cos(latitude)  */
        
  /*
   ** Don't blow up if latitude is just a little out of the value
   ** range as it may just be a rounding issue.  Also removed longitude
   ** test, it should be wrapped by cos() and sin().  NFW for PROJ.4, Sep/2001.
   */
        if( latitude < -PI_OVER_2 && latitude > -1.001 * PI_OVER_2 )
            latitude = -PI_OVER_2;
        else if( latitude > PI_OVER_2 && latitude < 1.001 * PI_OVER_2 )
            latitude = PI_OVER_2;
        else if ((latitude < -PI_OVER_2) || (latitude > PI_OVER_2)) { /* latitude out of range */
            errorCode |= GeocentricException.GEOCENT_LAT_ERROR;
        }
        
        if (errorCode!=0){
            throw new GeocentricException(errorCode);
        }
        /* no errors */
        if (longitude > PI)
            longitude -= (2*PI);
        sinLat = Math.sin(latitude);
        cosLat = Math.cos(latitude);
        sin2Lat = sinLat * sinLat;
        rn = a / (Math.sqrt(1.0e0 - e2 * sin2Lat));
        double x = (rn + height) * cosLat * Math.cos(longitude);
        double y = (rn + height) * cosLat * Math.sin(longitude);
        double z = ((rn * (1 - e2)) + height) * sinLat;
        
        
        return new double[]{x,y,z};
    } /* END OF Convert_Geodetic_To_Geocentric */
    
    
/*
 * The function Convert_Geocentric_To_Geodetic converts geocentric
 * coordinates (X, Y, Z) to geodetic coordinates (latitude, longitude,
 * and height), according to the current ellipsoid parameters.
 *
 *    X         : Geocentric X coordinate, in meters.         (input)
 *    Y         : Geocentric Y coordinate, in meters.         (input)
 *    Z         : Geocentric Z coordinate, in meters.         (input)
 *    latitude  : Calculated latitude value in radians.       (output)
 *    longitude : Calculated longitude value in radians.      (output)
 *    height    : Calculated height value, in meters.         (output)
 *
 * The method used here is derived from 'An Improved Algorithm for
 * Geocentric to Geodetic Coordinate Conversion', by Ralph Toms, Feb 1996
 */
    
    /* Note: Variable names follow the notation used in Toms, Feb 1996 */
    public double[] convertGeocentricToGeodetic(double x,double y,double z){
        double w;        /* distance from Z axis */
        double w2;       /* square of distance from Z axis */
        double t0;       /* initial estimate of vertical component */
        double t1;       /* corrected estimate of vertical component */
        double s0;       /* initial estimate of horizontal component */
        double s1;       /* corrected estimate of horizontal component */
        double sinB0;   /* sin(B0), B0 is estimate of Bowring aux variable */
        double sin3B0;  /* cube of sin(B0) */
        double cosB0;   /* cos(B0) */
        double sinP1;   /* sin(phi1), phi1 is estimated latitude */
        double cosP1;   /* cos(phi1) */
        double rn;       /* Earth radius at location */
        double sum;      /* numerator of cos(phi1) */
        boolean atPole;     /* indicates location is in polar region */
        
        atPole = false;
        double longitude=0,latitude=0,height=0;
        if (x != 0.0) {
            longitude = Math.atan2(y,x);
        }
        else {
            if (y > 0) {
                longitude = PI_OVER_2;
            }
            else if (y < 0) {
                longitude = -PI_OVER_2;
            }
            else {
                atPole = true;
                longitude = 0.0;
                if (z > 0.0) {  /* north pole */
                    latitude = PI_OVER_2;
                }
                else if (z < 0.0) {  /* south pole */
                    latitude = -PI_OVER_2;
                }
                else {  /* center of earth */
                    latitude = PI_OVER_2;
                    height = -b;
                    return new double[] {latitude,longitude,height};
                }
            }
        }
        w2 = x*x + y*y;
        w = Math.sqrt(w2);
        t0 = z * AD_C;
        s0 = Math.sqrt(t0 * t0 + w2);
        sinB0 = t0 / s0;
        cosB0 = w / s0;
        sin3B0 = sinB0 * sinB0 * sinB0;
        t1 = z + b * ep2 * sin3B0;
        sum = w - a * e2 * cosB0 * cosB0 * cosB0;
        s1 = Math.sqrt(t1*t1 + sum * sum);
        sinP1 = t1 / s1;
        cosP1 = sum / s1;
        rn = a / Math.sqrt(1.0 - e2 * sinP1 * sinP1);
        if (cosP1 >= COS_67P5) {
            height = w / cosP1 - rn;
        }
        else if (cosP1 <= -COS_67P5) {
            height = w / -cosP1 - rn;
        }
        else {
            height = z / sinP1 + rn * (e2 - 1.0);
        }
        if (atPole == false) {
            latitude = Math.atan(sinP1 / cosP1);
        }
        
        return new double[] {latitude,longitude,height};
        
    }
    
}
