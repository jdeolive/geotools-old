/*
 * Projection.java
 *
 * Created on February 19, 2002, 4:10 PM
 */

package org.geotools.proj4j;


/**
 *
 * @author  jamesm
 */
public abstract class Projection implements Constants {
    
    
    String descr;
    protected ParamSet params;
    protected Datum datum;
    boolean over;   /* over-range flag */
    boolean geoc;   /* geocentric latitude flag */
    protected double
    lam0, phi0, /* central longitude, latitude */
    x0, y0, /* easting and northing */
    k0,	/* general scaling factor */
    to_meter, fr_meter; /* cartesian scaling */
    
    
    boolean isLatLong;
    protected Ellipse ellipse;
    
    public void setParams(ParamSet ps)throws ProjectionException{
        this.params = ps;
        if(!params.contains("no_defs")){
            setDefaults();
        }
        isLatLong=false;
        datum = new Datum(ps); // note this may modify elipse parameters in ps
        ellipse = new Ellipse(ps);
        
        
        geoc = (ellipse.es!=0 && params.contains("geoc"));
        over = params.contains("over");//over ranging flag
        lam0 = params.getFloatParam("lon_0");//centeral meridian
        phi0 = params.getFloatParam("phi_0");//centeral latitude
        
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
    
    public void setDefaults(){//general
        
        params.addParamIfNotSet("ellips=WGS84");
    }
    
    
    
    
    public XY forward(LP lp) throws ProjectionException{
        XY xy = new XY();
        double t;
        
        /* check for forward and latitude or longitude overange */
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
                lp.lam = Misc.adjlon(lp.lam); /* adjust del longitude */
            xy =  doForward(lp);//the magic line
            /* adjust for major axis and easting/northings */
            
            xy.x = fr_meter * (ellipse.a * xy.x + x0);
            xy.y = fr_meter * (ellipse.a * xy.y + y0);
            
        }
        return xy;
    }
    public LP inverse(XY xy) throws ProjectionException{
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
            lp.lam = Misc.adjlon(lp.lam); /* adjust longitude to CM */
        if (geoc && Math.abs(Math.abs(lp.phi)-HALFPI) > EPS)
            lp.phi = Math.atan(ellipse.one_es * Math.tan(lp.phi));
        return lp;
    }
    
    protected abstract XY doForward(LP lp) throws ProjectionException;
    protected abstract LP doInverse(XY XY) throws ProjectionException;
    
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
    
    
    public boolean isLatLong(){
        return isLatLong;
    }
    
    
}
