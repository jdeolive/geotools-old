/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.util;

// Miscellaneous
import javax.swing.ProgressMonitor; // For JavaDoc


/**
 * Monitor the progress of some lengthly operation. This interface makes no
 * assumption about the output device. It may be the standard output stream
 * (see {@link org.geotools.gui.headless.ProgressPrinter} implementation),
 * a window ({@link org.geotools.gui.swing.ProgressWindow}) or mails automatically
 * sent to some address ({@link org.geotools.gui.headless.ProgressMailer}).
 * Additionnaly, this interface provides support for non-fatal warning and
 * exception reports.
 * <br><br>
 * All <code>ProgressListener</code> implementations are multi-thread safe,  even the
 * <cite>Swing</cite> implemention. <code>ProgressListener</code> can be invoked from
 * any thread, which never need to be the <cite>Swing</cite>'s thread. This is usefull
 * for performing lenghtly operation in a background thread. Example:
 *
 * <blockquote><pre>
 * &nbsp;ProgressListener p = new {@link org.geotools.gui.headless.ProgressPrinter}();
 * &nbsp;p.setDecription("Loading data");
 * &nbsp;p.start();
 * &nbsp;for (int j=0; j&lt;1000; j++) {
 * &nbsp;    // ... some process...
 * &nbsp;    if ((j &amp; 255) == 0)
 * &nbsp;        p.progress(j/10f);
 * &nbsp;}
 * &nbsp;p.complete();
 * </pre></blockquote>
 *
 * <strong>Note:</strong> The line <code>if ((j&nbsp;&amp;&nbsp;255)&nbsp;==&nbsp;0)</code>
 * is used for reducing the amount of calls to {@link #progress} (only once every 256 steps).
 * This is not mandatory, but may speed up the process.
 *
 * @version $Id: ProgressListener.java,v 1.4 2003/08/04 18:21:32 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gui.headless.ProgressPrinter
 * @see org.geotools.gui.headless.ProgressMailer
 * @see org.geotools.gui.swing.ProgressWindow
 * @see javax.swing.ProgressMonitor
 */
public interface ProgressListener {
    /**
     * Retourne le message d'écrivant l'opération
     * en cours. Si aucun message n'a été définie,
     * retourne <code>null</code>.
     */
    public abstract String getDescription();

    /**
     * Spécifie un message qui décrit l'opération en cours.
     * Ce message est typiquement spécifiée avant le début
     * de l'opération. Toutefois, cette méthode peut aussi
     * être appelée à tout moment pendant l'opération sans
     * que cela affecte le pourcentage accompli. La valeur
     * <code>null</code> signifie qu'on ne souhaite plus
     * afficher de description.
     */
    public abstract void setDescription(final String description);

    /**
     * Indique que l'opération a commencée.
     */
    public abstract void started();

    /**
     * Indique l'état d'avancement de l'opération. Le progrès est représenté par un
     * pourcentage variant de 0 à 100 inclusivement. Si la valeur spécifiée est en
     * dehors de ces limites, elle sera automatiquement ramenée entre 0 et 100.
     */
    public abstract void progress(final float percent);

    /**
     * Indique que l'opération est terminée. L'indicateur visuel informant des
     * progrès sera ramené à 100% ou disparaîtra, selon l'implémentation de la
     * classe dérivée. Si des messages d'erreurs ou d'avertissements étaient
     * en attente, ils seront écrits.
     */
    public abstract void complete();

    /**
     * Libère les ressources utilisées par cet objet. Si l'état d'avancement
     * était affiché dans une fenêtre, cette fenêtre peut être détruite.
     */
    public abstract void dispose();

    /**
     * Envoie un message d'avertissement. Ce message pourra être envoyé vers le
     * périphérique d'erreur standard, apparaître dans une fenêtre ou être tout
     * simplement ignoré.
     *
     * @param source Chaîne de caractère décrivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a été détectée. Peut être nul si la source n'est pas connue.
     * @param margin Texte à placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du numéro
     *        de ligne où s'est produite l'erreur dans le fichier <code>source</code>.
     * @param warning Message d'avertissement à écrire.
     */
    public abstract void warningOccurred(String source, String margin, String warning);

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'opération.
     * Cette méthode peut afficher la trace de l'exception dans une fenêtre ou à
     * la console, dépendemment de la classe dérivée.
     */
    public abstract void exceptionOccurred(final Throwable exception);
}
