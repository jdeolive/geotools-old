/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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

// J2SE dependencies
import javax.swing.JTree;


/**
 * A tree node with a name which may be different than the user object. The {@link JTree}
 * component invokes the {@link #toString} method for populating the tree widget. This class
 * overrides the default implementation (<code>{@link #getUserObject userObject}.toString</code>)
 * with a custom label.
 *
 * @author Martin Desruisseaux
 */
public class NamedTreeNode extends DefaultMutableTreeNode {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -5052321314347001298L;

    /**
     * The node label to be returned by {@link #toString}.
     */
    private final String name;

    /**
     * Creates a tree node that has no parent and no children, but which
     * allows children.
     *
     * @param name The node name to be returned by {@link #toString}.
     */
    public NamedTreeNode(final String name) {
        super();
        this.name = name;
    }

    /**
     * Creates a tree node with no parent, no children, but which allows
     * children, and initializes it with the specified user object.
     *
     * @param name The node name to be returned by {@link #toString}.
     * @param userObject an Object provided by the user that constitutes
     *                   the node's data
     */
    public NamedTreeNode(final String name, Object userObject) {
        super(userObject);
        this.name = name;
    }

    /**
     * Creates a tree node with no parent, no children, initialized with
     * the specified user object, and that allows children only if
     * specified.
     *
     * @param name The node name to be returned by {@link #toString}.
     * @param userObject an Object provided by the user that constitutes the node's data
     * @param allowsChildren if true, the node is allowed to have child
     *        nodes -- otherwise, it is always a leaf node
     */
    public NamedTreeNode(final String name, Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
        this.name = name;
    }

    /**
     * Returns this node label. This method is invoked by {@link JTree} for populating
     * the tree widget.
     */
    public String toString() {
        return name;
    }
}
