package org.geotools.styling;

import org.geotools.filter.Expression;



public interface TextMark extends Mark{
    
    public Expression getSymbol();
    
    public void setSymbol(String symbol);

    public Font[] getFonts();

    public void setWellKnownName(Expression wellKnownName);

    public Expression getWellKnownName();

    public void addFont(Font font);

    public void setSymbol(Expression symbol);

}
