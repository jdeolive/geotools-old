/*
 * Copyright 2001 by the University Corporation for Atmospheric Research,
 * P.O. Box 3000, Boulder CO 80307-3000, USA.
 * All rights reserved.
 * 
 * See the file LICENSE for terms.
 */

package javax.units;

/**
 * Provides support for the singular form of a unit name.  In general, unit
 * names are in Unicode and may contain multiple characters or embedded
 * blanks. Provision is made for returning unit names in multiple cases in order
 * to support systems with character set capabilities ranging from limited to
 * extensive.  Unit names always have general Unicode, mixed-case ASCII, and
 * upper-case ASCII forms.  The upper-case ASCII form of a unit name is the
 * mixed-case ASCII form converted to upper-case.</P>
 *
 * <P>This class is a concrete realization of the abstract class {@link 
 * SingleID}.  It is specialized for the creation of unit names.
 *
 * <P>Instances of this class are immutable.</P>
 *
 * @author JSR-108 Expert Group
 * @version $Revision: 1.1 $ $Date: 2004/05/09 15:47:13 $
 */
public class SingleName
    extends	NameID
{
    /**
     * Constructs from a single mixed-case ASCII name.  The given identifier 
     * will be used for both the Unicode and mixed-case ASCII names.
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param name		The singular form of the unit name in mixed-case
     *				ASCII.	May not be <code>null</code>.  May not
     *				contain a non-ASCII character.
     * @throws NullIDError	The argument is <code>null</code>.
     * @throws CaseException	The argument contains a non-ASCII character.
     * @see #SingleName(String,String)
     */
    protected SingleName(String name)
	throws NullIDError, CaseException
    {
	this(name, name);
    }

    /**
     * Constructs from Unicode and mixed-case ASCII forms of the singular unit
     * name.</P>
     *
     * <P>This method is protected to ensure use by factory methods and 
     * subclasses only.
     *
     * @param unicode		The singular form of the unit name in Unicode.
     *				May not be <code>null</code>.
     * @param ascii		The singular form of the unit name in mixed-case
     *				ASCII.  May not be <code>null</code>.  May not
     *				contain a non-ASCII character.
     * @throws NullIDError	One of the arguments is <code>null</code>.
     * @throws CaseException	The mixed-case ASCII name contains a non-ASCII
     *				character.
     */
    protected SingleName(String unicode, String ascii)
	throws NullIDError, CaseException
    {
	super(unicode, ascii);
    }

    /**
     * Factory method for obtaining an instance of this class from the singular
     * form of a unit name in mixed-case ASCII.  The Unicode version will be
     * the same as the ASCII version.  This method is, effectively, <code>new
     * SingleName(name)</code>.
     *
     * @param name		The singular form of the unit name in mixed-case
     *				ASCII.  May not be <code>null</code>.  May not
     *				contain a non-ASCII character.
     * @return			An instance of this class.
     * @throws NullIDError	The argument is <code>null</code>.
     * @throws CaseException	The name contains a non-ASCII character.
     */
    public static SingleName newSingleName(String name)
	throws NullIDError, CaseException
    {
	return newSingleName(name);
    }

    /**
     * Factory method for obtaining an instance of this class from both
     * Unicode and mixed-case ASCII forms.
     *
     * @param unicode		The Unicode form of the singular name.  May not
     *				be <code>null</code>.
     * @param ascii		The mixed-case ASCII form of the singular name.
     *				May not be <code>null</code>.  May not contain
     *				a non-ASCII character.
     * @return			An instance of this class.
     * @throws NullIDError	One of the arguments is <code>null</code>.
     * @throws CaseException	The mixed-case ASCII form contains a 
     *				non-ASCII character.
     */
    public static SingleName newSingleName(String unicode, String ascii)
	throws NullIDError, CaseException
    {
	return new SingleName(unicode, ascii);
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
    public String getSingle(IDCase whatCase)
    {
	return getID(whatCase);
    }

    /**
     * Returns the unit name in general Unicode.
     *
     * @return			The name for the unit in general Unicode.
     *				May be <code>null</code>.
     */
    public String getUnicodeSingle()
    {
	return getUnicode();
    }

    /**
     * Returns the unit name in mixed-case ASCII.
     *
     * @return			The name for the unit in mixed-case ASCII.
     *				May be <code>null</code>.
     */
    public String getMixedCaseSingle()
    {
	return getMixedCase();
    }

    /**
     * Returns the unit name in upper-case ASCII.
     *
     * @return			The name for the unit in upper-case ASCII.
     *				May be <code>null</code>.
     */
    public String getUpperCaseSingle()
    {
	return getUpperCase();
    }
}
