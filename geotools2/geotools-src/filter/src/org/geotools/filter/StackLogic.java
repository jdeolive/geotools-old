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

import java.util.*;

/**
 * Defines a like filter, which checks to see if an attribute matches a REGEXP.
 *
 * @version $Id: StackLogic.java,v 1.3 2002/06/05 14:06:17 loxnard Exp $
 * @author Rob Hranac, Vision for New York
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

        Filter tempFilter = null;

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
