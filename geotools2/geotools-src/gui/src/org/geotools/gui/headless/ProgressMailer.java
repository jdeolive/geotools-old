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

// Java Mail
import javax.mail.Session;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

// J2SE dependencies
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

// Formatting
import java.io.PrintWriter;
import java.io.CharArrayWriter;
import java.text.NumberFormat;
import java.text.FieldPosition;

// Geotools dependencies
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * Informe l'utilisateur des progrès d'une opération en envoyant
 * des courriers électroniques à intervalles régulier.
 *
 * @version $Id: ProgressMailer.java,v 1.2 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ProgressMailer implements ProgressListener {
    /**
     * Nom de l'opération en cours. Le pourcentage
     * sera écris à la droite de ce nom.
     */
    private String description;

    /**
     * Langue à utiliser pour le formattage.
     */
    private final Locale locale;

    /**
     * Session à utiliser pour
     * envoyer des courriels.
     */
    private final Session session;

    /**
     * Addresse des personnes à qui envoyer
     * un rapport sur les progrès.
     */
    private final Address[] address;

    /**
     * Laps de temps entre deux courriers électroniques
     * informant des progrès. On attendra que ce laps de
     * temps soit écoulés avant d'envoyer un nouveau courriel.
     */
    private long timeInterval = 3*60*60*1000L;

    /**
     * Date et heure à laquelle envoyer le prochain courriel.
     */
    private long nextTime;

    /**
     * Construit un objet qui informera des
     * progrès en envoyant des courriels.
     *
     * @param  host Nom du serveur à utiliser pour envoyer des courriels.
     * @param  address Adresse à laquelle envoyer les messages.
     * @throws AddressException si l'adresse spécifiée n'est pas dans un format valide.
     */
    public ProgressMailer(final String host, final String address) throws AddressException {
        this(Session.getDefaultInstance(properties(host)), new InternetAddress[] {
                new InternetAddress(address)});
    }

    /**
     * Construit un objet qui informera des
     * progrès en envoyant des courriels.
     *
     * @param session Session à utiliser pour envoyer des courriels.
     */
    public ProgressMailer(final Session session, final Address[] address) {
        this.session = session;
        this.address = address;
        this.locale  = Locale.getDefault();
        nextTime = System.currentTimeMillis();
    }

    /**
     * Retourne un ensemble de propriétés
     * nécessaires pour ouvrir une session.
     *
     * @param host Nom du serveur à utiliser pour envoyer des courriels.
     */
    private static final Properties properties(final String host) {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        return props;
    }

    /**
     * Retourne le laps de temps minimal entre deux courriers électroniques
     * informant des progrès. On attendra que ce laps de temps soit écoulés
     * avant d'envoyer un nouveau courriel.
     *
     * @return Intervalle de temps en millisecondes.
     */
    public long getTimeInterval() {
        return timeInterval;
    }

    /**
     * Spécifie le laps de temps minimal entre deux courriers électroniques
     * informant des progrès. On attendra que ce laps de temps soit écoulés
     * avant d'envoyer un nouveau courriel. Par défaut, un courriel n'est
     * envoyé qu'une fois tous les heures.
     *
     * @param interval Intervalle de temps en millisecondes.
     */
    public synchronized void setTimeInterval(final long interval) {
        this.timeInterval = interval;
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
     * Envoie le message spécifié par courrier électronique.
     *
     * @param method Nom de la méthode qui appelle celle-ci.
     *        Cette information est utilisée pour produire
     *        un message d'erreur en cas d'échec.
     * @param subjectKey Clé du sujet: {@link ResourceKeys#PROGRESS},
     *        {@link ResourceKeys#WARNING} ou {@link ResourceKeys#EXCEPTION}.
     * @param messageText Message à envoyer par courriel.
     */
    private void send(final String method, final int subjectKey, final String messageText) {
        try {
            final Message message = new MimeMessage(session);
            message.setFrom();
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(Resources.format(subjectKey));
            message.setSentDate(new Date());
            message.setText(messageText);
            Transport.send(message);
        } catch (MessagingException exception) {
            final LogRecord warning = new LogRecord(Level.WARNING,
                    "CATCH "+Utilities.getShortClassName(exception));
            warning.setSourceClassName(getClass().getName());
            warning.setSourceMethodName(method);
            warning.setThrown(exception);
            Logger.getLogger("org.geotools.gui.progress").log(warning);
        }
    }

    /**
     * Envoie par courrier électronique un rapport des progrès.
     *
     * @param method Nom de la méthode qui appelle celle-ci.
     *        Cette information est utilisée pour produire
     *        un message d'erreur en cas d'échec.
     * @param percent Pourcentage effectué (entre 0 et 100).
     */
    private void send(final String method, final float percent) {
        final Runtime      system = Runtime.getRuntime();
        final float   MEMORY_UNIT = (1024f*1024f);
        final float    freeMemory = system.freeMemory()  / MEMORY_UNIT;
        final float   totalMemory = system.totalMemory() / MEMORY_UNIT;
        final Resources resources = Resources.getResources(null);
        final NumberFormat format = NumberFormat.getPercentInstance(locale);
        final StringBuffer buffer = new StringBuffer(description!=null ?
                description : resources.getString(ResourceKeys.PROGRESSION));
        buffer.append(": "); format.format(percent/100, buffer, new FieldPosition(0));
        buffer.append('\n');
        buffer.append(resources.getString(ResourceKeys.MEMORY_HEAP_SIZE_$1,
                                          new Float(totalMemory)));
        buffer.append('\n');
        buffer.append(resources.getString(ResourceKeys.MEMORY_HEAP_USAGE_$1,
                                          new Float(1-freeMemory/totalMemory)));
        buffer.append('\n');
        send(method, ResourceKeys.PROGRESSION, buffer.toString());
    }

    /**
     * Envoie un courrier électronique indiquant
     * que l'opération vient de commencer.
     */
    public synchronized void started() {
        send("started", 0);
    }

    /**
     * Envoie un courrier électronique informant des progrès de l'opération.
     * Cette information ne sera pas nécessairement prise en compte. Cette
     * méthode n'envoie des rapport qu'à des intervalles de temps assez espacés
     * (par défaut 3 heure) afin de ne pas innonder l'utilisateur de courriels.
     */
    public synchronized void progress(float percent) {
        final long time = System.currentTimeMillis();
        if (time > nextTime) {
            nextTime = time + timeInterval;
            if (percent <  1f) percent =  1f;
            if (percent > 99f) percent = 99f;
            send("progress", percent);
        }
    }

    /**
     * Envoie un courrier électronique indiquant
     * que l'opération vient de se terminer.
     */
    public synchronized void complete() {
        send("complete", 100);
    }

    /**
     * Libère les ressources utilisées par cet objet.
     * L'implémentation par défaut ne fait rien.
     */
    public void dispose() {
    }

    /**
     * Envoie un message d'avertissement. Ce message
     * sera envoyée par courrier électronique.
     *
     * @param source Chaîne de caractère décrivant la source de l'avertissement.
     *        Il s'agira par exemple du nom du fichier dans lequel une anomalie
     *        a été détectée. Peut être nul si la source n'est pas connue.
     * @param margin Texte à placer dans la marge de l'avertissement <code>warning</code>,
     *        ou <code>null</code> s'il n'y en a pas. Il s'agira le plus souvent du numéro
     *        de ligne où s'est produite l'erreur dans le fichier <code>source</code>.
     * @param warning Message d'avertissement à écrire.
     */
    public synchronized void warningOccurred(final String source,
                                             final String margin,
                                             final String warning)
    {
        final StringBuffer buffer=new StringBuffer();
        if (source != null) {
            buffer.append(source);
            if (margin != null) {
                buffer.append(" (");
                buffer.append(margin);
                buffer.append(')');
            }
            buffer.append(": ");
        } else if (margin != null) {
            buffer.append(margin);
            buffer.append(": ");
        }
        buffer.append(warning);
        send("warningOccurred", ResourceKeys.WARNING, buffer.toString());
    }

    /**
     * Indique qu'une exception est survenue pendant le traitement de l'opération.
     * L'implémentation par défaut envoie la trace de l'exception par courrier
     * électronique.
     */
    public synchronized void exceptionOccurred(final Throwable exception) {
        final CharArrayWriter buffer = new CharArrayWriter();
        exception.printStackTrace(new PrintWriter(buffer));
        send("exceptionOccurred", ResourceKeys.EXCEPTION, buffer.toString());
    }
}
