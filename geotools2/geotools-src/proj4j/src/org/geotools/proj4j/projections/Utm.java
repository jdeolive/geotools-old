/*
 * Utm.java
 *
 * Created on 22 February 2002, 19:17
 */

package org.geotools.proj4j.projections;

import org.geotools.proj4j.*;

/**
 *
 * @author  James Macgill
 */
public class Utm extends Tmerc implements Constants{

    /** Creates a new instance of Utm */
    public Utm() {
    }
    
    public void setParams(ParamSet params)throws ProjectionException{
        super.setParams(params);
        int zone;

	if (es==0) throw new ProjectionException("elliptical usage required");
	y0 = params.contains("south") ? 10000000. : 0.;
	x0 = 500000.;
	if (params.contains("zone")){ /* zone input ? */
               zone = params.getIntegerParam("zone");
		if (zone > 0 && zone <= 60){
			--zone;
                }
		else{
			throw new ProjectionException("invalid UTM zone number");
                }
        }
	else /* nearest central meridian input */
            zone = (int)(Math.floor((Misc.adjlon(lam0) + PI) * 30. / PI));//is the int cast a problem?
		if (zone < 0)
			zone = 0;
		else if (zone >= 60)
			zone = 59;
	lam0 = (zone + .5) * PI / 30. - PI;
	k0 = 0.9996;
	phi0 = 0.;
    }

}
