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
import java.util.Arrays;
import org.geotools.util.WeakHashSet;


/**
 * Ensemble de préfix. Cette classe maintient une liste d'objets
 * {@link Prefix} en ordre croissant et sans doublons, c'est-à-dire qu'elle garanti
 * qu'il n'y aura pas deux préfix représentant la même quantité {@link Prefix#amount}.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
/*public*/ 
final class PrefixSet implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8301096197856692402L;
    
    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;
    
    /**
     * Ensemble de préfix. Les préfix de cet ensemble doivent
     * obligatoirement être un ordre croissant et sans doublons.
     */
    private final Prefix[] prefix;
    
    /**
     * Construit un ensemble de préfix. Le tableau <code>p</code>
     * sera copié, puis classé. Les éventuels doublons seront éliminés.
     * Le tableau <code>p</code> original ne sera pas affecté par ces
     * traitements.
     */
    private PrefixSet(final Prefix[] p) {
        final Prefix[] px=new Prefix[p.length];
        System.arraycopy(p, 0, px, 0, px.length);
        Arrays.sort(px);
        int length=px.length;
        for (int i=length; --i>=1;) {
            if (px[i].amount == px[i-1].amount) {
                px[i]=null;
                length--;
            }
        }
        int i=0;
        prefix=new Prefix[length];
        for (int j=0; j<px.length; j++) {
            if (px[j]!=null) {
                prefix[i++]=px[j];
            }
        }
        assert i==length;
    }
    
    /**
     * Construit un ensemble de préfix. Le tableau <code>p</code>
     * sera copié, puis classé. Les éventuels doublons seront éliminés.
     * Le tableau <code>p</code> original ne sera pas affecté par ces
     * traitements.
     */
    public static PrefixSet getPrefixSet(final Prefix[] p) {
        return new PrefixSet(p).intern();
    }
    
    /**
     * Retourne le préfix représenté par le symbole spéfifié.
     * Si aucun préfix ne correspond à ce symbole, retourne
     * <code>null</code>.
     *
     * @param  symbol Symbole du préfix recherché.
     * @return Préfix désigné par le symbole <code>symbol</code>.
     */
    public Prefix getPrefix(final String symbol) {
        for (int i=0; i<prefix.length; i++) {
            final Prefix p=prefix[i];
            if (symbol.equals(p.symbol)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Retourne le préfix représentant une quantité égale ou inférieure à la quantité spécifiée.
     * Si <code>amount</code> est inférieur à la plus petite quantité pouvant être représenté
     * par un préfix, alors cette méthode retourne <code>null</code>.
     */
    public Prefix getPrefix(double amount) {
        amount += 1E-8*Math.abs(amount); // Pour éviter d'éventuelles erreurs d'arrondissements.
        int index=Arrays.binarySearch(prefix, new Prefix(amount));
        if (index<0) {
            index = ~index;
            if (index==0) {
                return null;
            }
            if (index>prefix.length) {
                index=prefix.length;
            }
            index--;
        }
        return prefix[index];
    }
    
    /**
     * Retourne une chaîne de caractères qui énumère tous les préfix contenu dans
     * cet ensemble. La chaîne sera de la forme "milli(m),centi(c),déci(d),kilo(k)"
     * par exemple.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer();
        for (int i=0; i<prefix.length; i++) {
            final Prefix p=prefix[i];
            final String name=p.getLocalizedName();
            final String symb=p.symbol;
            if (name.length()!=0 || symb.length()!=0) {
                if (buffer.length()!=0) {
                    buffer.append(',');
                }
                buffer.append(name);
                if (symb.length()!=0) {
                    buffer.append('(');
                    buffer.append(symb);
                    buffer.append(')');
                }
            }
        }
        return buffer.toString();
    }
    
    /**
     * Vérifie si cet ensemble est identique à l'objet <code>other</code>
     * spécifié. Deux ensembles sont considérés identiques s'ils contienent
     * les mêmes préfix.
     */
    public boolean equals(final Object other) {
        if (other==this) return true; // slight optimisation
        if (other instanceof PrefixSet) {
            final Prefix[] array = ((PrefixSet) other).prefix;
            if (prefix.length == array.length) {
                for (int i=0; i<array.length; i++) {
                    if (!prefix[i].equals(array[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retourne un code représentant cet ensemble de préfix.
     */
    public int hashCode() {
        int code=prefix.length << 1;
        for (int i=0; i<prefix.length; i+=5) {
            code += prefix[i].hashCode();
        }
        return code;
    }
    
    /**
     * Retourne un exemplaire unique de cet ensemble de préfix. Une banque de préfix, initialement
     * vide, est maintenue de façon interne par la classe <code>PrefixSet</code>. Lorsque la méthode
     * <code>intern</code> est appelée, elle recherchera des préfix égaux à <code>this</code> au
     * sens de la méthode {@link #equals}. Si de tels préfix sont trouvés, ils seront retournés.
     * Sinon, les préfix <code>this</code> seront ajoutés à la banque de données en utilisant une
     * {@link java.lang.ref.WeakReference référence faible} et cette méthode retournera <code>this</code>.
     * <br><br>
     * De cette méthode il s'ensuit que pour deux ensembles de préfix <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    private final PrefixSet intern() {
        return (PrefixSet) pool.canonicalize(this);
    }
    
    /**
     * Après la lecture d'une unité, vérifie si ce préfix
     * apparaît déjà dans la banque des préfix {@link #PREFIX}.
     * Si oui, l'exemplaire de la banque sera retourné plutôt
     * que de garder inutilement le préfix courant comme copie.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
}
