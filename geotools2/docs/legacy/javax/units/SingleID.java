/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

/**
 * Provides support for a single unit identifier.  In general, identifiers are
 * in Unicode and may contain multiple characters and blanks. Provision is made
 * for returning unit identifiers in multiple cases in order to support systems
 * with character set capabilities ranging from limited to extensive.</P>
 *
 * <P>This class is the "single" component of the composite design pattern for
 * unit identifiers.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public abstract class SingleID
    extends	UnitID
{
    private final Identifier	id;	// won't be null

    /**
     * Constructs from a single identifier.  The Unicode form will be the
     * given identifier.  The mixed-case ASCII form will be the given
     * identifier if it contains only ASCII characters; otherwise, it will
     * be <code>null</code>.  The upper-case ASCII form will be the given
     * identifier if it contains only upper-case ASCII characters; otherwise; it
     * will be <code>null</code>.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param id		The identifier.  May not be <code>null</code>.
     * @throws NullIDError	The argument is <code>null</code>.
     */
    protected SingleID(String id)
	throws NullIDError
    {
	this.id = Identifier.newIdentifier(id);
    }

    /**
     * Constructs from Unicode, mixed-case ASCII, and upper-case forms of a
     * identifier.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param unicode		The Unicode form of the identifier.  May not be
     *				<code>null</code>.
     * @param ascii		The mixed-case ASCII form of the identifier.  If
     *				<code>null</code> and the Unicode form contains
     *				only ASCII characters, then the mixed-case ASCII
     *				form will be the same as the Unicode form.
     * @param upper		The upper-case ASCII form of the ID.  If
     *				<code>null</code> and the mixed-case ASCII form
     *				contains only upper-case ASCII characters, then
     *				the upper-case form will be the same as the
     *				ASCII form.
     * @throws NullIDError	The Unicode argument is <code>null</code>.
     * @throws CaseException	The mixed-case or upper-case ASCII form contains
     *				an invalid character.
     */
    protected SingleID(String unicode, String ascii, String upper)
	throws NullIDError, CaseException
    {
	this.id = Identifier.newIdentifier(unicode, ascii, upper);
    }

    /**
     * Returns the unit ID corresponding to a given character-set.
     *
     * @param whatCase		The character-set of the ID.  One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit ID corresponding to the given
     *				character-set. May be <code>null</code>.
     * @see SingleID#getID(IDCase)
     */
    public String getID(IDCase whatCase)
    {
	return id.getID(whatCase);
    }

    /**
     * Returns the unit ID in general Unicode.
     *
     * @return			The ID for the unit in general Unicode.
     *				Won't be <code>null</code>.
     * @see Identifier#getUnicode()
     */
    public String getUnicode()
    {
	return id.getUnicode();
    }

    /**
     * Returns the unit ID in mixed-case ASCII.
     *
     * @return			The ID for the unit in mixed-case ASCII.
     *				Won't be <code>null</code>.
     * @see Identifier#getMixedCase()
     */
    public String getMixedCase()
    {
	return id.getMixedCase();
    }

    /**
     * Returns the unit ID in upper-case ASCII.
     *
     * @return			The ID for the unit in upper-case ASCII.
     *				May be <code>null</code>.
     * @see Identifier#getUpperCase()
     */
    public String getUpperCase()
    {
	return id.getUpperCase();
    }

    /**
     * Indicates if this instance is semantically identical to an object.
     *
     * @param obj		The object to be compared with this instance.
     * @return			True if and only if this instance is 
     *				semantically identical to the object.
     */
    public boolean equals(Object obj)
    {
	boolean equals;
	if (!(obj instanceof SingleID))
	{
	    equals = false;
	}
	else
	{
	    SingleID	that = (SingleID)obj;
	    equals = this == that || id.equals(that.id);
	}
	return equals;
    }

    /**
     * Returns the hash code of this instance.	If <code>that</code> is another
     * instance of this class and <code>equals(that)</code> is true, then
     * <code>hashCode() == that.hashCode()</code> is true.
     *
     * @return			The hash code of this instance.
     */
    public int hashCode()
    {
	return id.hashCode();
    }
}
