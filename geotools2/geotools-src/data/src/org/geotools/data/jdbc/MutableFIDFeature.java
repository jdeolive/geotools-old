/* $Id: MutableFIDFeature.java,v 1.1 2004/01/12 23:58:08 seangeo Exp $
 * 
 * Created on 13/01/2004
 */
package org.geotools.data.jdbc;

import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: MutableFIDFeature.java,v 1.1 2004/01/12 23:58:08 seangeo Exp $
 * Last Modified: $Date: 2004/01/12 23:58:08 $ 
 */
public class MutableFIDFeature extends DefaultFeature {
    private String fid;
    
    protected MutableFIDFeature(DefaultFeatureType ft, Object[] attributes, String fid)
            throws IllegalAttributeException{
        super(ft, attributes);  
        setID(fid);
    }
    /**
     * @return Returns the fid.
     */
    public String getID() {
        return fid;
    }

    /** Sets the FID.
     * 
     *  This is protected for safety reason, i.e. so client classes can't
     *  use it by casting to it.
     * @param fid The fid to set.
     */
    protected void setID(String id) {
        this.fid = id;
    }
}
