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
 * Defines the requirements for a tree node object that can change.
 * It may changes by adding or removing child nodes, or by changing
 * the contents of a user object stored in the node. This interface
 * inherit the {@link #getUserObject getUserObject()} method from
 * Geotools's <code>TreeNode</code>. The Swing's <code>MutableTreeNode</code>
 * interface defines a {@link #setUserObject(Object) setUserObject(Object)}
 * method but doesn't defines or inherit any <code>getUserObject()</code>.
 *
 * @version $Id: MutableTreeNode.java,v 1.2 2004/04/30 19:40:16 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface MutableTreeNode extends javax.swing.tree.MutableTreeNode, TreeNode {
}
