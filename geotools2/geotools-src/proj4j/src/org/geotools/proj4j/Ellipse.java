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
public class Ellipse {
    protected String	id;	/* ellipse keyword name */
    protected String	major;	/* a= value */
    protected String	ell;	/* elliptical parameter */
    protected String	name;	/* comments */
    
    /** Creates a new instance of Elips
     *
     * @param id ellipse keyword name.
     * @param major 'a=' value
     * @param elliptical parameter
     * @param name description and comments
     */
    public Ellipse(String id, String major, String ell, String name) {
        this.id = id;
        this.major = major;
        this.ell = ell;
        this.name = name;
    }
    
    /**
     * Retreve standard predefined ellipse by keyword id.
     * @param  id keyword id of ellips to retrive, matching is case insensitive
     * @return An Ellips from the standard list or <code>null</code> if no match is found
     */
    public static Ellipse getEllipse(String id){
        for(int i=0;i<ellips.length;i++){
            if(id.equalsIgnoreCase(ellips[i].id)){
                return ellips[i];
            }
        }
        return null;
    }
    
    /**
     * Getter for the major paramiter
     * @return A String in the form 'a= '
     */
    public String getMajor(){
        return major;
    }
    
    /**
     * Getter for the elliptical perametier
     * @return A String containing the elliptical parameters
     **/
    public String getEll(){
        return ell;
    }
    
    /**
     * Getter for the descriptiong perametier
     * @return A String containing the full description and comments
     **/
    public String getName(){
        return name;
    }
    
    /**
     * List of standard ellips defenitions, individual ellips from this list can be retreved by calling {@link #getEllips}.
     */
    static final Ellipse[] ellips={
        //          id          major          elliptical    name/comments
        new Ellipse("MERIT",	"a=6378137.0", "rf=298.257", "MERIT 1983"),
        new Ellipse("SGS85",	"a=6378136.0", "rf=298.257",  "Soviet Geodetic System 85"),
        new Ellipse("GRS80",	"a=6378137.0", "rf=298.257222101", "GRS 1980(IUGG, 1980)"),
        new Ellipse("IAU76",	"a=6378140.0", "rf=298.257", "IAU 1976"),
        new Ellipse("airy",		"a=6377563.396", "b=6356256.910", "Airy 1830"),
        new Ellipse("APL4.9",	"a=6378137.0.",  "rf=298.25", "Appl. Physics. 1965"),
        new Ellipse("NWL9D",	"a=6378145.0.",  "rf=298.25", "Naval Weapons Lab., 1965"),
        new Ellipse("mod_airy",	"a=6377340.189", "b=6356034.446", "Modified Airy"),
        new Ellipse("andrae",	"a=6377104.43",  "rf=300.0", 	"Andrae 1876 (Den., Iclnd.)"),
        new Ellipse("aust_SA",	"a=6378160.0", "rf=298.25", "Australian Natl & S. Amer. 1969"),
        new Ellipse("GRS67",	"a=6378160.0", "rf=298.2471674270", "GRS 67(IUGG 1967)"),
        new Ellipse("bessel",	"a=6377397.155", "rf=299.1528128", "Bessel 1841"),
        new Ellipse("bess_nam",	"a=6377483.865", "rf=299.1528128", "Bessel 1841 (Namibia)"),
        new Ellipse("clrk66",	"a=6378206.4", "b=6356583.8", "Clarke 1866"),
        new Ellipse("clrk80",	"a=6378249.145", "rf=293.4663", "Clarke 1880 mod."),
        new Ellipse("CPM",  	"a=6375738.7", "rf=334.29", "Comm. des Poids et Mesures 1799"),
        new Ellipse("delmbr",	"a=6376428.",  "rf=311.5", "Delambre 1810 (Belgium)"),
        new Ellipse("engelis",	"a=6378136.05", "rf=298.2566", "Engelis 1985"),
        new Ellipse("evrst30",  "a=6377276.345", "rf=300.8017",  "Everest 1830"),
        new Ellipse("evrst48",  "a=6377304.063", "rf=300.8017",  "Everest 1948"),
        new Ellipse("evrst56",  "a=6377301.243", "rf=300.8017",  "Everest 1956"),
        new Ellipse("evrst69",  "a=6377295.664", "rf=300.8017",  "Everest 1969"),
        new Ellipse("evrstSS",  "a=6377298.556", "rf=300.8017",  "Everest (Sabah & Sarawak)"),
        new Ellipse("fschr60",  "a=6378166.",   "rf=298.3", "Fischer (Mercury Datum) 1960"),
        new Ellipse("fschr60m", "a=6378155.",   "rf=298.3", "Modified Fischer 1960"),
        new Ellipse("fschr68",  "a=6378150.",   "rf=298.3", "Fischer 1968"),
        new Ellipse("helmert",  "a=6378200.",   "rf=298.3", "Helmert 1906"),
        new Ellipse("hough",	"a=6378270.0", "rf=297.", "Hough"),
        new Ellipse("intl",		"a=6378388.0", "rf=297.", "International 1909 (Hayford)"),
        new Ellipse("krass",	"a=6378245.0", "rf=298.3", "Krassovsky, 1942"),
        new Ellipse("kaula",	"a=6378163.",  "rf=298.24", "Kaula 1961"),
        new Ellipse("lerch",	"a=6378139.",  "rf=298.257", "Lerch 1979"),
        new Ellipse("mprts",	"a=6397300.",  "rf=191.", "Maupertius 1738"),
        new Ellipse("new_intl",	"a=6378157.5", "b=6356772.2", "New International 1967"),
        new Ellipse("plessis",	"a=6376523.",  "b=6355863.", "Plessis 1817 (France)"),
        new Ellipse("SEasia",	"a=6378155.0", "b=6356773.3205", "Southeast Asia"),
        new Ellipse("walbeck",	"a=6376896.0", "b=6355834.8467", "Walbeck"),
        new Ellipse("WGS60",    "a=6378165.0",  "rf=298.3", "WGS 60"),
        new Ellipse("WGS66",	"a=6378145.0", "rf=298.25", "WGS 66"),
        new Ellipse("WGS72",	"a=6378135.0", "rf=298.26", "WGS 72"),
        new Ellipse("WGS84",    "a=6378137.0",  "rf=298.257223563", "WGS 84"),
        new Ellipse("sphere",   "a=6370997.0",  "b=6370997.0", "Normal Sphere (r=6370997)")
    };
    
}
