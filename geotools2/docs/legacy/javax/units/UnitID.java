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
 * Provides support for unit identifiers.  A unit identifier may comprise a name
 * (both singular and plural forms) and a symbol.  In general, names and symbols
 * are in Unicode.  Both names and symbols may contain multiple characters.
 * Names may contain embedded spaces.  Provision is made for returning unit
 * identifiers in multiple cases in order to support systems with character set
 * capabilities ranging from limited to extensive.</P>
 *
 * <P>This class is the abstract interface of the composite design pattern
 * for unit identifiers.  In general, the methods in this class are designed
 * to support the "single" class {@link SingleID} by implementing the default
 * action of returning <code>null</code> values.  Also in general, the
 * lower-level methods in this class are overridden in the composite classes
 * {@link CompleteID} and {@link UnitName}.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public abstract class UnitID
    implements	Serializable
{
    /**
     * Constructs from nothing.	Protected to ensure use by factory methods and
     * subclasses only.
     */
    protected UnitID()
    {}

    /**
     * Returns the unit identifier corresponding to a given form and
     * character-set.
     *
     * @param form		The form of the identifier.  One of {@link
     *				IDForm#SYMBOL}, {@link IDForm#SINGLE}, or {@link
     *				IDForm#PLURAL}.
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit identifier corresponding to the
     *				given form and character-set.  May be
     *				<code>null</code> or contain blanks.
     * @see #getSymbol(IDCase)
     * @see #getSingle(IDCase)
     * @see #getPlural(IDCase)
     */
    public final String getID(IDForm form, IDCase whatCase)
    {
	return
	    IDForm.SYMBOL.equals(form)
		? getSymbol(whatCase)
		: IDForm.SINGLE.equals(form)
		    ? getSingle(whatCase)
		    : getPlural(whatCase);
    }

    /**
     * Returns the default unit identifier corresponding to a given
     * character-set.
     *
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The default unit identifier corresponding to
     *				the character-set.  May be <code>null</code> or
     *				contain blanks.
     */
    public abstract String getDefaultID(IDCase whatCase);

    /**
     * Returns the unit symbol corresponding to a given character-set.
     * This method should be overridden when appropriate.
     *
     * @param whatCase		The character-set of the symbol.  One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit symbol corresponding to the given
     *				character-set. May be <code>null</code>.
     *				The method in this class always returns
     *				<code>null</code>.
     */
    public String getSymbol(IDCase whatCase)
    {
	return null;
    }

    /**
     * Returns the single form of the unit name corresponding to a given
     * character-set.
     * This method should be overridden when appropriate.
     *
     * @param whatCase		The character-set of the name.	One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The single form of the unit name corresponding
     *				to the given character-set. May be
     *				<code>null</code>.  The method in this class
     *				always returns <code>null</code>.
     */
    public String getSingle(IDCase whatCase)
    {
	return null;
    }

    /**
     * Returns the plural form of the unit name corresponding to a given
     * character-set.
     * This method should be overridden when appropriate.
     *
     * @param whatCase		The character-set of the name.	One
     *				of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The plural form of the unit name corresponding
     *				to the given character-set. May be
     *				<code>null</code>.  The method in this class
     *				always returns <code>null</code>.
     */
    public String getPlural(IDCase whatCase)
    {
	return null;
    }

    /**
     * Returns the given form of unit identifier in general Unicode.
     *
     * @param form		The form of the name.  One of {@link
     *				IDForm#SYMBOL}, {@link IDForm#SINGLE}, or {@link
     *				IDForm#PLURAL}.
     * @return			The requested form of the unit identifier in
     *				general Unicode. May be <code>null</code> or
     *				contain blanks.
     * @see #getUnicodeSymbol()
     * @see #getUnicodeSingle()
     * @see #getUnicodePlural()
     */
    public final String getUnicode(IDForm form)
    {
	return
	    IDForm.SYMBOL.equals(form)
		? getUnicodeSymbol()
		: IDForm.SINGLE.equals(form)
		    ? getUnicodeSingle()
		    : getUnicodePlural();
    }

    /**
     * Returns the given form of unit identifier in mixed-case ASCII.
     *
     * @param form		The form of the name.  One of {@link
     *				IDForm#SYMBOL}, {@link IDForm#SINGLE}, or {@link
     *				IDForm#PLURAL}.
     * @return			The requested form of the unit identifier in
     *				mixed-case ASCII. May be <code>null</code> or
     *				contain blanks.
     * @see #getMixedCaseSymbol()
     * @see #getMixedCaseSingle()
     * @see #getMixedCasePlural()
     */
    public final String getMixedCase(IDForm form)
    {
	return
	    IDForm.SYMBOL.equals(form)
		? getMixedCaseSymbol()
		: IDForm.SINGLE.equals(form)
		    ? getMixedCaseSingle()
		    : getMixedCasePlural();
    }

    /**
     * Returns the given form of unit identifier in upper-case ASCII.
     *
     * @param form		The form of the name.  One of {@link
     *				IDForm#SYMBOL}, {@link IDForm#SINGLE}, or {@link
     *				IDForm#PLURAL}.
     * @return			The requested form of the unit identifier in
     *				upper-case ASCII. May be <code>null</code> or
     *				contain blanks.
     * @see #getUpperCaseSymbol()
     * @see #getUpperCaseSingle()
     * @see #getUpperCasePlural()
     */
    public final String getUpperCase(IDForm form)
    {
	return
	    IDForm.SYMBOL.equals(form)
		? getUpperCaseSymbol()
		: IDForm.SINGLE.equals(form)
		    ? getUpperCaseSingle()
		    : getUpperCasePlural();
    }

    /**
     * Returns the unit name corresponding to a given form and character-set.
     *
     * @param plural		If true, then the plural form of the name is
     *				returned.
     * @param whatCase		The character-set of the name.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The unit name corresponding to the given
     *				character-set.	May be <code>null</code> or
     *				contain blanks.
     * @see #getSingle(IDCase)
     * @see #getPlural(IDCase)
     */
    public final String getName(boolean plural, IDCase whatCase)
    {
	return 
	    plural
		? getSingle(whatCase)
		: getPlural(whatCase);
    }

    /**
     * Returns the unit name corresponding to a given form in general Unicode.
     *
     * @param plural		If true, then the plural form of the name is
     *				returned.
     * @return			The unit name corresponding to the given form.
     *				May be <code>null</code> or contain blanks.
     * @see #getUnicodeSingle()
     * @see #getUnicodePlural()
     */
    public final String getUnicodeName(boolean plural)
    {
	return 
	    plural
		? getUnicodePlural()
		: getUnicodeSingle();
    }

    /**
     * Returns the unit name corresponding to a given form in mixed-case ASCII.
     *
     * @param plural		If true, then the plural form of the name is
     *				returned.
     * @return			The unit name corresponding to the given form.
     *				May be <code>null</code> or contain blanks.
     * @see #getMixedCaseSingle()
     * @see #getMixedCasePlural()
     */
    public final String getMixedCaseName(boolean plural)
    {
	return 
	    plural
		? getMixedCasePlural()
		: getMixedCaseSingle();
    }

    /**
     * Returns the unit name corresponding to a given form in upper-case ASCII.
     *
     * @param plural		If true, then the plural form of the name is
     *				returned.
     * @return			The unit name corresponding to the given form.
     *				May be <code>null</code> or contain blanks.
     * @see #getUpperCaseSingle()
     * @see #getUpperCasePlural()
     */
    public final String getUpperCaseName(boolean plural)
    {
	return 
	    plural
		? getUpperCasePlural()
		: getUpperCaseSingle();
    }

    /**
     * Returns the unit symbol in general Unicode.
     * This method should be overridden when appropriate.
     *
     * @return			The symbol for the unit in general Unicode.  May
     *				be <code>null</code>.  The method in this class
     *				always returns <code>null</code>.
     */
    public String getUnicodeSymbol()
    {
	return null;
    }

    /**
     * Returns the singular form of the unit name in general Unicode.
     * This method should be overridden when appropriate.
     *
     * @return			The singular form of the unit name in general
     *				Unicode.  May be <code>null</code>.  The method
     *				in this class always returns <code>null</code>.
     */
    public String getUnicodeSingle()
    {
	return null;
    }

    /**
     * Returns the plural form of the unit name in general Unicode.
     * This method should be overridden when appropriate.
     *
     * @return			The plural form of the unit name in general
     *				Unicode.  May be <code>null</code>.  The method
     *				in this class always returns <code>null</code>.
     */
    public String getUnicodePlural()
    {
	return null;
    }

    /**
     * Returns the unit symbol in mixed-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The symbol for the unit in mixed-case ASCII.
     *				May be <code>null</code>.  The method in this
     *				class always returns <code>null</code>.
     */
    public String getMixedCaseSymbol()
    {
	return null;
    }

    /**
     * Returns the singular form of the unit name in mixed-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The singular form of the unit name in mixed-case
     *				ASCII.	May be <code>null</code>.  The method in
     *				this class always returns <code>null</code>.
     */
    public String getMixedCaseSingle()
    {
	return null;
    }

    /**
     * Returns the plural form of the unit name in mixed-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The plural form of the unit name in mixed-case
     *				ASCII.	May be <code>null</code>.  The method in
     *				this class always returns <code>null</code>.
     */
    public String getMixedCasePlural()
    {
	return null;
    }

    /**
     * Returns the unit symbol in upper-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The symbol for the unit in upper-case ASCII.
     *				May be <code>null</code>.  The method in this
     *				class always returns <code>null</code>.
     */
    public String getUpperCaseSymbol()
    {
	return null;
    }

    /**
     * Returns the singular form of the unit name in upper-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The singular form of the unit name in upper-case
     *				ASCII.	May be <code>null</code>.  The method in
     *				this class always returns <code>null</code>.
     */
    public String getUpperCaseSingle()
    {
	return null;
    }

    /**
     * Returns the plural form of the unit name in upper-case ASCII.
     * This method should be overridden when appropriate.
     *
     * @return			The plural form of the unit name in upper-case
     *				ASCII.	May be <code>null</code>.  The method in
     *				this class always returns <code>null</code>.
     */
    public String getUpperCasePlural()
    {
	return null;
    }

    /**
     * Returns the string representation of this identifier.  Same as
     * <code>getDefaultID(IDCase.UNICODE)</code>.
     *
     * @return			The string representation of this identifier.
     *				May be <code>null</code> or contain blanks.
     * @see #getDefaultID(IDCase)
     */
    public String toString()
    {
	return getDefaultID(IDCase.UNICODE);
    }

    /**
     * Indicates if this instance is semantically identical to an object.
     *
     * @param obj		The object to be compared with this instance.
     * @return			True if and only if this instance is 
     *				semantically identical to the object.
     */
    public abstract boolean equals(Object obj);

    /**
     * Returns the hash code of this instance.	If <code>that</code> is another
     * instance of this class and <code>equals(that)</code> is true, then
     * <code>hashCode() == that.hashCode()</code> is true.
     *
     * @return			The hash code of this instance.
     */
    public abstract int hashCode();
}
