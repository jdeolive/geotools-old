/*
 * Constants.java
 *
 * Created on 22 February 2002, 01:24
 */

package org.geotools.proj4j;

/** A set of handy constants used by a number of classes in this package.
 * Classes which need to access them can implement this interface and use them directly.
 * Note, many of these values are pulled from the origional PROJ code and may be replaced in future Java versions with calls, e.g. Math.PI
 * @author James Macgill
 */
public interface Constants {
    /** Unknown constant
     * TODO: Find its use and meaning
     */    
    public static final double SIXTH = .1666666666666666667; /* 1/6 */
    /** Unknown constant
     * TODO: Find its use and meaning
     */    
    public static final double RA4 = .04722222222222222222; /* 17/360 */
    /** Unknown constant
     * TODO: Find its use and meaning
     */    
    public static final double RA6 = .02215608465608465608; /* 67/3024 */
    /** Unknown constant
     * TODO: Find its use and meaning
     */    
    public static final double RV4 =.06944444444444444444; /* 5/72 */
    /** Unknown constant
     * TODO: Find its use and meaning
     */    
    public static final double RV6 = .04243827160493827160; /* 55/1296 */
    
    /** PI/2
     */    
     public static final double HALFPI	=	1.5707963267948966;
     /** PI/4
      */     
    public static final double FORTPI	=	0.78539816339744833;
    /** PI This may well be replaced by Math.PI
     */    
    public static final double PI	=	3.14159265358979323846;
    /** PI*2
     */    
    public static final double TWOPI	=	6.2831853071795864769;
    /** Epsilon, standard acceptable error in many methods.
     */    
    public static final double EPS      =       1.0e-12;

}

