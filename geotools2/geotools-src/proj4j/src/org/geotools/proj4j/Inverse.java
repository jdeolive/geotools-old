/*
 * Inverse.java
 *
 * Created on 20 February 2002, 02:49
 */

package org.geotools.proj4j;



/**
 *
 * @author  James Macgill
 */
public class Inverse {
    
    public static final double HALFPI	=	1.5707963267948966;
    public static final double FORTPI	=	0.78539816339744833;
    public static final double PI	=	3.14159265358979323846;
    public static final double TWOPI	=	6.2831853071795864769;
    public static final double EPS      =       1.0e-12;
    /** Creates a new instance of Inverse */
    public Inverse() {
    }
    
    
    public static LP inv(XY xy, Projection p) throws ProjectionException {
        LP lp = new LP();
        
        /* can't do as much preliminary checking as with forward */
        if (xy.x == Double.MAX_VALUE || xy.y == Double.MAX_VALUE) {
            lp.lam = lp.phi = Double.MAX_VALUE;
            //pj_errno = -15;
        }
        //errno = pj_errno = 0; //this was just set, and now we clear it???
        xy.x = (xy.x * p.to_meter - p.x0) * p.ra; /* descale and de-offset */
        xy.y = (xy.y * p.to_meter - p.y0) * p.ra;
        try{
            //magic line //lp = (*P->inv)(xy, P); /* inverse project */
            lp.lam += p.lam0; /* reduce from del lp.lam */
            if (!p.over)
                lp.lam = Adjlon.adjlon(lp.lam); /* adjust longitude to CM */
            if (p.geoc && Math.abs(Math.abs(lp.phi)-HALFPI) > EPS)
                lp.phi = Math.atan(p.one_es * Math.tan(lp.phi));
        }
        catch(ProjectionException pe){
            lp.lam = lp.phi = Double.MAX_VALUE;
        }
        
        return lp;
    }
}
