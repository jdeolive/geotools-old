/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

import java.io.Serializable;

/**
 * Provides support for enumerating the form of a word.  Allowed
 * forms are: symbol, single, and plural.
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public final class IDForm
{
    /**
     * Single-name form indicator.
     */
    public static final IDForm	SINGLE= new IDForm();

    /**
     * Plural-name form indicator.
     */
    public static final IDForm	PLURAL = new IDForm();

    /**
     * Symbol form indicator.
     */
    public static final IDForm	SYMBOL = new IDForm();

    private IDForm()
    {}

    /**
     * Indicates if this instance is semantically identical to an object.
     *
     * @param obj		The object to be compared against.
     * @return			True if and only if this instance is 
     *				semantically identical to the object.
     */
    public boolean equals(Object obj)
    {
	return this == obj;
    }
}
