/*
 * Elips.java
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
     * TODO: Only a small number of the full PROJ set are listed, the rest need to be moved over.
     * TODO: A hard coded list may be a bad idea, parhaps these should be loaded from a .parameter file.
     */ 
     static final Ellipse[] ellips={
         //          id          major          elliptical    name/comments
         new Ellipse("MERIT",	"a=6378137.0", "rf=298.257", "MERIT 1983"),
         new Ellipse("SGS85",	"a=6378136.0", "rf=298.257",  "Soviet Geodetic System 85"),
         //...
         new Ellipse("WGS84",    "a=6378137.0",  "rf=298.257223563", "WGS 84")      
     };
    
}
