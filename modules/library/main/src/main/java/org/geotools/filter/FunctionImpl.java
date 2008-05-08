package org.geotools.filter;

import java.util.List;

import org.geotools.filter.expression.ExpressionAbstract;
import org.opengis.filter.expression.Function;

/**
 * 
 * @author Cory Horner, Refractions Research
 *
 */
public class FunctionImpl extends ExpressionAbstract implements Function {

    /** function name **/
    String name;

    /** function params **/
    List params;
    
    /**
     * Gets the name of this function.
     *
     * @return the name of the function.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the function.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the function parameters.
     */
    public List getParameters() {
        return params;
    }
    
    /**
     * Sets the function parameters.
     */
    public void setParameters(List params) {
        this.params = params;
    }

}
