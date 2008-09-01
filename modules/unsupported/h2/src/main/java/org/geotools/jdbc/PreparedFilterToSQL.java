package org.geotools.jdbc;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.jdbc.FilterToSQL;
import org.opengis.filter.expression.Literal;

/**
 * Extension of FilterToSQL intended for use with prepared statements.
 * <p>
 * Each time a {@link Literal} is visited, a '?' is encoded, and the 
 * value and type of the literal are stored, available after the fact 
 * via {@link #getLiteralValues()} and {@link #getLiteralTypes()}. 
 * 
 * </p>
 * @author Justin Deoliveira, OpenGEO
 *
 */
public class PreparedFilterToSQL extends FilterToSQL {

    /**
     * ordered list of literal values encountered and their types
     */
    List<Object> literalValues = new ArrayList();
    List<Class> literalTypes = new ArrayList();
    boolean prepareEnabled = true;
    
    public PreparedFilterToSQL() {
        super();
    }

    /**
     * If true (default) a sql statement with literal placemarks is created, otherwise
     * a normal statement is created
     * @return
     */
    public boolean isPrepareEnabled() {
        return prepareEnabled;
    }

    public void setPrepareEnabled(boolean prepareEnabled) {
        this.prepareEnabled = prepareEnabled;
    }

    public PreparedFilterToSQL(Writer out) {
        super(out);
    }

    public Object visit(Literal expression, Object context)
            throws RuntimeException {
        if(!prepareEnabled)
            return super.visit(expression, context);
        
        //evaluate the literal and store it for later
        Object literalValue = evaluateLiteral( expression, (Class) context );
        literalValues.add(literalValue);
        if(literalValue != null)
            literalTypes.add(literalValue.getClass());
        else if(context instanceof Class)
            literalTypes.add((Class) context);
        
        try {
            out.write( "?" );
        } 
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        
        return context;
    }
    
    public List<Object> getLiteralValues() {
        return literalValues;
    }
    
    public List<Class> getLiteralTypes() {
        return literalTypes;
    }
}
