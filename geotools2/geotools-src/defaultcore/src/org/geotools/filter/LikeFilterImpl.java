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

package org.geotools.filter;

// J2SE dependencies
import java.util.regex.*;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: LikeFilterImpl.java,v 1.4 2002/10/25 11:37:43 ianturton Exp $
 * @author Rob Hranac, Vision for New York
 */
public class LikeFilterImpl extends AbstractFilterImpl implements LikeFilter {

    /** 
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");

    /** The attribute value, which must be an attribute expression. */
    protected Expression attribute = null;

    /** The (limited) REGEXP pattern. */
    protected String pattern = null;

    /** The single wildcard for the REGEXP pattern. */
    private String wildcardSingle = ".?";
    /** The escaped version of the single wildcard for the REGEXP pattern. */
    private String escapedWildcardSingle = "\\.\\?";
    /** The multiple wildcard for the REGEXP pattern. */
    private String wildcardMulti = ".*";
    /** The escaped version of the multiple wildcard for the REGEXP pattern. */
    private String escapedWildcardMulti = "\\.\\*";
    /** The escape sequence for the REGEXP pattern. */
    private String escape = "\\";
    /** the pattern compiled into a java regex */
    private java.util.regex.Pattern compPattern = null;
    private Matcher matcher = null;
    /**
     * Constructor which flags the operator as like.
     */
    protected LikeFilterImpl() {
        filterType = LIKE;
    }


    /**
     * Sets the expression to be evalutated as being like the pattern
     *
     * @param attribute The value of the attribute for comparison. 
     * @throws IllegalFilterException Filter is illegal.
     */
    public void setValue(Expression attribute)
        throws IllegalFilterException {
        
        if( (attribute.getType() != DefaultExpression.ATTRIBUTE_STRING) ||
            permissiveConstruction ) {
            this.attribute = attribute;
        }
        else {
            throw new IllegalFilterException("Attempted to add something other than a string attribute expression to a like filter.");
        }
    }
    /**
     * gets the Value (left hand side) of this filter
     * @return the expression that is the value of the filter
     */
    public Expression getValue(){
        return attribute;
    }
    
    /** Sets the match pattern for this FilterLike.
     *
     * @param wildcardMulti the string that represents a mulitple character (1->n) wildcard
     * @param wildcardSingle the string that represents a single character (1) wildcard
     * @param escape the string that represents an escape character
     * @param pattern the expression which evaluates to the match pattern for this filter
     */
    public void setPattern(Expression p, String wildcardMulti, String wildcardSingle, String escape) {
        String pattern = p.toString();
        setPattern(pattern,wildcardMulti,wildcardSingle,escape);
    }

    /** Sets the match pattern for this FilterLike.
     *
     * @param wildcardMulti the string that represents a mulitple character (1->n) wildcard
     * @param wildcardSingle the string that represents a single character (1) wildcard
     * @param escape the string that represents an escape character
     * @param pattern the string which contains the match pattern for this filter
     */
    public void setPattern(String pattern, String wildcardMulti, String wildcardSingle, String escape) {

        // The following things happen for both wildcards:
        //  (1) If a user-defined wildcard exists, replace with Java wildcard
        //  (2) If a user-defined escape exists, Java wildcard + user-escape
        //  Then, test for matching pattern and return result.
        
        char esc = escape.charAt(0);
        LOGGER.finer("wildcard "+wildcardMulti+" single "+wildcardSingle);
        LOGGER.finer("escape "+escape+" esc "+esc+" esc == \\ "+(esc == '\\'));
        
        escapedWildcardMulti = fixSpecials(wildcardMulti);
        escapedWildcardSingle = fixSpecials(wildcardSingle);
        
        
        LOGGER.finer("after fixing: wildcard "+wildcardMulti+" single "+wildcardSingle+" escape "+escape);
        LOGGER.finer("start pattern = "+pattern);
        
        // escape any special chars which are not our wildcards
        StringBuffer tmp = new StringBuffer("");
        
        boolean escapedMode = false;
        for(int i=0;i<pattern.length();i++){
            char c = pattern.charAt(i);
            LOGGER.finer("tmp = "+tmp+" looking at "+c);
            
            if(pattern.regionMatches(false, i, escape, 0, escape.length())) { // skip the escape string
                LOGGER.finer("escape ");
                escapedMode = true;
                
                i+=escape.length();
                c=pattern.charAt(i);
            }
            if(pattern.regionMatches(false, i, wildcardMulti, 0, wildcardMulti.length())){ // replace with java wildcard
                LOGGER.finer("multi wildcard");
                if(escapedMode){
                    LOGGER.finer("escaped ");
                    tmp.append(escapedWildcardMulti);
                }else{
                    tmp.append(this.wildcardMulti);
                }
                i+=wildcardMulti.length()-1;
                escapedMode = false;
                continue;
            }
            if(pattern.regionMatches(false, i, wildcardSingle, 0, wildcardSingle.length())){ // replace with java single wild card
                LOGGER.finer("single wildcard");
                if(escapedMode){
                    LOGGER.finer("escaped ");
                    tmp.append(escapedWildcardSingle);
                }else{
                    tmp.append(this.wildcardSingle);
                }
                i+=wildcardSingle.length()-1;
                escapedMode = false;
                continue;
            }
            if(isSpecial(c)){
                LOGGER.finer("special");
                tmp.append(this.escape+c);
                escapedMode = false;
                continue;
            }
            tmp.append(c);
            escapedMode = false;
        }
        
        this.pattern = tmp.toString();
        LOGGER.finer("final pattern "+this.pattern);
        compPattern = java.util.regex.Pattern.compile(this.pattern);
        matcher = compPattern.matcher("");
    }
    
    public String getPattern(){
        return this.pattern;
    }

    /**
     * Determines whether or not a given feature matches this pattern.
     *
     * @param feature Specified feature to examine.
     * @return Flag confirming whether or not this feature is inside the filter.
     * @throws MalformedFilterException Filter is not internally consistent.
     */
    public boolean contains(Feature feature) {

        // Checks to ensure that the attribute has been set
        if( attribute == null ) {
            return false;
        }

        else {

            // Note that this converts the attribute (whatever it is) to a string
            //  for comparison.  Unlike the math or geometry filters, which
            //  require specific types to function correctly, this filter
            //  using the mandatory string representation in Java
            // Of course, this does not guarantee a meaningful result, but it
            //  does guarantee a valid result.

            //LOGGER.finest("pattern: " + pattern);
            //LOGGER.finest("string: " + attribute.getValue(feature).toString());
            //return attribute.getValue(feature).toString().matches(pattern);
            
            matcher.reset(attribute.getValue(feature).toString());
            return matcher.matches();
        }
    }


    public String toString() {
        return "[ " + attribute.toString() + " is like " + pattern + " ]";        
    }
    

    /** Getter for property escape.
     * @return Value of property escape.
     */
    public java.lang.String getEscape() {
        return escape;
    }
    
    
    /** Getter for property wildcardMulti.
     * @return Value of property wildcardMulti.
     */
    public java.lang.String getWildcardMulti() {
        return wildcardMulti;
    }
    
    
    /** Getter for property wildcardSingle.
     * @return Value of property wildcardSingle.
     */
    public java.lang.String getWildcardSingle() {
        return wildcardSingle;
    }
    
    /** convienience method to determine if a character is special to the regex 
     * system.
     * @param c the character to test
     * @return is the character a special character.
     */
    private boolean isSpecial(final char c){
        if(c == '.' || c == '?' || c == '*' || c == '^' || c == '$' ||
            c == '+' || c == '[' || c == ']' || c == '(' ||
            c == ')' || c == '|' || c == '\\' || c== '&' ){
            return true;
        }else{
            return false;
        }
    }
    
    /** convienience method to escape any character that is special to the regex 
     * system.
     * @param in the string to fix
     * @return the fixed string
     */
    private String fixSpecials(final String in){
        StringBuffer tmp = new StringBuffer("");
        for(int i = 0;i<in.length();i++){
            char c = in.charAt(i);
            if(isSpecial(c)){
                tmp.append(this.escape+c);
            }else{
                tmp.append(c);
            }
        }
        return tmp.toString();
    }

      
    /** 
     * Compares this filter to the specified object.  Returns true 
     * if the passed in object is the same as this filter.  Checks 
     * to make sure the filter types, the value, and the pattern are
     * the same.
     &
     * @param obj - the object to compare this LikeFilter against.
     * @return true if specified object is equal to this filter; false otherwise.
     */           
     public boolean equals(Object obj) {
	if (obj.getClass() == this.getClass()){
	    LikeFilterImpl lFilter = (LikeFilterImpl)obj;
	    return (lFilter.getFilterType() == this.filterType &&
		    lFilter.getValue().equals(this.attribute) &&
		    lFilter.getPattern().equals(this.pattern));
	} else {
	    return false;
	}
    }
       /** Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing which needs
     * infomration from filter structure.
     *
     * Implementations should always call: visitor.visit(this);
     *
     * It is importatant that this is not left to a parent class unless the parents
     * API is identical.
     *
     * @param visitor The visitor which requires access to this filter,
     *                the method must call visitor.visit(this);
     *
     */
    public void accept(FilterVisitor visitor) { 
        visitor.visit(this);
    }
}
