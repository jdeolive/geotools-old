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


/**
 * Représentation d'un préfix du système métrique. Un objet <code>Prefix</code>
 * peut par exemple représenter des "centi" (symbole "c") comme dans "centimètres"
 * (symbole "cm"). La description du paquet <code>org.geotools.units</code> donne
 * une liste des préfix standards du système SI.
 *
 * <p><em>Note: this class has a natural ordering that is inconsistent with equals.</em>
 * La méthode {@link #compareTo} ne compare que le champ {@link #amount}, tandis
 * que la méthode {@link #equals} compare tous les champs ({@link #name},
 * {@link #symbol} et {@link #amount}).</p>
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
/*public*/ 
final class Prefix implements Comparable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3289659964721709283L;
    
    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    static final WeakHashSet pool=new WeakHashSet();
    
    /**
     * Nom neutre du préfix. Le système SI définit plusieurs noms de préfix, parmi lesquels on trouve
     * "milli", "centi" et "kilo". Certaines unités (notamment des unités du type {@link ScaledUnit})
     * pourront combiner leurs noms avec un nom de préfix. Par exemple le préfix "centi" (symbole "c")
     * pourra être combiné avec les unités "mètres" (symbole "m") pour former les "centimètres" (symbole
     * "cm"). La chaîne <code>name</code> peut être vide, mais ne sera jamais nulle. Notez enfin que
     * <code>name</code> est "language-neutral". Pour obtenir un nom dans la langue de l'utilisateur,
     * utilisez la méthode {@link #getLocalizedName}.
     */
    public final String name;
    
    /**
     * Symbole du préfix. La plupart des symboles de préfix n'ont qu'une seule lettre. Il s'agit
     * la plupart du temps de la première lettre de <code>name</code>, parfois en majuscule. Les
     * majuscules et minuscules sont significatifs et très importants. Par exemple le symbole "m"
     * est pour "milli" tandis que le symbole "M" est pour "mega".
     */
    public final String symbol;
    
    /**
     * Quantité représenté par ce préfix. Pour les préfix SI, cette quantité est toujours une puissance de 10.
     * Par exemple pour les "kilo" (symbole 'k'), la quantité <code>amount</code> est 1000. Cette quantité ne
     * sera jamais <code>NaN</code> ni infinie.
     */
    public final double amount;
    
    /**
     * Construit un préfix temporaire. Ce constructeur ne sert qu'à effectuer
     * des recherches dans une liste de préfix par {@link PrefixSet}.
     */
    Prefix(final double amount) {
        this.name   = "";
        this.symbol = "";
        this.amount = amount;
    }
    
    /**
     * Construit un nouveau préfix.
     *
     * @param name    Nom du préfix (par exemple "centi" comme dans "centimètres").
     * @param symbol  Symbole du préfix (par exemple "c" pour "centimètres").
     * @param amount  Quantité représenté par ce préfix (par exemple 0.01 pour "c").
     */
    private Prefix(final String name, final String symbol, final double amount) {
        this.name   = name.trim();
        this.symbol = symbol.trim();
        this.amount = amount;
        if (!(amount>0) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Construit un nouveau préfix.
     *
     * @param name    Nom du préfix (par exemple "centi" comme dans "centimètres").
     * @param symbol  Symbole du préfix (par exemple "c" pour "centimètres").
     * @param amount  Quantité représenté par ce préfix (par exemple 0.01 pour "c").
     */
    public static Prefix getPrefix(final String name, final String symbol, final double amount) {
        return new Prefix(name, symbol, amount).intern();
    }
    
    /**
     * Retourne le nom du préfix dans la langue de l'utilisateur.
     * Par exemple le préfix "deci" est écrit "déci" en français.
     */
    public String getLocalizedName() {
        return org.geotools.resources.units.Prefix.localize(name);
    }
    
    /**
     * Retourne le symbole du préfix. Cette méthode retourne
     * systématiquement le champ {@link #symbol}.
     */
    public String toString() {
        return symbol;
    }
    
    /**
     * Compare deux préfix. Cette méthode compare les quantités {@link #amount} de façon à permettre un classement
     * des préfix en ordre croissant de quantité. Contrairement à la méthode {@link #equals}, <code>compareTo</code>
     * ne compare pas les noms et symboles des préfix. Ainsi, deux préfix représentant la même quantité mais avec
     * des symboles différents seront considérés égaux par <code>compareTo</code>.
     */
    public int compareTo(final Object object) {
        final Prefix that = (Prefix) object;
        if (this.amount > that.amount) return +1;
        if (this.amount < that.amount) return -1;
        return 0;
    }
    
    /**
     * Indique si ce préfix est identique à l'objet spécifié.
     * Cette méthode retourne <code>true</code> si <code>object</code> est aussi un
     * objet <code>Prefix</code> et si les deux préfix ont les mêmes nom et symbole
     * et représentent la même quantité {@link #amount}.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            return true; // slight optimisation
        }
        if (object instanceof Prefix) {
            final Prefix prefix = (Prefix) object;
            return Double.doubleToLongBits(amount)==Double.doubleToLongBits(prefix.amount) &&
            symbol         .equals          (prefix.symbol) &&
            name           .equals          (prefix.name);
        }
        return false;
    }
    
    /**
     * Retourne un code représentant ce préfix.
     */
    public int hashCode() {
        final long code=Double.doubleToLongBits(amount);
        return (int) code ^ (int) (code >>> 32);
    }
    
    /**
     * Retourne un exemplaire unique de ce préfix. Une banque de préfix, initialement
     * vide, est maintenue de façon interne par la classe <code>Prefix</code>. Lorsque
     * la méthode <code>intern</code> est appelée, elle recherchera un préfix égale à
     * <code>this</code> au sens de la méthode {@link #equals}. Si un tel préfix est
     * trouvé, il sera retourné. Sinon, le préfix <code>this</code> sera ajouté à la
     * banque de données en utilisant une référence faible et cette méthode retournera
     * <code>this</code>.
     * <br><br>
     * De cette méthode il s'ensuit que pour deux préfix <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    private final Prefix intern() {
        return (Prefix) pool.canonicalize(this);
    }
    
    /**
     * Après la lecture d'une unité, vérifie si ce préfix
     * apparaît déjà dans la banque des préfix. Si oui,
     * l'exemplaire de la banque sera retourné plutôt
     * que de garder inutilement le préfix courant comme copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
