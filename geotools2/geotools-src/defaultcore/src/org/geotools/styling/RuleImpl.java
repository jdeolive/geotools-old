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


import java.util.ArrayList;
import java.util.List;

import org.geotools.filter.Filter;

/** Provides the default implementation of Rule.
 * 
 * @version $Id: RuleImpl.java,v 1.10 2003/08/10 08:39:28 seangeo Exp $
 * @author James Macgill
 */
public class RuleImpl implements Rule, Cloneable {
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

    public void addLegendGraphic(Graphic graphic) {
        graphics.add(graphic);
    }

    /** A set of equivalent Graphics in different formats which can be used
     * as a legend against features stylized by the symbolizers in this
     * rule.
     *
     * @param graphics An array of Graphic objects, any of which can be used as
     *         the legend.
     *
     */
    public void setLegendGraphic(Graphic[] graphics) {
        this.graphics.clear();

        for (int i = 0; i < graphics.length; i++) {
            addLegendGraphic(graphics[i]);
        }
    }

    public void setSymbolizers(Symbolizer[] syms) {
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

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean hasElseFilter() {
        return hasElseFilter;
    }

    public void setIsElseFilter(boolean flag) {
        hasElseFilter = flag;
    }

    public void setHasElseFilter() {
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
    
    public void accept(StyleVisitor visitor){
        visitor.visit(this);
    }
    
    /** Creates a deep copy clone of the rule.
     * @see org.geotools.styling.Rule#clone()
     */
    public Object clone() {
        Rule clone;
        try {
            clone = (Rule) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen.
        }
        
        Graphic[] legends = new Graphic[graphics.size()];
        for (int i = 0; i < legends.length; i++) {
            Graphic legend = (Graphic) graphics.get(i);
            legends[i] = (Graphic) legend.clone();
        } 
        clone.setLegendGraphic(legends);
        
        Symbolizer[] symbArray = new Symbolizer[symbolizers.length];
        for (int i = 0; i < symbArray.length; i++) {
            symbArray[i] = (Symbolizer) symbolizers[i].clone();
        }
        clone.setSymbolizers(symbArray);
        
        return clone;
    }

    /** Generates a hashcode for the Rule.
     * 
     *  @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        result = PRIME * result + hashCodeHelper(symbolizers);
        if (graphics != null) {
            result = PRIME * result + graphics.hashCode();
        }
        if (name != null) {
            result = PRIME * result + name.hashCode();
        }
        if (title != null) {
            result = PRIME * result + title.hashCode();
        }
        if (abstractStr != null) {
            result = PRIME * result + abstractStr.hashCode();
        }
        if (filter != null) {
            result = PRIME * result + filter.hashCode();
        }
        result = PRIME * result + (hasElseFilter ? 1 : 0);
        long temp = Double.doubleToLongBits(maxScaleDenominator);
        result = PRIME * result + (int) (temp >>> 32);
        result = PRIME * result + (int) (temp & 0xFFFFFFFF);
        temp = Double.doubleToLongBits(minScaleDenominator);
        result = PRIME * result + (int) (temp >>> 32);
        result = PRIME * result + (int) (temp & 0xFFFFFFFF);

        return result;
    }

    /*
     * Helper method to compute the hashCode of arbitrary arrays.
     */
    private int hashCodeHelper(Object a) {
        final int PRIME = 1000003;
        if (a == null) {
            return 0;
        }
        if (!a.getClass().isArray()) {
            return a.hashCode();
        }

        int result = 0;
        int aLength = java.lang.reflect.Array.getLength(a);
        for (int i = 0; i < aLength; i++) {
            result = PRIME * result + hashCodeHelper(java.lang.reflect.Array.get(a, i));
        }

        return result;
    }

    /** Compares this Rule with another for equality.
     *  
     *  <p>Two RuleImpls are equal if all their properties
     *  are equal.
     * 
     *  @param oth The other rule to compare with.
     *  @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        RuleImpl other = (RuleImpl) oth;
        
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else {
            if (!this.name.equals(other.name)) {
                return false;
            }
        }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        } else {
            if (!this.title.equals(other.title)) {
                return false;
            }
        }
        if (this.abstractStr == null) {
            if (other.abstractStr != null) {
                return false;
            }
        } else {
            if (!this.abstractStr.equals(other.abstractStr)) {
                return false;
            }
        }
        if (this.filter == null) {
            if (other.filter != null) {
                return false;
            }
        } else {
            if (!this.filter.equals(other.filter)) {
                return false;
            }
        }

        if (this.hasElseFilter != other.hasElseFilter) {
            return false;
        }

        if (Double.doubleToLongBits(maxScaleDenominator) != 
                Double.doubleToLongBits(other.maxScaleDenominator)) {
            return false;
        }

        if (Double.doubleToLongBits(minScaleDenominator) != 
                Double.doubleToLongBits(other.minScaleDenominator)) {
            return false;
        }
        if (!equalsHelper(symbolizers, other.symbolizers)) {
            return false;
        }
        if (this.graphics == null) {
            if (other.graphics != null) {
                return false;
            }
        } else {
            if (!this.graphics.equals(other.graphics)) {
                return false;
            }
        }

        return true;
    }

    /*
     * Helper method to compare two arbitrary arrays.
     */
    private boolean equalsHelper(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            return a.equals(b);
        }

        int aLength = java.lang.reflect.Array.getLength(a);
        if (aLength != java.lang.reflect.Array.getLength(b)) {
            return false;
        }

        for (int i = 0; i < aLength; i++) {
            if (!equalsHelper(java.lang.reflect.Array.get(a, i), java.lang.reflect.Array.get(b, i))) {
                return false;
            }
        }

        return true;
    }

}