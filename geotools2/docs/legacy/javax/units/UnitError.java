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
 * Provides support for runtime exceptions of the javax.units package.  Runtime
 * exceptions do not have to be declared.
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class UnitError
    extends	RuntimeException
{
    /**
     * Constructs from a message.
     *
     * @param message		The message.
     */
    public UnitError(String message)
    {
	super(message);
    }
}
