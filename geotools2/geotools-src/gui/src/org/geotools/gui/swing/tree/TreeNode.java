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
 * Defines the requirements for an object that can be used as a
 * tree node in a {@link javax.swing.JTree}. This interface add
 * the <code>getUserObject()</code> to Swing's interface, which
 * seems to have been forgotten in J2SE.
 *
 * @version $Id: TreeNode.java,v 1.2 2004/04/30 19:40:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface TreeNode extends javax.swing.tree.TreeNode {
    /**
     * Returns this node's user object.
     *
     * @return the Object stored at this node by the user
     */
    public abstract Object getUserObject();
}
