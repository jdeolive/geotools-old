/*
 * Aea.java
 *
 * Created on 23 February 2002, 01:29
 */

package org.geotools.proj4j.projections;
import org.geotools.proj4j.*;
/**
 *
 * @author  James Macgill 
 */
public class Aea extends org.geotools.proj4j.Projection {
    
    double	ec,n,c,dd,n2,rho0,rho,phi1,phi2;
    double[]	en;
    boolean ellips;
    
    /* determine latitude angle phi-1 */
    public static final int N_ITER = 15;
    public static final double EPSILON = 1.0e-7;
    public static final double TOL = 1.0e-10;
    public static final double EPS10 =	1.e-10;
    public static final double TOL7 =	1.e-7;
    
    public double phi1_(double qs, double te, double tone_es) {
        int i;
        double phi, sinpi, cospi, con, com, dphi;
        
        phi = Math.asin(.5 * qs);
        if (te < EPSILON)
            return( phi );
        i = N_ITER;
        do {
            sinpi = Math.sin(phi);
            cospi = Math.cos(phi);
            con = te * sinpi;
            com = 1. - con * con;
            dphi = .5 * com * com / cospi * (qs / tone_es -
            sinpi / com + .5 / te * Math.log((1. - con) /
            (1. + con)));
            phi += dphi;
        } while (Math.abs(dphi) > TOL && --i!=0);
        return( i!=0 ? phi : Double.MAX_VALUE );
    }
    
    /** Creates a new instance of Aea */
    public Aea() {
    }
    
    protected XY  eForward(LP lp) throws ProjectionException {
        XY xy = new XY(); // ellipse
        if ((rho = c - (ellips ? n * Misc.qsfn(Math.sin(lp.phi),ellipse.e, ellipse.one_es) : n2 * Math.sin(lp.phi))) < 0.) throw new ProjectionException("tolerance condition error");
        rho = dd * Math.sqrt(rho);
        xy.x = rho * Math.sin( lp.lam *= n );
        xy.y = rho0 - rho * Math.cos(lp.lam);
        return (xy);
    }
    
    protected LP  eInverse(XY xy) throws ProjectionException {
        LP lp = new LP();; // ellipsoid
        xy.y = rho0-xy.y;
        rho = Math.sqrt(xy.x*xy.x+xy.y*xy.y);
        if(rho  != 0.0 ) {
            if (n < 0.) {
                rho = -rho;
                xy.x = -xy.x;
                xy.y = -xy.y;
            }
            lp.phi =  rho / dd;
            if (ellips) {
                lp.phi = (c - lp.phi * lp.phi) / n;
                if (Math.abs(ec - Math.abs(lp.phi)) > TOL7) {
                    lp.phi = phi1_(lp.phi, ellipse.e, ellipse.one_es);
                    if (lp.phi == Double.MAX_VALUE)
                        throw new ProjectionException("tolerance condition error");
                } else
                    lp.phi = (lp.phi < 0.) ? -HALFPI : HALFPI;
            } else
                lp.phi = (c - lp.phi * lp.phi) / n2;
            if (Math.abs(lp.phi) <= 1.){
                lp.phi = Math.asin(lp.phi);
            }
            else
                lp.phi = lp.phi < 0. ? -HALFPI : HALFPI;
                lp.lam = Math.atan2(xy.x, xy.y) / n;
        } else {
            lp.lam = 0.;
            lp.phi = n > 0. ? HALFPI : - HALFPI;
        }
        return (lp);
    }
    
    protected XY doForward(LP lp) throws ProjectionException {
        return eForward(lp);
    }
    
    protected LP doInverse(XY xy) throws ProjectionException {
        return eInverse(xy);
    }
    
    protected void setup(ParamSet params){
        phi1 = params.getRadiansParam("lat_1");
        phi2 = params.getRadiansParam("lat_2");
    }
    
    public void setParams(ParamSet params)throws ProjectionException{
        super.setParams(params);
        setup(params);
        double cosphi, sinphi;
        boolean secant;
        
        if (Math.abs(phi1 + phi2) < EPS10) throw new ProjectionException("conic lat_1 = -lat_2");
        n = sinphi = Math.sin(phi1);
        cosphi = Math.cos(phi1);
        secant =(Math.abs(phi1 - phi2) >= EPS10);
        if(ellipse.es>0.){
            ellips=true;
            double ml1, m1;
            
            en = MeridinalDistance.enfn(ellipse.es);
            m1 = Misc.msfn(sinphi, cosphi, ellipse.es);
            ml1 =Misc.qsfn(sinphi, ellipse.e, ellipse.one_es);
            if (secant) { /* secant cone */
                double ml2, m2;
                
                sinphi = Math.sin(phi2);
                cosphi = Math.cos(phi2);
                m2 = Misc.msfn(sinphi, cosphi, ellipse.es);
                ml2 = Misc.qsfn(sinphi, ellipse.e, ellipse.one_es);
                n = (m1 * m1 - m2 * m2) / (ml2 - ml1);
            }
            ec = 1. - .5 * ellipse.one_es * Math.log((1. - ellipse.e) /
            (1. + ellipse.e)) / ellipse.e;
            c = m1 * m1 + n * ml1;
            dd = 1. / n;
            rho0 = dd * Math.sqrt(c - n * Misc.qsfn(Math.sin(phi0),ellipse.e, ellipse.one_es));
        } else {
            if (secant) n = .5 * (n + Math.sin(phi2));
            n2 = n + n;
            c = cosphi * cosphi + n2 * sinphi;
            dd = 1. / n;
            rho0 = dd * Math.sqrt(c - n2 * Math.sin(phi0));
        }
    }
    public void setDefaults(){
        super.setDefaults();
        params.addParamIfNotSet("lat_1=29.5");
        params.addParamIfNotSet("lat_2=45.5");
    }
}
