/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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

// Swing dependencies
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;

// Events
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Layout
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

// Collections
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

// Miscellaneous
import java.net.URL;
import java.util.Arrays;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.SwingUtilities;


/**
 * A widget showing selected and unselected items in two disjoint list.
 * The list on the right side shows items available for selection. The
 * list on the left side shows items already selected. User can move
 * items from one list to the other using buttons in the middle.
 *
 * @version $Id: DisjointLists.java,v 1.1 2003/06/25 12:57:41 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class DisjointLists extends JPanel {
    /**
     * The list model. Each {@link DisjointLists} object will use two instances
     * of this class.  Both instances share the same list of elements, but have
     * their own list of index of visibles elements.
     */
    private static final class Model extends AbstractListModel {
        /**
         * The list of elements shared by both lists.
         * Not all elements in this list will be displayed.
         */
        private final List choices;

        /**
         * The index of valids elements in the list.
         */
        private int[] visibles = new int[12];

        /**
         * The number of valid elements in {@link #visibles}.
         */
        private int size;

        /**
         * Construct a model for the specified list of elements.
         */
        public Model(final List choices) {
            this.choices = choices;
        }

        /**
         * Returns the number of valid elements.
         */
        public int getSize() {
            assert size>=0 && size<=choices.size();
            return size;
        }

        /**
         * Returns the element at the specified index.
         */
        public Object getElementAt(final int index) {
            assert index>=0 && index<size : index;
            return choices.get(visibles[index]);
        }

        /**
         * Makes sure that {@link #visibles} has the specified capacity.
         */
        private void ensureCapacity(final int capacity) {
            if (visibles.length < capacity) {
                visibles = XArray.resize(visibles, Math.max(size*2, capacity));
            }
        }

        /**
         * Move elements in the specified range from this model to an other model.
         *
         * @param lower Lower index (inclusive) in this model.
         * @param upper Upper index (exclusive) in this model.
         * @param dest  The destination model.
         */
        public void move(final int lower, final int upper, final Model dest) {
            if (upper > lower) {
                assert lower>=0 && upper<=size;
                final int length = upper-lower;
                dest.ensureCapacity(dest.size + length);
                System.arraycopy(visibles, lower, dest.visibles, dest.size, length);
                System.arraycopy(visibles, upper, visibles, lower, size-upper);
                dest.size += length;
                this.size -= length;
                this.fireIntervalRemoved(this, lower, upper-1);
                dest.fireIntervalAdded(length);
            }
        }

        /**
         * Fire when an interval has been appened to this object.
         */
        private void fireIntervalAdded(final int length) {
            fireIntervalAdded(this, size-length, size-1);
        }

        /**
         * Add all elements from the specified collection.
         */
        public void addAll(final Collection items) {
            if (!items.isEmpty()) {
                choices.addAll(items);
                final int length = items.size();
                ensureCapacity(size + length);
                final int max = choices.size();
                for (int i=max-length; i<max; i++) {
                    visibles[size++] = i;
                }
                fireIntervalAdded(length);
            }
        }
    }

    /**
     * Action invoked when the user pressed a button. This action
     * invokes {@link Model#move} with selected indices.
     */
    private static final class Action implements ActionListener {
        /**
         * The source and target lists.
         */
        private final JList source, target;

        /**
         * <code>true</code> if we should move all items on action/
         */
        private final boolean all;

        /**
         * Construct a new "move" action.
         */
        public Action(final JList source, final JList target, final boolean all) {
            this.source = source;
            this.target = target;
            this.all    = all;
        }

        /**
         * Invoked when the user pressed a "move" button.
         */
        public void actionPerformed(final ActionEvent event) {
            final Model source = (Model)this.source.getModel();
            final Model target = (Model)this.target.getModel();
            if (all) {
                source.move(0, source.getSize(), target);
                return;
            }
            final int[] indices = this.source.getSelectedIndices();
            Arrays.sort(indices);
            for (int i=0; i<indices.length;) {
                int lower = indices[i];
                int upper = lower+1;
                while (++i<indices.length && indices[i]==upper) {
                    upper++;
                }
                source.move(lower, upper, target);
                final int length = (upper-lower);
                for (int j=i; j<indices.length; j++) {
                    indices[j] -= length;
                }
            }
        }
    }

    /**
     * The list on the left side. This list is initially empty.
     */
    private final JList left;

    /**
     * The list on the right side. This is the list that contains
     * the element selectable by the user.
     */
    private final JList right;

    /**
     * Construct a new list.
     */
    public DisjointLists() {
        super(new GridBagLayout());
        /*
         * Setup lists
         */
        final List choices = new ArrayList();
        left  = new JList(new Model(choices));
        right = new JList(new Model(choices));
        final JScrollPane  leftPane = new JScrollPane( left);
        final JScrollPane rightPane = new JScrollPane(right);
        final Dimension size = new Dimension(160, 200);
        leftPane .setPreferredSize(size);
        rightPane.setPreferredSize(size);
        /*
         * Setup buttons
         */
        final ClassLoader loader = getClass().getClassLoader();
        final JButton add        = getButton(loader, "StepBack",    "<",  "Ajouter les éléments sélectionnés");
        final JButton remove     = getButton(loader, "StepForward", ">",  "Retirer les éléments sélectionnés");
        final JButton addAll     = getButton(loader, "Rewind",      "<<", "Ajouter tout");
        final JButton removeAll  = getButton(loader, "FastForward", ">>", "Retirer tout");
        add      .addActionListener(new Action(right, left, false));
        remove   .addActionListener(new Action(left, right, false));
        addAll   .addActionListener(new Action(right, left,  true));
        removeAll.addActionListener(new Action(left, right,  true));
        /*
         * Build UI
         */
        final GridBagConstraints c = new GridBagConstraints();
        c.gridy=0; c.gridwidth=1; c.gridheight=4; c.weightx=c.weighty=1; c.fill=c.BOTH;
        c.gridx=0; add( leftPane,  c);
        c.gridx=2; add(rightPane, c);

        c.insets.left = c.insets.right = 9;
        c.gridx=1; c.gridheight=1; c.weightx=0; c.fill=c.HORIZONTAL;
        c.gridy=0; c.anchor=c.SOUTH;  add(add,       c);
        c.gridy=3; c.anchor=c.NORTH;  add(removeAll, c);
        c.gridy=2; c.weighty=0;       add(addAll,    c);
        c.gridy=1; c.insets.bottom=9; add(remove,    c);
    }

    /**
     * Returns a button.
     *
     * @param loader The class loader for loading the button's image.
     * @param image  The image name to load in the "media" category from the
     *               <A HREF="http://developer.java.sun.com/developer/techDocs/hi/repository/">Swing
     *               graphics repository</A>.
     * @param fallback The fallback to use if the image is not found.
     * @param description a brief description to use for tooltips.
     * @return The button.
     */
    private static JButton getButton(final ClassLoader loader, final String image,
                                     final String fallback, final String description)
    {
        final URL url = loader.getResource("toolbarButtonGraphics/media/"+image+"16.gif");
        final JButton button;
        if (url != null) {
            button = new JButton(new ImageIcon(url, description));
        } else {
            button = new JButton(fallback);
        }
        button.setToolTipText(description);
        return button;
    }

    /**
     * Add all elements from the specified collection into the list on the right side.
     *
     * @param items Items to add.
     */
    public void addElements(final Collection items) {
        ((Model)right.getModel()).addAll(items);
    }

    /**
     * Returns all elements in the list on the left side.
     *
     * @return All elements on the left side.
     */
    public Collection getSelectedElements() {
        final Model model = (Model) left.getModel();
        final Object[] list = new Object[model.getSize()];
        for (int i=0; i<list.length; i++) {
            list[i] = model.getElementAt(i);
        }
        return Arrays.asList(list);
    }

    /**
     * Display this component in a dialog box and wait for the user to press "Ok".
     * This method can be invoked from any thread.
     *
     * @param  owner The owner (may be null).
     * @param  title The title to write in the window bar.
     * @return <code>true</code> if the user pressed "okay", or <code>false</code> otherwise.
     */
    public boolean showDialog(final Component owner, final String title) {
        return SwingUtilities.showOptionDialog(owner, this, title);
    }
}
