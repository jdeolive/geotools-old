/*
 * Elips.java
 *
 * Created on 19 February 2002, 22:25
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class Ellips {
	String	id;	/* ellipse keyword name */
	String	major;	/* a= value */
	String	ell;	/* elliptical parameter */
	String	name;	/* comments */

    /** Creates a new instance of Elips */
    public Ellips(String id,String major,String ell,String name) {
        this.id = id;
        this.major = major;
        this.ell = ell;
        this.name = name;
    }

     static final Ellips[] ellips={
         new Ellips("MERIT",	"a=6378137.0", "rf=298.257", "MERIT 1983"),
         new Ellips("SGS85",	"a=6378136.0", "rf=298.257",  "Soviet Geodetic System 85")
     };
    
}
