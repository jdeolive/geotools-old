/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.gui.progress;

// Miscellaneous
import javax.swing.ProgressMonitor; // For JavaDoc
import org.geotools.resources.Utilities;


/**
 * Affiche l'état d'avancement d'une longue opération, ainsi que d'éventuels avertissements.
 * La classe <code>Progress</code> ne suppose pas que l'état d'avancement sera reporté dans
 * une fenêtre. Par exemple {@link PrintProgress} reporte l'état d'avancement sur le périphérique
 * de sortie standard, ce qui est pratique pour les programmes exécutés sur la ligne de commandes.
 * Exemple: le code suivant pourrait être utilisé pour informer des progrès de la lecture d'un
 * fichier de 1000 lignes:
 *
 * <blockquote><pre>
 * &nbsp;Progress p = new {@link PrintProgress}();
 * &nbsp;p.setDecription("Loading data");
 * &nbsp;p.start();
 * &nbsp;for (int j=0; j&lt;1000; j++) {
 * &nbsp;    // ... some process...
 * &nbsp;    if ((j &amp; 255) == 0)
 * &nbsp;        p.progress(j*0.1f);
 * &nbsp;}
 * &nbsp;p.complete();
 * </pre></blockquote>
 *
 * <strong>Note:</strong>
 *       La ligne <code>if ((j&nbsp;&amp;&nbsp;255)&nbsp;==&nbsp;0)</code> est utilisée pour
 *       n'appeller la méthode {@link #progress} qu'une fois toutes les 256 lignes.
 *       Ce n'est pas obligatoire, mais se traduit souvent par une augmentation sensible
 *       de la vitesse de traitement.
 *
 * <p>Toutes les classes dérivées de {@link Progress} sont sécuritaires dans un environnement
 * multi-threads, et peuvent être exécutées dans n'importe quel thread. Ca implique en particulier
 * qu'elles peuvent être exécutées dans un thread autre que celui de <i>Swing</i>,  même si les
 * progrès doivent être reportés dans une fenêtre de <i>Swing</i>.  Exécuter un long calcul dans
 * un thread séparé est en général une pratique recommandée.</p>
 *
 * @version $Id: Progress.java,v 1.1 2003/02/03 14:51:04 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public abstract class Progress {
    /**
     * Construit un objet qui représentera
     * les progrès d'une longue opération.
     */
    public Progress() {
    }

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
     * Libère les ressources utilisées par l'état d'avancement. Si l'état
     * d'avancement était affichée dans une fenêtre, cette fenêtre peut être
     * détruite. L'implémentation par défaut ne fait rien.
     */
    public void dispose() {
    }

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

    /**
     * Retourne la chaîne <code>margin</code> sans les
     * éventuelles parenthèses qu'elle pourrait avoir
     * de part et d'autre.
     */
    static String trim(String margin) {
        margin = margin.trim();
        int lower = 0;
        int upper = margin.length();
        while (lower<upper && margin.charAt(lower+0)=='(') lower++;
        while (lower<upper && margin.charAt(upper-1)==')') upper--;
        return margin.substring(lower, upper);
    }

    /**
     * Returns a string representation for this object.
     */
    public synchronized String toString() {
        return Utilities.getShortClassName(this)+'['+getDescription()+']';
    }
}
