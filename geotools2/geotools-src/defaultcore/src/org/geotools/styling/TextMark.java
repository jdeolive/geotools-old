/*
 * TextMark.java
 *
 * Created on 01 August 2002, 10:28
 */

package org.geotools.styling;

// J2SE dependencies
import java.util.ArrayList;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.filter.*;

/**
 *
 * @author  iant
 */
public class TextMark extends DefaultMark implements Mark, Symbol {
    
    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    
    Expression wellKnownName = null;
    ArrayList fonts = new ArrayList();
    Expression symbol;
    /** Creates a new instance of TextMark */
    public TextMark(Font font, String symbol) {
        addFont(font);
        setSymbol(symbol);
        try {
            wellKnownName = new ExpressionLiteral("Symbol");
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build default fill: " + ife);
        }
    }
    
    public TextMark(Font font, Expression symbol) {
        addFont(font);
        setSymbol(symbol);
        try {
            wellKnownName = new ExpressionLiteral("Symbol");
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build default fill: " + ife);
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
        try {
            this.symbol = new ExpressionLiteral(symbol);
        } catch (org.geotools.filter.IllegalFilterException ife){
            LOGGER.severe("Failed to build default fill: " + ife);
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
