/*
 * DefaultFont.java
 *
 * Created on 03 July 2002, 12:51
 */

package org.geotools.styling;

import org.geotools.filter.Expression;

/**
 *
 * @author  iant
 */
public class DefaultFont implements Font {
        private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(DefaultFont.class);
    private Expression fontFamily = null;
    private Expression fontSize = null;
    private Expression fontStyle = null;
    private Expression fontWeight = null;
    /** Creates a new instance of DefaultFont */
    public DefaultFont() {
        try{
            fontSize = new org.geotools.filter.ExpressionLiteral(new Integer(10));
            fontStyle = new org.geotools.filter.ExpressionLiteral("normal");
            fontWeight = new org.geotools.filter.ExpressionLiteral("normal");
            fontFamily = new org.geotools.filter.ExpressionLiteral("Courier");
        } catch (org.geotools.filter.IllegalFilterException ife){
            _log.fatal("Failed to build defaultFont: "+ife);
            System.err.println("Failed to build defaultFont: "+ife);
        }
    }
 
    /** Getter for property fontFamily.
     * @return Value of property fontFamily.
     */
    public Expression getFontFamily() {
        return fontFamily;
    }    
    
    /** Setter for property fontFamily.
     * @param fontFamily New value of property fontFamily.
     */
    public void setFontFamily(Expression fontFamily) {
        this.fontFamily = fontFamily;
    }
    
    /** Getter for property fontSize.
     * @return Value of property fontSize.
     */
    public Expression getFontSize() {
        return fontSize;
    }
    
    /** Setter for property fontSize.
     * @param fontSize New value of property fontSize.
     */
    public void setFontSize(Expression fontSize) {
        this.fontSize = fontSize;
    }
    
    /** Getter for property fontStyle.
     * @return Value of property fontStyle.
     */
    public Expression getFontStyle() {
        return fontStyle;
    }
    
    /** Setter for property fontStyle.
     * @param fontStyle New value of property fontStyle.
     */
    public void setFontStyle(Expression fontStyle) {
        this.fontStyle = fontStyle;
    }
    
    /** Getter for property fontWeight.
     * @return Value of property fontWeight.
     */
    public Expression getFontWeight() {
        return fontWeight;
    }
    
    /** Setter for property fontWeight.
     * @param fontWeight New value of property fontWeight.
     */
    public void setFontWeight(Expression fontWeight) {
        this.fontWeight = fontWeight;
    }
    
}
