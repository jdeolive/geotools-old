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

// User interface
import java.awt.Dimension;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.Icon;

// Input/output
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ImageReaderWriterSpi;

// Manifest
import java.util.jar.Manifest;
import java.util.jar.Attributes;

// Formatting
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// Miscellaneous
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Iterator;

// Java Advanced Imaging
import javax.media.jai.JAI;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * An "About" dialog box. This dialog box contains the application's title and some
 * system informations (Java and OS version, free memory, image readers and writers, running
 * threads, etc.). The application version can be fetched from a {@link Manifest} object,
 * usually build from the <code>META-INF/Manifest.mf</code> file. This manifest should contains
 * entries for <code>Implementation-Title</code>, <code>Implementation-Version</code> and
 * <code>Implementation-Vendor</code> values, as suggested in the
 * <A HREF="http://java.sun.com/docs/books/tutorial/jar/basics/manifest.html#versioning">Java
 * tutorial</A>.
 * In addition to the above-cited standard entries, the <code>About</code> class understand also
 * an optional <code>Implementation-Date</code> entry. This entry can contains the product date
 *  in the <code>"yyyy-MM-dd HH:mm:ss"</code> patter. If presents, this date will be localized
 * according user's locale and appended to the version number.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/About.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id: About.java,v 1.3 2003/11/12 14:14:24 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class About extends JPanel {
    /**
     * The amount of bytes in one "unit of memory" to be displayed.
     */
    private static final float HEAP_SIZE_UNIT = (1024f*1024f);

    /**
     * The entry for timestamp in the manifest file.
     */
    private static final String TIMESTAMP = "Implementation-Date";

    /**
     * Thread qui aura la charge de faire des mises à jour en arrière-plan.
     * Ce champ sera <code>null</code> s'il n'y en a pas.
     */
    private final ThreadList updater;

    /**
     * The localized resources to use.
     */
    private final Resources resources;

    /**
     * Construct a new dialog box with the Geotools's logo.
     */
    public About() {
        this("org/geotools/resources/gui/Geotools.png", About.class, null);
    }

    /**
     * Construct a new dialog box for the specified application class.  This constructor
     * use the class loader for loading the manifest file. It also use the class package
     * to find the right entry into the manifest.
     *
     * @param logo        The application's logo. It may be a {@link JComponent}, an
     *                    {@link Icon} object or an resource path (i.e. a file to be
     *                    fetch in the classpath) as a {@link String}.
     * @param application The application's class. Application name will be fetch
     *                    from the manifest file (<code>META-INF/Manifest.mf</code>).
     * @param tasks       Group of running threads, or <code>null</code> if there is none.
     */
    public About(final Object logo, final Class application, final ThreadGroup tasks) {
        this(logo, getAttributes(application), application.getClassLoader(), tasks);
    }

    /**
     * Construct a new dialog box from the specified manifest attributes.
     *
     * @param logo        The application's logo. It may be a {@link JComponent}, an
     *                    {@link Icon} object or an resource path (i.e. a file to be
     *                    fetch in the classpath) as a {@link String}.
     * @param attributes  The manifest attributes containing application name and version number.
     * @param tasks       Group of running threads, or <code>null</code> if there is none.
     */
    public About(final Object logo, final Attributes attributes, final ThreadGroup tasks) {
        this(logo, attributes, null, tasks);
    }

    /**
     * Construct a new dialog box.
     *
     * @param logo        The application's logo. It may be a {@link JComponent}, an
     *                    {@link Icon} object or an resource path (i.e. a file to be
     *                    fetch in the classpath) as a {@link String}.
     * @param attributes  The manifest attributes containing application name and version number.
     * @param loader      The application's class loader.
     * @param tasks       Group of running threads, or <code>null</code> if there is none.
     */
    private About(final Object logo, final Attributes attributes,
                  ClassLoader loader, final ThreadGroup tasks)
    {
        super(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints();
        gc.weightx=1; gc.weighty=1;
        final Locale locale = getDefaultLocale();
        resources = Resources.getResources(locale);
        if (loader == null) {
            loader = getClass().getClassLoader();
            // TODO: it would be nice to fetch the caller's class loader instead
        }
        /*
         * Get the free memory before any futher work.
         */
        final Runtime    system = Runtime.getRuntime(); system.gc();
        final float  freeMemory = system.freeMemory()  / HEAP_SIZE_UNIT;
        final float totalMemory = system.totalMemory() / HEAP_SIZE_UNIT;
        /*
         * Get application's name, version and vendor from the manifest attributes.
         * If an implementation date is specified, append it to the version string.
         */
        String application = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        String version     = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        String vendor      = attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        try  {
            final String dateString = attributes.getValue(TIMESTAMP);
            if (dateString != null) {
                final Date         date = getDateFormat().parse(dateString);
                final DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
                if (version!=null && version.trim().length()!=0) {
                    StringBuffer buffer = new StringBuffer(version);
                    buffer.append(" (");
                    buffer = format.format(date, buffer, new FieldPosition(0));
                    buffer.append(')');
                    version = buffer.toString();
                }
                else {
                    version = format.format(date);
                }
            }
        } catch (ParseException exception) {
            /*
             * The implementation date can't be parsed. This is not a show-stopper;
             * the "About" dialog box will just not includes the implementation date.
             */
            Utilities.unexpectedException("org.geotools.gui.swing", "About", "<init>", exception);
        }
        /*
         * If the user supplied a logo, load it and display it in the dialog's upper part (NORTH).
         * The tabbed pane will be added below the logo, in the dialog's central part (CENTER).
         */
        if (logo != null) {
            final JComponent title;
            if (logo instanceof JComponent) {
                title = (JComponent) logo;
            } else if (logo instanceof Icon) {
                title = new JLabel((Icon) logo);
            } else {
                final String text = String.valueOf(logo);
                final URL url = loader.getResource(text);
                if (url == null) {
                    final JLabel label = new JLabel(text);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setBorder(BorderFactory.createEmptyBorder(
                                6/*top*/, 6/*left*/, 6/*bottom*/, 6/*right*/));
                    title = label;
                } else {
                    title = new JLabel(new ImageIcon(url));
                }
            }
            title.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(0/*top*/, 0/*left*/, 6/*bottom*/, 0/*right*/),
                            BorderFactory.createCompoundBorder(
                            BorderFactory.createLoweredBevelBorder(), title.getBorder())));
            gc.gridx=0; gc.gridy=0; gc.insets.top=9;
            add(title, gc);
        }
        final JTabbedPane        tabs = new JTabbedPane();
        final JLabel totalMemoryLabel = new JLabel(resources.getString(ResourceKeys.MEMORY_HEAP_SIZE_$1,  new Float(totalMemory)));
        final JLabel percentUsedLabel = new JLabel(resources.getString(ResourceKeys.MEMORY_HEAP_USAGE_$1, new Float(1-freeMemory/totalMemory)));
        gc.gridx=0; gc.gridy=1; gc.fill=gc.BOTH;
        add(tabs, gc);
        /*
         * MAIN TAB (Application name and version informations)
         */
        if (true) {
            final JPanel pane = new JPanel(new GridBagLayout());
            final GridBagConstraints c=new GridBagConstraints();
            c.gridx=0; c.weightx=1;
            c.gridy=0; c.insets.top=12;
            pane.add(new JLabel(application), c);
            c.gridy++; c.insets.top=0;
            pane.add(new JLabel(resources.getString(ResourceKeys.VERSION_$1, version)), c);
            c.gridy++;
            pane.add(new JLabel(vendor), c);
            c.gridy++; c.insets.top=6;
            pane.add(new JLabel(resources.getString(ResourceKeys.JAVA_VERSION_$1,
                                                    System.getProperty("java.version"))), c);
            c.gridy++; c.insets.top=0;
            pane.add(new JLabel(resources.getString(ResourceKeys.JAVA_VENDOR_$1,
                                                    System.getProperty("java.vendor" ))), c);
            c.gridy++; c.insets.top=6;
            pane.add(new JLabel(resources.getString(ResourceKeys.OS_NAME_$1,
                                                    System.getProperty("os.name"))), c);
            c.gridy++; c.insets.top=0;
            pane.add(new JLabel(resources.getString(ResourceKeys.OS_VERSION_$2,
                                                    System.getProperty("os.version"),
                                                    System.getProperty("os.arch"))), c);
            c.gridy++; c.insets.top=12;
            pane.add(new JLabel(resources.getString(ResourceKeys.TILE_CACHE_CAPACITY_$1, new Float(
                 JAI.getDefaultInstance().getTileCache().getMemoryCapacity()/HEAP_SIZE_UNIT))), c);
            c.gridy++; c.insets.top=0;
            pane.add(totalMemoryLabel, c);
            c.gridy++; c.insets.bottom=12;
            pane.add(percentUsedLabel, c);
            tabs.addTab(resources.getString(ResourceKeys.SYSTEM), pane);
        }
        /*
         * RUNNING TASKS TAB
         */
        if (tasks != null) {
            updater = new ThreadList(tasks, totalMemoryLabel, percentUsedLabel, resources);
            final JPanel pane = new JPanel(new BorderLayout());
            final JList  list = new JList(updater);
            pane.add(new JLabel(resources.getString(ResourceKeys.RUNNING_TASKS)), BorderLayout.NORTH);
            pane.add(new JScrollPane(list), BorderLayout.CENTER);
            pane.setBorder(BorderFactory.createEmptyBorder(9,9,9,9));
            tabs.addTab(resources.getString(ResourceKeys.TASKS), pane);
        } else {
            updater = null;
        }
        /*
         * IMAGE ENCODERS/DECODERS TAB
         */
        if (true) {
            final StringBuffer buffer = new StringBuffer();
            final Map mimes = new TreeMap();
            boolean writer = false;
            do {
                final int   titleKey;
                final Class category;
                if (writer) {
                    titleKey = ResourceKeys.ENCODERS;
                    category = ImageWriterSpi.class;
                } else {
                    titleKey = ResourceKeys.DECODERS;
                    category = ImageReaderSpi.class;
                }
                String title = resources.getString(titleKey);
                Iterator  it = IIORegistry.getDefaultInstance().getServiceProviders(category, true);
                while (it.hasNext()) {
                    final ImageReaderWriterSpi spi = (ImageReaderWriterSpi) it.next();
                    final String  name = spi.getDescription(locale);
                    final String[] mimeTypes = spi.getMIMETypes();
                    patchMimes(mimeTypes);
                    for (int i=0; i<mimeTypes.length; i++) {
                        final String mimeType = mimeTypes[i];
                        DefaultMutableTreeNode child = (DefaultMutableTreeNode)mimes.get(mimeType);
                        if (child == null) {
                            child = new DefaultMutableTreeNode(mimeType);
                            mimes.put(mimeType, child);
                        }
                        child.add(new DefaultMutableTreeNode(name, false));
                    }
                    if (title!=null && mimeTypes.length!=0) {
                        if (buffer.length() != 0) {
                            buffer.append(" / ");
                        }
                        buffer.append(title);
                        title = null;
                    }
                }
            } while ((writer = !writer));
            final String title = buffer.toString();
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);
            for (final Iterator it=mimes.values().iterator(); it.hasNext();) {
                root.add((DefaultMutableTreeNode) it.next());
            }
            JComponent tree = new JTree(root);
            tree.setBorder(BorderFactory.createEmptyBorder(6,6,0,0));
            tree = new JScrollPane(tree);
            tabs.addTab(resources.getString(ResourceKeys.IMAGES), setup(tree));
        }
        /*
         * JAI OPERATIONS TAB
         */
        if (true) {
            final JComponent tree = new RegisteredOperationBrowser();
            tabs.addTab(resources.getString(ResourceKeys.OPERATIONS), setup(tree));
        }
    }

    /**
     * Setup the border for the specified component.
     */
    private static JComponent setup(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(3,3,3,3), component.getBorder()));
        component.setPreferredSize(new Dimension(200, 200));
        return component;
    }

    /**
     * Patch the mime type, replacing "" by "(raw)" for JAI I/O codec.
     */
    private static void patchMimes(String[] mimes) {
        for (int i=0; i<mimes.length; i++) {
            String name = mimes[i].trim();
            if (name.length() == 0) {
                name = "(raw)";
            }
            mimes[i] = name;
        }
    }

    /**
     * Returns attribute for the specified class.
     */
    private static Attributes getAttributes(final Class classe) {
        InputStream stream = classe.getClassLoader().getResourceAsStream("META-INF/Manifest.mf");
        if (stream != null) try {
            final Manifest manifest = new Manifest(stream);
            stream.close();
            String name = classe.getName().replace('.','/');
            int index;
            while ((index=name.lastIndexOf('/'))>=0) {
                final Attributes attributes = manifest.getAttributes(name.substring(0, index+1));
                if (attributes!=null) return attributes;
                name = name.substring(0, index);
            }
            return manifest.getMainAttributes();
        } catch (IOException exception) {
            Utilities.unexpectedException("org.geotools.gui.swing",
                                          "About", "getAttributes", exception);
        }
        // Use empty manifest attributes.
        return new Attributes();
    }

    /**
     * Returns a neutral date format for timestamp.
     */
    private static DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
    }

    /**
     * Modèle représentant la liste des processus actif dans un {@link ThreadGroup}.
     * Cette liste se mettre automatiquement à jour de façon périodique.
     *
     * @version $Id: About.java,v 1.3 2003/11/12 14:14:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class ThreadList extends AbstractListModel implements Runnable {
        /**
         * Processus qui met à jour <code>ThreadList</code>,
         * ou <code>null</code> s'il n'y en a pas. On peut
         * tuer le processus actif en donnant la valeur
         * <code>null</code> à cette variable.
         */
        public transient Thread worker;

        /**
         * Liste des processus en cours.
         */
        private final ThreadGroup tasks;

        /**
         * Liste des noms des processus en cours. Cette
         * liste sera mises à jour périodiquement.
         */
        private String[] names=new String[0];

        /**
         * Texte dans lequel écrire la mémoire totale réservée.
         */
        private final JLabel totalMemory;

        /**
         * Texte dans lequel écrire le pourcentage de mémoire utilisée.
         */
        private final JLabel percentUsed;

        /**
         * The localized resources to use.
         */
        private final Resources resources;

        /**
         * Construit une liste des processus actifs
         * dans le groupe <code>tasks</code> spécifié.
         */
        public ThreadList(final ThreadGroup tasks, final JLabel totalMemory,
                          final JLabel percentUsed, final Resources resources)
        {
            this.tasks       = tasks;
            this.totalMemory = totalMemory;
            this.percentUsed = percentUsed;
            this.resources   = resources;
        }

        /**
         * Retourne le nombre d'éléments dans la liste.
         */
        public int getSize() { // NO synchronized here
            return names.length;
        }

        /**
         * Retourne un des éléments de la liste.
         */
        public Object getElementAt(final int index) { // NO synchronized here
            return names[index];
        }

        /**
         * Ajoute un objet à la liste des objets intéressés
         * à être informé des changements apportés à la liste.
         */
        public synchronized void addListDataListener(final ListDataListener listener) {
            super.addListDataListener(listener);
        }

        /**
         * Démarre le thread.
         */
        public synchronized void start() {
            if (worker == null) {
                worker=new Thread(this, Resources.format(ResourceKeys.ABOUT));
                worker.setPriority(Thread.MIN_PRIORITY);
                worker.setDaemon(true);
                worker.start();
            }
        }

        /**
         * Met à jour le contenu de la liste à interval régulier.
         * Cette méthode est exécutée dans une boucle jusqu'à ce
         * qu'elle soit interrompue en donnant la valeur nulle à
         * {@link #tasks}.
         */
        public synchronized void run() {
            String oldTotalMemory = null;
            String oldPercentUsed = null;
            while (worker==Thread.currentThread() && listenerList.getListenerCount()!=0) {
                final Runtime     system = Runtime.getRuntime();
                final float  freeMemoryN = system.freeMemory()  / HEAP_SIZE_UNIT;
                final float totalMemoryN = system.totalMemory() / HEAP_SIZE_UNIT;
                String   totalMemoryText = resources.getString(ResourceKeys.MEMORY_HEAP_SIZE_$1,
                                                            new Float(totalMemoryN));
                String   percentUsedText = resources.getString(ResourceKeys.MEMORY_HEAP_USAGE_$1,
                                                            new Float(1-freeMemoryN/totalMemoryN));

                Thread[] threadArray = new Thread[tasks.activeCount()];
                String[] threadNames = new String[tasks.enumerate(threadArray)];
                int c=0; for (int i=0; i<threadNames.length; i++) {
                    if (threadArray[i]!=worker) {
                        threadNames[c++]=threadArray[i].getName();
                    }
                }
                threadNames = (String[])XArray.resize(threadNames, c);

                if (Arrays.equals(names, threadNames)) {
                    threadNames = null;
                }
                if (totalMemoryText.equals(oldTotalMemory)) {
                    totalMemoryText=null;
                } else {
                    oldTotalMemory=totalMemoryText;
                }
                if (percentUsedText.equals(oldPercentUsed)) {
                    percentUsedText = null;
                } else {
                    oldPercentUsed = percentUsedText;
                }
                if (threadNames!=null || totalMemoryText!=null || percentUsedText!=null) {
                    final String[]     names = threadNames;
                    final String totalMemory = totalMemoryText;
                    final String percentUsed = percentUsedText;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            update(names, totalMemory, percentUsed);
                        }
                    });
                }
                try {
                    wait(4000);
                } catch (InterruptedException exception) {
                    // Quelqu'un a réveillé ce thread. Retourne au travail.
                }
            }
            worker = null;
        }

        /**
         * Met à jour le contenu de la liste. Cette méthode
         * est appelée périodiquement dans le thread de Swing.
         */
        private synchronized void update(final String[] newNames,
                                         final String totalMemory,
                                         final String percentUsed)
        {
            if (newNames != null) {
                final int count = Math.max(names.length, newNames.length);
                names = newNames;
                fireContentsChanged(this, 0, count-1);
            }
            if (totalMemory!=null) this.totalMemory.setText(totalMemory);
            if (percentUsed!=null) this.percentUsed.setText(percentUsed);
        }
    }

    /**
     * Popups the dialog box and wait for the user. This method
     * always invoke {@link #start} before showing the dialog,
     * and {@link #stop} after disposing it.
     */
    public void showDialog(final Component owner) {
        try {
            start();
            SwingUtilities.showMessageDialog(owner, this,
                           resources.getMenuLabel(ResourceKeys.ABOUT), JOptionPane.PLAIN_MESSAGE);
        } finally {
            stop();
        }
    }

    /**
     * Start a daemon thread updating dialog box information. Updated information include
     * available memory and the list of running tasks. <strong>You <u>must</u> invoke the
     * {@link #stop} method after <code>start()</code></strong> (typically in a <code>try..finally</code>
     * construct) in order to free resources. <code>stop()</code> is not automatically
     * invoked by the garbage collector.
     */
    protected void start() {
        final ThreadList updater = this.updater;
        if (updater != null) {
            updater.start();
        }
    }

    /**
     * Free any resources used by this dialog box.  <strong>This method must be invoked
     * after {@link #start}</strong> in order to free resources. <code>stop()</code> is
     * not automatically invoked by the garbage collector.
     */
    protected void stop() {
        final ThreadList updater = this.updater;
        if (updater != null) {
            updater.worker = null; // Stop the thread.
        }
        // Le thread avait une référence indirecte vers 'this' via 'ListDataListener'
    }

    /**
     * Convenience method for setting the <code>Implementation-Date</code>
     * attributes to the current date.
     *
     * @param attributes Attributes in which setting the compilation date.
     */
    public static void touch(final Attributes attributes) {
        attributes.putValue(TIMESTAMP, getDateFormat().format(new Date()));
    }

    /**
     * Display the default "About" dialog box. This method is usefull
     * for testing the widget appareance and for checking system informations.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        new About().showDialog(null);
        System.exit(0);
    }
}
