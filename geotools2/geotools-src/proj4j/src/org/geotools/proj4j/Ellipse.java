/*
 * Ellipse.java
 *
 * Created on 19 February 2002, 22:25
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class Ellipse implements Constants {
//    protected String	id;	/* ellipse keyword name */
//    protected String	major;	/* a= value */
//    protected String	ell;	/* elliptical parameter */
//    protected String	name;	/* comments */
    public double a,  /* major axis or radius if es==0 */
    e,  /* eccentricity */
    es, /* e ^ 2 */
    ra, /* 1/A */
    one_es, /* 1 - e^2 */
    rone_es; /* 1/one_es */

    
    /** Creates a new instance of Elips
     *
     * @param id ellipse keyword name.
     * @param major 'a=' value
     * @param elliptical parameter
     * @param name description and comments
     */
    public Ellipse(ParamSet params) throws ProjectionException{
        System.out.println(params.toString());
        setup(params);
    }
    private void setup(ParamSet params) throws ProjectionException{
        double b=0.0;
        a=es=0d;
        /* R takes precedence */
        if(params.contains("R")){
            a = params.getFloatParam("R");
        }
        else{ /* probable elliptical figure */
            if(params.contains("ellips")){
                String[] defaults = Ellipse.getDefaultsForEllipse(params.getStringParam("ellips"));
                if(ellips==null){
                    throw new ProjectionException("Unknown ellipse");
                }
                System.out.println("Ellipse Defaults "+defaults[1]+" "+defaults[2]);
                params.addParamIfNotSet(defaults[1]); // major axis
                params.addParamIfNotSet(defaults[2]); // elliptical param
            }
            if(params.contains("a")){
                a=params.getFloatParam("a");
            }
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
        e=Math.sqrt(es);
        ra=1f/a;
        one_es=1f-es;
        if(one_es==0){
            throw new ProjectionException("effective eccentricity = 1");
        }
        rone_es = 1f/one_es;
        System.out.println(params.toString());
    }
    /**
     * Retreve standard predefined ellipse by keyword id.
     * @param  id keyword id of ellips to retrive, matching is case insensitive
     * @return An Ellips from the standard list or <code>null</code> if no match is found
     */
    public static String[] getDefaultsForEllipse(String id){
        for(int i=0;i<ellips.length;i++){
            if(id.equalsIgnoreCase(ellips[i][0])){
                return ellips[i];
            }
        }
        return null;
    }
    
    
    /** Getter for property a.
     * @return Value of property a.
     */
    public double getA() {
        return a;
    }
    
    
    /** Getter for property e.
     * @return Value of property e.
     */
    public double getE() {
        return e;
    }
    
    
    
    /**
     * List of standard ellips defenitions, individual ellips from this list can be retreved by calling {@link #getEllips}.
     */
    static final String[][] ellips={
        //          id          major          elliptical    name/comments
        new String[] {"MERIT",	"a=6378137.0", "rf=298.257", "MERIT 1983"},
        new String[] {"SGS85",	"a=6378136.0", "rf=298.257",  "Soviet Geodetic System 85"},
        new String[] {"GRS80",	"a=6378137.0", "rf=298.257222101", "GRS 1980(IUGG, 1980)"},
        new String[] {"IAU76",	"a=6378140.0", "rf=298.257", "IAU 1976"},
        new String[] {"airy",		"a=6377563.396", "b=6356256.910", "Airy 1830"},
        new String[] {"APL4.9",	"a=6378137.0.",  "rf=298.25", "Appl. Physics. 1965"},
        new String[] {"NWL9D",	"a=6378145.0.",  "rf=298.25", "Naval Weapons Lab., 1965"},
        new String[] {"mod_airy",	"a=6377340.189", "b=6356034.446", "Modified Airy"},
        new String[] {"andrae",	"a=6377104.43",  "rf=300.0", 	"Andrae 1876 (Den., Iclnd.)"},
        new String[] {"aust_SA",	"a=6378160.0", "rf=298.25", "Australian Natl & S. Amer. 1969"},
        new String[] {"GRS67",	"a=6378160.0", "rf=298.2471674270", "GRS 67(IUGG 1967)"},
        new String[] {"bessel",	"a=6377397.155", "rf=299.1528128", "Bessel 1841"},
        new String[] {"bess_nam",	"a=6377483.865", "rf=299.1528128", "Bessel 1841 (Namibia)"},
        new String[] {"clrk66",	"a=6378206.4", "b=6356583.8", "Clarke 1866"},
        new String[] {"clrk80",	"a=6378249.145", "rf=293.4663", "Clarke 1880 mod."},
        new String[] {"CPM",  	"a=6375738.7", "rf=334.29", "Comm. des Poids et Mesures 1799"},
        new String[] {"delmbr",	"a=6376428.",  "rf=311.5", "Delambre 1810 (Belgium)"},
        new String[] {"engelis",	"a=6378136.05", "rf=298.2566", "Engelis 1985"},
        new String[] {"evrst30",  "a=6377276.345", "rf=300.8017",  "Everest 1830"},
        new String[] {"evrst48",  "a=6377304.063", "rf=300.8017",  "Everest 1948"},
        new String[] {"evrst56",  "a=6377301.243", "rf=300.8017",  "Everest 1956"},
        new String[] {"evrst69",  "a=6377295.664", "rf=300.8017",  "Everest 1969"},
        new String[] {"evrstSS",  "a=6377298.556", "rf=300.8017",  "Everest (Sabah & Sarawak)"},
        new String[] {"fschr60",  "a=6378166.",   "rf=298.3", "Fischer (Mercury Datum) 1960"},
        new String[] {"fschr60m", "a=6378155.",   "rf=298.3", "Modified Fischer 1960"},
        new String[] {"fschr68",  "a=6378150.",   "rf=298.3", "Fischer 1968"},
        new String[] {"helmert",  "a=6378200.",   "rf=298.3", "Helmert 1906"},
        new String[] {"hough",	"a=6378270.0", "rf=297.", "Hough"},
        new String[] {"intl",		"a=6378388.0", "rf=297.", "International 1909 (Hayford)"},
        new String[] {"krass",	"a=6378245.0", "rf=298.3", "Krassovsky, 1942"},
        new String[] {"kaula",	"a=6378163.",  "rf=298.24", "Kaula 1961"},
        new String[] {"lerch",	"a=6378139.",  "rf=298.257", "Lerch 1979"},
        new String[] {"mprts",	"a=6397300.",  "rf=191.", "Maupertius 1738"},
        new String[] {"new_intl",	"a=6378157.5", "b=6356772.2", "New International 1967"},
        new String[] {"plessis",	"a=6376523.",  "b=6355863.", "Plessis 1817 (France)"},
        new String[] {"SEasia",	"a=6378155.0", "b=6356773.3205", "Southeast Asia"},
        new String[] {"walbeck",	"a=6376896.0", "b=6355834.8467", "Walbeck"},
        new String[] {"WGS60",    "a=6378165.0",  "rf=298.3", "WGS 60"},
        new String[] {"WGS66",	"a=6378145.0", "rf=298.25", "WGS 66"},
        new String[] {"WGS72",	"a=6378135.0", "rf=298.26", "WGS 72"},
        new String[] {"WGS84",    "a=6378137.0",  "rf=298.257223563", "WGS 84"},
        new String[] {"sphere",   "a=6370997.0",  "b=6370997.0", "Normal Sphere (r=6370997)"}
    };
    
}
