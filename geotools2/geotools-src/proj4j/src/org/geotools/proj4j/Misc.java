/*
 * Adjlong.java
 *
 * Created on 20 February 2002, 03:04
 */

package org.geotools.proj4j;

import java.util.StringTokenizer;
/**
 *
 * @author  James Macgill
 */
public class Misc {
    
    
    /* reduce argument to range +/- PI */
    
    
    public static final double SPI =    3.14159265359;
    public static final double TWOPI =  6.2831853071795864769;
    public static final double ONEPI =  3.14159265358979323846;
    
    public static double adjlon(double lon) {
        if (Math.abs(lon) <= SPI) return( lon );
        lon += ONEPI;  /* adjust to 0..2pi rad */
        lon -= TWOPI * Math.floor(lon / TWOPI); /* remove integral # of 'revolutions'*/
        lon -= ONEPI;  /* adjust back to -pi..pi rad */
        return lon ;
    }
    
    //static final int MAX_WORK = 64;
    static final String sym = "NnEeSsWw";
    static final double[] vm = {
	.0174532925199433,
	.0002908882086657216,
	.0000048481368110953599
    };
    public static double dmsToR(String is){
	int n, nl;
	String work;
	double v, tv;

	//if (rs.length!=0)
	//	rs[0] = is;
	/* copy sting into work space */
        work = is.trim();
	StringTokenizer tok = new StringTokenizer(work,"Dd\'\"Rr+-",true);
        String sign = tok.nextToken();
	if (sign == "+" || sign == "-");
	else sign = "+";
	for (v = 0., nl = 0 ; nl < 3 ; nl = n + 1 ) {
                String s = tok.nextToken();
                if(s==".")break;
                //if its not a digit then what?
                try{
                    tv= Double.parseDouble(s);
                }
                catch(NumberFormatException nfe){
                    return Double.MAX_VALUE;
                }
                s = tok.nextToken();//should be a seperator;
                if(s.equalsIgnoreCase("d")) {n=0; break;}
                else if(s.equals("\'")){ n=1; break ;}
                else if(s.equals("\"")){ n=2; break ;}
                else if(s.equalsIgnoreCase("r")){
			if (nl!=0) {
				//pj_errno = -16;
				return Double.MAX_VALUE;
			}

			s = tok.nextToken();
			v = tv;
			n = 4;
                        continue;
                }
                else{
                    v += tv * vm[nl];
                    n = 4;
                    continue;
		}
		//if (n < nl) {
			//pj_errno = -16;
		//	return Double.MAX_VALUE;
		//}
		//v += tv * vm[n];
		//++s;
	}
		/* postfix sign */
        char end = work.charAt(work.length());
        if(sym.indexOf(end)>=0){
            sign = (sym.indexOf(end)>=4)?"-" : "+";
        }
	if (sign == "-")
		v = -v;
	return v;
}
    
    
}
