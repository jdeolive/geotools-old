/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import java.util.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @author Rob Hranac, Vision for New York
 * @version 
 */
public class StackLogic extends Stack {


    /** The (limited) REGEXP pattern. */
    private short logicType;


    /**
     * Constructor which flags the operator as between.
     */
    public StackLogic () {
        super();
    }


    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public void addLogic(short logicType)
        throws IllegalFilterException {
        
        if( !FilterDefault.isLogicFilter(logicType) ) {
            throw new IllegalFilterException
                ("Add logic filter type does not match declared type.");
        }
        else {
            this.push(new Stack());
            this.logicType = logicType;
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param logicType The limited REGEXP pattern for this string. 
     */
    public void simplifyLogic(short logicType) 
        throws IllegalFilterException {

        Filter tempFilter;

        if( this.logicType != logicType ) {
            throw new IllegalFilterException
                ("End logic filter type does not match declared type.");
        } 
        try {

            Stack tempStack = (Stack) this.pop();

            if( logicType == FilterDefault.LOGIC_NOT ) {
                tempFilter = (Filter) tempStack.pop();
                tempFilter = tempFilter.not();
                this.push( tempFilter );
            }
            else if ( logicType == FilterDefault.LOGIC_OR ){
                while( !tempStack.empty() ) {
                    tempFilter = tempFilter.or((Filter) tempStack.pop());
                    this.push( tempFilter );
                }
            }
            else if( logicType == FilterDefault.LOGIC_AND ) {
                while( !tempStack.empty() ) {
                    tempFilter = tempFilter.and((Filter) tempStack.pop());
                    this.push( tempFilter );
                }
            }
            else {
                throw new IllegalFilterException
                    ("Filter type is not logic.");
            }


        } 
        catch(EmptyStackException e) {
            throw new IllegalFilterException
                ("Attempted to construct logic filter w/o sub filters.");
        } 
        catch(Exception e) {
            throw new IllegalFilterException
                ("Logic stack returned non-Stack; Stack was expected.");
        }
    }

    /**
     * Sets the matching pattern for this FilterLike.
     *
     * @param pattern The limited REGEXP pattern for this string. 
     */
    public boolean isComplete() {

        try {
            if( this.peek() instanceof Filter ) {
                Filter tempFilter = (Filter) this.pop();
                if( this.empty() ) {
                    this.push( tempFilter );
                    return true;
                }
                else {
                    this.push( tempFilter );
                    return false;
                }
            } 
            else {
                return false;
            }
        }
        catch(Exception e) {
            return false;
        }

    }
    
}
