/*
 * Tmerc.java
 *
 * Created on February 20, 2002, 2:30 PM
 */

package org.geotools.proj4j.projections;

/**
 *
 * @author  jamesm
 */
public class Tmerc extends org.geotools.proj4j.Projection {
        protected double	esp; 
        protected double	ml0;
	protected double en[];
        boolean useEllips;
    
    /** Creates a new instance of Tmerc */
    public Tmerc() {
    }
    
    static XY  e_forward (LP lp, PJ *P) { XY xy ; /* ellipse */
        double al, als, n, cosphi, sinphi, t;

        sinphi = sin(lp.phi); cosphi = cos(lp.phi);
        t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
        t *= t;
        al = cosphi * lp.lam;
        als = al * al;
        al /= sqrt(1. - P->es * sinphi * sinphi);
        n = P->esp * cosphi * cosphi;
        xy.x = P->k0 * al * (1.  +
                .16666666666666666666  * als * (1. - t + n +
                .05  * als * (5. + t * (t - 18.) + n * (14. - 58. * t)
                + .02380952380952380952  * als * (61. + t * ( t * (179. - t) - 4
79. ) )
                )));
        xy.y = P->k0 * (pj_mlfn(lp.phi, sinphi, cosphi, P->en) - P->ml0 +
                sinphi * al * lp.lam * .5  * ( 1. +
                .08333333333333333333  * als * (5. - t + n * (9. + 4. * n) +
                .03333333333333333333  * als * (61. + t * (t - 58.) + n * (270.
- 330 * t)
                + .01785714285714285714  * als * (1385. + t * ( t * (543. - t) -
 3111.) )
                ))));
        return (xy);
}
static XY  s_forward (LP lp, PJ *P) { XY xy ; /* sphere */
        double b, cosphi;

        b = (cosphi = cos(lp.phi)) * sin(lp.lam);
        if (fabs(fabs(b) - 1.) <= 1.e-10 ) { pj_errno = -20; return(xy); } ;
        if (fabs(fabs(b) - 1.) <= 1.e-10 ) { pj_errno = -20; return(xy); } ;
        xy.x = P->ml0  * log((1. + b) / (1. - b));
        if ((b = fabs( xy.y = cosphi * cos(lp.lam) / sqrt(1. - b * b) )) >= 1.)
{
                if ((b - 1.) > 1.e-10 ) { pj_errno = -20; return(xy); }
                else xy.y = 0.;
        } else
                xy.y = acos(xy.y);
        if (lp.phi < 0.) xy.y = -xy.y;
        xy.y = P->esp  * (xy.y - P->phi0);
        return (xy);
}
static LP  e_inverse (XY xy, PJ *P) { LP lp ; /* ellipsoid */
        double n, con, cosphi, d, ds, sinphi, t;

        lp.phi = pj_inv_mlfn(P->ml0 + xy.y / P->k0, P->es, P->en);
        if (fabs(lp.phi) >= 1.5707963267948966 ) {
                lp.phi = xy.y < 0. ? - 1.5707963267948966  : 1.5707963267948966
;
                lp.lam = 0.;
        } else {
                sinphi = sin(lp.phi);
                cosphi = cos(lp.phi);
                t = fabs(cosphi) > 1e-10 ? sinphi/cosphi : 0.;
                n = P->esp * cosphi * cosphi;
                d = xy.x * sqrt(con = 1. - P->es * sinphi * sinphi) / P->k0;
                con *= t;
                t *= t;
                ds = d * d;
                lp.phi -= (con * ds / (1.-P->es)) * .5  * (1. -
                        ds * .08333333333333333333  * (5. + t * (3. - 9. *  n) +
 n * (1. - 4 * n) -
                        ds * .03333333333333333333  * (61. + t * (90. - 252. * n
 +
                                45. * t) + 46. * n
                   - ds * .01785714285714285714  * (1385. + t * (3633. + t * (40
95. + 1574. * t)) )
                        )));
                lp.lam = d*(1.  -
                        ds* .16666666666666666666 *( 1. + 2.*t + n -
                        ds* .05 *(5. + t*(28. + 24.*t + 8.*n) + 6.*n
                   - ds * .02380952380952380952  * (61. + t * (662. + t * (1320.
 + 720. * t)) )
                ))) / cosphi;
        }
        return (lp);
}
static LP  s_inverse (XY xy, PJ *P) { LP lp ; /* sphere */
        double h, g;

        h = exp(xy.x / P->esp );
        g = .5 * (h - 1. / h);
        h = cos(P->phi0 + xy.y / P->esp );
        lp.phi = asin(sqrt((1. - h * h) / (1. + g * g)));
        if (xy.y < 0.) lp.phi = -lp.phi;
        lp.lam = (g || h) ? atan2(g, h) : 0.;
        return (lp);
}

    
    
    
    public XY forward(LP lp){
    }
    
    
    public void setParams(ParamSet ps)throws ProjectionException{
        super.setP(ps);
        if(es!=0){
            en = MeridinalDistance.enfn(es);
            ml0 = MeridinalDistance.mlfn(phi0, Math.sin(phi0), Math.cos(phi0), en);
            esp = es / (1. - es);
            useEllips = true;
        }
        else{
            aks0 = k0;
            aks5 = .5 * aks0;
            useEllips = false;
        }
    }
            

}
