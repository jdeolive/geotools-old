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
 * @version $Id: RuleImpl.java,v 1.3 2003/05/16 21:10:21 jmacgill Exp $
 * @author James Macgill
 */
import java.util.ArrayList;
import java.util.List;
import org.geotools.filter.Filter;

public class RuleImpl implements org.geotools.styling.Rule {

    private Symbolizer[] symbolizers;
    private List graphics = new ArrayList();
    
    private String name = "name";
    
    private String title = "title";
    
    private String abstractStr = "Abstract";
    private Filter filter = null;
    private boolean hasElseFilter = false;
    private double maxScaleDenominator = Double.POSITIVE_INFINITY;
    private double minScaleDenominator = 0.0;
    /** Creates a new instance of DefaultRule */
    protected RuleImpl() {
        symbolizers = new Symbolizer[0];
    }
    /** Creates a new instance of DefaultRule */
    protected RuleImpl(Symbolizer[] symbolizers) {
        this.symbolizers = symbolizers;
    }
    public Graphic[] getLegendGraphic() {
        return (Graphic[]) graphics.toArray(new Graphic[0]);
    }
    public void addLegendGraphic(Graphic g){
        graphics.add(g);
    }
    /** A set of equivalent Graphics in different formats which can be used
     * as a legend against features stylized by the symbolizers in this
     * rule.
     *
     * @param An array of Graphic objects, any of which can be used as
     *         the legend.
     *
     */
    public void setLegendGraphic(Graphic[] graphics) {
        for(int i=0;i<graphics.length;i++){
            addLegendGraphic(graphics[i]);
        }
    }
    
    
    public void setSymbolizers(Symbolizer[] syms){
        symbolizers = syms;
    }
    
    public Symbolizer[] getSymbolizers() {
        return symbolizers;
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
    
    public Filter getFilter() {
        return filter;
    }
    
    public void setFilter(Filter filter){
        this.filter = filter;
    }
    
    public boolean hasElseFilter() {
        return hasElseFilter;
    }
    public void setIsElseFilter(boolean flag){
        hasElseFilter = flag;
    }
    public void setHasElseFilter(){
        hasElseFilter = true;
    }
    
    /** Getter for property maxScaleDenominator.
     * @return Value of property maxScaleDenominator.
     *
     */
    public double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }
    
    /** Setter for property maxScaleDenominator.
     * @param maxScaleDenominator New value of property maxScaleDenominator.
     *
     */
    public void setMaxScaleDenominator(double maxScaleDenominator) {
        this.maxScaleDenominator = maxScaleDenominator;
    }
    
    /** Getter for property minScaleDenominator.
     * @return Value of property minScaleDenominator.
     *
     */
    public double getMinScaleDenominator() {
        return minScaleDenominator;
    }
    
    /** Setter for property minScaleDenominator.
     * @param minScaleDenominator New value of property minScaleDenominator.
     *
     */
    public void setMinScaleDenominator(double minScaleDenominator) {
        this.minScaleDenominator = minScaleDenominator;
    }
    
    
    
}
