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
 *
 */

package org.geotools.styling;

/**
 * @version $Id: DefaultFeatureTypeStyle.java,v 1.5 2002/06/04 16:52:09 loxnard Exp $
 * @author James Macgill
 */
public class DefaultFeatureTypeStyle implements org.geotools.styling.FeatureTypeStyle {
    private Rule[] ruleList;// Possibly not the best storage
    private String featureTypeName = "feature";
    String name = "name";
    String title = "title";
    String abstractStr = "abstract";
    /** Creates a new instance of DefaultFeatureTypeStyle */
    public DefaultFeatureTypeStyle() {
        ruleList = new Rule[0];
    }
    
    public String getFeatureTypeName() {
        return featureTypeName;//HACK: - generic, catch all type name
    }
    
    public Rule[] getRules() {
        return ruleList;
    }
    
    public String[] getSymantecTypeIdentifiers() {
        return new String[]{"generic:geometry"};//HACK: - generic catch all identifier
    }
    
    public void setRules(Rule[] rules){
        ruleList = rules;
    }
    
    public void setFeatureTypeName(String name) {
        featureTypeName=name;
    }
    
    /**
     * Getter for property abstractStr.
     * @return Value of property abstractStr.
     */
    public java.lang.String getAbstract() {
        return abstractStr;
    }
    
    /**
     * Setter for property abstractStr.
     * @param abstractStr New value of property abstractStr.
     */
    public void setAbstract(java.lang.String abstractStr) {
        this.abstractStr = abstractStr;
    }
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Getter for property title.
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }
    
    /**
     * Setter for property title.
     * @param title New value of property title.
     */
    public void setTitle(java.lang.String title) {
        this.title = title;
    }
    
}
