/*
 * TextMark.java
 *
 * Created on 01 August 2002, 10:28
 */

package org.geotools.styling;

/**
 *
 * @author  iant
 */
import java.util.ArrayList;
import org.geotools.filter.*;

public class TextMark extends DefaultMark implements Mark, Symbol {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger("Styling");
    Expression wellKnownName = null;
    ArrayList fonts = new ArrayList();
    Expression symbol;
    /** Creates a new instance of TextMark */
    public TextMark(Font font, String symbol) {
        addFont(font);
        setSymbol(symbol);
        try{
            wellKnownName = new ExpressionLiteral("Symbol");
        } catch (IllegalFilterException ife){
            _log.error("Unable to build TextMark " + ife);
        }
    }
    
    public TextMark(Font font, Expression symbol) {
        addFont(font);
        setSymbol(symbol);
        try{
            wellKnownName = new ExpressionLiteral("Symbol");
        } catch (IllegalFilterException ife){
            _log.error("Unable to build TextMark " + ife);
        }
    }
    
    /** This parameter gives the well-known name of the symbol of the mark.<br>
     * 
     * @return The well-known name of this symbol
     */
    public Expression getWellKnownName() {
        return wellKnownName;
    }
    
    /** Getter for property font.
     * @return Value of property font.
     */
    public org.geotools.styling.Font[] getFonts() {
        return (Font[]) fonts.toArray(new Font[0]);
    }
    
    /** Setter for property font.
     * @param font New value of property font.
     */
    public void addFont(org.geotools.styling.Font font) {
        this.fonts.add(font);
    }
    
    /** Getter for property symbol.
     * @return Value of property symbol.
     */
    public Expression getSymbol() {
        return symbol;
    }
    
    /** Setter for property symbol.
     * @param symbol New value of property symbol.
     */
    public void setSymbol(java.lang.String symbol) {
        try{
            this.symbol = new ExpressionLiteral(symbol);
        } catch (IllegalFilterException ife){
            _log.error("failed to build expression from string " + symbol, ife);
        }
    }
    
    public void setSymbol(Expression symbol){
        this.symbol = symbol;
    }
    /** Setter for property wellKnownName.
     * @param wellKnownName New value of property wellKnownName.
     */
    public void setWellKnownName(org.geotools.filter.Expression wellKnownName) {
        // this is really blank the name is always symbol
    }
    
}
