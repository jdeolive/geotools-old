/*
 * Adjlong.java
 *
 * Created on 20 February 2002, 03:04
 */

package org.geotools.proj4j;

import java.util.StringTokenizer;
/** A set of standard functions used by many projections.
 *
 * @author James Macgill
 */
public class Functions implements Constants{
    
    
    
    
   
    
    
    public static final double SPI =    3.14159265359;
    public static final double TWOPI =  6.2831853071795864769;
    public static final double ONEPI =  3.14159265358979323846;
    
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
    /**
     * Reduces argument to range +/- PI. 
     * @param lon the value to adjust
     * @return double the adjusted value
     **/
    public static double adjlon(double lon) {
        if (Math.abs(lon) <= SPI) return( lon );
        lon += ONEPI;  /* adjust to 0..2pi rad */
        lon -= TWOPI * Math.floor(lon / TWOPI); /* remove integral # of 'revolutions'*/
        lon -= ONEPI;  /* adjust back to -pi..pi rad */
        return lon ;
    }
    
    static final String sym = "NnEeSsWw";
    static final double[] vm = {
        .0174532925199433,
        .0002908882086657216,
        .0000048481368110953599
    };
    /** Converts a degres minutes seconds string into a radians value.
     * @param is The String to convert
     * @return double The value of is in radians
     */    
    public static double dmsToR(String is){
        int n=0, nl;
        String work;
        double v, tv;
        String sign,s;
        boolean signFlag=true;
        
        //if (rs.length!=0)
        //	rs[0] = is;
        /* copy sting into work space */
        work = is.trim();
        StringTokenizer tok = new StringTokenizer(work,"Dd\'\"Rr+-"+sym,true);
        s = tok.nextToken();
        if (!(s.equals( "+") || s.equals("-"))){
            sign = "+";
            signFlag=false;
        }else{
            sign=s;
            signFlag=true;
        }
        for (v = 0., nl = 0 ; nl < 3 ; nl = n+1  ) {
            if(signFlag) {
                s = tok.nextToken();
            }else{
                signFlag=true;
            }
            
            //if its not a digit then what?
            try{
                tv= Double.parseDouble(s);
            }
            catch(NumberFormatException nfe){
                return Double.MAX_VALUE;
            }
            if(tok.hasMoreTokens()){
                s = tok.nextToken();//should be a seperator;
                if(s.equalsIgnoreCase("d")) {n=0;}
                else if(s.equals("\'")){ n=1;}
                else if(s.equals("\"")){ n=2;}
                else if(s.equalsIgnoreCase("r")){
                    if (nl!=0) {
                        //pj_errno = -16;
                        return Double.MAX_VALUE;
                    }
                    v = tv;
                    n = 4;
                    continue;
                }else{
                    // it must be a direction character
                    v+=tv * vm[n];
                    break;
                }
            }else{
                v+=tv * vm[n];
                break;
            }
            
            v += tv * vm[n];
            if(!tok.hasMoreTokens())return v;
        }
        /* postfix sign */
        char end = work.charAt(work.length()-1);
        if(sym.indexOf(end)>=0){
            sign = (sym.indexOf(end)>=4)?"-" : "+";
        }
        if (sign.equals("-")){
            v = -v;
        }
        return v;
    }
    
    
    /* determine small q */
    /** returns small q
     * @param sinphi
     * @param e
     * @param one_es
     * @return  */    
    public static double qsfn(double sinphi, double e, double one_es) {
        double con;
        if (e >= EPS) {
            con = e * sinphi;
            return (one_es * (sinphi / (1. - con * con) -
            (.5 / e) * Math.log((1. - con) / (1. + con))));
        } else
            return (sinphi + sinphi);
    }
    
    public static double msfn(double sinphi, double cosphi, double es) {
        return (cosphi / Math.sqrt(1. - es * sinphi * sinphi));
    }
}
