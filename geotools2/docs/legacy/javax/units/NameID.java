/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

/**
 * Provides support for a unit name.  In general, a unit name is in Unicode
 * and may contain multiple characters or embedded blanks.  Provision is made
 * for returning a unit name in multiple cases in order to support systems
 * with character set capabilities ranging from limited to extensive.  A Unit
 * name always has general Unicode, mixed-case ASCII, and upper-case ASCII
 * forms.  The upper-case ASCII form of a unit name is the mixed-case ASCII form
 * converted to upper-case.</P>
 *
 * <P>This class is the abstract superclass of the concrete classes {@link 
 * SingleName} and {@link PluralName}.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public abstract class NameID
    extends	SingleID
{
    /**
     * Constructs from Unicode and mixed-case ASCII forms of the unit name.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param unicode		The unit name in Unicode.  May not be
     *				<code>null</code>.
     * @param ascii		The unit name in mixed-case ASCII.  May not be
     *				<code>null</code>.
     * @throws NullIDError	One of the arguments is <code>null</code>.
     * @throws CaseException	The mixed-case ASCII name contains a non-ASCII
     *				character.
     */
    protected NameID(String unicode, String ascii)
	throws NullIDError, CaseException
    {
	super(
	    unicode,
	    ascii,
	    Identifier.isUpper(ascii) ? ascii : ascii.toUpperCase());
    }

    /**
     * Returns the default unit identifier corresponding to a given
     * character-set.  This method is the same as {@link #getName(IDCase)}.
     *
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The default unit identifier corresponding to
     *				the character-set.  May contain blanks
     *				but won't be <code>null</code>.
     * @see #getName(IDCase)
     */
    public String getDefaultID(IDCase whatCase)
    {
	return getName(whatCase);
    }

    /**
     * Returns the unit name corresponding to a given character-set.
     *
     * @param whatCase		The character-set of the name.	One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit name corresponding to the given
     *				character-set. May be <code>null</code>.
     */
    public String getName(IDCase whatCase)
    {
	return getID(whatCase);
    }
}
