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

// Geotools dependencies
import org.geotools.util.Cloneable;
import org.geotools.util.EqualsUtils;


/**
 * @version $Id: FeatureTypeStyleImpl.java,v 1.14 2003/09/06 04:52:31 seangeo Exp $
 * @author James Macgill
 */
public class FeatureTypeStyleImpl implements FeatureTypeStyle, Cloneable {
    private java.util.List ruleList = new java.util.ArrayList();
    private String featureTypeName = "feature";
    private String name = "name";
    private String title = "title";
    private String abstractStr = "abstract";

    /** Creates a new instance of FeatureTypeStyleImpl */
    protected FeatureTypeStyleImpl() {
    }

    /** Creates a new instance of FeatureTypeStyleImpl */
    protected FeatureTypeStyleImpl(Rule[] rules) {
        setRules(rules);
    }

    public String getFeatureTypeName() {
        return featureTypeName;
    }

    public Rule[] getRules() {
        return (Rule[]) ruleList.toArray(new Rule[0]);
    }

    public String[] getSemantecTypeIdentifiers() {
        return new String[] { "generic:geometry" }; //HACK: - generic catch all identifier
    }

    public void setSemantecTypeIdentifiers(String[] types) {
        // since these are defined yet we can ignore it
    }

    public void setRules(Rule[] rules) {
        ruleList.clear();

        for (int i = 0; i < rules.length; i++) {
            addRule(rules[i]);
        }
    }

    public void addRule(Rule rule) {
        ruleList.add(rule);
    }

    public void setFeatureTypeName(String name) {
        featureTypeName = name;
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

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Creates a deep copy clone of the FeatureTypeStyle.
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
            ruleArray[i] = (Rule) ((Cloneable)rule).clone();
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
            result = PRIME * result + ruleList.hashCode();
        }
        if (featureTypeName != null) {
            result = PRIME * result + featureTypeName.hashCode();
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

        return result;
    }

    /** Compares this FeatureTypeStyleImpl with another.
     * 
     *  <p>Two FeatureTypeStyles are equal if they contain
     *  equal properties and an equal list of Rules.
     * 
     *  @param oth The other FeatureTypeStyleImpl to compare with.
     *  @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof FeatureTypeStyleImpl) {
            FeatureTypeStyleImpl other = (FeatureTypeStyleImpl) oth;
            return EqualsUtils.equals(name, other.name) &&
                EqualsUtils.equals(title, other.title) &&
                EqualsUtils.equals(abstractStr, other.abstractStr) &&
                EqualsUtils.equals(featureTypeName, other.featureTypeName) &&
                EqualsUtils.equals(ruleList, other.ruleList);
        }
        
        return false;
    }

}