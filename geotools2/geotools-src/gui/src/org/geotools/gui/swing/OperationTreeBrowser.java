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
package org.geotools.gui.swing;

// J2SE dependencies
import java.util.List;
import java.util.Locale;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.RenderedImage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

// JAI dependencies
import javax.media.jai.OperationNode;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.PropertySource;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.gui.swing.tree.TreeNode;
import org.geotools.gui.swing.tree.NamedTreeNode;
import org.geotools.gui.swing.tree.MutableTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;


/**
 * Display a chain of {@link RenderedImage} as a tree. The specified image is the root of the
 * tree. Each source image is a children node (with potentially their own source images and/or
 * parameters) and each parameter is a children leaf.
 *
 * @version $Id: OperationTreeBrowser.java,v 1.2 2003/07/27 21:27:00 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author Lionel Flahaut 
 */
public class OperationTreeBrowser extends JPanel {
    /**
     * The image properties viewer.
     */
    private final ImageProperties imageProperties = new ImageProperties();

    /**
     * Construct a new browser for the given image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderedImage source) {
        super(new BorderLayout());
        final JTree tree = new JTree(getTree(source, getDefaultLocale()));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setBorder(BorderFactory.createEmptyBorder(6,6,0,0));

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                new JScrollPane(tree), imageProperties);
        add(split, BorderLayout.CENTER);
        setPreferredSize(new Dimension(600,250));
        final Listeners listeners = new Listeners();
        tree.addTreeSelectionListener(listeners);
    }

    /**
     * Returns a name for the given image. The default implementation returns the operation
     * name if the image is an instance of {@link RenderedOp}. Otherwise, it returns the
     * image class.
     *
     * @param  image The image.
     * @return A name for the given image.
     */
    private static String getName(final Object image) {
        if (image instanceof OperationNode) {
            return ((OperationNode) image).getOperationName();
        }
        return Utilities.getShortClassName(image);
    }

    /**
     * Returns a tree with all sources and parameters for the given source.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    public static TreeModel getTree(final RenderedImage image, final Locale locale) {
        return new DefaultTreeModel(getNode(image, locale));
    }

    /**
     * Returns the root node of a tree with all sources and parameters for the given source.
     *
     * @param  image The last image from an operation chain.
     * @param  locale The locale for tree node names.
     * @return The tree for the given image and all its sources.
     */
    private static MutableTreeNode getNode(final RenderedImage image, final Locale locale) {
        final DefaultMutableTreeNode root = new NamedTreeNode(getName(image), image);
        final List sources = image.getSources();
        if (sources != null) {
            final int n = sources.size();
            for (int i=0; i<n; i++) {
                root.add(getNode((RenderedImage)sources.get(i), locale));
            }
        }
        if (image instanceof OperationNode) {
            addParameters(root, (OperationNode)image, locale);
        }
        return root;
    }

    /**
     * Add the parameters from the specified operation to the specified tree node.
     *
     * @param root The tree node to add parameters to.
     * @param operation The operation for which to fetch parameters.
     * @param  locale The locale for tree node names.
     */
    private static void addParameters(final DefaultMutableTreeNode root,
                                      final OperationNode     operation,
                                      final Locale            locale)
    {
        final ParameterBlock param = operation.getParameterBlock();
        final ParameterListDescriptor descriptor;
        if (param instanceof ParameterList) {
            descriptor = ((ParameterList) param).getParameterListDescriptor();
        } else {
            final String name = operation.getOperationName();
            final String mode = operation.getRegistryModeName();
            descriptor = operation.getRegistry().getDescriptor(mode, name)
                                                .getParameterListDescriptor(mode);
        }
        Resources resources = null;
        final String[] names = descriptor.getParamNames();
        final int n = param.getNumParameters();
        for (int i=0; i<n; i++) {
            String name = null;
            if (names!=null && i<names.length) {
                name = names[i];
            }
            if (name == null) {
                if (resources == null) {
                    resources = Resources.getResources(locale);
                }
                name = resources.getString(ResourceKeys.PARAMETER_$1, new Integer(i));
            }
            root.add(new NamedTreeNode(name, param.getObjectParameter(i), false));
        }
    }

    /**
     * The listener for various event in the {@link OperationTreeBrowser} widget.
     *
     * @version $Id: OperationTreeBrowser.java,v 1.2 2003/07/27 21:27:00 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Listeners implements TreeSelectionListener {
        /** 
         * Called whenever the value of the selection changes.
         */
        public void valueChanged(final TreeSelectionEvent event) {
            Object selection = null;
            final TreePath path = event.getPath();
            if (path != null) {
                selection = path.getLastPathComponent();
                if (selection instanceof TreeNode) {
                    selection = ((TreeNode) selection).getUserObject();
                }
            }
            if (selection instanceof RenderedImage) {
                imageProperties.setImage((RenderedImage) selection);
            } else if (selection instanceof RenderableImage) {
                imageProperties.setImage((RenderableImage) selection);
            } else if (selection instanceof PropertySource) {
                imageProperties.setImage((PropertySource) selection);
            } else {
                imageProperties.setImage((PropertySource) null);
            }
        }
    }

    /**
     * Show the operation chain for the given image in the given owner.
     *
     * @param image The last image from an operation chain.
     * @param owner The owner widget, or <code>null</code> if none.
     */
    public static void showDialog(final RenderedImage image, final Component owner) {
        final OperationTreeBrowser browser = new OperationTreeBrowser(image);
        final String title = Resources.getResources(getDefaultLocale())
                                      .getString(ResourceKeys.OPERATIONS);
        if (SwingUtilities.showOptionDialog(owner, browser, title)) {
            // TODO: User clicked on "Ok".
        }
    }
}
