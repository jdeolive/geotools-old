/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.resources;

// Miscellaneous
import java.io.Serializable;
import org.geotools.io.TableWriter;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Statistiques concernant une suite de nombres réels. Cette classe permet de faire
 * une fois pour toutes certains calculs statistiques (minimum, maximum, moyenne...)
 * sur une série de données et d'en mémoriser les résultats. Pour effectuer ces calculs,
 * il suffit de créer un objet de cette classe et d'appeller {@link #add(double)} pour
 * chacune des données. Les valeurs NaN ne seront pas prises en compte. Pour obtenir
 * des statistiques sur l'intervalle d'échantillonnage des données plutôt que les données
 * elles-mêmes, voyez {@link DeltaStatistics}.
 *
 * @version $Id: Statistics.java,v 1.1 2003/01/12 17:53:24 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Statistics implements Cloneable, Serializable {
    /**
     * Valeur minimale qui aie été transmise à la méthode
     * {@link #add(double)}. Lors de la construction, ce
     * champs est initialisé à NaN.
     */
    private double min = Double.NaN;

    /**
     * Valeur maximale qui aie été transmise à la méthode
     * {@link #add(double)}. Lors de la construction, ce
     * champs est initialisé à NaN.
     */
    private double max = Double.NaN;

    /**
     * Somme de toutes les valeurs qui ont été transmises à
     * la méthode {@link #add(double)}. Lors de la construction,
     * ce champs est initialisé à 0.
     */
    private double sum = 0;

    /**
     * Somme des carrés de toutes les valeurs qui ont été
     * transmises à la méthode {@link #add(double)}. Lors
     * de la construction, ce champs est initialisé à 0.
     */
    private double sum2 = 0;

    /**
     * Nombre de données autres que NaN qui ont été transmises
     * à la méthode {@link #add(double)}. Lors de la construction,
     * ce champs est initialisé à 0.
     */
    private int n = 0;

    /**
     * Nombre de données égales à NaN qui ont été transmises à
     * la méthode {@link #add(double)}. Les NaN sont ingorés lors
     * du calcul des statistiques, mais on les compte quand même
     * au passage. Lors de la construction ce champs est initialisé à 0.
     */
    private int nNaN = 0;

    /**
     * Construit des statistiques initialisées à NaN.
     */
    public Statistics() {
    }

    /**
     * Construit des statistics initialisées avec les valeurs spécifiées.
     * Si <code>min</code> est supérieur à <code>max</code>, alors les
     * valeurs de <code>min</code> et <code>max</code> seront automatiquement
     * inversées.
     *
     * @param min Valeur minimale.
     * @param max Valeur maximale.
     * @param sum Somme des valeurs
     * @param sum2 Somme des carrés des valeurs.
     * @param n Nombre de données.
     */
    public Statistics(final double min, final double max,
                      final double sum, final double sum2, final int n)
    {
        this.min  = (min>max) ? max : min;
        this.max  = (max<min) ? min : max;
        this.sum  = sum;
        this.sum2 = Math.abs(sum2);
        this.n    = Math.abs(n);
        this.nNaN = 0;
    }

    /**
     * Réinitialise les statistiques. Cette méthode permet
     * de réutiliser le même objet pour faire de nouvelles
     * statistiques.
     */
    public void reset() {
        min  = Double.NaN;
        max  = Double.NaN;
        sum  = 0;
        sum2 = 0;
        n    = 0;
        nNaN = 0;
    }

    /**
     * Prend en compte une nouvelle donnée dans les calculs statistiques.
     * Typiquement, une application appelle cette méthode dans une boucle
     * balayant chaque données d'un vecteur.
     *
     * @param datum nouvelle donnée à prendre en compte.
     *              Les NaN ne seront pas pris en compte.
     *
     * @see #add(long)
     * @see #add(double[])
     */
    public void add(final double datum) {
        if (!Double.isNaN(datum)) {
            /*
             *  Les deux prochaines lignes utilisent !(a>=b) au
             *  lieu de (a<b) afin de prendre en compte les NaN.
             */
            if (!(min<=datum)) min=datum;
            if (!(max>=datum)) max=datum;
            sum2 += (datum*datum);
            sum  += datum;
            n++;
        } else {
            nNaN++;
        }
    }

    /**
     * Prend en compte une nouvelle donnée dans les calculs statistiques.
     *
     * @param datum Nouvelle donnée à prendre en compte.
     *
     * @see #add(double)
     * @see #add(long[])
     */
    public void add(final long datum) {
        final double fdatum=datum;
        if (!(min<=fdatum)) min=fdatum;
        if (!(max>=fdatum)) max=fdatum;
        sum2 += (fdatum*fdatum);
        sum  += fdatum;
        n++;
    }

    /**
     * Ajoute aux statistiques de cet objet les statistiques
     * de l'objet spécifié en arguments. Rien ne sera fait si
     * cet objet est <code>null</code>.
     *
     * @param stats statistiques à ajouter aux statistiques actuelles.
     */
    public void add(final Statistics stats) {
        if (stats != null) {
            // "if (a<b)" équivaut à "if (!isNaN(a) && a<b)".
            if (Double.isNaN(min) || stats.min<min) min=stats.min;
            if (Double.isNaN(max) || stats.max>max) max=stats.max;
            sum2 += stats.sum2;
            sum  += stats.sum;
            n    += stats.n;
            nNaN += stats.nNaN;
        }
    }

    /**
     * Renvoie le nombre de données NaN qui ont été transmises à
     * la méthode {@link #add(double)}. Les NaN n'interviennent
     * aucunement dans les calculs statistiques, mais sont quand
     * même compté au passage à titre informatif.
     *
     * @return Nombre de données NaN. Ces données auront
     *         été ignorées dans le calcul des statistiques.
     */
    public int countNaN() {
        return nNaN;
    }

    /**
     * Renvoie le nombre de données autres que NaN qui
     * ont été transmises à la méthode {@link #add(double)}.
     *
     * @return nombre de données autre que NaN.
     */
    public int count() {
        return n;
    }

    /**
     * Renvoie la valeur minimale. Si aucune donnée
     * n'a été spécifiée, cette méthode retourne NaN.
     *
     * @return La valeur minimale.
     * @see #maximum
     */
    public double minimum() {
        return min;
    }

    /**
     * Renvoie la valeur maximale. Si aucune donnée
     * n'a été spécifiée, cette méthode retourne NaN.
     *
     * @return La valeur maximale.
     * @see #minimum
     */
    public double maximum() {
        return max;
    }

    /**
     * Retourne la différence entre le maximum
     * et le minimum. Si aucune donnée n'a été
     * spécifiée, cette méthode retourne NaN.
     *
     * @return Le maximum moins le minimum.
     * @see #minimum
     * @see #maximum
     */
    public double range() {
        return max-min;
    }

    /**
     * Renvoie la valeur moyenne. Si aucune donnée
     * n'a été spécifiée, cette méthode retourne NaN.
     *
     * @return La valeur moyenne.
     */
    public double mean() {
        return sum/n;
    }

    /**
     * Renvoie la valeur de la racine des moyennes des carrés (Root Mean Squares).
     * Si aucune donnée n'a été spécifiée, cette méthode retourne NaN.
     */
    public double rms() {
        return Math.sqrt(sum2/n);
    }

    /**
     * Retourne l'écart type des échantillons par rapport à la moyenne. Si les données
     * fournies aux différentes méthodes <code>add(...)</code> se distribuent selon une
     * loi normale, alors l'écart type est la distance de part et d'autre de la moyenne
     * dans lequel se trouveraient environ 84% des données. Le tableau ci-dessous donne
     * le pourcentage approximatif des données que l'on trouve de part et d'autre de la
     * moyenne à des distances telles que 2 ou 3 fois l'écart-type.
     *
     * <table align=center>
     *   <tr><td>&nbsp;0.5&nbsp;</td><td>&nbsp;69.1%&nbsp;</td></tr>
     *   <tr><td>&nbsp;1.0&nbsp;</td><td>&nbsp;84.2%&nbsp;</td></tr>
     *   <tr><td>&nbsp;1.5&nbsp;</td><td>&nbsp;93.3%&nbsp;</td></tr>
     *   <tr><td>&nbsp;2.0&nbsp;</td><td>&nbsp;97.7%&nbsp;</td></tr>
     *   <tr><td>&nbsp;3.0&nbsp;</td><td>&nbsp;99.9%&nbsp;</td></tr>
     * </table>
     *
     * @param allPopulation La valeur <code>true</code> indique que les données fournies
     *        aux différentes méthodes <code>add(...)</code> représentent l'ensemble de
     *        la polulation. La valeur <code>false</code> indique que ces données ne
     *        représentent qu'un échantillon de la population, ce qui est généralement le
     *        cas. Si le nombre de données est élevé, alors les valeurs <code>true</code>
     *        et <code>false</code> donneront sensiblement les mêmes résultats.
     */
    public double standardDeviation(final boolean allPopulation) {
        return Math.sqrt((sum2 - sum*sum/n) / (allPopulation ? n : n-1));
    }

    /**
     * Renvoie une copie de ces statistiques.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            InternalError e=new InternalError(exception.getMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Indique si ces statistiques sont
     * identiques à celles spécifiées.
     */
    public boolean equals(Object obj) {
        if (obj!=null && getClass().equals(obj.getClass())) {
            final Statistics cast = (Statistics) obj;
            return n==cast.n &&
                   Double.doubleToLongBits(min ) == Double.doubleToLongBits(cast.min) &&
                   Double.doubleToLongBits(max ) == Double.doubleToLongBits(cast.max) &&
                   Double.doubleToLongBits(sum ) == Double.doubleToLongBits(cast.sum) &&
                   Double.doubleToLongBits(sum2) == Double.doubleToLongBits(cast.sum2);
        }
        return false;
    }

    /**
     * Renvoie un code "hash value"
     * pour ces statistiques.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(min) ^
                          Double.doubleToLongBits(max) ^
                          Double.doubleToLongBits(sum) ^
                          Double.doubleToLongBits(sum2);
        return (int) code ^ (int) (code >>> 32) ^ n;
    }

    /**
     * Renvoie une représentation textuelle des statistiques.
     * Cette représentation sera de la forme (dans la langue
     * de l'utilisateur):
     *
     * <blockquote><pre>
     *     Compte:      8726
     *     Minimum:    6.853
     *     Maximum:    8.259
     *     Moyenne:    7.421
     *     RMS:        7.846
     *     Écart-type: 6.489
     * </pre></blockquote>
     *
     * Chaque statistiques sera séparée de son étiquette par
     * des espaces blancs. Cette méthode correspond en fait
     * à un appel de <code>{@link #toString(boolean) toString}(false)</code>.
     */
    public final String toString() {
        return toString(false);
    }

    /**
     * Renvoie une représentation textuelle des statistiques. Cette méthode
     * est similaire à {@link #toString()}, mais offre la possibilité de
     * séparer les étiquettes des valeurs par des tabulations ('\t') plutôt
     * que des espaces.
     */
    public String toString(final boolean tabulations) {
        String text = Resources.format(ResourceKeys.STATISTICS_TO_STRING_$6, new Number[] {
            new Integer(count()),
            new Double(minimum()),
            new Double(maximum()),
            new Double(mean()),
            new Double(rms()),
            new Double(standardDeviation(false))
        });
        if (!tabulations) {
            final TableWriter tmp = new TableWriter(null);
            tmp.write(text);
            text = tmp.toString();
        }
        return text;
    }

    /**
     * Statistiques concernant les intervalles entres des nombres réels.
     * Cette classe est utile lorsque l'on veut obtenir l'intervalle
     * d'échantillonnage entre des données ou déterminer si ces données
     * sont en ordre croissant ou décroissant.
     *
     * @version $Id: Statistics.java,v 1.1 2003/01/12 17:53:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    public static class Delta extends Statistics {
        /**
         * Valeur de la dernière donnée qui avait été
         * transmises à la méthode {@link #add(double)}.
         */
        private transient double last = Double.NaN;

        /**
         * Valeur de la dernière donnée qui avait été
         * transmises à la méthode {@link #add(long)}.
         */
        private transient double last_long;

        /**
         * Indique si le dernier appel de <code>add</code>
         * était avec un entier long en argument.
         */
        private transient boolean longValid;

        /**
         * Construit des statistiques initialisées à NaN.
         */
        public Delta() {
        }

        /**
         * Construit des statistics initialisées
         * avec les valeurs spécifiées.
         */
        public Delta(final double min, final double max,
                     final double sum, final double sum2, final int n)
        {
            super(min, max, sum, sum2, n);
        }

        /**
         * Réinitialise ces statistiques à NaN.
         */
        public void reset() {
            super.reset();
            last = Double.NaN;
        }

        /**
         * Ajoute une donnée aux statistiques. Cet objet retiendra des statistiques
         * (minimum, maximum, etc...) au sujet de la différence entre cette donnée
         * et la donnée précédente qui avait été spécifiée lors du dernier appel de
         * cette méthode ou de l'autre méthode {@link #add(long)}.
         */
        public void add(final double datum) {
            super.add(datum-last);
            last      = datum;
            longValid = false;
        }

        /**
         * Ajoute une donnée aux statistiques. Cet objet retiendra des statistiques
         * (minimum, maximum, etc...) au sujet de la différence entre cette donnée
         * et la donnée précédente qui avait été spécifiée lors du dernier appel de
         * cette méthode ou de l'autre méthode {@link #add(double)}.
         */
        public void add(final long datum) {
            if (longValid) {
                super.add(datum-last_long); // add(long)
            } else {
                super.add(datum-last); // add(double)
            }
            last      = datum;
            last_long = datum;
            longValid = true;
        }

        /**
         * Ajoute à ces statistiques celles de l'objet spécifiées. Cette méthode
         * n'accepte que des objets qui de classe <code>DeltaStatistics</code> ou
         * dérivée.
         *
         * @throws ClassCastException Si <code>stats</code> n'est
         *         pas de la classe <code>DeltaStatistics</code>.
         */
        public void add(final Statistics stats) throws ClassCastException {
            super.add((Delta) stats);
        }
    }
}
