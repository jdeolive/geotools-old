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
    public static final int  PJD_UNKNOWN   =0;
    public static final int  PJD_3PARAM    =1;   /* Molodensky */
    public static final int  PJD_7PARAM    =2;   /* Molodensky */
    public static final int  PJD_GRIDSHIFT =3;
    public static final int  PJD_WGS84     =4;   /* WGS84 (or anything considered equivelent) */
   
    String descr;
    ParamSet params;
    Datum datum;
    boolean over;   /* over-range flag */
    boolean geoc;   /* geocentric latitude flag */
    protected double
    a,  /* major axis or radius if es==0 */
    e,  /* eccentricity */
    es, /* e ^ 2 */
    ra, /* 1/A */
    one_es, /* 1 - e^2 */
    rone_es, /* 1/one_es */
    lam0, phi0, /* central longitude, latitude */
    x0, y0, /* easting and northing */
    k0,	/* general scaling factor */
    to_meter, fr_meter; /* cartesian scaling */
    int     datumType; /* PJD_UNKNOWN/3PARAM/7PARAM/GRIDSHIFT/WGS84 */
    //double datum_params[] = new double[7]; //now defined in Datum
    boolean isLatLong;
    
    public void setParams(ParamSet ps)throws ProjectionException{
        this.params = ps;
        if(!params.contains("no_defs")){
            setDefaults();
        }
        isLatLong=false;
        setupDatum();
        setupEll();
        e=Math.sqrt(es);
        ra=1f/a;
        one_es=1f-es;
        if(one_es==0){
            throw new ProjectionException("effective eccentricity = 1");
        }
        rone_es = 1f/one_es;
        if(datumType==PJD_3PARAM
        && datum.getParams()[0]==0
        && datum.getParams()[1]==0
        && datum.getParams()[2]==0
        && a == 6378137
        && Math.abs(es-0.006694379990) < 0.000000000050 )/*WSG84/GRS80*/ {
            datumType=PJD_WGS84;
        }
        geoc = (es!=0 && params.contains("geoc"));
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
        if(!params.contains("ellips")){
            params.addParam("ellips","WGS84");
        }
    }
    
    protected void setupEll() throws ProjectionException{
        double b=0.0, e;
        a=es=0d;
        /* R takes precedence */
        if(params.contains("R")){
            a = params.getFloatParam("R");
        }
        else{ /* probable elliptical figure */
            if(params.contains("ellips")){
                Ellips ellips = Ellips.getEllips(params.getStringParam("ellips"));
                if(ellips==null){
                    throw new ProjectionException("Unknown ellipse");
                }
                params.addParam(ellips.getMajor());
                params.addParam(ellips.getEll());
            }
            a=params.getFloatParam("a");
            if(params.contains("es")){
                es=params.getFloatParam("es");
            }
            else if(params.contains("e")){
                e = params.getFloatParam("e");
                es = e*e;
            }
            else if(params.contains("rf")){/* recip flattening */
                es=params.getFloatParam("rf");
                if(es==0){
                    throw new ProjectionException("reciprocal flattening (1/f) = 0");
                }
                es = 1f/es;
                es = es*(2f-es);
            }
            else if(params.contains("f")){/* flattening */
                es = params.getFloatParam("f");
                es = es * (2-es);
            }
            else if(params.contains("b")){ /* minor axis */
                b = params.getFloatParam("b");
                es = 1f-(b*b)/(a*a);
            }
            if(b==0){
                b=a*Math.sqrt(es);
            }
            /* following options turn ellipsoid into equivalent sphere */
            if(params.contains("R_A")){/* sphere--area of ellipsoid */
                a=1f-es*(SIXTH*es*(RA4+es*RA6));
                es=0f;
            }
            else if(params.contains("R_V")){/* sphere--vol. of ellipsoid */
                a=1f-es*(SIXTH*es*(RV4+es*RV6));
                es=0f;
            }
            else if(params.contains("R_a")){/* sphere--arithmetic mean */
                a=0.5f*(a+b);
                es=0;
            }
            else if(params.contains("R_g")){/* sphere--geometric mean */
                a=Math.sqrt(a*b);
                es=0;
            }
            else if(params.contains("R_h")){/* sphere--harmonic mean */
                a=2f*a*b/(a+b);
                es=0;
            }
            else if(params.contains("R_lat_a")){/* sphere--arith. */
                double tmp1 = Math.sin(params.getFloatParam("R_lat_a"));
                if(Math.abs(tmp1)>HALFPI){
                    throw new ProjectionException("|radius reference latitude| > 90");
                }
                tmp1 =1f-es*tmp1*tmp1;
                a = 0.5f*(1f-es*tmp1)/tmp1*Math.sqrt(tmp1);
                es=0;
            }
            else if(params.contains("R_lat_g")){ /* or geom. mean at latitude */
                double tmp2;
                
                tmp2 = Math.sin(params.getFloatParam("R_lat_g"));
                if(Math.abs(tmp2)>HALFPI){
                    throw new ProjectionException("|radius reference latitude| > 90");
                }
                tmp2 =1f-es*tmp2*tmp2;
                a=Math.sqrt(1f-es)/tmp2;
                es=0;
            }
            
            //should probably removed added params
        }
        if(es<0){
            throw new ProjectionException("squared eccentricity < 0");
        }
        if(a<=0){
            throw new ProjectionException("major axis or radius = 0 or not given");
        }
    }
    
    protected void setupDatum() throws ProjectionException{
        String name, towgs84, nadgrids;
        
        datumType = PJD_UNKNOWN;
        
        if(params.contains("datum")){//more of this needs to move into Datum.java
            datum = Datum.getDatum(params.getStringParam("datum"));
            if(datum==null) throw new ProjectionException("datum unknown");
            String ellipse = datum.getEllipseID();
            if(ellipse != null || ellipse.length() >0){
                params.addParam("ellips",ellipse);
            }
            String defn = datum.getDefn();
            if(defn != null || defn.length() >0){
                params.addParam(defn);
            }
            if(params.contains("nadgrids")){
                 /* We don't actually save the value separately.  It will continue
                  *  to exist in the param list for use in applyGridShift
                  */
                datumType = PJD_GRIDSHIFT;
            }
            else if(params.contains("towgs84")){
                if(datum.getParamCount()==3) datumType = PJD_3PARAM;
                if(datum.getParamCount()==7) datumType = PJD_7PARAM;
            }
        }
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
                lp.phi = Math.atan(rone_es * Math.tan(lp.phi));
            lp.lam -= lam0;	/* compute del lp.lam */
            if (!over)
                lp.lam = Misc.adjlon(lp.lam); /* adjust del longitude */
            xy =  doForward(lp);//the magic line
            /* adjust for major axis and easting/northings */
            
            xy.x = fr_meter * (a * xy.x + x0);
            xy.y = fr_meter * (a * xy.y + y0);
            
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
        xy.x = (xy.x * to_meter - x0) * ra; /* descale and de-offset */
        xy.y = (xy.y * to_meter - y0) * ra;
        lp=doInverse(xy);
        lp.lam += lam0; /* reduce from del lp.lam */
        if (!over)
            lp.lam = Misc.adjlon(lp.lam); /* adjust longitude to CM */
        if (geoc && Math.abs(Math.abs(lp.phi)-HALFPI) > EPS)
            lp.phi = Math.atan(one_es * Math.tan(lp.phi));
        return lp;
    }
    
    protected abstract XY doForward(LP lp) throws ProjectionException;
    protected abstract LP doInverse(XY XY) throws ProjectionException;
    
    public boolean isDatumEqual(Projection test){
        if( datumType != test.datumType ) {
            return false;
        }
        else if( a != test.a
        || Math.abs(es - test.es) > 0.000000000050 ) {
        /* the tolerence for es is to ensure that GRS80 and WGS84 are
           considered identical */
            return false;
        }
        else if( datumType == PJD_GRIDSHIFT ) {
            return params.getStringParam("nadgrids").equals(test.params.getStringParam("nadgrids"));
        }
        else if( datumType == PJD_3PARAM || datumType == PJD_7PARAM ) {
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
