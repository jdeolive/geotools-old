/*
 * LP.java
 *
 * Created on 20 February 2002, 02:46
 */

package org.geotools.proj4j;
import java.util.StringTokenizer;
/**
 *
 * @author  James Macgill
 */
public class LP {
    public double lam, phi;
    /** Creates a new instance of LP */
    public LP(){
    }
    
    public LP(double lam,double phi) {
        this.lam = lam;
        this.phi = phi;
    }
   
    public LP(String coord){
        StringTokenizer tok = new StringTokenizer(coord," ,");
        lam = Misc.dmsToR(tok.nextToken());
        phi = Misc.dmsToR(tok.nextToken());
    }
    
    public LP(String lam,String phi){
        this.lam = Misc.dmsToR(lam);
        this.phi = Misc.dmsToR(phi);
    }
    
    
}
