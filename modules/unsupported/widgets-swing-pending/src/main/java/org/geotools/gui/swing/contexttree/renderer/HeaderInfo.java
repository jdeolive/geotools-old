/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.contexttree.renderer;

import javax.swing.Icon;

/**
 *
 * @author johann sorel
 */
public class HeaderInfo {

    private final String identify;
    private final String inheader;
    private final Icon comp;
    
    
    /**
     * 
     * @param identify
     * @param inheader
     * @param tooltip
     * @param icon
     */
    public HeaderInfo(String identify, String inheader, Icon icon){
        this.identify = identify;
        this.inheader = inheader;
        this.comp = icon;
    }
        
    public String getHeaderText(){
        return inheader;
    }
    
    public Icon getIcon(){
        return comp;
    }
            
    @Override
    public String toString() {
        return identify;
    }

    
    
    
}
