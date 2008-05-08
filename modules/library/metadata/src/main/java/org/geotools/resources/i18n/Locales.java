/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.resources.i18n;

import java.util.Arrays;
import java.util.Locale;
import org.geotools.resources.Arguments;
import org.geotools.resources.XArray;


/**
 * Static i18n methods.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Locales {
    /**
     * Do not allow instantiation of this class.
     */
    private Locales() {
    }

    /**
     * Returns available locales.
     *
     * @todo Current implementation returns a hard-coded list.
     *       Future implementations may perform a more intelligent work.
     */
    public static Locale[] getAvailableLanguages() {
        return new Locale[] {
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.GERMAN
            // TODO: missing constants for SPANISH, PORTUGUES and GREEK
        };
    }

    /**
     * Returns the list of available locales.
     */
    public static Locale[] getAvailableLocales() {
        final Locale[] languages = getAvailableLanguages();
        Locale[] locales = Locale.getAvailableLocales();
        int count = 0;
        for (int i=0; i<locales.length; i++) {
            final Locale locale = locales[i];
            if (containsLanguage(languages, locale)) {
                locales[count++] = locale;
            }
        }
        locales = (Locale[]) XArray.resize(locales, count);
        return locales;
    }

    /**
     * Returns {@code true} if the specified array of locales contains at least
     * one element with the specified language.
     */
    private static boolean containsLanguage(final Locale[] locales, final Locale language) {
        final String code = language.getLanguage();
        for (int i=0; i<locales.length; i++) {
            if (code.equals(locales[i].getLanguage())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of available locales formatted as string in the specified locale.
     */
    public static String[] getAvailableLocales(final Locale locale) {
        final Locale[] locales = getAvailableLocales();
        final String[] display = new String[locales.length];
        for (int i=0; i<locales.length; i++) {
            display[i] = locales[i].getDisplayName(locale);
        }
        Arrays.sort(display);
        return display;
    }

    /**
     * Prints the list of available locales.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        final String[] locales = getAvailableLocales(arguments.locale);
        for (int i=0; i<locales.length; i++) {
            arguments.out.println(locales[i]);
        }
    }
}
