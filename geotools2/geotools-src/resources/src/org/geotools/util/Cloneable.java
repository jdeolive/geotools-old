/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 */
package org.geotools.util;


/**
 * Interface for cloneable classes. A cloneable class implements the standard
 * {@link java.lang.Cloneable} interface from J2SE and additionnaly overrides
 * the {@link Object#clone()} method with public access. For some reason lost
 * in the mists of time, the J2SE's {@link java.lang.Cloneable} interface doesn't
 * declare the <code>clone()</code> method, which make it hard to use. This Geotools's
 * <code>Cloneable</code> interface add this missing method, which avoid the need to
 * cast an interface to its implementation in order to clone it.
 *
 * @version $Id: Cloneable.java,v 1.2 2003/11/12 14:13:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see java.lang.Cloneable
 * @see <A HREF="http://developer.java.sun.com/developer/bugParade/bugs/4098033.html">Cloneable
 *      doesn't define <code>clone()</code></A> on Sun's bug parade
 */
public interface Cloneable extends java.lang.Cloneable {
    /**
     * Creates and returns a copy of this object.
     * The precise meaning of "copy" may depend on the class of the object.
     *
     * @return A clone of this instance.
     * @see Object#clone
     */
    public Object clone();
}
