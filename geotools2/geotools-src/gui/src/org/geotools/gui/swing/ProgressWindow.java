/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JLayeredPane;
import javax.swing.JDesktopPane;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.BoundedRangeModel;
import java.lang.reflect.InvocationTargetException;

// Geotools dependencies
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * Informe l'utilisateur des progrès d'une opération à l'aide de messages dans une fenêtre.
 * Cette classe peut aussi écrire des avertissements, ce qui est utile entre autre lors de la
 * lecture d'un fichier de données durant laquelle on veut signaler les erreurs mais sans arrêter
 * la lecture pour autant.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/ProgressWindow.png"></p>
 * <p>&nbsp;</p>
 *
 * <p>Cette classe est conçue pour fonctionner correctement même si ses méthodes sont appellées
 * dans un autre thread que celui de <i>Swing</i>. Il est donc possible de faire la longue
 * opération en arrière plan et d'appeller les méthodes de cette classe sans se soucier des
 * problèmes de synchronisation. En général, faire l'opération en arrière plan est recommandé
 * afin de permettre le rafraichissement de l'écran par <i>Swing</i>.</p>
 *
 * @version $Id: ProgressWindow.java,v 1.3 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ProgressWindow implements ProgressListener {
    /**
     * Largeur initiale de la fenêtre des progrès, en pixels.
     */
    private static final int WIDTH = 360;

    /**
     * Hauteur initiale de la fenêtre des progrès, en pixels.
     */
    private static final int HEIGHT = 120;

    /**
     * Hauteur de la zone de texte qui contiendra des messages d'avertissements.
     */
    private static final int WARNING_HEIGHT = 120;

    /**
     * Largeur de la marge horizontale, en pixels.
     */
    private static final int HMARGIN = 12;

    /**
     * Largeur de la marge verticale, en pixels.
     */
    private static final int VMARGIN = 9;

    /**
     * Nombre d'espaces à placer dans la marge de
     * la fenêtre contenant les messages d'erreurs.
     */
    private static final int WARNING_MARGIN = 8;

    /**
     * Fenêtre affichant les progrès de la longue opération.
     * Il peut s'agir notamment d'un objet {@link JDialog} ou
     * d'un objet {@link JInternalFrame}, dépendamment de la
     * composante parente.
     */
    private final Component window;

    /**
     * Conteneur dans lequel insérer les éléments tels que
     * la barre des progrès. Ca peut être le même objet que
     * {@link #window}, mais pas nécessairement.
     */
    private final JComponent content;

    /**
     * Barre des progrès. La plage de cette barre doit
     * obligatoirement aller au moins de 0 à 100.
     */
    private final JProgressBar progressBar;

    /**
     * Description de l'opération en cours. Des exemples de descriptions
     * seraient "Lecture de l'en-tête" ou "Lecture des données".
     */
    private final JLabel description;

    /**
     * Région dans laquelle afficher les messages d'avertissements.
     * Cet objet doit être de la classe {@link JTextArea}. il ne sera
     * toutefois construit que si des erreurs surviennent effectivement.
     */
    private JComponent warningArea;

    /**
     * Source du dernier message d'avertissement. Cette information est
     * conservée afin d'éviter de répéter la source lors d'éventuels
     * autres messages d'avertissements.
     */
    private String lastSource;
    
    /**
     * Construit une fenêtre qui informera des progrès d'une opération.
     * La fenêtre n'apparaîtra pas imédiatement. Elle n'apparaîtra que
     * lorsque la méthode {@link #started} sera appelée.
     *
     * @param parent Composante parente. La fenêtre des progrès sera
     *        construite dans le même cadre que cette composante. Ce
     *        paramètre peut être nul s'il n'y a pas de parent.
     */
    public ProgressWindow(final Component parent) {
        /*
         * Création de la fenêtre qui contiendra
         * les composantes affichant le progrès.
         */
        Dimension       parentSize;
        final Resources  resources = Resources.getResources(parent!=null ? parent.getLocale() : null);
        final String         title = resources.getString(ResourceKeys.PROGRESSION);
        final JDesktopPane desktop = JOptionPane.getDesktopPaneForComponent(parent);
        if (desktop != null) {
            final JInternalFrame frame = new JInternalFrame(title);
            window                     = frame;
            content                    = new JPanel();
            parentSize                 = desktop.getSize();
            frame.setContentPane(content); // Pour avoir un fond opaque
            frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
            desktop.add(frame, JLayeredPane.PALETTE_LAYER);
        } else {
            final Frame frame    = JOptionPane.getFrameForComponent(parent);
            final JDialog dialog = new JDialog(frame, title);
            window               = dialog;
            content              = (JComponent) dialog.getContentPane();
            parentSize           = frame.getSize();
            if (parentSize.width==0 || parentSize.height==0) {
                parentSize=Toolkit.getDefaultToolkit().getScreenSize();
            }
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setResizable(false);
        }
        window.setBounds((parentSize.width-WIDTH)/2, (parentSize.height-HEIGHT)/2, WIDTH, HEIGHT);
        /*
         * Création de l'étiquette qui décrira l'opération
         * en cours. Au départ, aucun texte ne sera placé
         * dans cette étiquette.
         */
        description = new JLabel();
        description.setHorizontalAlignment(JLabel.CENTER);
        /*
         * Procède à la création de la barre des progrès.
         * Le modèle de cette barre sera retenu pour être
         */
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createEmptyBorder(6,9,6,9),
                              progressBar.getBorder()));
        /*
         * Dispose les éléments à l'intérieur de la fenêtre.
         * On leur donnera une bordure vide pour laisser un
         * peu d'espace entre eux et les bords de la fenêtre.
         */
        content.setLayout(new GridLayout(2,1));
        content.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createEmptyBorder(VMARGIN,HMARGIN,VMARGIN,HMARGIN),
                          BorderFactory.createEtchedBorder()));
        content.add(description);
        content.add(progressBar);
    }

    /**
     * Returns a localized string for the specified key.
     */
    private String getString(final int key) {
        return Resources.getResources(window.getLocale()).getString(key);
    }

    /**
     * Retourne le titre de la fenêtre. Il s'agira en général
     * du titre de la boîte de dialogue. Par défaut, ce titre
     * sera "Progression" dans la langue de l'utilisateur.
     */
    public String getTitle() {
        return get(Caller.TITLE);
    }

    /**
     * Définit le titre de la fenêtre des progrès. Un argument
     * nul rétablira le titre par défaut de la fenêtre.
     */
    public void setTitle(final String name) {
        set(Caller.TITLE, (name!=null) ? name : getString(ResourceKeys.PROGRESSION));
    }

    /**
     * Retourne le message d'écrivant l'opération
     * en cours. Si aucun message n'a été définie,
     * retourne <code>null</code>.
     */
    public String getDescription() {
        return get(Caller.LABEL);
    }

    /**
     * Spécifie un message qui décrit l'opération en cours.
     * Ce message est typiquement spécifiée avant le début
     * de l'opération. Toutefois, cette méthode peut aussi
     * être appelée à tout moment pendant l'opération sans
     * que cela affecte le pourcentage accompli. La valeur
     * <code>null</code> signifie qu'on ne souhaite plus
     * afficher de description.
     */
    public void setDescription(final String description) {
        set(Caller.LABEL, description);
    }

    /**
     * Indique que l'opération a commencée. L'appel de cette
     * méthode provoque l'apparition de la fenêtre si elle
     * n'était pas déjà visible.
     */
    public void started() {
        call(Caller.STARTED);
    }

    /**
     * Indique l'état d'avancement de l'opération. Le progrès est représenté par un
     * pourcentage variant de 0 à 100 inclusivement. Si la valeur spécifiée est en
     * dehors de ces limites, elle sera automatiquement ramenée entre 0 et 100.
     */
    public void progress(final float percent) {
        int p=(int) percent; // round toward 0
        if (p<  0) p=  0;
        if (p>100) p=100;
        set(Caller.PROGRESS, p);
    }

    /**
     * Indique que l'opération est terminée. L'indicateur visuel informant des
     * progrès disparaîtra, sauf si des messages d'erreurs ou d'avertissements
     * ont été affichés.
     */
    public void complete() {
        call(Caller.COMPLETE);
    }

    /**
     * Libère les ressources utilisées par l'état d'avancement. Si l'état
     * d'avancement était affichée dans une fenêtre, cette fenêtre peut être
     * détruite.
     */
    public void dispose() {
        call(Caller.DISPOSE);
    }

    /**
     * Écrit un message d'avertissement. Les messages apparaîtront dans
     * une zone de texte sous la barre des progrès. Cette zone de texte
     * ne deviendra visible qu'après l'écriture d'au moins un message.
     *
     * @param source Chaîne de caractère décrivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a été détectée. Peut être nul si la source n'est pas connue.
     * @param margin Texte à placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du numéro
     *        de ligne où s'est produite l'erreur dans le fichier <code>source</code>. Ce
     *        texte sera automatiquement placé entre parenthèses.
     * @param warning Message d'avertissement à écrire.
     */
    public synchronized void warningOccurred(final String source, String margin,
                                             final String warning)
    {
        final StringBuffer buffer = new StringBuffer(warning.length()+16);
        if (source != lastSource) {
            lastSource = source;
            if (warningArea != null) {
                buffer.append('\n');
            }
            buffer.append(source!=null ? source : getString(ResourceKeys.UNTITLED));
            buffer.append('\n');
        }
        int wm = WARNING_MARGIN;
        if (margin != null) {
            margin = trim(margin);
            if (margin.length() != 0) {
                wm -= (margin.length()+3);
                buffer.append(Utilities.spaces(wm));
                buffer.append('(');
                buffer.append(margin);
                buffer.append(')');
                wm = 1;
            }
        }
        buffer.append(Utilities.spaces(wm));
        buffer.append(warning);
        if (buffer.charAt(buffer.length()-1) != '\n') {
            buffer.append('\n');
        }
        set(Caller.WARNING, buffer.toString());
    }

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'opération.
     * L'implémentation par défaut fait apparaître le message de l'exception dans
     * une fenêtre séparée.
     */
    public void exceptionOccurred(final Throwable exception) {
        ExceptionMonitor.show(window, exception);
    }

    /**
     * Retourne la chaîne <code>margin</code> sans les
     * éventuelles parenthèses qu'elle pourrait avoir
     * de part et d'autre.
     */
    private static String trim(String margin) {
        margin = margin.trim();
        int lower = 0;
        int upper = margin.length();
        while (lower<upper && margin.charAt(lower+0)=='(') lower++;
        while (lower<upper && margin.charAt(upper-1)==')') upper--;
        return margin.substring(lower, upper);
    }

    /**
     * Interroge une des composantes de la boîte des progrès.
     * L'interrogation sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information désirée. Ce code doit être une
     *         des constantes telles que {@link Caller#TITLE}
     *         ou {@link Caller#LABEL}.
     * @return L'information demandée.
     */
    private String get(final int task) {
        final Caller caller = new Caller(-task);
        SwingUtilities.invokeAndWait(caller);
        return caller.text;
    }

    /**
     * Modifie l'état d'une des composantes de la boîte des progrès.
     * La modification sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information à modifier. Ce code doit être une
     *         des constantes telles que {@link Caller#TITLE}
     *         ou {@link Caller#LABEL}.
     * @param  text Le nouveau texte.
     */
    private void set(final int task, final String text) {
        final Caller caller = new Caller(task);
        caller.text = text;
        EventQueue.invokeLater(caller);
    }

    /**
     * Modifie l'état d'une des composantes de la boîte des progrès.
     * La modification sera faite dans le thread de <i>Swing</i>.
     *
     * @param  task Information à modifier. Ce code doit être une
     *         des constantes telles que {@link Caller#PROGRESS}.
     * @param  value Nouvelle valeur à affecter à la composante.
     */
    private void set(final int task, final int value) {
        final Caller caller = new Caller(task);
        caller.value = value;
        EventQueue.invokeLater(caller);
    }

    /**
     * Appelle une méthode <i>Swing</i> sans argument.
     * @param  task Méthode à appeler. Ce code doit être une
     *         des constantes telles que {@link Caller#STARTED}
     *         ou {@link Caller#DISPOSE}.
     */
    private void call(final int task) {
        EventQueue.invokeLater(new Caller(task));
    }

    /**
     * Tâche à exécuter dans le thread de <i>Swing</i> pour interroger
     * ou modifier l'état d'une composante. Cette tache est destinée à être appelée par
     * les méthodes {@link EventQueue#invokeLater} et {@link EventQueue#invokeAndWait}.
     * Les tâches possibles sont désignées par des constantes telles que {@link #TITLE}
     * et {@link #LABEL}. Une valeur positive signifie que l'on modifie l'état de cette
     * composante (dans ce cas, il faut d'abord avoir affecté une valeur à {@link #text}),
     * tandis qu'une valeur négative signifie que l'on interroge l'état de la comosante
     * (dans ce cas, il faudra extrait l'état du champ {@link #text}).
     *
     * @version $Id: ProgressWindow.java,v 1.3 2003/05/13 11:01:39 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private class Caller implements Runnable {
        /**
         * Constante indiquant que l'on souhaite interroger
         * ou modifier le titre de la boîte des progrès.
         */
        public static final int TITLE=1;

        /**
         * Constante indiquant que l'on souhaite interroger
         * ou modifier la description des progrès.
         */
        public static final int LABEL=2;

        /**
         * Constante indiquant que l'on souhaite modifier
         * la valeur de la barre des progrès.
         */
        public static final int PROGRESS=3;

        /**
         * Constante indiquant que l'on souhaite
         * faire apparaître un avertissement.
         */
        public static final int WARNING=4;

        /**
         * Constante indiquant que l'on souhaite
         * faire apparaître la boîte des progrès.
         */
        public static final int STARTED=5;

        /**
         * Constante indiquant que l'on souhaite
         * faire disparaître la boîte des progrès.
         */
        public static final int COMPLETE=6;

        /**
         * Constante indiquant que l'on souhaite
         * faire disparaître la boîte des progrès.
         */
        public static final int DISPOSE=7;

        /**
         * Constante indiquant la tâche que l'on souhaite effectuer. Il doit s'agir
         * d'une valeur telle que {@link #TITLE} et {@link #LABEL}, ainsi que leurs
         * valeurs négatives.
         */
        private final int task;

        /**
         * Valeur à affecter ou valeur retournée. Pour des valeurs positives de {@link #task},
         * il s'agit de la valeur à affecter à une composante. Pour des valeurs négatives de
         * {@link #task}, il s'agit de la valeur retournée par une composante.
         */
        public String text;

        /**
         * Valeur à affecter à la barre des progrès.
         */
        public int value;

        /**
         * Construit un objet qui effectura la tâche identifiée par la constante <code>task</code>.
         * Cette constantes doit être une valeur telle que {@link #TITLE} et {@link #LABEL}, ou une
         * de leurs valeurs négatives.
         */
        public Caller(final int task) {
            this.task = task;
        }

        /**
         * Exécute la tâche identifiée par la constante {@link #task}.
         */
        public void run() {
            final BoundedRangeModel model = progressBar.getModel();
            switch (task) {
                case   +LABEL: description.setText(text);  return;
                case   -LABEL: text=description.getText(); return;
                case PROGRESS: model.setValue(value); progressBar.setIndeterminate(false); return;
                case  STARTED: model.setRangeProperties(  0,1,0,100,false); window.setVisible(true); break;
                case COMPLETE: model.setRangeProperties(100,1,0,100,false); window.setVisible(warningArea!=null); break;
            }
            synchronized (ProgressWindow.this) {
                if (window instanceof JDialog) {
                    final JDialog window = (JDialog) ProgressWindow.this.window;
                    switch (task) {
                        case   +TITLE: window.setTitle(text);  return;
                        case   -TITLE: text=window.getTitle(); return;
                        case  STARTED: window.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); return;
                        case COMPLETE: window.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);       return;
                        case  DISPOSE: window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                                       if (warningArea==null || !window.isVisible()) window.dispose();
                                       return;
                    }
                } else {
                    final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                    switch (task) {
                        case   +TITLE: window.setTitle(text);     return;
                        case   -TITLE: text=window.getTitle();    return;
                        case  STARTED: window.setClosable(false); return;
                        case COMPLETE: window.setClosable(true);  return;
                        case  DISPOSE: window.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
                                       if (warningArea==null || !window.isVisible()) window.dispose();
                                       return;
                    }
                }
                /*
                 * Si la tâche spécifiée n'est aucune des tâches énumérées ci-haut,
                 * on supposera que l'on voulait afficher un message d'avertissement.
                 */
                if (warningArea == null) {
                    final JTextArea     warningArea = new JTextArea();
                    final JScrollPane        scroll = new JScrollPane(warningArea);
                    final JPanel              panel = new JPanel(new BorderLayout());
                    final JPanel              title = new JPanel(new BorderLayout());
                    ProgressWindow.this.warningArea = warningArea;
                    warningArea.setFont(Font.getFont("Monospaced"));
                    warningArea.setEditable(false);
                    title.setBorder(BorderFactory.createEmptyBorder(0,HMARGIN,VMARGIN,HMARGIN));
                    panel.add(content,                                     BorderLayout.NORTH);
                    title.add(new JLabel(getString(ResourceKeys.WARNING)), BorderLayout.NORTH );
                    title.add(scroll,                                      BorderLayout.CENTER);
                    panel.add(title,                                       BorderLayout.CENTER);
                    if (window instanceof JDialog) {
                        final JDialog window = (JDialog) ProgressWindow.this.window;
                        window.setContentPane(panel);
                        window.setResizable(true);
                    } else {
                        final JInternalFrame window = (JInternalFrame) ProgressWindow.this.window;
                        window.setContentPane(panel);
                        window.setResizable(true);
                    }
                    window.setSize(WIDTH, HEIGHT+WARNING_HEIGHT);
                    window.setVisible(true); // Seems required in order to force relayout.
                }
                final JTextArea warningArea=(JTextArea) ProgressWindow.this.warningArea;
                warningArea.append(text);
            }
        }
    }
}
