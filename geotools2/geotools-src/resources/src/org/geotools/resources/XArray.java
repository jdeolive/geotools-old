/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
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
 *     UNITED KINDOM: James Macgill
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
import java.lang.System;
import java.lang.reflect.Array;


/**
 * Simple operations on arrays. This class provides a central place for
 * inserting and deleting elements in an array, as well as resizing the array.
 * This class may be removed if JavaSoft provide some language construct
 * functionally equivalent to C/C++'s <code>realloc</code>.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public final class XArray {
    /**
     * Toute construction d'objet
     * de cette classe est interdites.
     */
    private XArray() {
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que
     * <code>array</code> mais avec la longueur <code>length</code> spécifiée.
     * Si la longueur désirée <code>length</code> est plus grande que la
     * longueur initiale du tableau <code>array</code>, alors le tableau
     * retourné contiendra tous les éléments de <code>array</code> avec en plus
     * des éléments initialisés à <code>null</code> à la fin du tableau. Si au
     * contraire la longueur désirée <code>length</code> est plus courte que la
     * longueur initiale du tableau <code>array</code>, alors le tableau sera
     * tronqué (c'est à dire que les éléments en trop de <code>array</code>
     * seront oubliés). Si la longueur de <code>array</code> est égale à
     * <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur
     *         <code>length</code> et contenant les données de
     *         <code>array</code>.
     */
    private static Object doResize(final Object array, final int length) {
        final int current=Array.getLength(array);
        if (current!=length) {
            final Object newArray=Array.newInstance(array.getClass().getComponentType(), length);
            System.arraycopy(array, 0, newArray, 0, Math.min(current, length));
            return newArray;
        }
        else return array;
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à <code>null</code>
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static Object[] resize(final Object[] array, final int length) {
        return (Object[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static double[] resize(final double[] array, final int length) {
        return (double[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static float[] resize(final float[] array, final int length) {
        return (float[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static long[] resize(final long[] array, final int length) {
        return (long[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static int[] resize(final int[] array, final int length) {
        return (int[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static short[] resize(final short[] array, final int length) {
        return (short[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static byte[] resize(final byte[] array, final int length) {
        return (byte[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à 0
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static char[] resize(final char[] array, final int length) {
        return (char[]) doResize(array, length);
    }

    /**
     * Renvoie un nouveau tableau qui contiendra les mêmes éléments que <code>array</code> mais avec la longueur <code>length</code>
     * spécifiée. Si la longueur désirée <code>length</code> est plus grande que la longueur initiale du tableau <code>array</code>,
     * alors le tableau retourné contiendra tous les éléments de <code>array</code> avec en plus des éléments initialisés à <code>false</code>
     * à la fin du tableau. Si au contraire la longueur désirée <code>length</code> est plus courte que la longueur initiale du tableau
     * <code>array</code>, alors le tableau sera tronqué (c'est à dire que les éléments en trop de <code>array</code> seront oubliés).
     * Si la longueur de <code>array</code> est égale à <code>length</code>, alors <code>array</code> sera retourné tel quel.
     *
     * @param  array Tableau à copier.
     * @param  length Longueur du tableau désiré.
     * @return Tableau du même type que <code>array</code>, de longueur <code>length</code> et contenant les données de <code>array</code>.
     */
    public static boolean[] resize(final boolean[] array, final int length) {
        return (boolean[]) doResize(array, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à
     *                retirer. Tous les éléments de <code>array</code> qui
     *                suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec
     *                des éléments retirés. Cette méthode peut retourner
     *                directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    private static Object doRemove(final Object array, final int index, final int length) {
        if (length==0) {
            return array;
        }
        int array_length=Array.getLength(array);
        final Object newArray=Array.newInstance(array.getClass().getComponentType(), array_length-=length);
        System.arraycopy(array, 0,            newArray, 0,                  index);
        System.arraycopy(array, index+length, newArray, index, array_length-index);
        return newArray;
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static Object[] remove(final Object[] array, final int index, final int length) {
        return (Object[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static double[] remove(final double[] array, final int index, final int length) {
        return (double[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static float[] remove(final float[] array, final int index, final int length) {
        return (float[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static long[] remove(final long[] array, final int index, final int length) {
        return (long[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static int[] remove(final int[] array, final int index, final int length) {
        return (int[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static short[] remove(final short[] array, final int index, final int length) {
        return (short[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static byte[] remove(final byte[] array, final int index, final int length) {
        return (byte[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static char[] remove(final char[] array, final int index, final int length) {
        return (char[]) doRemove(array, index, length);
    }

    /**
     * Retire des éléments au milieu d'un tableau.
     *
     * @param array   Tableau dans lequel retirer des éléments.
     * @param index   Index dans <code>array</code> du premier élément à retirer.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'éléments à retirer.
     * @return        Tableau qui contient la données de <code>array</code> avec des éléments retirés.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static boolean[] remove(final boolean[] array, final int index, final int length) {
        return (boolean[]) doRemove(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués d'élements nuls.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec
     *                l'espace suplémentaire. Cette méthode peut retourner
     *                directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    private static Object doInsert(final Object array, final int index, final int length) {
        if (length==0) {
            return array;
        }
        final int array_length=Array.getLength(array);
        final Object newArray=Array.newInstance(array.getClass().getComponentType(), array_length+length);
        System.arraycopy(array, 0,     newArray, 0,            index             );
        System.arraycopy(array, index, newArray, index+length, array_length-index);
        return newArray;
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués d'élements nuls.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static Object[] insert(final Object[] array, final int index, final int length) {
        return (Object[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static double[] insert(final double[] array, final int index, final int length) {
        return (double[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static float[] insert(final float[] array, final int index, final int length) {
        return (float[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> qui suivent cet index peuvent être décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static long[] insert(final long[] array, final int index, final int length) {
        return (long[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static int[] insert(final int[] array, final int index, final int length) {
        return (int[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static short[] insert(final short[] array, final int index, final int length) {
        return (short[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static byte[] insert(final byte[] array, final int index, final int length) {
        return (byte[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de zéros.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static char[] insert(final char[] array, final int index, final int length) {
        return (char[]) doInsert(array, index, length);
    }

    /**
     * Insère des espaces au milieu d'un tableau.
     * Ces "espaces" seront constitués de <code>false</code>.
     *
     * @param array   Tableau dans lequel insérer des espaces.
     * @param index   Index de <code>array</code> où insérer les espaces.
     *                Tous les éléments de <code>array</code> dont l'index est
     *                égal ou supérieur à <code>index</code> seront décalés.
     * @param length  Nombre d'espaces à insérer.
     * @return        Tableau qui contient la données de <code>array</code> avec l'espace suplémentaire.
     *                Cette méthode peut retourner directement <code>dst</code>, mais la plupart du temps
     *                elle retournera un tableau nouvellement créé.
     */
    public static boolean[] insert(final boolean[] array, final int index, final int length) {
        return (boolean[]) doInsert(array, index, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau
     * <code>src</code> sera inséré en totalité ou en partie dans le tableau
     * <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer
     *                dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de
     *                <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de
     *                <code>src</code>. Tous les éléments de <code>dst</code>
     *                dont l'index est égal ou supérieur à <code>dst_pos</code>
     *                seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et
     *                <code>dst</code>. Cette méthode peut retourner directement
     *                <code>dst</code>, mais jamais <code>src</code>. La plupart
     *                du temps, elle retournera un tableau nouvellement créé.
     */
    private static Object doInsert(final Object src, final int src_pos, final Object dst, final int dst_pos, final int length) {
        if (length==0) {
            return dst;
        }
        final int dst_length=Array.getLength(dst);
        final Object newArray=Array.newInstance(dst.getClass().getComponentType(), dst_length+length);
        System.arraycopy(dst, 0,       newArray, 0,              dst_pos           );
        System.arraycopy(src, src_pos, newArray, dst_pos,        length            );
        System.arraycopy(dst, dst_pos, newArray, dst_pos+length, dst_length-dst_pos);
        return newArray;
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static Object[] insert(final Object[] src, final int src_pos, final Object[] dst, final int dst_pos, final int length) {
        return (Object[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static double[] insert(final double[] src, final int src_pos, final double[] dst, final int dst_pos, final int length) {
        return (double[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static float[] insert(final float[] src, final int src_pos, final float[] dst, final int dst_pos, final int length) {
        return (float[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static long[] insert(final long[] src, final int src_pos, final long[] dst, final int dst_pos, final int length) {
        return (long[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static int[] insert(final int[] src, final int src_pos, final int[] dst, final int dst_pos, final int length) {
        return (int[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static short[] insert(final short[] src, final int src_pos, final short[] dst, final int dst_pos, final int length) {
        return (short[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static byte[] insert(final byte[] src, final int src_pos, final byte[] dst, final int dst_pos, final int length) {
        return (byte[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static char[] insert(final char[] src, final int src_pos, final char[] dst, final int dst_pos, final int length) {
        return (char[]) doInsert(src, src_pos, dst, dst_pos, length);
    }

    /**
     * Insère une portion de tableau dans un autre tableau. Le tableau <code>src</code>
     * sera inséré en totalité ou en partie dans le tableau <code>dst</code>.
     *
     * @param src     Tableau à insérer dans <code>dst</code>.
     * @param src_pos Index de la première donnée de <code>src</code> à insérer dans <code>dst</code>.
     * @param dst     Tableau dans lequel insérer des données de <code>src</code>.
     * @param dst_pos Index de <code>dst</code> où insérer les données de <code>src</code>.
     *                Tous les éléments de <code>dst</code> dont l'index est
     *                égal ou supérieur à <code>dst_pos</code> seront décalés.
     * @param length  Nombre de données de <code>src</code> à insérer.
     * @return        Tableau qui contient la combinaison de <code>src</code> et <code>dst</code>. Cette
     *                méthode peut retourner directement <code>dst</code>, mais jamais <code>src</code>.
     *                La plupart du temps, elle retournera un tableau nouvellement créé.
     */
    public static boolean[] insert(final boolean[] src, final int src_pos, final boolean[] dst, final int dst_pos, final int length) {
        return (boolean[]) doInsert(src, src_pos, dst, dst_pos, length);
    }
}
