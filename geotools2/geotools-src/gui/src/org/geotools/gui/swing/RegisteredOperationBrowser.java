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
import java.util.Arrays;
import java.util.Locale;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.text.JTextComponent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.image.renderable.RenderedImageFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.gui.swing.tree.Trees;
import org.geotools.gui.swing.tree.TreeNode;
import org.geotools.gui.swing.tree.NamedTreeNode;
import org.geotools.gui.swing.tree.MutableTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.resources.gui.ResourceKeys;
import org.geotools.resources.gui.Resources;


/**
 * Browse through the registered JAI operations.
 *
 * @version $Id: RegisteredOperationBrowser.java,v 1.1 2003/07/25 18:05:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class RegisteredOperationBrowser extends JPanel {
    /**
     * The text area for operation's description.
     */
    private final JLabel description = new JLabel(" ");

    /**
     * The text area for the version and vendor.
     */
    private final JLabel version = new JLabel(" ");
    
    /**
     * Construct a new operation browser for the default {@link JAI} instance.
     */
    public RegisteredOperationBrowser() {
        super(new BorderLayout());
        final JTree tree = new JTree(getTree());
        tree.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 0));
        add(new JScrollPane(tree), BorderLayout.CENTER);
        /*
         * Add labels (description and version number).
         */
        final Box labels = Box.createVerticalBox();
        labels.add(description);
        labels.add(version);
        labels.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 0));
        add(labels, BorderLayout.SOUTH);
        /*
         * Configure the operations tree.
         */
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(final TreeSelectionEvent event) {
                final TreePath path = event.getNewLeadSelectionPath();
                if (path != null) {
                    for (int i=path.getPathCount(); --i>=0;) {
                        Object candidate = path.getPathComponent(i);
                        if (candidate instanceof TreeNode) {
                            candidate = ((TreeNode) candidate).getUserObject();
                            if (candidate instanceof OperationDescriptor) {
                                selected((OperationDescriptor) candidate);
                                return;
                            }
                        }
                    }
                }
                selected(null);
            }
        });
    }

    /**
     * Invoked when the user selected a new operation in the tree. The default implementation
     * display the operation description in the text area.
     *
     * @param operation The selected operation, or <code>null</code> if no operation is
     *        selected.
     */
    protected void selected(final OperationDescriptor operation) {
        final String description, version;
        if (operation != null) {
            final Locale locale = getLocale();
            final ResourceBundle resources = operation.getResourceBundle(locale);
            description = resources.getString("Description");
            version     = Resources.getResources(locale).getString(ResourceKeys.VERSION_$1,
                          resources.getString("Version")) + ", " +
                          resources.getString("Vendor");
        } else {
            description = " ";
            version     = " ";
        }
        this.description.setText(description);
        this.version    .setText(version    );
    }

    /**
     * Returns a tree view of all operations registered in the default {@link JAI} instance.
     * Labels will be formatted in the Swing's {@linkplain #getDefaultLocale default locale}.
     *
     * @return All JAI operations as a tree.
     */
    public static TreeModel getTree() {
        return getTree(JAI.getDefaultInstance().getOperationRegistry(), getDefaultLocale());
    }

    /**
     * Returns a tree view of all operations registered in the given registry.
     *
     * @param  registry The registry (e.g. {@link JAI#getOperationRegistry()}).
     * @param  The locale (e.g. {@link Locale#getDefault()}).
     * @return All JAI operations as a tree.
     *
     * @see #getTree()
     * @see JAI#getDefaultInstance()
     * @see JAI#getOperationRegistry()
     * @see Locale#getDefault()
     */
    public static TreeModel getTree(final OperationRegistry registry, final Locale locale) {
        final Resources resources = Resources.getResources(locale);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                                                resources.getString(ResourceKeys.OPERATIONS));
        /*
         * Add registry modes ("rendered", "renderable", etc.),
         * and gets the operation descriptors for each mode.
         */
        final String[] modes = registry.getRegistryModes();
        Arrays.sort(modes);
        for (int i=0; i<modes.length; i++) {
            final String mode = modes[i];
            final DefaultMutableTreeNode modeNode = new DefaultMutableTreeNode(mode);
            final List descriptors = registry.getDescriptors(mode);
            Collections.sort(descriptors, new Comparator() {
                public int compare(final Object obj1, final Object obj2) {
                    final RegistryElementDescriptor desc1 = (RegistryElementDescriptor) obj1;
                    final RegistryElementDescriptor desc2 = (RegistryElementDescriptor) obj2;
                    return desc1.getName().compareTo(desc2.getName());
                }
            });
            /*
             * Add the operations ("add", "convolve", etc.) and their parameters.
             */
            for (final Iterator it=descriptors.iterator(); it.hasNext();) {
                final RegistryElementDescriptor descriptor;
                final DefaultMutableTreeNode descriptorNode;
                final ParameterListDescriptor param;
                descriptor     = (RegistryElementDescriptor)it.next();
                descriptorNode = new NamedTreeNode(descriptor.getName(), descriptor);
                param          = descriptor.getParameterListDescriptor(mode);
                if (param != null) {
                    final String[] names = param.getParamNames();
                    if (names != null) {
                        // No sorting; the order is relevant
                        for (int j=0; j<names.length; j++) {
                            descriptorNode.add(new DefaultMutableTreeNode(names[j], false));
                        }
                    }
                }
                modeNode.add(descriptorNode);
            }
            root.add(modeNode);
        }
        return new DefaultTreeModel(root, true);
    }

    /**
     * Display the operation browser from the command line. This method is usefull for checking
     * the widget appearance and the list of registered {@link JAI} operations. If this method
     * is launch with the <code>-print</code> argument, then the tree of operations will be sent
     * to standard output.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        if (arguments.getFlag("-print")) {
            arguments.out.println(Trees.toString(getTree()));
        } else {
            final JFrame frame = new JFrame(Resources.format(ResourceKeys.OPERATIONS));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(new RegisteredOperationBrowser());
            frame.pack();
            frame.show();
        }
    }
}
