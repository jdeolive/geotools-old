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
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;

// JAI dependencies
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PropertySource;
import javax.media.jai.OperationNode;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;

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
 * @version $Id: OperationTreeBrowser.java,v 1.3 2003/07/28 22:36:53 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author Lionel Flahaut 
 *
 * @see ImageProperties
 * @see ParameterEditor
 * @see RegisteredOperationBrowser
 */
public class OperationTreeBrowser extends JPanel {
    /** Key for {@link PropertySource}. */  private static final String IMAGE     = "Image";
    /** Key for parameter card.         */  private static final String PARAMETER = "Parameter";

    /**
     * The image properties panel. Will be constructed only when first needed,
     * and the added to the card layout with the <code>IMAGE</code> name.
     */
    private ImageProperties imageProperties;

    /**
     * The parameter properties panel. Will be constructed only when first needed,
     * and the added to the card layout with the <code>PARAMETER</code> name.
     */
    private ParameterEditor parameterEditor;

    /**
     * The properties panel. The content for this panel depends on
     * the selected tree item, but usually includes the following:
     * <ul>
     *   <li>An {@link ImageProperties} instance.</li>
     *   <li>An {@link ParameterEditor} instance.</li>
     * </ul>
     */
    private final Container cards = new JPanel(new CardLayout());

    /**
     * Construct a new browser for the given image.
     *
     * @param source The last image from the rendering chain to browse.
     */
    public OperationTreeBrowser(final RenderedImage source) {
        super(new BorderLayout());
        final Listeners listeners = new Listeners();
        final JTree tree = new JTree(getTree(source, getDefaultLocale()));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setBorder(BorderFactory.createEmptyBorder(6,6,0,0));
        tree.addTreeSelectionListener(listeners);

        final JSplitPane split;
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), cards);
        split.setDividerLocation(220);
        add(split, BorderLayout.CENTER);

        setPreferredSize(new Dimension(600,250));
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
     * @version $Id: OperationTreeBrowser.java,v 1.3 2003/07/28 22:36:53 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class Listeners implements TreeSelectionListener {
        /** 
         * Called whenever the value of the selection changes. This method uses the
         * {@link TreeNode#getAllowsChildren} in order to determines if the selection
         * is a source (allows children = <code>true</code>) or a parameter
         * (allows children = <code>false</code>).
         */
        public void valueChanged(final TreeSelectionEvent event) {
            Object        selection  = null;   // The selected tree element.
            boolean       isSource   = false;  // Is 'selected' a source or a parameter?
            OperationNode operation  = null;   // The parent of the selected element as an op.
            int           paramIndex = -1;     // The index of the selected element.
            final TreePath path = event.getPath();
            if (path != null) {
                selection = path.getLastPathComponent();
                /*
                 * Some of piece of code in the following block can work with the Swing's
                 * TreeNode (i.e. it doesn't require the fixed Geotools's TreeNode).
                 */
                if (selection instanceof javax.swing.tree.TreeNode) {
                    javax.swing.tree.TreeNode node = (javax.swing.tree.TreeNode)selection;
                    isSource = node.getAllowsChildren();
                    node = node.getParent();
                    if (node instanceof TreeNode) {
                        final Object candidate = ((TreeNode)node).getUserObject();
                        if (candidate instanceof OperationNode) {
                            operation = (OperationNode) candidate;
                            final int count = node.getChildCount();
                            for (int n=-1,i=0; i<count; i++) {
                                final javax.swing.tree.TreeNode leaf=node.getChildAt(i);
                                if (!leaf.getAllowsChildren()) {
                                    n++; // Count only parameters, not sources.
                                }
                                if (leaf == selection) {
                                    paramIndex = n;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (selection instanceof TreeNode) {
                    selection = ((TreeNode) selection).getUserObject();
                }
            }
            if (isSource) {
                showSourceEditor(selection);
            } else {
                showParameterEditor(selection);
            }
            if (parameterEditor != null) {
                parameterEditor.setDescription(operation, paramIndex);
            }
        }
    }

    /**
     * Invoked when the user clicks on a source node in the operation tree (left pane).
     * This method show a properties panel in the right pane appropriate for the given
     * selection.
     *
     * @param  selection The user selection. This object is usually an instance of
     *         {@link RenderedImage}, {@link RenderableImage} or {@link PropertySource}.
     * @return <code>true</code> if this method has been able to find an editor, or
     *         <code>false</code> otherwise.
     */
    protected boolean showSourceEditor(final Object selection) {
        if (imageProperties == null) {
            imageProperties = new ImageProperties();
            cards.add(imageProperties, IMAGE);
        }
        ((CardLayout) cards.getLayout()).show(cards, IMAGE);
        if (selection instanceof RenderedImage) {
            imageProperties.setImage((RenderedImage) selection);
            return true;
        }
        if (selection instanceof RenderableImage) {
            imageProperties.setImage((RenderableImage) selection);
            return true;
        }
        if (selection instanceof PropertySource) {
            imageProperties.setImage((PropertySource) selection);
            return true;
        }
        imageProperties.setImage((PropertySource) null);
        return false;
    }

    /**
     * Invoked when the user clicks on a parameter node in the operation tree (left pane).
     * This method show a properties panel in the right pane appropriate for the given
     * selection.
     *
     * @param  selection The user selection. This object is usually an instance of
     *         {@link Number}, {@link KernelJAI}, {@link LookupTableJAI} or some other
     *         parameter object.
     * @return <code>true</code> if this method has been able to find an editor, or
     *         <code>false</code> otherwise.
     */
    protected boolean showParameterEditor(final Object selection) {
        if (parameterEditor == null) {
            parameterEditor = new ParameterEditor();
            cards.add(parameterEditor, PARAMETER);
        }
        ((CardLayout) cards.getLayout()).show(cards, PARAMETER);
        return parameterEditor.setParameterValue(selection) != null;
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
