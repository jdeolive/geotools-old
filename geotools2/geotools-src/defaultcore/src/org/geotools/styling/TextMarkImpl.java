/*
 * TextMark.java
 *
 * Created on 01 August 2002, 10:28
 */
package org.geotools.styling;

import org.geotools.filter.Expression;


/**
 *
 * @author  iant
 */
public class TextMarkImpl extends MarkImpl implements TextMark {
    /**
     * The logger for the default core module.
     */

    //private static final Logger LOGGER = Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private Expression wellKnownName = null;
    private java.util.List fonts = new java.util.ArrayList();
    private Expression symbol;

    /** Creates a new instance of TextMark */
    public TextMarkImpl(Font font, String symbol) {
        super();
        addFont(font);
        setSymbol(symbol);
        wellKnownName = filterFactory.createLiteralExpression("Symbol");
    }

    public TextMarkImpl(Font font, Expression symbol) {
        super();
        addFont(font);
        setSymbol(symbol);
        wellKnownName = filterFactory.createLiteralExpression("Symbol");
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
        this.symbol = filterFactory.createLiteralExpression(symbol);
    }

    public void setSymbol(Expression symbol) {
        this.symbol = symbol;
    }

    /** Setter for property wellKnownName.
     * @param wellKnownName New value of property wellKnownName.
     */
    public void setWellKnownName(Expression wellKnownName) {
        // this is really blank the name is always symbol
    }
}