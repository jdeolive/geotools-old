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
package org.geotools.renderer.style;

/**
 * Base class for resolved styles. Styles are resolved according a particular rendering context.
 * The base class make no assumption about the output device (AWT, SWT, <i>etc.</i>). However,
 * a particular output device may need to be choosen for concrete subclasses, for example
 * {@link Style2D} for targeting <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>.
 *
 * @version $Id: Style.java,v 1.1 2003/05/28 10:23:51 desruisseaux Exp $
 */
public abstract class Style {
    /**
     * Construct a default style.
     */
    public void Style() {
    }
}
