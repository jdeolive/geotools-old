/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;
import org.opengis.filter.Filter;
import org.opengis.util.Cloneable;
import org.opengis.util.InternationalString;
import org.opengis.style.Description;

/**
 * Provides the default implementation of Rule.
 *
 * @author James Macgill
 * @source $URL$
 * @version $Id$
 */
public class RuleImpl implements Rule, Cloneable {
    private List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
    private List<Graphic> graphics = new ArrayList<Graphic>();
    private String name;
    private String title;
    private String abstractStr;
    private Filter filter = null;
    private boolean hasElseFilter = false;
    private double maxScaleDenominator = Double.POSITIVE_INFINITY;
    private double minScaleDenominator = 0.0;

    /**
     * Creates a new instance of DefaultRule
     */
    protected RuleImpl() {
    }

    /**
     * Creates a new instance of DefaultRule
     *
     * @param symbolizers DOCUMENT ME!
     */
    protected RuleImpl(Symbolizer[] symbolizers) {
        this.symbolizers.addAll(Arrays.asList(symbolizers));
    }

    public Graphic[] getLegendGraphic() {
        return (Graphic[]) graphics.toArray(new Graphic[0]);
    }

    public void addLegendGraphic(Graphic graphic) {
        graphics.add(graphic);
    }

    /**
     * A set of equivalent Graphics in different formats which can be used as a
     * legend against features stylized by the symbolizers in this rule.
     *
     * @param graphics An array of Graphic objects, any of which can be used as
     *        the legend.
     */
    public void setLegendGraphic(Graphic[] graphics) {
        List<Graphic> graphicList = Arrays.asList(graphics);
    	
        this.graphics.clear();
        this.graphics.addAll(graphicList);
    }

    public void addSymbolizer(Symbolizer symb) {
        this.symbolizers.add(symb);
    }

    public void setSymbolizers(Symbolizer[] syms) {
        List<Symbolizer> symbols = Arrays.asList(syms);
        this.symbolizers.clear();
        this.symbolizers.addAll(symbols);
    }

    public Symbolizer[] getSymbolizers() {
        return (Symbolizer[]) symbolizers.toArray(new Symbolizer[symbolizers
            .size()]);
    }
    
    public List<Symbolizer> symbolizers(){
    	return symbolizers;
    }

    public Description getDescription() {
    	return new Description(){
			public InternationalString getAbstract() {
				return new SimpleInternationalString(abstractStr);
			}

			public InternationalString getTitle() {
				return new SimpleInternationalString(title);
			}    	
			public Object accept(org.opengis.style.StyleVisitor visitor, Object data) {
				return visitor.visit(this, data);
			}    		
    	};
    }
    /**
     * Getter for property abstractStr.
     *
     * @return Value of property abstractStr.
     */
    public java.lang.String getAbstract() {
        return abstractStr;
    }

    /**
     * Setter for property abstractStr.
     *
     * @param abstractStr New value of property abstractStr.
     */
    public void setAbstract(java.lang.String abstractStr) {
        this.abstractStr = abstractStr;
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;        
    }

    /**
     * Getter for property title.
     *
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Setter for property title.
     *
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

    /**
     * Getter for property maxScaleDenominator.
     *
     * @return Value of property maxScaleDenominator.
     */
    public double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Setter for property maxScaleDenominator.
     *
     * @param maxScaleDenominator New value of property maxScaleDenominator.
     */
    public void setMaxScaleDenominator(double maxScaleDenominator) {
        this.maxScaleDenominator = maxScaleDenominator;
    }

    /**
     * Getter for property minScaleDenominator.
     *
     * @return Value of property minScaleDenominator.
     */
    public double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Setter for property minScaleDenominator.
     *
     * @param minScaleDenominator New value of property minScaleDenominator.
     */
    public void setMinScaleDenominator(double minScaleDenominator) {
        this.minScaleDenominator = minScaleDenominator;
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone of the rule.
     *
     * @see org.geotools.styling.Rule#clone()
     */
    public Object clone() {
        try {
            RuleImpl clone = (RuleImpl) super.clone();
            clone.graphics = new ArrayList<Graphic>();
            clone.symbolizers = new ArrayList<Symbolizer>();
            clone.filter = filter; // TODO: we must duplicate!

            Graphic[] legends = new Graphic[graphics.size()];

            for (int i = 0; i < legends.length; i++) {
                Graphic legend = (Graphic) graphics.get(i);
                legends[i] = (Graphic) ((Cloneable) legend).clone();
            }

            clone.setLegendGraphic(legends);

            Symbolizer[] symbArray = new Symbolizer[symbolizers.size()];

            for (int i = 0; i < symbArray.length; i++) {
                Symbolizer symb = (Symbolizer) symbolizers.get(i);
                symbArray[i] = (Symbolizer) ((Cloneable) symb).clone();
            }

            clone.setSymbolizers(symbArray);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This will never happen", e);
        }
    }

    /**
     * Generates a hashcode for the Rule.
     * 
     * <p>
     * For complex styles this can be an expensive operation since the hash
     * code is computed using all the hashcodes of the object within the
     * style.
     * </p>
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        result = (PRIME * result) + symbolizers.hashCode();

        if (graphics != null) {
            result = (PRIME * result) + graphics.hashCode();
        }

        if (name != null) {
            result = (PRIME * result) + name.hashCode();
        }

        if (title != null) {
            result = (PRIME * result) + title.hashCode();
        }

        if (abstractStr != null) {
            result = (PRIME * result) + abstractStr.hashCode();
        }

        if (filter != null) {
            result = (PRIME * result) + filter.hashCode();
        }

        result = (PRIME * result) + (hasElseFilter ? 1 : 0);

        long temp = Double.doubleToLongBits(maxScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);
        temp = Double.doubleToLongBits(minScaleDenominator);
        result = (PRIME * result) + (int) (temp >>> 32);
        result = (PRIME * result) + (int) (temp & 0xFFFFFFFF);

        return result;
    }

    /**
     * Compares this Rule with another for equality.
     * 
     * <p>
     * Two RuleImpls are equal if all their properties are equal.
     * </p>
     * 
     * <p>
     * For complex styles this can be an expensive operation since it checks
     * all objects for equality.
     * </p>
     *
     * @param oth The other rule to compare with.
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof RuleImpl) {
            RuleImpl other = (RuleImpl) oth;

            return Utilities.equals(name, other.name)
            && Utilities.equals(title, other.title)
            && Utilities.equals(abstractStr, other.abstractStr)
            && Utilities.equals(filter, other.filter)
            && (hasElseFilter == other.hasElseFilter)
            && Utilities.equals(graphics, other.graphics)
            && Utilities.equals(symbolizers, other.symbolizers)
            && (Double.doubleToLongBits(maxScaleDenominator) == Double
            .doubleToLongBits(other.maxScaleDenominator))
            && (Double.doubleToLongBits(minScaleDenominator) == Double
            .doubleToLongBits(other.minScaleDenominator));
        }

        return false;
    }
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append( "<RuleImpl");
        if( name != null ){
            buf.append(":");
            buf.append( name );
        }
        buf.append("> ");
        buf.append( filter );
        if( symbolizers != null ){
            buf.append( "\n" );
            for( Symbolizer symbolizer : symbolizers ){
                buf.append( "\t");
                buf.append( symbolizer );
                buf.append( "\n");            
            }
        }
        return buf.toString();
    }
}
