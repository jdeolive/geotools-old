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
package org.geotools.gui.headless;

// Gestion des entrés/sorties
import java.lang.System;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;

// Gestion du texte
import java.text.NumberFormat;
import java.text.BreakIterator;

// Divers
import org.geotools.util.ProgressListener;
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * Informe l'utilisateur des progrès d'une opération à l'aide de messages envoyé vers un
 * flot. L'avancement de l'opération sera affiché en pourcentage sur une ligne (généralement
 * le périphérique de sortie standard). Cette classe peut aussi écrire des avertissements,
 * ce qui est utile entre autre lors de la lecture d'un fichier de données durant laquelle
 * on veut signaler des anomalies mais sans arrêter la lecture pour autant.
 *
 * @version $Id: ProgressPrinter.java,v 1.2 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ProgressPrinter implements ProgressListener {
    /**
     * Nom de l'opération en cours. Le pourcentage sera écris à la droite de ce nom.
     */
    private String description;

    /**
     * Flot utilisé pour l'écriture de l'état d'avancement d'un
     * processus ainsi que pour les écritures des commentaires.
     */
    private final PrintWriter out;

    /**
     * Indique si le caractère '\r' ramène au début de la ligne courante sur
     * ce système. On supposera que ce sera le cas si le système n'utilise
     * pas la paire "\r\n" pour changer de ligne (comme le system VAX-VMS).
     */
    private final boolean CR_supported;

    /**
     * Longueur maximale des lignes. L'espace utilisable sera un peu
     * moindre car quelques espaces seront laissés en début de ligne.
     */
    private final int maxLength;

    /**
     * Nombre de caractères utilisés lors de l'écriture de la dernière ligne.
     * Ce champ est mis à jour par la méthode {@link #carriageReturn} chaque
     * fois que l'on déclare que l'on vient de terminer l'écriture d'une ligne.
     */
    private int lastLength;

    /**
     * Position à laquelle commencer à écrire le pourcentage. Cette information
     * est gérée automatiquement par la méthode {@link #progress}. La valeur -1
     * signifie que ni le pourcentage ni la description n'ont encore été écrits.
     */
    private int percentPosition = -1;

    /**
     * Dernier pourcentage écrit. Cette information est utilisée
     * afin d'éviter d'écrire deux fois le même pourcentage, ce
     * qui ralentirait inutilement le système. La valeur -1 signifie
     * qu'on n'a pas encore écrit de pourcentage.
     */
    private float lastPercent = -1;

    /**
     * Format à utiliser pour écrire les pourcentages.
     */
    private NumberFormat format;

    /**
     * Objet utilisé pour couper les lignes correctements lors de l'affichage
     * de messages d'erreurs qui peuvent prendre plusieurs lignes.
     */
    private BreakIterator breaker;

    /**
     * Indique si cet objet a déjà écrit des avertissements. Si
     * oui, on ne réécrira pas le gros titre "avertissements".
     */
    private boolean hasPrintedWarning;

    /**
     * Source du dernier message d'avertissement. Cette information est
     * conservée afin d'éviter de répéter la source lors d'éventuels
     * autres messages d'avertissements.
     */
    private String lastSource;

    /**
     * Construit un objet qui écrira sur le périphérique de sortie standard
     * ({@link java.lang.System#out}) l'état d'avancement d'une opération.
     * La longueur par défaut des lignes sera de 80 caractères.
     */
    public ProgressPrinter() {
        this(new PrintWriter(Arguments.getWriter(System.out)));
    }

    /**
     * Construit un objet qui écrira sur le périphérique de
     * sortie spécifié l'état d'avancement d'une opération.
     * La longueur par défaut des lignes sera de 80 caractères.
     */
    public ProgressPrinter(final PrintWriter out) {
        this(out, 80);
    }

    /**
     * Construit un objet qui écrira sur le périphérique de
     * sortie spécifié l'état d'avancement d'une opération.
     *
     * @param out périphérique de sortie à utiliser pour écrire l'état d'avancement.
     * @param maxLength Longueur maximale des lignes. Cette information est utilisée
     *        par {@link #warningOccurred} pour répartir sur plusieurs lignes des
     *        messages qui ferait plus que la longueur <code>lineLength</code>.
     */
    public ProgressPrinter(final PrintWriter out, final int maxLength) {
        this.out = out;
        this.maxLength = maxLength;
        final String lineSeparator = System.getProperty("line.separator");
        CR_supported=(lineSeparator!=null && lineSeparator.equals("\r\n"));
    }

    /**
     * Efface le reste de la ligne (si nécessaire) puis repositionne le curseur au début
     * de la ligne. Si les retours chariot ne sont pas supportés, alors cette méthode va
     * plutôt passer à la ligne suivante. Dans tous les cas, le curseur se trouvera au
     * début d'une ligne et la valeur <code>length</code> sera affecté au champ
     * {@link #lastLength}.
     *
     * @param length Nombre de caractères qui ont été écrit jusqu'à maintenant sur cette ligne.
     *        Cette information est utilisée pour ne mettre que le nombre d'espaces nécessaires
     *        à la fin de la ligne.
     */
    private void carriageReturn(final int length) {
        if (CR_supported && length<maxLength) {
            for (int i=length; i<lastLength; i++)  {
                out.print(' ');
            }
            out.print('\r');
            out.flush();
        } else {
            out.println();
        }
        lastLength = length;
    }

    /**
     * Ajoute des points à la fin de la ligne jusqu'à représenter
     * le pourcentage spécifié. Cette méthode est utilisée pour
     * représenter les progrès sur un terminal qui ne supporte
     * pas les retours chariots.
     *
     * @param percent Pourcentage accompli de l'opération. Cette
     *        valeur doit obligatoirement se trouver entre 0 et
     *        100 (ça ne sera pas vérifié).
     */
    private void completeBar(final float percent) {
        final int end = (int) ((percent/100)*((maxLength-2)-percentPosition)); // Round toward 0.
        while (lastLength < end) {
            out.print('.');
            lastLength++;
        }
    }

    /**
     * Retourne le message d'écrivant l'opération
     * en cours. Si aucun message n'a été définie,
     * retourne <code>null</code>.
     */
    public String getDescription() {
        return description;
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
    public synchronized void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Indique que l'opération a commencée.
     */
    public synchronized void started() {
        int length = 0;
        if (description != null) {
            out.print(description);
            length=description.length();
        }
        if (CR_supported) {
            carriageReturn(length);
        }
        out.flush();
        percentPosition   = length;
        lastPercent       = -1;
        lastSource        = null;
        hasPrintedWarning = false;
    }

    /**
     * Indique l'état d'avancement de l'opération. Le progrès est représenté par un
     * pourcentage variant de 0 à 100 inclusivement. Si la valeur spécifiée est en
     * dehors de ces limites, elle sera automatiquement ramenée entre 0 et 100.
     */
    public synchronized void progress(float percent) {
        if (percent<0  ) percent=0;
        if (percent>100) percent=100;
        if (CR_supported) {
            /*
             * Si le périphérique de sortie supporte les retours chariot,
             * on écrira l'état d'avancement comme un pourcentage après
             * la description, comme dans "Lecture des données (38%)".
             */
            if (percent != lastPercent) {
                if (format == null) {
                    format = NumberFormat.getPercentInstance();
                }
                final String text = format.format(percent/100.0);
                int length = text.length();
                percentPosition = 0;
                if (description != null) {
                    out.print(description);
                    out.print(' ');
                    length += (percentPosition=description.length())+1;
                }
                out.print('(');
                out.print(text);
                out.print(')');
                length += 2;
                carriageReturn(length);
                lastPercent=percent;
            }
        } else {
            /*
             * Si le périphérique ne supporte par les retours chariots, on
             * écrira l'état d'avancement comme une série de points placés
             * après la description, comme dans "Lecture des données......"
             */
            completeBar(percent);
            lastPercent=percent;
            out.flush();
        }
    }

    /**
     * Indique que l'opération est terminée. L'indicateur visuel informant des
     * progrès sera ramené à 100% ou disparaîtra. Si des messages d'erreurs ou
     * d'avertissements étaient en attente, ils seront écrits.
     */
    public synchronized void complete() {
        if (!CR_supported) {
            completeBar(100);
        }
        carriageReturn(0);
        out.flush();
    }

    /**
     * Libère les ressources utilisées par cet objet.
     * L'implémentation par défaut ne fait rien.
     */
    public void dispose() {
    }

    /**
     * Envoie un message d'avertissement. La première fois que cette méthode est appellée, le mot
     * "AVERTISSEMENTS" sera écrit en lettres majuscules au milieu d'une boîte. Si une source est
     * spécifiée (argument <code>source</code>), elle ne sera écrite qu'à la condition qu'elle
     * n'est pas la même que celle du dernier avertissement. Si une note de marge est spécifiée
     * (argument <code>margin</code>), elle sera écrite entre parenthèses à la gauche de
     * l'avertissement <code>warning</code>.
     *
     * @param source Chaîne de caractère décrivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a été détectée. Peut être nul si la source n'est pas connue.
     * @param margin Texte à placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du numéro
     *        de ligne où s'est produite l'erreur dans le fichier <code>source</code>.
     * @param warning Message d'avertissement à écrire. Si ce message est
     *        plus long que la largeur de l'écran (telle que spécifiée au
     *        moment de la construction, alors il sera automatiquement
     *        distribué sur plusieurs lignes correctements indentées.
     */
    public synchronized void warningOccurred(final String source, String margin,
                                             final String warning)
    {
        carriageReturn(0);
        if (!hasPrintedWarning) {
            printInBox(Resources.format(ResourceKeys.WARNING));
            hasPrintedWarning=true;
        }
        if (!Utilities.equals(source, lastSource)) {
            out.println();
            out.println(source!=null ? source : Resources.format(ResourceKeys.UNTITLED));
            lastSource=source;
        }
        /*
         * Procède à l'écriture de l'avertissement avec (de façon optionnelle)
         * quelque chose dans la marge (le plus souvent un numéro de ligne).
         */
        String prefix="    ";
        String second=prefix;
        if (margin != null) {
            margin = trim(margin);
            if (margin.length() != 0) {
                final StringBuffer buffer = new StringBuffer(prefix);
                buffer.append('(');
                buffer.append(margin);
                buffer.append(") ");
                prefix=buffer.toString();
                buffer.setLength(0);
                second=Utilities.spaces(prefix.length());
            }
        }
        int width=maxLength-prefix.length()-1;
        if (breaker == null) {
            breaker=BreakIterator.getLineInstance();
        }
        breaker.setText(warning);
        int start=breaker.first(), end=start, nextEnd;
        while ((nextEnd=breaker.next()) != BreakIterator.DONE) {
            while (nextEnd-start > width) {
                if (end <= start) {
                    end=Math.min(nextEnd, start+width);
                }
                out.print(prefix);
                out.println(warning.substring(start, end));
                prefix=second;
                start=end;
            }
            end=Math.min(nextEnd, start+width);
        }
        if (end>start) {
            out.print(prefix);
            out.println(warning.substring(start, end));
        }
        if (!CR_supported && description!=null) {
            out.print(description);
            completeBar(lastPercent);
        }
        out.flush();
    }

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'opération.
     * L'implémentation par défaut écrit "Exception" dans une boîte, puis envoie
     * la trace vers le périphérique de sortie spécifiée au constructeur.
     */
    public synchronized void exceptionOccurred(final Throwable exception) {
        carriageReturn(0);
        printInBox(Resources.format(ResourceKeys.EXCEPTION));
        exception.printStackTrace(out);
        hasPrintedWarning = false;
        out.flush();
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
     * Écrit dans une boîte entouré d'astérix le texte spécifié en argument.
     * Ce texte doit être sur une seule ligne et ne pas comporter de retour
     * chariot. Les dimensions de la boîte seront automatiquement ajustées.
     * @param text Texte à écrire (une seule ligne).
     */
    private void printInBox(String text) {
        int length = text.length();
        for (int pass=-2; pass<=2; pass++) {
            switch (Math.abs(pass)) {
                case 2: for (int j=-10; j<length; j++) out.print('*');
                        out.println();
                        break;

                case 1: out.print("**");
                        for (int j=-6; j<length; j++) out.print(' ');
                        out.println("**");
                        break;

                case 0: out.print("**   ");
                        out.print(text);
                        out.println("   **");
                        break;
            }
        }
    }
}
