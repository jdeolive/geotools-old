/*
 * Airy.java
 *
 * Created on 06 March 2002, 21:08
 */

package org.geotools.proj4j.projections;
import org.geotools.proj4j.*;
/**
 *
 * @author James Macgill
 */
public class Airy extends org.geotools.proj4j.Projection implements Constants{
    
    protected double p_halfpi,sinph0,cosph0,Cb;
    int mode;
    boolean no_cut;	/* do not cut at hemisphere limit */
    
    protected static final int N_POLE = 0,S_POLE= 1, EQUIT=2, OBLIQ=3;
    
    /** Creates a new instance of Airy */
    public Airy() {
    }
    
    protected XY sForward(LP lp) throws ProjectionException{/* spheroid */
        XY xy = new XY();
        double  sinlam, coslam, cosphi, sinphi, t, s, Krho, cosz;
        
        sinlam = Math.sin(lp.lam);
        coslam = Math.cos(lp.lam);
        switch (mode) {
            case EQUIT:
            case OBLIQ:
                sinphi = Math.sin(lp.phi);
                cosphi = Math.cos(lp.phi);
                cosz = cosphi * coslam;
                if (mode == OBLIQ)
                    cosz = sinph0 * sinphi + cosph0 * cosz;
                if (!no_cut && cosz < -EPS)
                    throw new ProjectionException("F ERROR");
                if (Math.abs(s = 1. - cosz) > EPS) {
                    t = 0.5 * (1. + cosz);
                    Krho = -Math.log(t)/s - Cb / t;
                } else
                    Krho = 0.5 - Cb;
                xy.x = Krho * cosphi * sinlam;
                if (mode == OBLIQ)
                    xy.y = Krho * (cosph0 * sinphi -
                    sinph0 * cosphi * coslam);
                else
                    xy.y = Krho * sinphi;
                break;
            case S_POLE:
            case N_POLE:
                lp.phi = Math.abs(p_halfpi - lp.phi);
                if (!no_cut && (lp.phi - EPS) > HALFPI)
                    throw new ProjectionException("F ERROR");
                if ((lp.phi *= 0.5) > EPS) {
                    t = Math.tan(lp.phi);
                    Krho = -2.*(Math.log(Math.cos(lp.phi)) / t + t * Cb);
                    xy.x = Krho * sinlam;
                    xy.y = Krho * coslam;
                    if (mode == N_POLE)
                        xy.y = -xy.y;
                } else
                    xy.x = xy.y = 0.;
        }
        return (xy);
    }
    
    /**
     * Sets the ParamSet for this projection.  Called once to initialise the projection.
     * Subclasses should ensure they call super.setParams(ps)
     * @param ps The ParamSet to use.
     * @throws ProjectionException Thrown if any of the required parameters are illegal or missing.
     */    
    public void setParams(ParamSet params)throws ProjectionException{
        super.setParams(params);
        double beta;
        
        no_cut = params.contains("no_cut");
        beta = 0.5 * (HALFPI - params.getRadiansParam("lat_b"));
        if (Math.abs(beta) < EPS)
            Cb = -0.5;
        else {
            Cb = 1./Math.tan(beta);
            Cb *= Cb * Math.log(Math.cos(beta));
        }
        if (Math.abs(Math.abs(phi0) - HALFPI) < EPS)
            if (phi0 < 0.) {
                p_halfpi = -HALFPI;
                mode = S_POLE;
            } else {
                p_halfpi =  HALFPI;
                mode = N_POLE;
            }
        else {
            if (Math.abs(phi0) < EPS)
                mode = EQUIT;
            else {
                mode = OBLIQ;
                sinph0 = Math.sin(phi0);
                cosph0 = Math.cos(phi0);
            }
        }
        ellipse.es = 0.;
    }
    
    
    
    /**
     * Must be implemented by concrete subclasses of Projection.
     * The method is called by forward() after some initial pre-processing of lp.
     * @param lp The lat, long values to project.
     * @return XY The xy coordinates of the projected lp.
     * @throws ProjectionException Thrown if the projection is not possible or if the values in lp are invalid.
     */
    protected XY doForward(LP lp) throws ProjectionException {
        return sForward(lp);
    }
    
    
    /**
     * Must be re-defined in concrete subclasses which support inverse projection.
     * By default, this method throws a ProjectionException - Inverse Projection Not Supported.
     * @param xy The xy coordinates to un-project.
     * @return LP The value of xy back projected to lat long values.
     * @throws ProjectionException Thrown if inverse projection is not possible or if xy contains illegal values.
     */   
    public String getDescription() {
        return java.util.ResourceBundle.getBundle("org/geotools/proj4j/projections/i18n").getString("AIRY");
    }
    
}
