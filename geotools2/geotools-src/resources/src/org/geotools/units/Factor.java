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
import java.io.IOException;
import java.io.ObjectStreamException;

// Divers
import org.geotools.util.WeakHashSet;


/**
 * Classe représentant une unité de base et la valeur de son exposant.
 * Cette classe est utilisée en argument par la méthode {@link DerivedUnit#getInstance}.
 * Elle n'a pas de constructeur publique; il n'existe pas d'autres moyen de créer de
 * nouveaux objets <code>Factor</code> que d'utiliser la méthode statique
 * {@link #getFactor}.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
/*public*/ 
final class Factor implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7448171508684390207L;
    
    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;
    
    /**
     * L'unité de base de ce facteur.
     */
    public final BaseUnit baseUnit;
    
    /**
     * La valeur de l'exposant associée
     * à l'unité de base de ce facteur.
     */
    public final int power;
    
    /**
     * Construit un facteur qui représentera une
     * unité de base élevée à la puissance spécifiée.
     *
     * @param baseUnit L'unité de base.
     * @param power La puissance à laquelle élever l'unité de base.
     */
    private Factor(final BaseUnit baseUnit, final int power) {
        if (baseUnit!=null) {
            this.baseUnit = baseUnit;
            this.power    = power;
        } else {
            throw new NullPointerException();
        }
    }
    
    /**
     * Retourne un facteur qui représentera une
     * unité de base élevée à la puissance spécifiée.
     *
     * @param baseUnit L'unité de base.
     * @param power La puissance à laquelle élever l'unité de base.
     */
    public static Factor getFactor(final BaseUnit baseUnit, final int power) {
        return new Factor(baseUnit, power).intern();
    }
    
    /**
     * Retourne un facteur dont l'exposant de l'unité
     * est de signe inverse de celui de ce facteur.
     */
    public final Factor inverse() {
        return getFactor(baseUnit, -power);
    }
    
    /**
     * Retourne le symbole de ce facteur. Ce sera le
     * symbole de l'unité de base avec son exposant.
     * Par exemple "m", "m²" ou "kg^-1".
     */
    public String toString() {
        return UnitFormat.DEFAULT.format(this, new StringBuffer()).toString();
    }
    
    /**
     * Indique si ce facteur est identique ou réciproque au facteur spécifié.
     *
     * @param  that L'autre facteur (peut être nul).
     * @return <code>+1</code> Si les deux facteurs sont identiques.<br>
     *         <code>-1</code> Si les deux facteurs sont réciproques (par exemple <i>s</i> et 1/<i>s</i>).<br>
     *         <code> 0</code> Si les deux facteurs ne sont ni identiques ni réciproques, ou si <code>that</code> est nul.
     */
    final int compareDimensionality(final Factor that) {
        if (that!=null && baseUnit.equals(that.baseUnit)) {
            if (power == +that.power) return +1;
            if (power == -that.power) return -1;
        }
        return 0;
    }
    
    /**
     * Vérifie si ce facteur est identique au facteur spécifié. Cette méthode retourne <code>true</code>
     * si les deux facteurs utilisent les mêmes unités {@link #baseUnits} avec la même puissance {@link
     * #power}.
     */
    final boolean equals(final Factor factor) {
        return power==factor.power && baseUnit.equals(factor.baseUnit);
    }
    
    /**
     * Vérifie si ce facteur est identique à l'objet spécifié. Cette méthode retourne <code>true</code>
     * si <code>object</code> est aussi un objet <code>Factor</code> et si les deux facteurs utilisent
     * les mêmes unités {@link #baseUnit} avec la même puissance {@link #power}.
     */
    public boolean equals(final Object object) {
        return (object==this) || // slight optimisation
               ((object instanceof Factor) && equals((Factor) object));
    }
    
    /**
     * Retourne un code à peu près
     * unique pour ce facteur.
     */
    public int hashCode() {
        return baseUnit.hashCode()+power;
    }
    
    /**
     * Retourne un exemplaire unique de ce facteur. Une banque de facteurs, initialement
     * vide, est maintenue de façon interne par la classe <code>Unit</code>. Lorsque la
     * méthode <code>intern</code> est appelée, elle recherchera un facteur égale à
     * <code>this</code> au sens de la méthode {@link #equals}. Si un tel facteur est
     * trouvé, il sera retourné. Sinon, le facteur <code>this</code> sera ajouté à la
     * banque de données en utilisant une référence faible et cette méthode retournera
     * <code>this</code>.
     * <br><br>
     * De cette méthode il s'ensuit que pour deux facteurs <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    private final Factor intern() {
        return (Factor) pool.canonicalize(this);
    }
    
    /**
     * Après la lecture d'un facteur, vérifie si ce facteur
     * apparaît déjà dans la banque {@link Unit#pool}.
     * Si oui, l'exemplaire de la banque sera retourné plutôt
     * que de garder inutilement le facteur courant comme copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
