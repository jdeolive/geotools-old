/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.gui.swing.tree;


/**
 * General-purpose node in a tree data structure. This default implementation implements
 * Geotools's {@link MutableTreeNode} interface, which inherit a <code>getUserObject()</code>
 * method. This method is provided in Swing's <code>DefaultMutableTreeNode</code> implementation
 * but seems to have been forgotten in interfaces.
 *
 * @version $Id: DefaultMutableTreeNode.java,v 1.2 2004/04/30 19:40:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class DefaultMutableTreeNode extends javax.swing.tree.DefaultMutableTreeNode
        implements MutableTreeNode
{
    /**
     * Creates a tree node that has no parent and no children, but which
     * allows children.
     */
    public DefaultMutableTreeNode() {
        super();
    }

    /**
     * Creates a tree node with no parent, no children, but which allows
     * children, and initializes it with the specified user object.
     *
     * @param userObject an Object provided by the user that constitutes
     *                   the node's data
     */
    public DefaultMutableTreeNode(Object userObject) {
        super(userObject);
    }

    /**
     * Creates a tree node with no parent, no children, initialized with
     * the specified user object, and that allows children only if
     * specified.
     *
     * @param userObject an Object provided by the user that constitutes
     *        the node's data
     * @param allowsChildren if true, the node is allowed to have child
     *        nodes -- otherwise, it is always a leaf node
     */
    public DefaultMutableTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }
}
