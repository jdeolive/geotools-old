/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

/**
 * Provides support for unit names.  A unit name comprises both singular and
 * plural forms. In general, names are in Unicode and may contain multiple
 * characters or embedded spaces. Provision is made for returning unit names in
 * multiple cases in order to support systems with character set capabilities
 * ranging from limited to extensive.  Unit names always have general Unicode,
 * mixed-case ASCII, and upper-case ASCII forms.  The upper-case ASCII form of a
 * unit name is the mixed-case ASCII form converted to upper-case.</P>
 *
 * <P>This class is a "single" component of the composite design pattern for
 * unit identifiers but is the "composite" component of the same design pattern
 * for unit names.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class UnitName
    extends	UnitID
{
    /**
     * The singular form of the unit name.
     */
    private final SingleName	single;		// won't be null

    /**
     * The plural form of the unit name.
     */
    private final PluralName	plural;		// won't be null

    /**
     * Constructs from singular and plural forms of the name.  Protected to
     * ensure use by factory methods and subclasses only.
     *
     * @param single		The singular form of the unit name.  May not be
     *				<code>null</code>.
     * @param plural		The plural form of the unit name. May not be
     *				<code>null</code>.
     * throws NullIDError	One of the names is null.
     */
    protected UnitName(SingleName single, PluralName plural)
	throws NullIDError
    {
	if (single == null || plural == null)
	    throw new NullIDError(
		getClass().getName() + ".<init>(SingleName,PluralName): " +
		"One of the names is null");
	this.single = single;
	this.plural = plural;
    }

    /**
     * Factory method for obtaining an instance of this class from singular
     * and plural forms of the unit name.
     *
     * @param single		The singular form of the unit name.  May not be
     *				<code>null</code>.
     * @param plural		The plural form of the unit name. May not be
     *				<code>null</code>.
     * throws NullIDError	One of the names is null.
     */
    public static UnitName newUnitName(SingleName single, PluralName plural)
	throws NullIDError
    {
	return new UnitName(single, plural);
    }

    /**
     * Returns the default unit identifier corresponding to a given
     * character-set.  This method is the same as {@link #getSingle(IDCase)}.
     *
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The default unit identifier corresponding to
     *				the character-set.  May contain blanks
     *				but won't be <code>null</code>.
     * @see #getSingle(IDCase)
     */
    public String getDefaultID(IDCase whatCase)
    {
	return getSingle(whatCase);
    }

    /**
     * Returns the single form of the unit name in a given character-set.
     *
     * @param whatCase		The desired character-set.  One
     *				of: {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The name of the unit in the given character-set.
     *				May be <code>null</code> or contain blanks.
     */
    public final String getSingle(IDCase whatCase)
    {
	return single.getID(whatCase);
    }

    /**
     * Returns the plural form of the unit name in a given character-set.
     *
     * @param whatCase		The desired character-set.  One
     *				of: {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The name of the unit in the given character-set.
     *				May be <code>null</code> or contain blanks.
     */
    public final String getPlural(IDCase whatCase)
    {
	return plural.getID(whatCase);
    }

    /**
     * Returns a given form of the unit name in general Unicode.
     *
     * @param plural		If true, then the plural form of the name is
     *				returned.
     * @return			The name of the unit in Unicode.  May be
     *				<code>null</code> or contain blanks.
     * @see #getUnicodeSingle()
     * @see #getUnicodePlural()
     */
    public final String getUnicode(boolean plural)
    {
	return
	    plural
		? getUnicodePlural()
		: getUnicodeSingle();
    }

    /**
     * Returns the single form of the unit name in general Unicode.
     *
     * @return			The single form of the unit name in Unicode.
     *				May be <code>null</code> or contain blanks.
     */
    public final String getUnicodeSingle()
    {
	return single.getUnicode();
    }

    /**
     * Returns the plural form of the unit name in general Unicode.
     *
     * @return			The plural form of the unit name in Unicode.
     *				May be <code>null</code> or contain blanks.
     */
    public final String getUnicodePlural()
    {
	return plural.getUnicode();
    }

    /**
     * Returns the single form of the unit name in mixed-case ASCII.
     *
     * @return			The single form of the unit name mixed-case
     *				ASCII.	May be <code>null</code> or contain
     *				blanks.
     */
    public final String getMixedCaseSingle()
    {
	return single.getMixedCase();
    }

    /**
     * Returns the plural form of the unit name in mixed-case ASCII.
     *
     * @return			The plural form of the unit name in mixed-case
     *				ASCII.	May be <code>null</code> or contain
     *				blanks.
     */
    public final String getMixedCasePlural()
    {
	return plural.getMixedCase();
    }

    /**
     * Returns the single form of the unit name in upper-case ASCII.
     *
     * @return			The single form of the unit name in upper-case
     *				ASCII.	May be <code>null</code> or contain
     *				blanks.
     */
    public final String getUpperCaseSingle()
    {
	return single.getUpperCase();
    }

    /**
     * Returns the plural form of the unit name in upper-case ASCII.
     *
     * @return			The plural form of the unit name in upper-case
     *				ASCII.	May be <code>null</code> or contain
     *				blanks.
     */
    public final String getUpperCasePlural()
    {
	return plural.getUpperCase();
    }

    /**
     * Returns the string representation of this instance.
     *
     * @return			The string representation of this instance.
     *				May be <code>null</code> or contain blanks.
     */
    public String toString()
    {
	return single.toString() + " (" + plural.toString() + ")";
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
	boolean	equals;
	if (!(obj instanceof UnitName))
	{
	    equals = false;
	}
	else
	{
	    UnitName	that = (UnitName)obj;
	    equals = this == that || (
		single.equals(that.single) && plural.equals(that.plural));
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
	return single.hashCode() ^ plural.hashCode();
    }
}
