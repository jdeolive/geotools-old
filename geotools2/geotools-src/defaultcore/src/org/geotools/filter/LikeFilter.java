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

import org.apache.log4j.Category; 
import org.geotools.data.*;
import org.geotools.feature.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: LikeFilter.java,v 1.10 2002/07/24 12:02:44 ianturton Exp $
 * @author Rob Hranac, Vision for New York
 */
public class LikeFilter extends AbstractFilter {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(LikeFilter.class.getName());

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


    /**
     * Constructor which flags the operator as like.
     */
    public LikeFilter () {
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
        
        if( (attribute.getType() != ExpressionDefault.ATTRIBUTE_STRING) ||
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
        _log.debug("wildcard "+wildcardMulti+" single "+wildcardSingle);
        _log.debug("escape "+escape+" esc "+esc+" esc == \\ "+(esc == '\\'));
        

        _log.debug("after fixing: wildcard "+wildcardMulti+" single "+wildcardSingle+" escape "+escape);
        _log.debug("start pattern = "+pattern);
        
        // escape any special chars which are not our wildcards
        StringBuffer tmp = new StringBuffer("");
        
        boolean escapedMode = false;
        for(int i=0;i<pattern.length();i++){
            char c = pattern.charAt(i);
            _log.debug("tmp = "+tmp+" looking at "+c);
            
            if(pattern.regionMatches(false, i, escape, 0, escape.length())) { // skip the escape string
                _log.debug("escape ");
                escapedMode = true;
                
                i+=escape.length();
                c=pattern.charAt(i);
            }
            if(pattern.regionMatches(false, i, wildcardMulti, 0, wildcardMulti.length())){ // replace with java wildcard
                _log.debug("multi wildcard");
                if(escapedMode){
                    _log.debug("escaped ");
                    tmp.append(this.escapedWildcardMulti);
                }else{
                    tmp.append(this.wildcardMulti);
                }
                i+=wildcardMulti.length()-1;
                escapedMode = false;
                continue;
            }
            if(pattern.regionMatches(false, i, wildcardSingle, 0, wildcardSingle.length())){ // replace with java single wild card
                _log.debug("single wildcard");
                if(escapedMode){
                    _log.debug("escaped ");
                    tmp.append(this.escapedWildcardSingle);
                }else{
                    tmp.append(this.wildcardSingle);
                }
                i+=wildcardSingle.length()-1;
                escapedMode = false;
                continue;
            }
            if(isSpecial(c)){
                _log.debug("special");
                tmp.append(this.escape+c);
                escapedMode = false;
                continue;
            }
            tmp.append(c);
            escapedMode = false;
        }
        
        this.pattern = tmp.toString();
        _log.debug("final pattern "+this.pattern);
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

            //_log.info("pattern: " + pattern);
            //_log.info("string: " + attribute.getValue(feature).toString());
            return attribute.getValue(feature).toString().matches(pattern);
        }
    }


    public String toString() {
        return "[ " + attribute.toString() + " is like " + pattern + " ]";        
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
                
}
