/*
 * Units - Temporary implementation for Geotools 2
 * Copyright (C) 1998 University Corporation for Atmospheric Research (Unidata)
 *               1998 Bill Hibbard & al. (VisAD)
 *               1999 Pêches et Océans Canada
 *               2000 Institut de Recherche pour le Développement
 *               2002 Centre for Computational Geography
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Library General Public License for more details (http://www.gnu.org/).
 *
 *
 *    This package is inspired from the units package of VisAD.
 *    Unidata and Visad's work is fully acknowledged here.
 *
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.units;

// Entrés/sorties
import java.io.Serializable;
import java.io.ObjectStreamException;

// Divers
import org.geotools.util.WeakHashSet;
import org.geotools.resources.Utilities;


/**
 * Représente une transformation entre deux unités. Par convention, tout
 * les objets <code>UnitTransform</code> sont toujours imutables. Il est
 * donc sécuritaire de partager plusieurs références vers le même objet.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public abstract class UnitTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 59496814325077015L;

    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;

    /**
     * Unité selon laquelle seront
     * exprimées les valeurs initiales.
     */
    public final Unit fromUnit;

    /**
     * Unité selon laquelle seront
     * exprimées les valeurs finales.
     */
    public final Unit toUnit;

    /**
     * Construit un objet qui aura la charge de convertir
     * des données exprimées selon les unités spécifiées.
     */
    /*protected*/ UnitTransform(final Unit fromUnit, final Unit toUnit) {
        this.fromUnit = fromUnit;
        this.toUnit   = toUnit;
    }

    /**
     * Indique si cette transformation affine représente une transformation idéntitée.
     * L'implémentation par défaut retourne <code>toUnit.equalsIgnoreSymbol(fromUnit)</code>.
     */
    public boolean isIdentity() {
        return toUnit.equalsIgnoreSymbol(fromUnit);
    }

    /**
     * Effectue la conversion d'unités d'une valeur.
     * @param value Valeur exprimée selon les unités {@link #fromUnit}.
     * @return Valeur exprimée selon les unités {@link #toUnit}.
     */
    public abstract double convert(double value);

    /**
     * Effectue la conversion d'unités d'un tableaux de valeurs.
     * L'implémentation par défaut appelle {@link #convert(double)}
     * dans une boucle. Les classes dérivées devraient redéfinir cette
     * méthode avec une implémentation plus efficace.
     *
     * @param values Valeurs exprimées selon les unités {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #toUnit}.
     */
    public void convert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=convert(values[i]);
        }
    }

    /**
     * Effectue la conversion d'unités d'un tableaux de valeurs.
     * L'implémentation par défaut appelle {@link #convert(double)}
     * dans une boucle. Les classes dérivées devraient redéfinir cette
     * méthode avec une implémentation plus efficace.
     *
     * @param values Valeurs exprimées selon les unités {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #toUnit}.
     */
    public void convert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=(float) convert(values[i]);
        }
    }

    /**
     * Effectue la conversion inverse d'unités d'une valeur.
     * @param value Valeur exprimée selon les unités {@link #toUnit}.
     * @return Valeur exprimée selon les unités {@link #fromUnit}.
     */
    public abstract double inverseConvert(double value);

    /**
     * Effectue la conversion inverse d'unités d'un tableaux de valeurs.
     * L'implémentation par défaut appelle {@link #inverseConvert(double)}
     * dans une boucle. Les classes dérivées devraient redéfinir cette
     * méthode avec une implémentation plus efficace.
     *
     * @param values Valeurs exprimées selon les unités {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #fromUnit}.
     */
    public void inverseConvert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=inverseConvert(values[i]);
        }
    }

    /**
     * Effectue la conversion inverse d'unités d'un tableaux de valeurs.
     * L'implémentation par défaut appelle {@link #inverseConvert(double)}
     * dans une boucle. Les classes dérivées devraient redéfinir cette
     * méthode avec une implémentation plus efficace.
     *
     * @param values Valeurs exprimées selon les unités {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #fromUnit}.
     */
    public void inverseConvert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i]=(float) inverseConvert(values[i]);
        }
    }

    /**
     * Retourne un exemplaire unique de cette transformation. Une banque de
     * transformation, initialement vide, est maintenue de façon interne par
     * la classe <code>UnitTransform</code>. Lorsque la méthode <code>intern</code>
     * est appellée, elle recherchera une transformation égale à <code>this</code>
     * au sens de la méthode {@link #equals}. Si une telle transformation fut trouvée,
     * elle sera retournée. Sinon, la trsnsformation <code>this</code> sera ajoutée à
     * la banque de données en utilisant une référence faible et cette méthode retournera
     * <code>this</code>.
     *
     * <p>De cette méthode il s'ensuit que pour deux transformations <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.</p>
     */
    /*public*/ final UnitTransform intern() {
        return (UnitTransform) pool.canonicalize(this);
    }

    /**
     * Indique si cet objet est identique à l'objet spécifié.
     * Les deux objets seront considirés identiques s'ils
     * sont de la même classe et font les conversions à partir
     * de et vers les mêmes unités.
     */
    public boolean equals(final Object o) {
        if (o!=null && getClass().equals(o.getClass())) {
            final UnitTransform ut=(UnitTransform) o;
            return fromUnit.equals(ut.fromUnit) && toUnit.equals(ut.toUnit);
        }
        return false;
    }

    /**
     * Retourne un code représentant
     * cette transformation d'unités.
     */
    public int hashCode() {
        return fromUnit.hashCode() ^ toUnit.hashCode();
    }

    /**
     * Retourne une chaîne de caractères représentant
     * cette transformation. La chaîne sera de la forme
     *
     * <code>UnitTransform[km/h&nbsp;-->&nbsp;m/s]</code>
     */
    public String toString() {
        return Utilities.getShortClassName(this)+'['+fromUnit+" --> "+toUnit+']';
    }
    
    /**
     * Après la lecture d'une transformation, vérifie si cette transformation apparaît
     * déjà dans la banque des unités <code>pool</code>. Si oui, l'exemplaire de la banque
     * sera retourné plutôt que de garder inutilement la transformation courante comme
     * copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
