/*
 * Tmerc.java
 *
 * Created on February 20, 2002, 2:30 PM
 */

package org.geotools.proj4j.projections;
import org.geotools.proj4j.*;
/**
 *
 * @author  jamesm 
 */



public class Tmerc extends org.geotools.proj4j.Projection {
    public static final double FC1 = 1.;
    public static final double FC2 =.5;
    public static final double FC3 =.16666666666666666666;
    public static final double FC4 =.08333333333333333333;
    public static final double FC5 =.05;
    public static final double FC6 =.03333333333333333333;
    public static final double FC7 =.02380952380952380952;
    public static final double FC8 =.01785714285714285714;
    
    protected double	esp;
    protected double	ml0;
    protected double en[];
    boolean useEllips;
    
    /** Creates a new instance of Tmerc */
    public Tmerc() {
    }
    
    protected XY  eForward(LP lp) { XY xy = new XY(); // ellipse
    double al, als, n, cosphi, sinphi, t;
    
    sinphi =Math.sin(lp.phi); cosphi = Math.cos(lp.phi);
    t = (Math.abs(cosphi) > 1e-10 )? sinphi/cosphi : 0.;
    t *= t;
    al = cosphi * lp.lam;
    als = al * al;
    al /= Math.sqrt(1. - ellipse.es * sinphi * sinphi);
    n = esp * cosphi * cosphi;
    xy.x = k0 * al * (1.+FC3*als*(1.-t+n+
    .05  * als * (5. + t * (t - 18.) + n * (14. - 58. * t)+
    FC7  * als * (61. + t * ( t * (179. - t) - 479. ) )
    )));
    xy.y = k0 * (Functions.mlfn(lp.phi, sinphi, cosphi, en) - ml0 +
    sinphi * al * lp.lam * .5  * ( 1. +
    FC4  * als * (5. - t + n * (9. + 4. * n) +
    FC6  * als * (61. + t * (t - 58.) + n * (270.- 330 * t)+
    FC8  * als * (1385. + t * ( t * (543. - t) -3111.) )
    ))));
    return (xy);
    }
    
    protected  XY  sForward(LP lp) throws ProjectionException{ XY xy = new XY() ; // sphere
    double b, cosphi;
    
    b = (cosphi = Math.cos(lp.phi)) * Math.sin(lp.lam);
    if (Math.abs(Math.abs(b) - 1.) <= 1.e-10 ) { throw new ProjectionException("tolerance condition error"); } ;
    if (Math.abs(Math.abs(b) - 1.) <= 1.e-10 ) { throw new ProjectionException("tolerance condition error"); } ;
    xy.x = ml0  * Math.log((1. + b) / (1. - b));
    if ((b = Math.abs( xy.y = cosphi * Math.cos(lp.lam) / Math.sqrt(1. - b * b) )) >= 1.) {
        if ((b - 1.) > 1.e-10 ) { throw new ProjectionException("tolerance condition error"); }
        else xy.y = 0.;
    } else
        xy.y = Math.acos(xy.y);
    if (lp.phi < 0.) xy.y = -xy.y;
    xy.y = esp  * (xy.y - phi0);
    return (xy);
    }
    
    protected LP  eInverse(XY xy) throws ProjectionException { LP lp = new LP();; // ellipsoid
    double n, con, cosphi, d, ds, sinphi, t;
    
    lp.phi = Functions.invMlfn(ml0 + xy.y / k0, ellipse.es, en);
    if (Math.abs(lp.phi) >= HALFPI ) {
        lp.phi = xy.y < 0. ? -HALFPI : HALFPI;
        lp.lam = 0.;
    } else {
        sinphi = Math.sin(lp.phi);
        cosphi = Math.cos(lp.phi);
        t = Math.abs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
        n = esp * cosphi * cosphi;
        d = xy.x * Math.sqrt(con = 1. - ellipse.es * sinphi * sinphi) / k0;
        con *= t;
        t *= t;
        ds = d * d;
        lp.phi -= (con * ds / (1.-ellipse.es)) * .5  * (1. -
        ds * FC4  * (5. + t * (3. - 9. *  n) +
        n * (1. - 4 * n) -
        ds * FC6  * (61. + t * (90. - 252. * n  +
        45. * t) + 46. * n - ds * FC8  * (1385. + t * (3633. + t * (4095. + 1574. * t)) )
        )));
        
        lp.lam = d*(1.  -
        ds* FC3 *( 1. + 2.*t + n -
        ds* .05 *(5. + t*(28. + 24.*t + 8.*n) + 6.*n-ds * FC7  * (61. + t * (662. + t *
        (1320. + 720. * t)) )
        ))) / cosphi;
    }
    return (lp);
    }
    
    protected  LP  sInverse(XY xy) { LP lp = new LP(); // sphere
    double h, g;
    
    h = Math.exp(xy.x / esp );
    g = .5 * (h - 1. / h);
    h = Math.cos(phi0 + xy.y / esp );
    lp.phi = Math.asin(Math.sqrt((1. - h * h) / (1. + g * g)));
    if (xy.y < 0.) lp.phi = -lp.phi;
    lp.lam = (g!=0 || h!=0) ? Math.atan2(g, h) : 0.;
    return (lp);
    }
    
    protected XY doForward(LP lp)throws ProjectionException{
        return(useEllips)?eForward(lp):sForward(lp);
    }
    protected LP doInverse(XY xy) throws ProjectionException{
        return(useEllips)?eInverse(xy):sInverse(xy);
    }
    
    public void setParams(ParamSet ps)throws ProjectionException{
        super.setParams(ps);
        if(ellipse.es!=0){
            en = Functions.enfn(ellipse.es);
            ml0 = Functions.mlfn(phi0, Math.sin(phi0), Math.cos(phi0), en);
            esp = ellipse.es / (1. - ellipse.es);
            useEllips = true;
        }
        else{
            esp = k0;
            ml0 = .5 * esp;
            useEllips = false;
        }
    }
    
    
}
