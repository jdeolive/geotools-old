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
 * Provides support for unit identifiers with both a name (both singular and
 * plural forms) and a symbol.	In general, names and symbols are in Unicode.
 * Both names and symbols may contain multiple characters.  Names may contain
 * embedded spaces.  Provision is made for returning unit identifiers in
 * multiple cases in order to support systems with character set capabilities
 * ranging from limited to extensive.</P>
 *
 * <P>This class is a "composite" component of the composite design pattern for
 * unit identifiers.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class CompleteID
    extends	UnitID
{
    /**
     * The name of the unit in all character-sets.
     */
    private final UnitName	name;

    /**
     * The symbol for the unit in all character-sets.
     */
    private final UnitSymbol	symbol;

    /**
     * Constructs from a unit name and symbol.	Protected to ensure use by
     * factory methods and subclasses only.
     *
     * @param name		The name of the unit.  May be <code>null</code>.
     * @param symbol		The symbol for the unit.  May be
     *				<code>null</code>.
     */
    protected CompleteID(UnitName name, UnitSymbol symbol)
    {
	this.name = name;
	this.symbol = symbol;
    }

    /**
     * Factory method for obtaining an instance of this class from a unit name
     * and symbol.
     *
     * @param name		The name of the unit.  May be <code>null</code>.
     * @param symbol		The symbol for the unit.  May be
     *				<code>null</code>.
     */
    public static CompleteID newCompleteID(UnitName name, UnitSymbol symbol)
    {
	return new CompleteID(name, symbol);
    }

    /**
     * Returns the default unit identifier corresponding to a given
     * character-set.  The default unit identifier for this class is the unit
     * symbol if that is non-<code>null</code>; otherwise, it is the singular
     * form of the unit name.
     *
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The default unit identifier corresponding to
     *				the character-set.  May be <code>null</code> or
     *				contain blanks.
     * @see #getSymbol(IDCase)
     * @see #getSingle(IDCase)
     */
    public final String getDefaultID(IDCase whatCase)
    {
	String	id = getSymbol(whatCase);
	if (id == null)
	    id = getSingle(whatCase);
	return id;
    }

    /**
     * Returns the unit symbol corresponding to a given character-set.
     *
     * @param whatCase		The character-set of the symbol.  One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit symbol corresponding to the given
     *				character-set. May be <code>null</code>.
     * @see UnitSymbol#getID(IDCase)
     */
    public final String getSymbol(IDCase whatCase)
    {
	return symbol.getID(whatCase);
    }

    /**
     * Returns the single form of the unit name corresponding to a given
     * character-set.
     *
     * @param whatCase		The character-set of the name.	One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The single form of the unit name corresponding
     *				to the given character-set. May be
     *				<code>null</code>.
     * @see UnitName#getSingle(IDCase)
     */
    public final String getSingle(IDCase whatCase)
    {
	return name.getSingle(whatCase);
    }

    /**
     * Returns the plural form of the unit name corresponding to a given
     * character-set.
     *
     * @param whatCase		The character-set of the name.	One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The plural form of the unit name corresponding
     *				to the given character-set. May be
     *				<code>null</code>.
     * @see UnitName#getPlural(IDCase)
     */
    public final String getPlural(IDCase whatCase)
    {
	return name.getPlural(whatCase);
    }

    /**
     * Returns the unit symbol in general Unicode.
     *
     * @return			The symbol for the unit in general Unicode.
     *				May be <code>null</code>.
     * @see UnitSymbol#getUnicodeSymbol()
     */
    public final String getUnicodeSymbol()
    {
	return symbol.getUnicodeSymbol();
    }

    /**
     * Returns the singular form of the unit name in general Unicode.
     *
     * @return			The singular form of the unit name in general
     *				Unicode.  May be <code>null</code>.
     * @see UnitName#getUnicodeSingle()
     */
    public final String getUnicodeSingle()
    {
	return name.getUnicodeSingle();
    }

    /**
     * Returns the plural form of the unit name in general Unicode.
     *
     * @return			The plural form of the unit name in general
     *				Unicode.  May be <code>null</code>.
     * @see UnitName#getUnicodePlural()
     */
    public final String getUnicodePlural()
    {
	return name.getUnicodePlural();
    }

    /**
     * Returns the unit symbol in mixed-case ASCII.
     *
     * @return			The symbol for the unit in mixed-case ASCII.
     *				May be <code>null</code>.
     * @see UnitSymbol#getMixedCaseSymbol()
     */
    public final String getMixedCaseSymbol()
    {
	return symbol.getMixedCaseSymbol();
    }

    /**
     * Returns the singular form of the unit name in mixed-case ASCII.
     *
     * @return			The singular form of the unit name in mixed-case
     *				ASCII.  May be <code>null</code>.
     * @see UnitName#getMixedCaseSingle()
     */
    public final String getMixedCaseSingle()
    {
	return name.getMixedCaseSingle();
    }

    /**
     * Returns the plural form of the unit name in mixed-case ASCII.
     *
     * @return			The plural form of the unit name in mixed-case
     *				ASCII.  May be <code>null</code>.
     * @see UnitName#getMixedCasePlural()
     */
    public final String getMixedCasePlural()
    {
	return name.getMixedCasePlural();
    }

    /**
     * Returns the unit symbol in upper-case ASCII.
     *
     * @return			The symbol for the unit in upper-case ASCII.
     *				May be <code>null</code>.
     * @see UnitSymbol#getUpperCaseSymbol()
     */
    public final String getUpperCaseSymbol()
    {
	return symbol.getUpperCaseSymbol();
    }

    /**
     * Returns the singular form of the unit name in upper-case ASCII.
     *
     * @return			The singular form of the unit name in upper-case
     *				ASCII.  May be <code>null</code>.
     * @see UnitName#getUpperCaseSingle()
     */
    public final String getUpperCaseSingle()
    {
	return name.getUpperCaseSingle();
    }

    /**
     * Returns the plural form of the unit name in upper-case ASCII.
     *
     * @return			The plural form of the unit name in upper-case
     *				ASCII.  May be <code>null</code>.
     * @see UnitName#getUpperCasePlural()
     */
    public final String getUpperCasePlural()
    {
	return name.getUpperCasePlural();
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
	if (!(obj instanceof CompleteID))
	{
	    equals = false;
	}
	else
	{
	    CompleteID	that = (CompleteID)obj;
	    equals = this == that || (
		name.equals(that.name) && symbol.equals(that.symbol));
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
	return name.hashCode() ^ symbol.hashCode();
    }
}
