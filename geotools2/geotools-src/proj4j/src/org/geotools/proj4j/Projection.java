/*
 * Projection.java
 *
 * Created on February 19, 2002, 4:10 PM
 */

package org.geotools.proj4j;


/** 
 * The abstract base class from which all projection implementations are derived.
 * Subclasses should provide setup() and setDefaults() methods if required.
 *
 * Calls to forward() and inverse() are passed on to doForward, doInverse in implementing classes.
 *
 * @author James Macgill
 * @version $Revision: 1.10 $ $Date: 2002/03/05 23:53:27 $
 */
public abstract class Projection implements Constants {
    
    
    String descr;
    /** The set of parameters for this projection.
     */    
    protected ParamSet params;
    /** The Datum definition for this projection.
     */    
    protected Datum datum;
    boolean over;   /* over-range flag */
    boolean geoc;
    
    /** Central longitude.
     */
    protected double lam0;
    
    /** Central latitude.
     */
    protected double phi0;
    
    /** False easting.
     */
    protected double x0;
    
    /** False northing.
     */
    protected double y0;
    
    /** Unknown standard param value.
     * TODO: find definition.
     */    
    protected double
    k0;
    
    /** To meters conversion factor.
     * TODO: should use an instance of Unit.
     */
    protected double to_meter;
    
    /** From meters conversion factor.
     * TODO: should use an instance of Unit.
     */    
    protected double fr_meter; 
      
    
    boolean isLatLong;
    /** Ellipse definition for this projection.
     */    
    protected Ellipse ellipse;
    
    /**
     * Sets the ParamSet for this projection.  Called once to initialise the projection.
     * @param ps The ParamSet to use.
     * @throws ProjectionException Thrown if any of the required parameters are illegal or missing.
     */    
    public void setParams(ParamSet ps)throws ProjectionException{
        this.params = ps;
        if(!params.contains("no_defs")){
            setDefaults();
        }
        isLatLong=false;
        datum = new Datum(ps); // note: this may modify ellipse parameters in ps
        ellipse = new Ellipse(ps);
        
        
        geoc = (ellipse.es!=0 && params.contains("geoc"));
        over = params.contains("over");//over ranging flag
        lam0 = params.getFloatParam("lon_0");//central meridian
        phi0 = params.getFloatParam("phi_0");//central latitude
        
        //false easting and northing
        x0 = params.getFloatParam("x_0");
        y0 = params.getFloatParam("y_0");
        
        //general scaling factor
        if(params.contains("k_0")){
            k0=params.getFloatParam("k_0");
        }
        else if(params.contains("k")){
            k0 = params.getFloatParam("k");
        }
        else{
            k0=1f;
        }
        if(k0<=0){
            throw new ProjectionException("k <= 0");
        }
        
        //set units
        double s=0;
        Unit unit;
        if(params.contains("units")){
            unit = Unit.getUnit(params.getStringParam("units"));
            if(unit==null){
                throw new ProjectionException("Unkown units");
            }
            s = unit.to_meter;
            
            if (s!=0 || s==params.getFloatParam("to_meter")){
                to_meter = unit.to_meter;
                fr_meter = 1f/to_meter;
            }
        }
        else{
            to_meter = fr_meter = 1.;
        }
    }
    
    /** Sets any required parameters that are missing to their default values if possible.
     */    
    public void setDefaults(){//general
        
        params.addParamIfNotSet("ellips=WGS84");
    }
    
    
    
    
    /** Performs a forwards projection.
     * Following pre-processing, doForward(LP) is called in the concrete subclass.
     * The result from doForward is post processed before being returned.
     * @param lp The lat, long values to project.
     * @return XY The xy coordinates of the projected lp.
     * @throws ProjectionException Thrown if the projection is not possible or if the values in lp are invalid.
     */    
    public final XY forward(LP lp) throws ProjectionException{
        XY xy = new XY();
        double t;
        
        /* Checks for forward and latitude or longitude over range */
        if ((t = Math.abs(lp.phi)-HALFPI) >  EPS || Math.abs(lp.lam) > 10.) {
            xy.x = xy.y = Double.MAX_VALUE;
            throw new ProjectionException("latitude or longitude exceeded limits "+lp.phi+" "+lp.lam);
        } else { /* proceed with projection */
            if (Math.abs(t) <= EPS)
                lp.phi = lp.phi < 0. ? -HALFPI : HALFPI;
            else if (geoc)
                lp.phi = Math.atan(ellipse.rone_es * Math.tan(lp.phi));
            lp.lam -= lam0;	/* compute del lp.lam */
            if (!over)
                lp.lam = Functions.adjlon(lp.lam); /* adjust del longitude */
            xy =  doForward(lp);//the magic line
            /* adjust for major axis and easting/northings */
            
            xy.x = fr_meter * (ellipse.a * xy.x + x0);
            xy.y = fr_meter * (ellipse.a * xy.y + y0);
            
        }
        return xy;
    }
    /** Performs an inverse projection.
     * Following pre-processing, doInverse(XY) is called in the concrete subclass.
     * The result from doInverse is post processed before being returned.
     * @param xy The xy coordinates to un-project.
     * @return LP The value of xy back projected to lat long values.
     * @throws ProjectionException Thrown if inverse projection is not possible or if xy contains illegal values.
     */    
    public final LP inverse(XY xy) throws ProjectionException{
        LP lp = new LP();
        
        /* can't do as much preliminary checking as with forward */
        if (xy.x == Double.MAX_VALUE || xy.y == Double.MAX_VALUE) {
            lp.lam = lp.phi = Double.MAX_VALUE;
            throw new ProjectionException("invalid x or y");
            //pj_errno = -15;
        }
        //errno = pj_errno = 0; //this was just set, and now we clear it???
        xy.x = (xy.x * to_meter - x0) * ellipse.ra; /* descale and de-offset */
        xy.y = (xy.y * to_meter - y0) * ellipse.ra;
        lp=doInverse(xy);
        lp.lam += lam0; /* reduce from del lp.lam */
        if (!over)
            lp.lam = Functions.adjlon(lp.lam); /* adjust longitude to CM */
        if (geoc && Math.abs(Math.abs(lp.phi)-HALFPI) > EPS)
            lp.phi = Math.atan(ellipse.one_es * Math.tan(lp.phi));
        return lp;
    }
    
    /**
     * Must be implemented by concrete subclasses of Projection.
     * The method is called by forward() after some initial pre-processing of lp.
     * @param lp The lat, long values to project.
     * @return XY The xy coordinates of the projected lp.
     * @throws ProjectionException Thrown if the projection is not possible or if the values in lp are invalid.
     */    
    protected abstract XY doForward(LP lp) throws ProjectionException;
    /**
     * Must be re-defined in concrete subclasses which support inverse projection.
     * By default, this method throws a ProjectionException - Inverse Projection Not Supported.
     * @param xy The xy coordinates to un-project.
     * @return LP The value of xy back projected to lat long values.
     * @throws ProjectionException Thrown if inverse projection is not possible or if xy contains illegal values.
     */    
    protected LP doInverse(XY xy) throws ProjectionException{
        throw new ProjectionException("Inverse Projections not supported");
    }
    
    /**
     * Compares the datum definition of this projection with that of test's datum.
     * @param test The projection to compare datums with.
     * @return True if the datums are equivalent.
     */    
    public boolean isDatumEqual(Projection test){
        if( datum.datumType != test.datum.datumType ) {
            return false;
        }
        else if( ellipse.a != test.ellipse.a
        || Math.abs(ellipse.es - test.ellipse.es) > 0.000000000050 ) {
        /* the tolerence for es is to ensure that GRS80 and WGS84 are
           considered identical */
            return false;
        }
        else if( datum.datumType == Datum.PJD_GRIDSHIFT ) {
            return params.getStringParam("nadgrids").equals(test.params.getStringParam("nadgrids"));
        }
        else if( datum.datumType == Datum.PJD_3PARAM || datum.datumType == Datum.PJD_7PARAM ) {
            double[] a = datum.getParams();
            double[] b = test.datum.getParams();
            for(int i=0;i<a.length;i++){
                if(a[i]!=b[i])return false;
            }
            return true;
        }
        else
            return true;
    }
    
    
    /**
     * Tests to see if this is a 'dummy' projection.
     * @return True if this projection is simply lat long.
     */    
    public boolean isLatLong(){
        return isLatLong;
    }
    
    
}
