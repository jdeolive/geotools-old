/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
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
 *     UNITED KINDOM: James Macgill
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
package org.geotools.swinggui;

// Graphics and geometry
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics2D;

// User interface
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JDesktopPane;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.AbstractButton;

// Events
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Input / Output
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.geotools.io.ExpandedTabWriter;

// Collections
import java.util.List;
import java.util.ArrayList;

// Miscellaneous
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * Utilitaire facilitant l'affichage de la trace d'une exception dans une
 * composante <i>Swing</i>. La classe {@link java.lang.Exception} standard
 * contient des méthodes qui écrivent la trace de l'exception vers le
 * périphérique d'erreur. Cette classe <code>ExceptionMonitor</code> ajoute
 * des méthodes statiques qui font apparaître le message et éventuellement
 * la trace d'une exception dans une composante visuelle.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/ExceptionMonitor.png"></p>
 * <p>&nbsp;</p>
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public final class ExceptionMonitor {
    /**
     * Nombre d'espaces à laisser entre chaque taquets de tabulation.
     */
    private static final int TAB_WIDTH=4;

    /**
     * Interdit la création d'objets de
     * classe <code>ExceptionMonitor</code>.
     */
    private ExceptionMonitor() {
    }

    /**
     * Affiche un message d'erreur pour l'exception <code>exception</code>
     * spécifiée. Notez que cette méthode peut être appelée à partir de
     * n'importe quel thread (pas nécessairement celui de <i>Swing</i>).
     *
     * @param owner Composante dans laquelle l'exception s'est produite,
     *        ou <code>null</code> si cette composante n'est pas connue.
     * @param exception Exception qui a été lancée et que l'on souhaite
     *        reporter à l'utilisateur.
     */
    public static void show(final Component owner, final Throwable exception) {
        show(owner, exception, null);
    }

    /**
     * Affiche un message d'erreur pour l'exception <code>exception</code>
     * spécifiée. Notez que cette méthode peut être appelée à partir de
     * n'importe quel thread (pas nécessairement celui de <i>Swing</i>).
     *
     * @param owner Composante dans laquelle l'exception s'est produite,
     *        ou <code>null</code> si cette composante n'est pas connue.
     * @param exception Exception qui a été lancée et que l'on souhaite
     *        reporter à l'utilisateur.
     * @param message Message à afficher. Si ce paramètre est nul, alors
     *        {@link Exception#getLocalizedMessage} sera appelée pour
     *        obtenir le message.
     */
    public static void show(final Component owner, final Throwable exception, final String message) {
        if (EventQueue.isDispatchThread()) {
            Pane.show(owner, exception, message);
        }
        else {
            final Runnable monitor=new Runnable()
            {
                public void run() {
                    Pane.show(owner, exception, message);
                }
            };
            try {
                EventQueue.invokeAndWait(monitor);
            } catch (InterruptedException error) {
                // On ne veut pas nous laisser dormir. Retourne au boulot.
            } catch (InvocationTargetException error) {
                final Throwable e = error.getTargetException();
                if (e instanceof RuntimeException) throw (RuntimeException)     e;
                if (e instanceof Error)            throw (Error)                e;
                Utilities.unexpectedException("org.geotools.gui", "ExceptionMonitor", "show", e);
            }
        }
    }

    /**
     * Ecrit la trace de l'exception spécifiée dans le graphique spécifié. Cette
     * méthode est utile lorsqu'une exception est survenue à l'intérieur d'une
     * méthode {@link Component#paint} et qu'on veut écrire cette exception
     * plutôt que de laisser une fenêtre vide.
     *
     * @param exception Exception dont on veut écrire la trace.
     * @param graphics Graphique dans lequel écrire cette exception. Ce
     *        graphique doit être dans son état initial (transformation
     *        affine par défaut, couleur par défaut, etc...)
     * @param widgetBounds Taille de la composante qui était en train de
     *        se faire dessiner.
     */
    public static void paintStackTrace(final Graphics2D graphics, final Rectangle widgetBounds, final Throwable exception) {
        /*
         * Obtient la trace de l'exception sous forme de chaîne
         * de caractères. Les retours chariots dans cette chaîne
         * peuvent être "\r", "\n" ou "r\n".
         */
        final String message=printStackTrace(exception);
        /*
         * Examine la chaîne de caractères ligne par ligne.
         * On créera des "glyphs" au fur et à mesure et on
         * en profitera pour calculer l'espace nécessaire.
         */
        double width=0, height=0;
        final List glyphs=new ArrayList();
        final List bounds=new ArrayList();
        final int length=message.length();
        final Font font=graphics.getFont();
        final FontRenderContext context=graphics.getFontRenderContext();
        for (int i=0; i<length;) {
            int ir=message.indexOf('\r', i);
            int in=message.indexOf('\n', i);
            if (ir<0) ir=length;
            if (in<0) in=length;
            final int irn=Math.min(ir,in);
            final GlyphVector line=font.createGlyphVector(context, message.substring(i, irn));
            final Rectangle2D rect=line.getVisualBounds();
            final double w=rect.getWidth();
            if (w>width) width=w;
            height += rect.getHeight();
            glyphs.add(line);
            bounds.add(rect);
            i = (Math.abs(ir-in)<=1 ? Math.max(ir,in) : irn)+1;
        }
        /*
         * Procède au traçage de tous les
         * glyphs précédemment calculés.
         */
        float xpos = (float) (0.5*(widgetBounds.width -width));
        float ypos = (float) (0.5*(widgetBounds.height-height));
        final int size=glyphs.size();
        for (int i=0; i<size; i++) {
            final GlyphVector line = (GlyphVector) glyphs.get(i);
            final Rectangle2D rect = (Rectangle2D) bounds.get(i);
            ypos += rect.getHeight();
            graphics.drawGlyphVector(line, xpos, ypos);
        }
    }

    /**
     * Retourne la trace d'une exception. Toutes les tabulations
     * auront été systématiquement remplacées par 4 espaces blancs.
     */
    private static String printStackTrace(final Throwable exception) {
        final StringWriter writer=new StringWriter();
        exception.printStackTrace(new PrintWriter(new ExpandedTabWriter(writer, TAB_WIDTH)));
        return writer.toString();
    }

    /**
     * Classe ayant la charge d'afficher le message d'une exception et
     * éventuellement sa trace. Le message apparaîtra dans une boîte de
     * dialogue ou dans une fenêtre interne, selon le parent.
     * <strong>Note:</strong> Toutes les méthodes de cette classe doivent
     * être appelées dans le même thread que celui de <i>Swing</i>.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private static final class Pane extends JOptionPane implements ActionListener {
        /**
         * Largeur par défaut (en nombre de colonnes) de la composante
         * qui affiche le message ou la trace de l'exception.
         */
        private static final int WIDTH=40;

        /**
         * Hauteur minimale (en pixels) de la boîte de
         * dialogue lorsqu'elle affiche aussi la trace.
         */
        private static final int HEIGHT=300;
    
        /**
         * Boîte de dialogue affichée. Il s'agira d'un objet
         * {@link JDialog} ou d'un objet {@link JInternalFrame}.
         */
        private final Component dialog;

        /**
         * Exception à afficher dans la boîte de dialogue.
         * La méthode {@link Throwable#getLocalizedMessage}
         * sera appellée pour obtenir le message à afficher.
         */
        private final Throwable exception;
    
        /**
         * Boîte qui contiendra la partie "message" de la boîte de dialogue
         * construite. Cette boîte sera agrandie si l'utilisateur demande à
         * voir la trace de l'exception.  Elle disposera les composantes en
         * utilisant {@link BorderLayout}.
         */
        private final Container message;

        /**
         * Composante affichant la trace de l'exception. Au départ, cette
         * composante sera nulle. Elle ne sera créée que si la trace fut
         * demandée par l'utilisateur.
         */
        private Component trace;

        /**
         * Indique si la trace est présentement visible. La valeur de ce
         * champ sera inversé à chaque fois que l'utilisateur appuie sur
         * le bouton "trace".
         */
        private boolean traceVisible;

        /**
         * Bouton servant à faire apparaître
         * ou disparaître la trace.
         */
        private final AbstractButton traceButton;

        /**
         * Taille initiale de la boîte de dialogue {@link #dialog}.
         * Cette information sera utilisée pour rétablir la boîte à
         * sa taille initiale lorsque la trace disparaît.
         */
        private final Dimension initialSize;

        /**
         * Resources dans la langue de l'utilisateur.
         */
        private final Resources resources;

        /**
         * Construit un panneau qui affichera le message d'erreur spécifié.
         *
         * @param owner     Composante parente de la boîte de dialogue à créer.
         * @param exception Exception que l'on veut reporter.
         * @param message   Message à afficher.
         * @param buttons   Boutons à placer sous le message. Ces boutons
         *                  doivent être dans l'ordre "Debug" et "Close".
         * @param resources Resources dans la langue de l'utilisateur.
         */
        private Pane(final Component owner,   final Throwable exception,
                     final Container message, final AbstractButton[] buttons,
                     final Resources resources) {
            super(message, ERROR_MESSAGE, OK_CANCEL_OPTION, null, buttons);
            this.exception   = exception;
            this.message     = message;
            this.resources   = resources;
            this.traceButton = buttons[0];
            buttons[0].addActionListener(this);
            buttons[1].addActionListener(this);
            /*
             * Construit la boîte de dialogue en détectant automatiquement si on
             * peut utiliser {@link InternalFrame}  ou si l'on doit se contenter
             * de {@link JDialog}. La trace de l'exception ne sera pas écrite
             * tout de suite.
             */
            final String classname=Utilities.getShortClassName(exception);
            final String title=resources.getString(ResourceKeys.ERROR_$1, classname);
            final JDesktopPane desktop=getDesktopPaneForComponent(owner);
            if (desktop!=null) {
                final JInternalFrame dialog=createInternalFrame(desktop, title);
                desktop.setLayer(dialog, JDesktopPane.MODAL_LAYER.intValue());
                dialog.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
                dialog.setResizable(false);
                dialog.pack();
                this.dialog=dialog;
            } else {
                final JDialog dialog=createDialog(owner, title);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setResizable(false);
                dialog.pack();
                this.dialog=dialog;
            }
            initialSize=dialog.getSize();
        }

        /**
         * Construit et fait apparaître une boîte de dialogue informant
         * l'utilisateur qu'une exception s'est produite. Cette méthode
         * doit être appelé dans le  même thread que <i>Swing</i>.
         */
        public static void show(final Component owner, final Throwable exception, String message) {
            final Resources resources = Resources.getResources((owner!=null) ? owner.getLocale() : null);
            if (message==null) {
                message=exception.getLocalizedMessage();
                if (message==null) {
                    final String classname = Utilities.getShortClassName(exception);
                    message=resources.getString(ResourceKeys.NO_DETAILS_$1, classname);
                }
            }
            final JTextArea textArea=new JTextArea(message, 1, WIDTH);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBackground(null);
            textArea.setBorder(null); // Certains L&F ont une bordure.
            /**
             * Construit le reste de la boîte de dialogue. La barre
             * de titre contiendra le nom de la classe de l'exception.
             */
            final JComponent messageBox=new JPanel(new BorderLayout());
            messageBox.add(textArea, BorderLayout.NORTH);
            final Pane pane=new Pane(owner, exception, messageBox, new AbstractButton[]
            {
                new JButton(resources.getString(ResourceKeys.DEBUG)),
                new JButton(resources.getString(ResourceKeys.CLOSE))
            }, resources);
            pane.dialog.setVisible(true);
        }

        /**
         * Fait apparaître la trace de l'exception en-dessous du message. Cette
         * méthode est appelée automatiquement lorsque le bouton "Debug" de la
         * boîte de dialogue est appuyé. Si la trace de l'exception n'a pas
         * encore été écrite, alors cette méthode construira les composantes
         * nécessaires une fois pour toute.
         */
        public void actionPerformed(final ActionEvent event) {
            if (event.getSource()!=traceButton) {
                dispose();
                return;
            }
            /*
             * Construit la trace de l'exception une fois pour
             * toute si elle n'avait pas déjà été construite.
             */
            if (trace==null) {
                JComponent traceComponent=null;
                for (Throwable cause  = exception;
                               cause != null;
                               cause  = cause.getCause())
                {
                    final JTextArea text=new JTextArea(1, WIDTH);
                    text.setTabSize(4);
                    text.setText(printStackTrace(cause));
                    text.setEditable(false);
                    final JScrollPane scroll=new JScrollPane(text);
                    if (traceComponent!=null) {
                        if (!(traceComponent instanceof JTabbedPane)) {
                            String classname = Utilities.getShortClassName(exception);
                            JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
                            tabs.addTab(classname, traceComponent);
                            traceComponent = tabs;
                        }
                        String classname = Utilities.getShortClassName(cause);
                        ((JTabbedPane) traceComponent).addTab(classname, scroll);
                    }
                    else {
                        traceComponent = scroll;
                    }
                }
                if (traceComponent==null) {
                    // Should not happen
                    return;
                }
                traceComponent.setBorder(BorderFactory.createCompoundBorder(
                                         BorderFactory.createEmptyBorder(12,0,0,0),
                                                      traceComponent.getBorder()));
                trace=traceComponent;
            }
            /*
             * Insère ou cache la trace de l'exception. Même si on cache la trace, on
             * ne la détruira pas au cas où l'utilisateur voudrait encore la réafficher.
             */
            final Resources resources = Resources.getResources(getLocale());
            traceButton.setText(resources.format(traceVisible ? ResourceKeys.DEBUG
                                                              : ResourceKeys.HIDE));
            traceVisible = !traceVisible;
            if (dialog instanceof Dialog) {
                ((Dialog) dialog).setResizable(traceVisible);
            } else {
                ((JInternalFrame) dialog).setResizable(traceVisible);
            }
            if (traceVisible) {
                message.add(trace, BorderLayout.CENTER);
                dialog.setSize(initialSize.width, HEIGHT);
            } else {
                message.remove(trace);
                dialog.setSize(initialSize);
            }
            dialog.validate();
        }

        /**
         * Libère les ressources utilisées par cette boîte de dialogue.
         * Cette méthode est appélée lorsque l'utilisateur a fermé la
         * boîte de dialogue qui reportait l'exception.
         */
        private void dispose() {
            if (dialog instanceof Window) {
                ((Window) dialog).dispose();
            } else {
                ((JInternalFrame) dialog).dispose();
            }
        }
    }
}
