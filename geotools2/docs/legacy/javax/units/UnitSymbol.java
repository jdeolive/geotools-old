/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

/**
 * Provides support for unit symbols.  In general, symbols are in Unicode and
 * may contain multiple characters but no embedded blanks. Provision is made for
 * returning unit symbols in multiple cases in order to support systems with
 * character set capabilities ranging from limited to extensive.</P>
 *
 * <P>This class is a concrete realization of the abstract class {@link 
 * SingleID}.  It is specialized for the creation of unit symbols.
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class UnitSymbol
    extends	SingleID
{
    /**
     * Constructs from a single symbol.  The Unicode form will be the
     * given identifier.  The mixed-case ASCII form will be the given
     * identifier if it contains only ASCII characters; otherwise, it will
     * be <code>null</code>.  The upper-case ASCII form will be the given
     * identifier if it contains only upper-case ASCII characters; otherwise; it
     * will be <code>null</code>.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param symbol		The symbol.  May not be <code>null</code>.  May
     *				not contain a blank.
     * @throws NullIDError	The argument is <code>null</code>.
     * @throws BlankException	The argument contains a blank.
     */
    protected UnitSymbol(String symbol)
	throws NullIDError, BlankException
    {
	super(symbol);
	if (symbol.indexOf(' ') != -1)
	    throw new BlankException(
		getClass().getName() + ".UnitSymbol(String): " +
		"The symbol string contains a blank: \"" + symbol + "\"");
    }

    /**
     * Constructs from Unicode, mixed-case ASCII, and upper-case forms of a
     * symbol.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param unicode		The Unicode form of the symbol.  May not be
     *				<code>null</code>.
     * @param ascii		The mixed-case ASCII form of the symbol.
     *				If <code>null</code> and the Unicode for is
     *				ASCII only, then the mixed-case ASCII form will
     *				be the same as the Unicode form.
     * @param upper		The upper-case ASCII form of the symbol.
     *				If <code>null</code> and the mixed-case ASCII
     *				form is upper-case only, then the upper-case
     *				form will be the same as the ASCII form.
     * @throws NullIDError	The Unicode argument is <code>null</code>.
     * @throws BlankException	One of the symbol strings contains a blank.
     * @throws CaseException	The mixed-case or upper-case ASCII form contains
     *				an invalid character.
     */
    protected UnitSymbol(String unicode, String ascii, String upper)
	throws NullIDError, BlankException, CaseException
    {
	super(unicode, ascii, upper);
	if (unicode.indexOf(' ') != -1 ||
	    (ascii != null && ascii.indexOf(' ') != -1) ||
	    (upper != null && upper.indexOf(' ') != -1))
	{
	    throw new BlankException(
		getClass().getName() + ".UnitSymbol(String,String): " +
		"One of the symbol strings contains blanks: \"" +
		unicode + "\", \"" + ascii + "\", \"" + upper + "\"");
	}
    }

    /**
     * Factory method for obtaining an instance of this class from
     * a single symbol.  This method is, effectively, <code>new
     * UnitSymbol(symbol)</code>.
     *
     * @param symbol		The symbol.  May not be <code>null</code>.  May
     *				not contain blanks.
     * @throws NullIDError	The argument is <code>null</code>.
     * @throws BlankException	The symbol contains a blank.
     * @see #UnitSymbol(String)
     */
    public static UnitSymbol newUnitSymbol(String symbol)
	throws NullIDError, BlankException
    {
	return new UnitSymbol(symbol);
    }

    /**
     * Factory method for obtaining an instance of this class from
     * Unicode, mixed-case ASCII, and uppper-case ASCII forms.  This method is,
     * effectively, <code>new UnitSymbol(unicode,ascii,upper)</code>.
     *
     * @param unicode		The Unicode form of the symbol.
     *				May not be <code>null</code>.
     * @param ascii		The mixed-case ASCII form of the symbol.
     *				May be <code>null</code>.
     * @param upper		The upper-case ASCII form of the symbol.  May
     *				be <code>null</code>.
     * @throws NullIDError	One of the arguments is <code>null</code>.
     * @throws BlankException	One of the symbol strings contains a blank.
     * @throws CaseException	The mixed-case ASCII or upper-case ASCII form
     *				contains an invalid character.
     */
    public static UnitSymbol newUnitSymbol(
	    String unicode, String ascii, String upper)
	throws NullIDError, BlankException, CaseException
    {
	return new UnitSymbol(unicode, ascii, upper);
    }

    /**
     * Returns the default unit identifier corresponding to a given
     * character-set.  This method is the same as {@link #getSymbol(IDCase)}.
     *
     * @param whatCase		The character-set of the identifier.
     *				One of {@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The default unit identifier corresponding to
     *				the character-set.  Won't contain blanks and
     *				won't be <code>null</code>.
     * @see #getSymbol(IDCase)
     */
    public String getDefaultID(IDCase whatCase)
    {
	return getSymbol(whatCase);
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
     */
    public String getSymbol(IDCase whatCase)
    {
	return getID(whatCase);
    }

    /**
     * Returns the unit symbol in general Unicode.
     *
     * @return			The symbol for the unit in general Unicode.
     *				May be <code>null</code>.
     */
    public String getUnicodeSymbol()
    {
	return getUnicode();
    }

    /**
     * Returns the unit symbol in mixed-case ASCII.
     *
     * @return			The symbol for the unit in mixed-case ASCII.
     *				May be <code>null</code>.
     */
    public String getMixedCaseSymbol()
    {
	return getMixedCase();
    }

    /**
     * Returns the unit symbol in upper-case ASCII.
     *
     * @return			The symbol for the unit in upper-case ASCII.
     *				May be <code>null</code>.
     */
    public String getUpperCaseSymbol()
    {
	return getUpperCase();
    }
}
