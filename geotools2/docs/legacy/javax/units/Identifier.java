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
 * Provides support for identifiers.  In general, identifiers are in Unicode and
 * may contain multiple characters or embedded spaces. Provision is made for
 * returning identifiers in upper-case-only ASCII, mixed-case ASCII, or general
 * Unicode in order to support systems with character set capabilities ranging
 * from limited to extensive.</P>
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class Identifier
    implements	Serializable
{
    /**
     * The identifier in general Unicode.
     */
    private final String	unicode;	// won't be null

    /**
     * The identifier in general ASCII.
     */
    private final String	ascii;		// may be null

    /**
     * The identifier in upper-case-only ASCII.
     */
    private final String	upper;		// may be null

    /**
     * Constructs from a single identifying string.  The Unicode form will
     * be the given identifier.  The mixed-case ASCII form will be the given
     * identifier if it contains only ASCII characters; otherwise, it will
     * be <code>null</code>.  The upper-case ASCII form will be the given
     * identifier if it contains only upper-case ASCII characters; otherwise; it
     * will be <code>null</code>.</P>
     *
     * <P>This method is protected to ensure use by factory methods and
     * subclasses only.</P>
     *
     * @param id		The identifier.  May not be <code>null</code>.
     * @throws NullIDError	The argument is <code>null</code>.
     */
    protected Identifier(String id)
	throws NullIDError
    {
	if (id == null)
	    throw new NullIDError(
		getClass().getName() + ".<init>(String): Null argument");
	unicode = id;
	ascii = isASCII(id) ? id : null;
	upper = isUpper(id) ? id : null;
    }

    /**
     * Constructs from Unicode, mixed-case ASCII, and upper-case ASCII forms of
     * an identifier.</P>
     *
     * <P>This method is protected to ensure use by factory methods and
     * subclasses only.</P>
     *
     * @param unicode		The Unicode form of the identifier.  May not
     *				be <code>null</code>.
     * @param ascii		The mixed-case ASCII form of the identifier.  If
     *				<code>null</code> and the Unicode form contains
     *				only ASCII characters, then the Unicode form
     *				will be used as the mixed-case ASCII form.
     * @param upper		The upper-case ASCII form of the identifier.
     *				If <code>null</code> and the mixed-case form
     *				contains only upper-case characters, then the
     *				mixed-case form will be used as the upper-case
     *				form.
     * @throws CaseException	The mixed-case or upper-case ASCII form contains
     *				invalid characters.
     * @throws NullIDError	The Unicode argument is <code>null</code>.
     */
    protected Identifier(String unicode, String ascii, String upper)
	throws NullIDError, CaseException
    {
	if (unicode == null)
	    throw new NullIDError(
		getClass().getName() + ".<init>(...): Null Unicode argument");
	if (ascii == null)
	{
	    if (isASCII(unicode))
		ascii = unicode;
	}
	else
	{
	    if (!isASCII(ascii))
		throw new CaseException(
		    getClass().getName() + ".<init>(...): " +
		    "Mixed-case ASCII form contains invalid characters: \"" +
		    ascii + "\"");
	}
	if (upper == null)
	{
	    if (isUpper(ascii))
		upper = ascii;
	}
	else
	{
	    if (!isUpper(upper))
		throw new CaseException(
		    getClass().getName() + ".<init>(...): " +
		    "Upper-case ASCII form contains invalid characters: \"" +
		    upper + "\"");
	}
	this.unicode = unicode;
	this.ascii = ascii;
	this.upper = upper;
    }

    /**
     * Factory method for obtaining an instance of this class from a
     * single identifying string.  This method is, effectively, <code>new
     * Identifier(id)<code>.
     *
     * @param id		The identifier.  May not be <code>null</code>.
     * @return			The instance of this class corresponding to the
     *				input.
     * @throws NullIDError	The argument is null.
     * @see #Identifier(String)
     */
    public static Identifier newIdentifier(String id)
	throws NullIDError
    {
	return new Identifier(id);
    }

    /**
     * Factory method for obtaining an instance of this class from
     * Unicode, mixed-case ASCII, and upper-case ASCII forms.  This method
     * is, effectively, <code>new Identifier(String,String,String).
     *
     * @param unicode		The Unicode form of the identifier.  May
     *				not be <code>null</code>.
     * @param ascii		The mixed-case ASCII form of the identifier.
     *				If <code>null</code> and the Unicode form is
     *				ASCII, then the Unicode form will be used as
     *				the mixed-case ASCII form.
     * @param upper		The upper-case ASCII form of the identifier.
     *				If <code>null</code> and the mixed-case form
     *				is upper-case only, then the mixed-case form
     *				will be used as the upper-case form.
     * @throws CaseException	The mixed-case ASCII form contains invalid 
     *				characters.
     * @throws NullIDError	The Unicode argument is null.
     * @see #Identifier(String,String,String)
     */
    public static Identifier newIdentifier(
	    String unicode, String ascii, String upper)
	throws NullIDError, CaseException
    {
	return new Identifier(unicode, ascii, upper);
    }

    /**
     * Returns the identifier in a requested character-set.
     *
     * @param whatCase		The character-set of the identifier.  One of
     *				{@link IDCase#UNICODE}, {@link
     *				IDCase#MIXED_CASE_ASCII}, or {@link
     *				IDCase#UPPER_CASE_ASCII}.
     * @return			The identifier in the requested
     *				character-set.  May be <code>null</code>.  If
     *				non-<code>null</code>, then all characters will
     *				be members of the requested character set.
     * @see #getUnicode()
     * @see #getMixedCase()
     * @see #getUpperCase()
     */
    public final String getID(IDCase whatCase)
    {
	return
	    whatCase == IDCase.UNICODE
		? getUnicode()
		: whatCase == IDCase.MIXED_CASE_ASCII
		    ? getMixedCase()
		    : getUpperCase();
    }

    /**
     * Returns the Unicode identifier.
     *
     * @return			The Unicode identifier.
     */
    public String getUnicode()
    {
	return unicode;
    }

    /**
     * Returns the mixed-case ASCII identifier.
     *
     * @return			The mixed-case ASCII identifier.
     *				Will be <code>null</code> if and only if
     *				<code>getUpperCase()</code> also returns
     *				<code>null</code>.
     */
    public final String getMixedCase()
    {
	return ascii;
    }

    /**
     * Returns the upper-case-only ASCII identifier.
     *
     * @return			The upper-case-only ASCII identifier.
     *				Will be <code>null</code> if and only if
     *				<code>getUpperCase()</code> also returns
     *				<code>null</code>.
     */
    public final String getUpperCase()
    {
	return ascii == null ? ascii : ascii.toUpperCase();
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
	if (!(obj instanceof Identifier))
	{
	    equals = false;
	}
	else
	{
	    Identifier	that = (Identifier)obj;
	    equals = this == that || (
		(unicode == null
		    ? that.unicode == null
		    : unicode.equals(that.unicode)) &&
		(ascii == null
		    ? that.ascii == null
		    : ascii.equals(that.ascii)));
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
	return
	    (unicode == null
		? 0
		: unicode.hashCode()) ^
	    (ascii == null
		? 0
		: ascii.hashCode());
    }

    /**
     * Indicates if a string contains only ASCII characters.
     *
     * @param string		The string to be investigated.  If <code>
     *				null</code>, then false is returned.
     * @return			False if and only if the string is <code>
     *				null</code> or contains a non-ASCII character.
     */
    protected static boolean isASCII(String string)
    {
	boolean	isASCII;
	if (string == null)
	{
	    isASCII = false;
	}
	else
	{
	    int	i = 0;
	    int	n = string.length();
	    while (i < n &&
		Character.UnicodeBlock.BASIC_LATIN.equals(
		    Character.UnicodeBlock.of(string.charAt(i++))))
		;
	    isASCII = i >= n;
	}
	return isASCII;
    }

    /**
     * Indicates if a string contains only upper-case ASCII characters.
     *
     * @param string		The string to be investigated.
     * @return			False if and only if the string contains
     *				a non-upper-case ASCII character.
     */
    protected static boolean isUpper(String string)
    {
	return
	    isASCII(string) && new String(string).toUpperCase().equals(string);
    }
}
