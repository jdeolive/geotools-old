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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gui.swing;

// J2SE dependencies
import java.text.Format;
import java.util.Arrays;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.IllegalComponentStateException;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Image I/O
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;

// Geotools dependencies
import org.geotools.pt.CoordinatePoint;
import org.geotools.renderer.j2d.GeoMouseEvent;
import org.geotools.renderer.j2d.MouseCoordinateFormat;
import org.geotools.resources.XArray;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * A status bar. This status bar contains three parts: 1) an arbitrary message
 * 2) A progress bar and 3) The mouse coordinates.   The mouse coordinates are
 * automatically filled if this component is registered into a {@link MapPane}
 * as below:
 *
 * <blockquote><pre>
 * mapPane.addMouseMotionListener(statusBar);
 * </pre></blockquote>
 *
 * Coordinates can be formatted in any coordinate system, as long as a transform
 * exists from the {@linkplain MapPane#getCoordinateSystem map pane's coordinate
 * system} and the {@linkplain MouseCoordinateFormat#getCoordinateSystem format's
 * coordinate system}. If no transformation path is found, the coordinate will be
 * formatted as "ERROR". The status bar CS can be set to matches the map pane CS
 * as below:
 *
 * <blockquote><pre>
 * {@link #getCoordinateFormat}.setCoordinateSystem(mapPane.getCoordinateSystem());
 * </pre></blockquote>
 *
 * @version $Id: StatusBar.java,v 1.7 2003/11/12 14:14:25 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class StatusBar extends JComponent implements MouseMotionListener {
    /**
     * Chaîne de caractères représentant un texte nul. Ce sera en général un
     * espace afin que l'étiquette conserve quand même une certaine hauteur.
     */
    private static final String NULL = " ";

    /**
     * Texte à afficher dans la barre d'état lorsqu'aucune opération n'est en cours.
     * S'il n'y a pas de texte à afficher, alors cette chaîne devrait être la constante
     * <code>StatusBar.NULL</code> plutôt que <code>null</code>.
     */
    private String text = NULL;

    /**
     * Composante dans lequel écrire des messages.
     */
    private final JLabel message = new JLabel(NULL);

    /**
     * Composante dans lequel écrire les coordonnées
     * pointées par le curseur de la souris.
     */
    private final JLabel coordinate = new JLabel(NULL);

    /**
     * The object to use for formatting coordinates.
     * Will be created only when first needed.
     */
    private MouseCoordinateFormat format;

    /**
     * The contextual menu for the "coordinate" area of status bar.
     * Will be created only when first needed.
     */
    private transient JPopupMenu coordinateMenu;

    /**
     * Progression d'une opération quelconque. Ce sera
     * souvent la progression de la lecture d'une image.
     */
    private final BoundedRangeModel progress;

    /**
     * Liste de numéros (<strong>en ordre croissant</code>) identifiant les objets
     * qui veulent écrire leur progression dans la barre des progrès. Chaque objet
     * {@link ProgressListener} a un numéro unique.  Le premier numéro de la liste
     * est celui de l'objet {@link ProgressListener} qui possède la barre des progrès.
     * On ne retient pas des références directes afin de ne pas nuire au travail du
     * ramasse-miettes.
     */
    private transient int[] progressQueue = new int[0]; // must be transient

    /**
     * Listen for {@link MouseListener#mouseExited} event. This is used
     * in order to erase the coordinates when the mouse exit the map pane.
     */
    private final MouseListener listener = new MouseAdapter() {
        public void mouseExited(final MouseEvent e) {
            setCoordinate(null);
        }
        public void mousePressed(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                trigPopup(event);
            }
        }
        public void mouseReleased(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                trigPopup(event);
            }
        }
    };

    /**
     * Construct a new status bar.
     */
    public StatusBar() {
        setLayout(new GridBagLayout());
        final JProgressBar progress = new JProgressBar();
        final GridBagConstraints c = new GridBagConstraints();

        c.gridy=0; c.fill=c.BOTH;
        c.gridx=0; c.weightx=1; add(message,    c);
        c.gridx=1; c.weightx=0; add(progress,   c);
        c.gridx=2;              add(coordinate, c);

        config(message);
        config(coordinate);
        final Dimension size = coordinate.getPreferredSize();
        size.width=250; coordinate.setPreferredSize(size);
        progress.setBorder(BorderFactory.createLoweredBevelBorder());
        this.progress = progress.getModel();
        coordinate.addMouseListener(listener);
    }

    /**
     * Construct a new status bar and register listeners.
     *
     * @param mapPane The map pane (usually a {@link MapPane} instance).
     */
    public StatusBar(final Component mapPane) {
        this();
        mapPane.addMouseListener(listener);
        mapPane.addMouseMotionListener(this);
    }

    /**
     * Configure la zone de texte spécifiée.
     */
    private static void config(final JLabel label)  {
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(0/*top*/, 6/*left*/, 0/*bottom*/,0/*right*/)));
    }

    /**
     * Registers a map pane for status bar management.  This will register listeners to track mouse
     * motion events occuring in the map pane area. Mouse locations will be formatted as geographic
     * coordinates according the current {@linkplain #getCoordinateFormat formatter} and {@linkplain
     * #setCoordinate written} in the status's bar coordinate area.
     *
     * @param mapPane The map pane (usually a {@link MapPane} instance).
     */
    public void registerMapPane(final Component mapPane) {
        mapPane.removeMouseMotionListener(this);
        mapPane.removeMouseListener(listener);
        mapPane.addMouseListener(listener);
        mapPane.addMouseMotionListener(this);
    }

    /**
     * Removes a map pane from status bar control.
     *
     * @param mapPane The map pane previously given to {@link #registerMapPane}.
     */
    public void unregisterMapPane(final Component mapPane) {
        mapPane.removeMouseMotionListener(this);
        mapPane.removeMouseListener(listener);
    }

    /**
     * Returns the text to display in the status bar. This is the text visible in the
     * message area, at the left. A <code>null</code> values means that no message are
     * currently displayed.
     */
    public String getText() {
        return (NULL.equals(text)) ? null : text;
    }

    /**
     * Set the text to display in the status bar. This is the
     * text to show in the message area, at the left side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     */
    public void setText(String text) {
        final String old = message.getText();
        if (text==null || text.length()==0) {
            text = NULL;
        }
        message.setText(this.text=text);
        firePropertyChange("text", old, text);
    }

    /**
     * Set the coordinate text to display in the status bar.
     * This is the text to show in the coordinate area, at the right side.
     *
     * @param text The coordinate to display, or <code>null</code> if none.
     *
     * @task REVISIT: This method doesn't fire a 'PropertyChangeEvent' for
     *       performance reason (this method is invoked very often). Should we?
     */
    public void setCoordinate(String text) {
        if (text==null || text.length()==0) {
            text = NULL;
        }
        coordinate.setText(text);
        // Do not fire event for performance reason. Should we?
    }

    /**
     * Returns the format to use for formatting coordinates. The output coordinate
     * system can be se with <code>getCoordinateFormat().setCoordinateSystem(...)</code>.
     *
     * @see #setCoordinate(String)
     * @see MouseCoordinateFormat#setCoordinateSystem
     */
    public MouseCoordinateFormat getCoordinateFormat() {
        if (format == null) try {
            format = new MouseCoordinateFormat(getLocale());
        } catch (IllegalComponentStateException exception) {
            // The component doesn't have a parent.
            // Construct a format using the default locale.
            format = new MouseCoordinateFormat();
        }
        return format;
    }

    /**
     * Set the format to use for formatting coordinates.
     * A null value reset the default format.
     */
    public void setCoordinateFormat(final MouseCoordinateFormat format) {
        final MouseCoordinateFormat old = this.format;
        this.format = format;
        firePropertyChange("coordinateFormat", old, format);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component. The default
     * implementation format the coordinate in the status bar coordinate area (at
     * the right side).
     */
    public void mouseMoved(final MouseEvent event) {
        if (event instanceof GeoMouseEvent) {
            if (format == null) {
                format = getCoordinateFormat();
            }
            setCoordinate(format.format((GeoMouseEvent) event));
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged. The default
     * implementation invokes <code>{@link #mouseMoved mouseMoved}(event)}</code> in order
     * to continue to format mouse's coordinate during the drag.
     */
    public void mouseDragged(final MouseEvent event) {
        mouseMoved(event);
    }

    /**
     * Invoked when a contextual menu has been trigged on a component.
     *
     * @param component The component on which the popup menu was trigged.
     */
    final void trigPopup(final MouseEvent event) {
        final Component component = event.getComponent();
        if (component == coordinate) {
            if (coordinateMenu == null) {
                coordinateMenu = new JPopupMenu();
                final MenuListener listener = new MenuListener();
                final Resources resources = Resources.getResources(getLocale());
                JMenuItem item = coordinateMenu.add(resources.getMenuLabel(ResourceKeys.FORMAT));
                item.addActionListener(listener);
                addPropertyChangeListener("coordinateFormat", listener);
            }
            coordinateMenu.show(component, event.getX(), event.getY());
        }
    }

    /**
     * Listeners for the popup menus.
     */
    private final class MenuListener implements ActionListener, PropertyChangeListener {
        /**
         * The format chooser. Will be constructed only when first needed.
         */
        private transient FormatChooser chooser;

        /**
         * Invoked when the user select the "Format" menu item.
         */
        public void actionPerformed(final ActionEvent event) {
            if (chooser == null) {
                chooser = new FormatChooser(getCoordinateFormat());
            }
            final Resources resources = Resources.getResources(getLocale());
            if (chooser.showDialog(StatusBar.this,
                                   resources.getString(ResourceKeys.COORDINATE_FORMAT)))
            {
                setCoordinateFormat((MouseCoordinateFormat) chooser.getFormat());
            }
        }

        /**
         * Invoked when a status bar property changed.
         */
        public void propertyChange(final PropertyChangeEvent event) {
            if (chooser != null) {
                chooser.setFormat(getCoordinateFormat());
            }
        }
    }

    /**
     * Returns a image I/O progress listener. This object can be used for updating the progress
     * bare in this status bar. This method can be invoked from any thread, and the progress
     * listener's methods can be invoked from any thread too (it doesn't need to be the
     * <i>Swing</i> thread).
     *
     * @param name The name of the image to be loaded. This name will appears
     *             in the status bar when the loading will start.
     */
    public IIOReadProgressListener getIIOReadProgressListener(final String name) {
        return new ProgressListener(Resources.getResources(getLocale()).
                                    getString(ResourceKeys.LOADING_$1, name));
    }




    /**
     * Classe chargée de réagir au progrès de la lecture.
     *
     * @version $Id: StatusBar.java,v 1.7 2003/11/12 14:14:25 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private final class ProgressListener implements IIOReadProgressListener, Runnable {
        /** No operation     */ private static final byte   NOP      = 0;
        /** Start loading    */ private static final byte   START    = 1;
        /** Loading progress */ private static final byte   PROGRESS = 2;
        /** End loading      */ private static final byte   END      = 3;

        /** Id number for {@link StatusBar#progressQueue}. */
        private int ID;

        /** Image name. */
        private final String name;
        
        /** Image name. */
        private String toWrite;

        /** Percent done. */
        private int percent;

        /** Operation to apply (@link #NOP}, {@link #START}, {@link #PROGRESS}, {@link #END}). */
        private byte operation = NOP;

        /** Ignored */ public void thumbnailStarted (ImageReader source, int im, int th) {}
        /** Ignored */ public void thumbnailProgress(ImageReader source, float percent ) {}
        /** Ignored */ public void thumbnailComplete(ImageReader source                ) {}
        /** Ignored */ public void sequenceStarted  (ImageReader source, int minIndex  ) {}
        /** Ignored */ public void sequenceComplete (ImageReader source                ) {}

        /** Setup the progress bar/ */
        public void imageStarted(ImageReader source, int imageIndex) {
            invokeLater(START, 0);
        }

        /** Update the progress bar. */
        public void imageProgress(ImageReader source, float percent) {
            invokeLater(PROGRESS, (int)percent);
        }

        /** Hide the progress bar. */
        public void imageComplete(ImageReader source) {
            invokeLater(END, 100);
        }

        /** Hide the progress bar. */
        public void readAborted(ImageReader source) {
            invokeLater(END, 0);
        }

        /**
         * Construit un objet chargé d'informer des progrès de la lecture d'une image.
         */
        protected ProgressListener(final String name) {
            this.name = name;
            toWrite   = name;
        }
        
        /**
         * Prépare une opération à exécuter dans le thread de <i>Swing</i>.
         * Cette opération sera décrite par le champ {@link #operation} et
         * consistera typiquement à initialiser la barre des progrès ou
         * afficher son pourcentage ({@link #percent}).
         *
         * @param nextOp  Code de l'opération ({@link #START}, {@link #PROGRESS} ou {@link #END}).
         * @param percent Pourcentage des progrès accomplis.
         */
        private void invokeLater(final byte nextOp, final int percent) {
            synchronized (progress) {
                final byte currentOp = this.operation;
                if (this.percent!=percent || currentOp!=nextOp) {
                    this.percent = percent;
                    switch (currentOp) {
                        case START: {
                            if (nextOp == END) {
                                this.operation = NOP;
                            }
                            // Sinon, on continue avec 'START'.
                            break;
                        }
                        case NOP: {
                            EventQueue.invokeLater(this);
                            // fall through
                        }
                        case PROGRESS: {
                            this.operation = nextOp;
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Exécute une opération préparée par {@link #invokeLater}. Cette opération peut
         * constiter à initialiser la barre des progrès ({@link #START}), informer des
         * progrès accomplis ({@link #PROGRESS}) ou informer que la tâche est terminée
         * ({@link #END}). Cette méthode doit obligatoirement être appelée dans le thread
         * de <i>Swing</i>.
         */
        public void run() {
            synchronized (progress) {
                try {
                    switch (operation) {
                        /*
                         * Si on démarre la lecture d'une nouvelle image, tente de
                         * prendre possession de la barre d'état.  Si on n'est pas
                         * le premier à demander la possession de la barre d'état,
                         * cet objet 'ProgressListener' sera placé dans une liste
                         * d'attente.
                         */
                        case START: {
                            toWrite = name;
                            if (lock()) {
                                flush();
                                progress.setRangeProperties(percent, 1, 0, 100, false);
                            }
                            break;
                        }
                        /*
                         * Si la lecture de l'image a avancé, on écrira les progrès dans la barre d'état
                         * à la condition que cette barre d'état nous appartient. On écrira le nom de
                         * l'opération si ce n'était pas déjà fait (c'est le cas si on n'avait pas pu
                         * prendre possession de la barre d'état au moment ou START avait été exécuté).
                         */
                        case PROGRESS: {
                            if (hasLock()) {
                                flush();
                                progress.setValue(percent);
                            }
                            break;
                        }
                        /*
                         * A la fin de la lecture, relâche la barre d'état. Elle
                         * pourra être récupérée par d'autres 'ProgressListener'
                         * qui étaient dans la liste d'attente.
                         */
                        case END: {
                            if (hasLock()) {
                                progress.setRangeProperties(0, 1, 0, 100, false);
                                message.setText(text);
                            }
                            unlock();
                            break;
                        }
                    }
                } catch (RuntimeException exception) {
                    ExceptionMonitor.show(StatusBar.this, exception);
                } finally {
                    operation = NOP;
                }
            }
        }

        /**
         * Ecrit dans la barre d'état la description de cet objet <code>ProgressListener</code>, si
         * ce n'était pas déjà fait.  Cette méthode ne doit être appelée que lorsque les conditions
         * suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans le thread de Swing.</li>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         *   <li>La méthode {@link #lock} ou {@link #hasLock} a retourné <code>true</code>.</li>
         * </ul>
         */
        private void flush() {
            assert Thread.holdsLock(progress);
            assert EventQueue.isDispatchThread();
            if (toWrite != null) {
                message.setText(toWrite);
                toWrite = null;
            }
        }

        /**
         * Vérifie si cet objet <code>ProgressBar</code> possède la barre d'état. Cette
         * méthode ne doit être appelée que lorsque les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private boolean hasLock() {
            assert Thread.holdsLock(progress);
            final int[] progressQueue = StatusBar.this.progressQueue;
            return (progressQueue.length>=1 && progressQueue[0]==ID);
        }

        /**
         * tente de prendre possession de la barre d'état. Cette méthode retourne <code>true</code>
         * si elle a effectivement réussie à en prendre possession, ou <code>false</code> si elle
         * s'est placée dans une liste d'attente. Cette méthode ne doit être appelée que lorsque
         * les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private boolean lock() {
            assert Thread.holdsLock(progress);
            final int index = Arrays.binarySearch(progressQueue, ID);
            if (index >= 0) {
                return index == 0;
            }
            final int length = progressQueue.length;
            if (length != 0) {
                ID = progressQueue[length-1]+1;
                if (ID <= 0) {
                    return false; // Too many ProgressListener
                }
                progressQueue = XArray.resize(progressQueue, length+1);
                progressQueue[length]=ID;
                return false;
            } else {
                progressQueue = new int[] {ID=1};
                return true;
            }
        }

        /**
         * Déclare que cet objet <code>ProgressBar</code> n'est plus intéressé
         * a posséder la barre d'état. Cette méthode ne doit être appelée que
         * lorsque les conditions suivantes ont été remplises:
         *
         * <ul>
         *   <li>Cette méthode est appelée dans un bloc synchronisé sur
         *       <code>StatusBar.progress</code>.</li>
         * </ul>
         */
        private void unlock() {
            assert Thread.holdsLock(progress);
            final int index = Arrays.binarySearch(progressQueue, ID);
            if (index >= 0) {
                progressQueue = XArray.remove(progressQueue, index, 1);
            }
            ID=0;
        }

        /**
         * Déclare que cet objet <code>ProgressListener</code>
         * n'est plus intéressé a posséder la barre d'état.
         */
        protected void finalize() throws Throwable {
            synchronized (progress) {
                unlock();
            }
            super.finalize();
        }
    }
}
