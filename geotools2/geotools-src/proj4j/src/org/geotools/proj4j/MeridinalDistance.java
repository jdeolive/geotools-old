/*
 * MeridinalDistance.java
 *
 * Created on 22 February 2002, 02:30
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class MeridinalDistance  {
/* meridinal distance for ellipsoid and inverse
 **	8th degree - accurate to < 1e-5 meters when used in conjuction
 **		with typical major axis values.
 **	Inverse determines phi to EPS (1e-11) radians, about 1e-6 seconds.
 */
    public static final double C00= 1.;
    public static final double C02= .25;
    public static final double C04= .046875;
    public static final double C06= .01953125;
    public static final double C08= .01068115234375;
    public static final double C22= .75;
    public static final double C44= .46875;
    public static final double C46= .01302083333333333333;
    public static final double C48= .00712076822916666666;
    public static final double C66= .36458333333333333333;
    public static final double C68= .00569661458333333333;
    public static final double C88= .3076171875;
    public static final double EPS= 1e-11;
    public static final int MAX_ITER= 10;
    public static final double EN_SIZE= 5;
    
    public static double[] enfn(double es){
        double t,en[];
        en = new double[5];
        en[0] = C00 - es * (C02 + es * (C04 + es * (C06 + es * C08)));
        en[1] = es * (C22 - es * (C04 + es * (C06 + es * C08)));
        en[2] = (t = es * es) * (C44 - es * (C46 + es * C48));
        en[3] = (t *= es) * (C66 - es * C68);
        en[4] = t * es * C88;
        return en;        
    }
    
    public static double mlfn(double phi, double sphi, double cphi, double[] en) {
	cphi *= sphi;
	sphi *= sphi;
	return(en[0] * phi - cphi * (en[1] + sphi*(en[2]
		+ sphi*(en[3] + sphi*en[4]))));
    }
    public static double invMlfn(double arg, double es, double[] en) throws ProjectionException {
	double s, t, phi, k = 1./(1.-es);
	int i;

	phi = arg;
	for (i = MAX_ITER; i>0 ; --i) { /* rarely goes over 2 iterations */
		s = Math.sin(phi);
		t = 1. - es * s * s;
		phi -= t = (mlfn(phi, s, Math.cos(phi), en) - arg) * (t * Math.sqrt(t)) * k;
		if (Math.abs(t) < EPS)
			return phi;
	}
	throw new ProjectionException("non-convergent inverse meridinal dist");
    }
}
