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

// Formattage de textes
import java.text.Format;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;

// Ecriture de nombres
import java.text.NumberFormat;
import java.text.DecimalFormat;

// Collections
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.geotools.util.WeakHashSet;

// Divers
import org.geotools.resources.XMath;
import org.geotools.resources.Utilities;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Classe chargée du formattage ou de l'interprétation des symboles d'une unité.
 * <strong>Cette classe n'est qu'un premier jet</strong>. Elle ne contient pas encore d'API qui
 * permettrait de contrôler la façon d'écrire les unités. Plus important, le contrat général voulant
 * que tout objet produit par {@link #format(Object)} soit lissible par {@link #parse(String)} <u>n'est
 * pas garanti</u> dans la version actuelle.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class UnitFormat {
    /**
     * Format par défaut à utiliser pour construire et interpréter les symboles des unités.
     * Ce format sera utilisé par les constructeurs des différentes classes {@link Unit}
     * pour créer des symboles, ainsi que par la méthode {@link Unit#getUnit} pour interpréter
     * un symbole.
     */
    static final UnitFormat DEFAULT=new UnitFormat();
    
    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool;
    
    /**
     * Inverse d'une petite valeur servant à éviter des erreurs d'arrondissements.
     * Cette valeur est définie arbitrairement à 2^24, soit environ 1.678E+7.
     */
    private static final double INV_EPS = 16777216;
    
    /**
     * Ordre de préférences des classes d'unités. Cette ordre sera pris en
     * compte si plusieurs unités ont été trouvés pour un symbole donné.
     */
    private static final Class[] PRIORITIES=new Class[] {
        BaseUnit.class,
        DerivedUnit.class,
        ScaledUnit.class,
        OffsetUnit.class
    };
    
    /**
     * Symbole représentant la multiplication d'une unité par un facteur.
     */
    private static final char SCALE_SYMBOL = '\u00D7';
    
    /**
     * Symbole représentant la multiplication de deux unités.
     */
    private static final char DOT_SYMBOL = '·'; // TODO: on devrait plutôt utiliser '\u22C5', mais ce symbole n'est pas affiché correctement.
    
    /**
     * Symbole représentant la division de deux unités.
     */
    private static final char SLASH_SYMBOL = '/';
    
    /**
     * Symbole de l'opérateur exposant.
     */
    private static final char EXPONENT_SYMBOL='^';
    
    /**
     * Symbole représentant l'ouverture d'une parenthèse.
     */
    private static final char OPEN_SYMBOL = '(';
    
    /**
     * Symbole représentant la fermeture d'une parenthèse.
     */
    private static final char CLOSE_SYMBOL = ')';
    
    /**
     * Construit un objet qui lira et écrira
     * des unités avec les paramètres par défaut.
     */
    public UnitFormat()
    {}
    
    /**
     * Construit un symbole à partir du facteur spécifié. Ce sera le symbole
     * de l'unité de base avec son exposant. Par exemple "m", "m²" ou "kg^-1".
     */
    final StringBuffer format(final Factor factor, final StringBuffer buffer) {
        if (factor.power!=0) {
            buffer.append(factor.baseUnit.symbol);
            if (factor.power!=1) {
                final String power = String.valueOf(factor.power);
                final int   length = power.length();
                final int  initPos = buffer.length();
                for (int i=0; i<length; i++) {
                    final char c=power.charAt(i);
                    final char s=Utilities.toSuperScript(c);
                    if (s==c) {
                        buffer.setLength(initPos);
                        buffer.append(EXPONENT_SYMBOL);
                        buffer.append(power);
                        return buffer;
                    }
                    buffer.append(s);
                }
            }
        } else {
            buffer.append('1');
        }
        return buffer;
    }
    
    /**
     * Construit un symbole à partir du tableau de facteurs spécifiés. L'implémentation par défaut place
     * au début les unités qui ont une puissance positive, puis à la droite d'un signe "/" les unités qui
     * ont une puissance négative. Le résultat ressemblera par exemple à "m/s". Si nécessaire, des parenthèses
     * seront utilisées.
     */
    final StringBuffer format(final Factor[] factors, StringBuffer buffer) {
        /*
         * Ajoute au buffer tous les termes qui se trouvent comme numérateurs
         * (puissance positive).  Au passage, on comptera le nombre de termes
         * qui se trouvent comme dénominateurs (puissance négative).
         */
        int numeratorCount   = 0;
        int denominatorCount = 0;
        for (int i=0; i<factors.length; i++) {
            final Factor factor=factors[i];
            if (factor.power>0) {
                if (numeratorCount!=0) buffer.append(DOT_SYMBOL);
                buffer=format(factor, buffer);
                numeratorCount++;
            } else if (factor.power<0) {
                denominatorCount++;
            }
        }
        /*
         * Ajoute au buffer tous les termes qui se trouvent comme dénominateurs
         * (puissance négative), s'il y en a.
         */
        if (denominatorCount!=0) {
            if (numeratorCount==0)   buffer.append('1');
            buffer.append(SLASH_SYMBOL);
            if (denominatorCount!=1) buffer.append(OPEN_SYMBOL);
            denominatorCount=0;
            for (int i=0; i<factors.length; i++) {
                final Factor factor=factors[i];
                if (factor.power<0) {
                    if (denominatorCount!=0) buffer.append(DOT_SYMBOL);
                    buffer=format(factor.inverse(), buffer);
                    denominatorCount++;
                }
            }
            if (denominatorCount!=1) {
                buffer.append(CLOSE_SYMBOL);
            }
        }
        return buffer;
    }
    
    /**
     * Renvoie une chaîne de caractères représentant la multiplication d'un symbole par un facteur.
     * Par exemple cette représentation pourrait être de la forme <code>"\u00D70,9144\u00A0m"</code>
     * pour représenter un yard. Si le symbole <code>unit</code> autorise l'utilisation de préfix, un
     * préfix pourra être placé devant le symbole plutôt que d'écrire le facteur. C'est le cas par
     * exemple des centimètres qui peuvent être écrits comme "cm".
     */
    final StringBuffer formatScaled(double amount, final SimpleUnit unit, StringBuffer buffer) {
        String          symbol = unit.symbol;
        final int       length = symbol.length();
        final int      initPos = buffer.length();
        final PrefixSet prefix = unit.prefix;
        if (prefix!=null) {
            /*
             * Commence par vérifier si le symbole de <code>unit</code> commençait
             * déjà par un des préfix. Si oui, on supprimera cet ancien préfix.
             */
            final String unprefixedSymbol = unit.getUnprefixedSymbol();
            if (symbol.endsWith(unprefixedSymbol)) // Test only to make sure...
            {
                final Prefix p=prefix.getPrefix(symbol.substring(0, symbol.length()-unprefixedSymbol.length()));
                if (p!=null) {
                    symbol=unprefixedSymbol;
                    amount *= p.amount;
                }
            }
            /*
             * Essaie de placer un nouveau préfix devant
             * le symbole, en fonction de l'échelle.
             */
            final Prefix p=prefix.getPrefix(amount);
            if (p!=null) {
                symbol = p.symbol+symbol;
                amount /= p.amount;
            }
        }
        /*
         * Si <code>amount</code> est presqu'une puissance de 10, arrondi
         * à la puissance de 10 la plus proche. Cette étape vise à réduire
         * certaines erreurs d'arrondissement.
         */
        final double power = Math.rint(XMath.log10(amount)*INV_EPS)/INV_EPS;
        if (power==Math.rint(power)) amount=XMath.pow10(power);
        /*
         * Si on n'a pas pu placer un préfix devant les unités,
         * alors on écrira un symbole de multiplication.
         */
        if (amount!=1) {
            final NumberFormat format=NumberFormat.getNumberInstance();
            buffer.append(SCALE_SYMBOL);
            format.format(amount, buffer, new FieldPosition(0));
            if (length!=0) {
                buffer.append('\u00A0'); // No-break space
            }
        }
        buffer.append(symbol);
        return buffer;
    }
    
    /**
     * Renvoie une chaîne de caractères représentant le décalage d'une unité. Par exemple cette représentation
     * pourrait être de la forme <code>"+273.15\u00A0K</code> pour représenter des degrés Celsius.
     */
    final StringBuffer formatOffset(final double offset, final Unit unit, StringBuffer buffer) {
        final String       symbol = unit.toString();
        final int          length = symbol.length();
        final NumberFormat format = NumberFormat.getNumberInstance();
        if (format instanceof DecimalFormat) {
            final DecimalFormat cast=(DecimalFormat) format;
            if (cast.getPositivePrefix().trim().length()==0) {
                cast.setPositivePrefix("+");
            }
        }
        format.format(offset, buffer, new FieldPosition(0));
        if (length!=0) {
            buffer.append('\u00A0'); // No-break space
            buffer.append(symbol);
        }
        return buffer;
    }
    
    /**
     * Retourne les unités qui correspondent au symbole spécifié. Si plus d'une
     * unité correspond au symbole spécifié, une unité arbitraire sera choisie.
     *
     * @param  symbol Symbole des unités recherchées. Cet argument ne doit pas être nul.
     * @return Si les unités ont été trouvés, l'objet {@link Unit} qui les représentent.
     *         Sinon, un objet {@link String} contenant la portion de chaîne qui n'a pas
     *         été reconnue.
     * @throws IllegalArgumentException si les parenthèses ne sont pas équilibrées.
     */
    final Object parse(final String symbol) throws IllegalArgumentException {
        final Set set=new HashSet(11);
        final String unrecognized=parse(symbol.replace('*', DOT_SYMBOL), set);
        final Unit[] units = (Unit[]) set.toArray(new Unit[set.size()]);
        switch (units.length) {
            case 0:  return unrecognized;
            case 1:  return units[0];
            default: return selectUnit(units);
        }
    }
    
    /**
     * Recherche des unités qui correspondent au symbole spécifié. Les unités trouvés seront ajoutés
     * dans l'ensemble <code>set</code>. Si aucune unité n'a été trouvée, la taille de <code>set</code>
     * n'aura pas augmentée. Dans ce cas, cette méthode retourne les caractères qui n'ont pas été reconnus.
     *
     * @param  symbol Symbole des unités recherchées.
     * @param  set Ensemble dans lequel placer les unités trouvées.
     * @return <code>null</code> si des unités ont été trouvées, ou sinon la portion
     *         de la chaîne <code>symbol</code> qui n'a pas été reconnue.
     * @throws IllegalArgumentException si les parenthèses ne sont pas équilibrées.
     */
    private String parse(String symbol, final Set set) throws IllegalArgumentException {
        symbol=symbol.trim();
        final int initialSize=set.size();
        /*
         * Ignore les parenthèses qui se trouvent au début ou à la fin des unités. Les éventuelles
         * parenthèses qui se trouverait au milieu ne sont pas pris en compte maintenant. Elles le
         * seront plus tard.   On ignore toujours le même nombre de parenthèses ouvrantes au début
         * que de parenthèses fermantes à la fin.
         */
        if (true) {
            int lower=0;
            int upper=0;
            int level=0;
            int index=0;
            final int length=symbol.length();
            while (index<length) {
                switch (symbol.charAt(index)) {
                    case  OPEN_SYMBOL: level++; index++; break;
                    case CLOSE_SYMBOL: level--; index++; break;
                    default: {
                        if (upper==0) lower=index;
                        upper=++index;
                        break;
                    }
                }
                if (level==0) break;
            }
            if (level!=0) {
                throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_NON_EQUILIBRATED_PARENTHESIS_$2,
                                symbol, String.valueOf(level>=0 ? CLOSE_SYMBOL : OPEN_SYMBOL)));
            }
            if (index==length) {
                upper=length-upper;
                if (lower>upper) lower=upper;
                if (upper>lower) upper=lower;
                upper=length-upper;
                symbol=symbol.substring(lower, upper);
            }
        }
        /*
         * Recherche les symboles de divisions ou de multiplications. Si un tel symbole est trouvé, on lira séparament
         * les unités qui précèdent et qui suivent ce symbole. On ne prend en compte que les symboles qui ne se trouvent
         * pas dans une parenthèse. Si un symbole se trouve dans une parenthèse, il sera pris en compte plus tard.
         */
        if (true) {
            int level=0;
            String unrecognized=null;
            for (int i=symbol.length(); --i>=0 && level<=0;) {
                final int power;
                switch (symbol.charAt(i)) {
                    case  OPEN_SYMBOL: level++; continue;
                    case CLOSE_SYMBOL: level--; continue;
                    case   DOT_SYMBOL: power=+1; break;
                    case SLASH_SYMBOL: power=-1; break;
                    default : continue;
                }
                if (level!=0) continue;
                /*
                 * Un signe de multiplication ou d'addition a été trouvé.
                 * Lit d'abord les unités avant ce signe, puis après ce signe.
                 */
                String tmp;
                final Set unitsA=new HashSet(11);
                final Set unitsB=new HashSet(11);
                tmp=parse(symbol.substring(0,i), unitsA); if (unrecognized==null) unrecognized=tmp;
                tmp=parse(symbol.substring(i+1), unitsB); if (unrecognized==null) unrecognized=tmp;
                for (final Iterator itA=unitsA.iterator(); itA.hasNext();) {
                    final Unit unitA = (Unit) itA.next();
                    for (final Iterator itB=unitsB.iterator(); itB.hasNext();) {
                        final Unit unitB = (Unit) itB.next();
                        try {
                            final Unit unit;
                            switch (power) {
                                case -1: unit=unitA.divide  (unitB);            break;
                                case +1: unit=unitA.multiply(unitB);            break;
                                default: unit=unitA.multiply(unitB.pow(power)); break;
                            }
                            set.add(unit);
                        } catch (UnitException exception) {
                            // ignore incompatible units.
                        }
                    }
                }
                return (set.size()==initialSize) ? unrecognized : null;
            }
            if (level!=0) {
                throw new IllegalArgumentException(Resources.format(
                                ResourceKeys.ERROR_NON_EQUILIBRATED_PARENTHESIS_$2,
                                symbol, String.valueOf(level>=0 ? CLOSE_SYMBOL : OPEN_SYMBOL)));
            }
        }
        /*
         * Parvenu à ce stade, on n'a détecté aucun symbole de multiplication ou de division
         * et aucune parenthèses. Il ne devrait rester que le symbole de l'unité, éventuellement
         * avec son préfix et un exposant. On tente maintenant d'interpréter ce symbole.
         */
        int           power = 1;
        boolean powerParsed = false;
        /*
         * La boucle suivante sera exécutée deux fois. La première fois, on n'aura pas tenté de prendre en compte
         * une éventuelle puissance après le symbole (par exemple le '2' dans "m²"), parce que le symbole avec sa
         * puissance a peut-être été déjà explicitement définie.   Si cette tentative a échoué, alors le deuxième
         * passage de la boucle prendra en compte un éventuel exposant.
         */
        while (true) {
            final int length=symbol.length();
            for (int lower=0; lower<length; lower++) {
                Unit unit=Unit.getCached(symbol.substring(lower));
                if (unit!=null) {
                    /*
                     * Parvenu à ce stade, nous avons trouvé une unité qui correspond au symbole <code>symbol</code>.
                     * S'il a fallu sauter des caractères pour trouver cette unité, alors les caractères ignorés doivent
                     * être un préfix. On tentera d'identifier le préfix en interrogeant la liste des préfix autorisés
                     * pour cette unité.
                     */
                    if (lower!=0) {
                        if (unit.prefix==null) continue;
                        final Prefix prefix=unit.prefix.getPrefix(symbol.substring(0, lower));
                        if (prefix==null) continue;
                        unit=unit.scale(prefix.amount);
                    }
                    /*
                     * Tente maintenant d'élever les unités à une puissance,
                     * s'ils sont suivit d'une puissance.
                     */
                    try {
                        set.add(unit.pow(power));
                    } catch (UnitException exception) {
                        continue;
                    }
                }
            }
            /*
             * Si c'est le second passage de la boucle, la puissance a déjà été
             * prise en compte. On terminera alors cette méthode maintenant.
             */
            if (powerParsed) {
                return (set.size()==initialSize) ? symbol : null;
            }
            powerParsed=true;
            /*
             * Si aucune unité n'a été trouvée lors du premier passage de la boucle, tente maintenant de prendre en compte
             * une éventuelle puissance qui aurait été spécifiée après les unités (comme par exemple le '2' dans "m²"). On
             * supposera que la puissance commence soit après le dernier caractère qui n'est pas un exposant, ou soit après
             * le symbole '^'.
             */
            int expStart;
            int symbolEnd=symbol.lastIndexOf(EXPONENT_SYMBOL);
            if (symbolEnd>=0) {
                // Positionne 'expStart' après le symbole '^'.
                expStart = symbolEnd+1;
            } else {
                for (symbolEnd=length; --symbolEnd>=0;) {
                    if (!Utilities.isSuperScript(symbol.charAt(symbolEnd))) {
                        symbolEnd++;
                        break;
                    }
                }
                // Il n'y a pas de symbole '^' à sauter pour 'expStart'.
                expStart = symbolEnd;
            }
            /*
             * Maintenant qu'on a séparé le symbole de l'exposant, tente d'interpréter l'exposant. Si l'interprétation
             * échoue, ou s'il n'y a pas d'exposant ou de symbole, alors ce n'est pas la peine de faire le deuxième
             * passage de la boucle; on fera donc un "break".
             */
            if (symbolEnd>=1 && expStart<length) {
                final String powerText; {
                    final StringBuffer tmp=new StringBuffer(symbol.substring(expStart));
                    for (int i=tmp.length(); --i>=0;) tmp.setCharAt(i, Utilities.toNormalScript(tmp.charAt(i)));
                    powerText=tmp.toString();
                }
                symbol=symbol.substring(0, symbolEnd);
                try {
                    power=Integer.parseInt(powerText);
                } catch (NumberFormatException exception) {
                    // TODO: le message d'erreur de 'Unit.getUnit(String)' n'est pas
                    //       vraiment approprié lorsqu'on retourne 'powerText'.
                    return (set.size()==initialSize) ? powerText : null;
                }
            } else {
                return (set.size()==initialSize) ? symbol : null;
            }
        }
    }
    
    /**
     * Sélectionne une unité. Cette méthode est appelée automatiquement par la méthode
     * {@link #parse} si elle a trouvé plusieurs unités qui utilisent le même symbole.
     * L'implémentation par défaut tentera de retourner de préférence une unité de la
     * classe {@link BaseUnit} ou {@link DerivedUnit}.
     * Les classes dérivées peuvent redéfinir cette méthode pour sélectionner une unité
     * selon d'autres critères, par exemple en demandant à l'utilisateur de choisir.
     *
     * @param  units Liste d'unités parmi lesquelles il faut faire un choix.
     *         La longueur de ce tableau sera d'au moins 2.
     * @return Unité choisie. Il n'est pas obligatoire que cette unité fasse
     *         partie du tableau <code>units</code> original.
     */
    protected Unit selectUnit(final Unit[] units) {
        for (int i=0; i<PRIORITIES.length; i++) {
            final Class c=PRIORITIES[i];
            for (int j=0; j<units.length; j++) {
                final Unit u=units[j];
                if (c.isAssignableFrom(u.getClass())) {
                    return u;
                }
            }
        }
        return units[0];
    }
}
