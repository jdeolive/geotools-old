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

// J2SE dependencies
import java.util.List;
import java.util.ArrayList;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;

// Geotools dependencies
import org.geotools.resources.XArray;


/**
 * Convenience static methods for trees operations.
 *
 * @version $Id: Trees.java,v 1.1 2003/05/29 16:04:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class Trees {
    /**
     * Interdit la création d'objets de cette classe.
     */
    private Trees() {
    }

    /**
     * Retourne les chemins vers l'objet spécifié. Cette méthode suppose que l'arborescence
     * est constituée de noeuds {@link org.geotools.gui.swing.tree.TreeNode} et comparera
     * <code>value</code> avec les objets retournés par la méthode
     * {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}. Les noeuds qui ne sont
     * pas des objets {@link org.geotools.gui.swing.tree.TreeNode} ne seront pas
     * comparés à <code>value</code>.
     *
     * @param  model Modèle dans lequel rechercher le chemin.
     * @param  value Objet à rechercher dans
     *         {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @return Chemins vers l'objet spécifié. Ce tableau peut avoir une
     *         longueur de 0, mais ne sera jamais <code>null</code>.
     */
    public static TreePath[] getPathsToUserObject(final TreeModel model, final Object value) {
        final List paths = new ArrayList(8);
        final Object[] path = new Object[8];
        path[0] = model.getRoot();
        getPathsToUserObject(model, value, path, 1, paths);
        return (TreePath[]) paths.toArray(new TreePath[paths.size()]);
    }

    /**
     * Implémentation de la recherche des chemins. Cette
     * méthode s'appele elle-même d'une façon récursive.
     *
     * @param  model  Modèle dans lequel rechercher le chemin.
     * @param  value  Objet à rechercher dans
     *                {@link org.geotools.gui.swing.tree.TreeNode#getUserObject}.
     * @param  path   Chemin parcouru jusqu'à maintenant.
     * @param  length Longueur valide de <code>path</code>.
     * @param  list   Liste dans laquelle ajouter les {@link TreePath} trouvés.
     * @return <code>path</code>, ou un nouveau tableau s'il a fallu l'agrandir.
     */
    private static Object[] getPathsToUserObject(final TreeModel model, final Object value,
                                                 Object[] path, final int length, final List list)
    {
        final Object parent = path[length-1];
        if (parent instanceof org.geotools.gui.swing.tree.TreeNode) {
            final Object nodeValue = ((org.geotools.gui.swing.tree.TreeNode)parent).getUserObject();
            if (nodeValue==value || (value!=null && value.equals(nodeValue))) {
                list.add(new TreePath(XArray.resize(path, length)));
            }
        }
        final int count = model.getChildCount(parent);
        for (int i=0; i<count; i++) {
            if (length >= path.length) {
                path = XArray.resize(path, length << 1);
            }
            path[length] = model.getChild(parent, i);
            path = getPathsToUserObject(model, value, path, length+1, list);
        }
        return path;
    }

    /**
     * Construit une chaîne de caractères qui contiendra le
     * noeud spécifié ainsi que tous les noeuds enfants.
     *
     * @param model  Arborescence à écrire.
     * @param node   Noeud de l'arborescence à écrire.
     * @param buffer Buffer dans lequel écrire le noeud.
     * @param level  Niveau d'indentation (à partir de 0).
     * @param last   Indique si les niveaux précédents sont
     *               en train d'écrire leurs derniers items.
     * @return       Le tableau <code>last</code>, qui peut
     *               éventuellement avoir été agrandit.
     */
    private static boolean[] toString(final TreeModel model, final Object node,
                                      final StringBuffer buffer, final int level, boolean[] last)
    {
        for (int i=0; i<level; i++) {
            if (i != level-1) {
                buffer.append(last[i] ? '\u00A0' : '\u2502');
                buffer.append("\u00A0\u00A0\u00A0");
            } else {
                buffer.append(last[i] ? '\u2514': '\u251C');
                buffer.append("\u2500\u2500\u2500");
            }
        }
        buffer.append(node);
        buffer.append('\n');
        if (level >= last.length) {
            last = XArray.resize(last, level*2);
        }
        final int count=model.getChildCount(node);
        for (int i=0; i<count; i++) {
            last[level] = (i == count-1);
            last=toString(model, model.getChild(node,i), buffer, level+1, last);
        }
        return last;
    }

    /**
     * Retourne une chaîne de caractères qui contiendra une
     * représentation graphique de l'arborescence spécifiée.
     * Cette arborescence apparaître correctement si elle
     * est écrite avec une police mono-espacée.
     *
     * @param  tree Arborescence à écrire.
     * @param  root Noeud à partir d'où commencer à tracer l'arborescence.
     * @return Chaîne de caractères représentant l'arborescence, ou
     *         <code>null</code> si <code>root</code> était nul.
     */
    private static String toString(final TreeModel tree, final Object root) {
        if (root == null) {
            return null;
        }
        final StringBuffer buffer = new StringBuffer();
        toString(tree, root, buffer, 0, new boolean[64]);
        return buffer.toString();
    }

    /**
     * Retourne une chaîne de caractères qui contiendra une
     * représentation graphique de l'arborescence spécifiée.
     * Cette arborescence apparaître correctement si elle
     * est écrite avec une police mono-espacée.
     *
     * @param  tree Arborescence à écrire.
     * @return Chaîne de caractères représentant l'arborescence, ou
     *         <code>null</code> si l'arborescence ne contenait aucun noeud.
     */
    public static String toString(final TreeModel tree) {
        return toString(tree, tree.getRoot());
    }

    /**
     * Retourne une chaîne de caractères qui contiendra une
     * représentation graphique de l'arborescence spécifiée.
     * Cette arborescence apparaître correctement si elle
     * est écrite avec une police mono-espacée.
     *
     * @param  node Noeud à partir d'où écrire l'arborescence.
     * @return Chaîne de caractères représentant l'arborescence.
     */
    public static String toString(final TreeNode node) {
        return toString(new DefaultTreeModel(node, true));
    }
}
