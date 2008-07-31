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
import java.util.logging.Logger;

import org.geotools.resources.Utilities;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.Cloneable;
import org.opengis.util.InternationalString;
import org.opengis.style.Description;

/**
 * DOCUMENT ME!
 *
 * @author James Macgill
 * @source $URL$
 * @version $Id$
 */
public class FeatureTypeStyleImpl implements FeatureTypeStyle, Cloneable {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.styling");
    private java.util.List<Rule> ruleList;
    private String featureTypeName = "Feature";
    private String name = "name";
    private String title = "title";
    private String abstractStr = "abstract";
    private String[] semanticTypeIdentifiers;

    /**
     * Creates a new instance of FeatureTypeStyleImpl
     *
     * @param rules DOCUMENT ME!
     */
    protected FeatureTypeStyleImpl(Rule[] rules) {
        this(Arrays.asList(rules));
    }

    protected FeatureTypeStyleImpl(List<Rule> rules) {
        ruleList = new ArrayList<Rule>();
        ruleList.addAll(rules);
    }

    /**
     * Creates a new instance of FeatureTypeStyleImpl
     */
    protected FeatureTypeStyleImpl() {
        ruleList = new ArrayList<Rule>();
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public Rule[] getRules() {
        return (Rule[]) ruleList.toArray(new Rule[0]);
    }

    public List<Rule> rules() {
        return ruleList;
    }

    public String[] getSemanticTypeIdentifiers() {
    	if (semanticTypeIdentifiers == null) {
    		return new String[] { "generic:geometry" }; //HACK: - generic catch all identifier	
    	} else {
    		return semanticTypeIdentifiers;
    	}
    }

    public void setSemanticTypeIdentifiers(String[] types) {
    	semanticTypeIdentifiers = types;
    }

    public void setRules(Rule[] rules) {
        ruleList.clear();
        ruleList.addAll(Arrays.asList(rules));

        // fireChanged();
    }

    public void addRule(Rule rule) {
        ruleList.add(rule);

        // fireChildAdded(rule);
    }

    public void setFeatureTypeName(String name) {
        if (name.equals("feature")) {
            LOGGER.warning(
                "FeatureTypeStyle with typename 'feature' - you probably meant to say 'Feature' (capital F) for the 'generic' FeatureType");
        }

        featureTypeName = name;
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

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone of the FeatureTypeStyle.
     *
     * @see org.geotools.styling.FeatureTypeStyle#clone()
     */
    public Object clone() {
        FeatureTypeStyle clone;

        try {
            clone = (FeatureTypeStyle) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // this should never happen.
        }

        Rule[] ruleArray = new Rule[ruleList.size()];

        for (int i = 0; i < ruleArray.length; i++) {
            Rule rule = (Rule) ruleList.get(i);
            ruleArray[i] = (Rule) ((Cloneable) rule).clone();
        }

        clone.setRules(ruleArray);

        return clone;
    }

    /**
     * Overrides hashCode.
     *
     * @return The hashcode.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (ruleList != null) {
            result = (PRIME * result) + ruleList.hashCode();
        }

        if (featureTypeName != null) {
            result = (PRIME * result) + featureTypeName.hashCode();
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

        return result;
    }

    /**
     * Compares this FeatureTypeStyleImpl with another.
     * 
     * <p>
     * Two FeatureTypeStyles are equal if they contain equal properties and an
     * equal list of Rules.
     * </p>
     *
     * @param oth The other FeatureTypeStyleImpl to compare with.
     *
     * @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof FeatureTypeStyleImpl) {
            FeatureTypeStyleImpl other = (FeatureTypeStyleImpl) oth;

            return Utilities.equals(name, other.name)
            && Utilities.equals(title, other.title)
            && Utilities.equals(abstractStr, other.abstractStr)
            && Utilities.equals(featureTypeName, other.featureTypeName)
            && Utilities.equals(ruleList, other.ruleList);
        }

        return false;
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append( "FeatureTypeStyleImpl");
        buf.append( "[");
    	if( name != null ) {
    		buf.append(" name=");
    		buf.append( name );
    	}
    	else {
    		buf.append( " UNNAMED");
    	}
    	buf.append( ", ");
    	buf.append( featureTypeName );
    	buf.append( ", rules=<");
    	buf.append( ruleList.size() );
    	buf.append( ">" );
    	if( ruleList.size() > 0 ){
    		buf.append( "(" );
    		buf.append( ruleList.get(0));
    		if( ruleList.size() > 1 ){
    			buf.append(",...");
    		}
    		buf.append( ")");
    	}    	
    	buf.append("]");
    	return buf.toString();
    }
}
