/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;

public class StyledLayerDescriptor{
    
    /** Holds value of property name. */
    private String name;    
    
    /** Holds value of property title. */
    private String title;    
   
    /** Holds value of property abstract. */
    private String abstractStr;
  
    private List layers = new ArrayList();
    
    public StyledLayer[] getStyledLayers(){
        return (StyledLayer[])layers.toArray(new StyledLayer[layers.size()]);
    }

    public void setSytledLayers(StyledLayer[] layers){}
    
    public void addStyledLayer(StyledLayer layer){
        layers.add(layer);
        
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public String getName() {
        return this.name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Getter for property title.
     * @return Value of property title.
     *
     */
    public String getTitle() {
        return this.title;
    }
    
    /** Setter for property title.
     * @param title New value of property title.
     *
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /** Getter for property abstractStr.
     * @return Value of property abstractStr.
     *
     */
    public java.lang.String getAbstract() {
        return abstractStr;
    }
    
    /** Setter for property abstractStr.
     * @param abstractStr New value of property abstractStr.
     *
     */
    public void setAbstract(java.lang.String abstractStr) {
        this.abstractStr = abstractStr;
    }
    
}
