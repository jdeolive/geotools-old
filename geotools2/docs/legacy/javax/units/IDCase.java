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
 * Provides support for enumerating character-set categories.  The allowed
 * character-set categories are:  Unicode, mixed-case ASCII, and upper-case-only
 * ASCII.
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public final class IDCase
{
    /**
     * General Unicode indicator.
     */
    public static final IDCase	UNICODE = new IDCase();

    /**
     * Mixed-case ASCII indicator.
     */
    public static final IDCase	MIXED_CASE_ASCII = new IDCase();

    /**
     * Upper-case ASCII indicator.
     */
    public static final IDCase	UPPER_CASE_ASCII = new IDCase();

    private IDCase()
    {}
}
