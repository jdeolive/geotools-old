/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.io.image;

// Colors
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;

// Input/output
import java.net.URL;
import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import javax.imageio.IIOException;
import java.nio.charset.Charset;
import java.text.ParseException;

// Miscellaneous
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;

// Resources
import org.geotools.io.LineFormat;
import org.geotools.resources.ImageUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * A factory class for {@link IndexColorModel} objects.
 * Default implementation for this class create {@link IndexColorModel} objects from
 * palette definition files. Definition files are text files containing an arbitrary
 * number of lines, each line containing RGB components ranging from 0 to 255 inclusive.
 * Empty line and line starting with '#' are ignored. Example:
 *
 * <blockquote><pre>
 * # RGB codes for SeaWiFs images
 * # (chlorophylle-a concentration)
 *
 *   033   000   096
 *   032   000   097
 *   031   000   099
 *   030   000   101
 *   029   000   102
 *   028   000   104
 *   026   000   106
 *   025   000   107
 * <i>etc...</i>
 * </pre></blockquote>
 *
 * The number of RGB codes doesn't have to match an {@link IndexColorModel}'s
 * map size. RGB codes will be automatically interpolated RGB values when needed.
 *
 * @version $Id: PaletteFactory.java,v 1.2 2002/07/23 17:53:36 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PaletteFactory {
    /**
     * The parent factory, or <code>null</code> if there is none.
     * The parent factory will be queried if a palette was not
     * found in current factory.
     */
    private final PaletteFactory parent;
    
    /**
     * The class loader from which to load the palette definition files.
     * If <code>null</code>, loading will occurs from the system current
     * working directory.
     */
    private final ClassLoader loader;
    
    /**
     * The base directory from which to search for palette definition files.
     * If <code>null</code>, then the working directory (".") is assumed.
     */
    private final File directory;
    
    /**
     * File extension.
     */
    private final String extension;
    
    /**
     * The charset to use for parsing files, or
     * <code>null</code> for the current default.
     */
    private final Charset charset;
    
    /**
     * The locale to use for parsing files. or
     * <code>null</code> for the current default.
     */
    private final Locale locale;
    
    /**
     * Construct a palette factory.
     *
     * @param parent    The parent factory, or <code>null</code> if there is none.
     *                  The parent factory will be queried if a palette was not
     *                  found in current factory.
     * @param loader    The class loader from which to load the palette definition files.
     *                  If <code>null</code>, loading will occurs from the system current
     *                  working directory.
     * @param directory The base directory from which to search for palette definition files.
     *                  If <code>null</code>, then <code>"."</code> is assumed.
     * @param extension File name extension, or <code>null</code> if there is no extension
     *                  to add to filename. If non-null, this extension will be automatically
     *                  appended to filename. It should starts with the character <code>'.'</code>.
     * @param charset   The charset to use for parsing files, or
     *                  <code>null</code> for the current default.
     * @param locale    The locale to use for parsing files. or
     *                  <code>null</code> for the current default.
     */
    public PaletteFactory(final PaletteFactory parent,
                          final ClassLoader    loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale)
    {
        if (extension!=null && !extension.startsWith(".")) {
            extension = "." + extension;
        }
        this.parent    = parent;
        this.loader    = loader;
        this.directory = directory;
        this.extension = extension;
        this.charset   = charset;
        this.locale    = locale;
    }
    
    /**
     * Returns a buffered reader for the specified name.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @return A buffered reader to read <code>name</code>.
     * @throws IOException if an I/O error occured.
     */
    private BufferedReader getReader(String name) throws IOException {
        if (extension!=null && !name.endsWith(extension)) {
            name += extension;
        }
        final File file = new File(directory, name);
        final InputStream stream;
        if (loader!=null) {
            stream = loader.getResourceAsStream(file.getPath());
        } else {
            stream = file.exists() ? new FileInputStream(file) : null;
        }
        if (stream==null) {
            return null;
        }
        return getReader(stream);
    }
    
    /**
     * Returns a buffered reader for the specified stream.
     *
     * @param  The input stream.
     * @return A buffered reader to read the input stream.
     * @throws IOException if an I/O error occured.
     */
    private BufferedReader getReader(final InputStream stream) throws IOException {
        final Reader reader = (charset!=null) ? new InputStreamReader(stream, charset) :
                                                new InputStreamReader(stream);
        return new BufferedReader(reader);
    }
    
    /**
     * Procède au chargement d'un ensemble de couleurs. Les couleurs doivent
     * être codées sur trois colonnes dans un fichier texte. Les colonnes
     * doivent être des entiers de 0 à 255 correspondant (dans l'ordre) aux
     * couleurs rouge (R), verte (G) et bleue (B). Les lignes vierges ainsi
     * que les lignes dont le premier caractère non-blanc est # seront ignorées.
     *
     * @param  input Flot contenant les codes de couleurs de la palette.
     * @return Couleurs obtenues à partir des codes lues.
     * @throws IOException si une erreur est survenue lors de la lecture.
     * @throws IIOException si une erreur est survenue lors de l'interprétation des codes de couleurs.
     */
    private Color[] getColors(final BufferedReader input) throws IOException {
        int values[]=new int[3]; // On attend exactement 3 composantes par ligne.
        final LineFormat reader = (locale!=null) ? new LineFormat(locale) : new LineFormat();
        final List colors       = new ArrayList();
        String line; while ((line=input.readLine())!=null) try {
            line=line.trim();
            if (line.length()==0)        continue;
            if (line.charAt(0)=='#')     continue;
            if (reader.setLine(line)==0) continue;
            values = reader.getValues(values);
            colors.add(new Color(byteValue(values[0]), byteValue(values[1]), byteValue(values[2])));
        } catch (ParseException exception) {
            final IIOException error = new IIOException(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return (Color[]) colors.toArray(new Color[colors.size()]);
    }
    
    /**
     * Load colors from a definition file.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @return The set of colors, or <code>null</code> if the set was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public Color[] getColors(final String name) throws IOException {
        final BufferedReader reader = getReader(name);
        if (reader==null) {
            return (parent!=null) ? parent.getColors(name) : null;
        }
        final Color[] colors = getColors(reader);
        reader.close();
        return colors;
    }
    
    /**
     * Load colors from an URL.
     *
     * @param  The palette's URL.
     * @return The set of colors, or <code>null</code> if the set was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public Color[] getColors(final URL url) throws IOException {
        final BufferedReader reader = getReader(url.openStream());
        final Color[] colors = getColors(reader);
        reader.close();
        return colors;
    }
    
    /**
     * Load an index color model from a definition file.
     * The returned model will use index from 0 to 255 inclusive.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @return The index color model, or <code>null</code> if the palettes was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public IndexColorModel getIndexColorModel(final String name) throws IOException {
        return getIndexColorModel(name, 0, 256);
    }
    
    /**
     * Load an index color model from a definition file.
     * The returned model will use index from <code>lower</code> inclusive to
     * <code>upper</code> exclusive. Other index will have transparent color.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @param  lower Palette's lower index (inclusive).
     * @param  upper Palette's upper index (exclusive).
     * @return The index color model, or <code>null</code> if the palettes was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    private IndexColorModel getIndexColorModel(final String name,
                                               final int    lower,
                                               final int    upper)
        throws IOException
    {
        final Color[] colors=getColors(name);
        if (colors==null) {
            return (parent!=null) ? parent.getIndexColorModel(name, lower, upper) : null;
        }
        final int[] ARGB = new int[1 << ImageUtilities.getBitCount(upper)];
        ImageUtilities.expand(colors, ARGB, lower, upper);
        return ImageUtilities.getIndexColorModel(ARGB);
    }
    
    /**
     * Vérifie que la valeur <code>value</code> spécifiée
     * est dans la plage [0..255] inclusivement.
     *
     * @throws ParseException si le nombre n'est pas dans la plage [0..255].
     */
    private static int byteValue(final int value) throws ParseException {
        if (value>=0 && value<256) return value;
        throw new ParseException(Resources.format(
                ResourceKeys.ERROR_RGB_OUT_OF_RANGE_$1, new Integer(value)), 0);
    }
}
