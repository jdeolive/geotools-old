/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// Collections
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Collections;
import java.util.Arrays;

// Input/output
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

// Miscellaneous
import java.util.Comparator;
import java.util.StringTokenizer;
import java.text.MessageFormat;
import java.lang.reflect.Field;


/**
 * Resource compiler. This class is run from the command line at compile time
 * only. <code>ResourceCompiler</code> scans for <code>.properties</code> files
 * and copies their content to <code>.utf</code> files using UTF8 encoding. It
 * also checks for key validity (making sure that the same set of keys is
 * defined in every language) and checks values for {@link MessageFormat}
 * compatibility.
 * Finally, it creates a <code>ResourceKeys.java</code> source file declaring
 * resource keys as integer constants.
 * <br><br>
 * <code>ResourceCompiler</code> and all <code>ResourceKeys</code> classes
 * don't need to be included in the final JAR file. They are used at
 * compile time only and no other classes should keep reference to them.
 *
 * @version $Id: ResourceCompiler.java,v 1.7 2003/08/19 15:05:22 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ResourceCompiler implements Comparator {
    /**
     * Special order for resource keys starting
     * with the specified prefix.
     */
    private static final String[] ORDER = {
        "WARNING_",
        "ERROR_"
    };

    /**
     * The class name for the interfaces to be generated.
     */
    private static final String CLASS_NAME = "ResourceKeys";

    /**
     * Extension for properties files.
     */
    private static final String PROPERTIES_EXT = ".properties";

    /**
     * Extension for resource files.
     */
    private static final String RESOURCES_EXT = ".utf";

    /**
     * Prefix for argument count in resource key names.
     * For example, a resource expecting one argument may
     * have a key name like "HELLO_$1".
     */
    private static final String ARGUMENT_COUNT_PREFIX = "_$";

    /**
     * Integer IDs allocated to resource keys. This map
     * uses <code>&lt;Integer,String&gt;</code> entries.
     */
    private final Map allocatedIDs = new HashMap();

    /**
     * Resource keys and their localized values. This map
     * uses <code>&lt;String,String&gt;</code> entries.
     */
    private final Map resources = new HashMap();

    /**
     * Construct a new <code>ResourceCompiler</code>.  This method will
     * immediately look for a <code>ResourceKeys.class</code> file. If
     * one is found, integer keys are loaded in order to reuse the same values.
     *
     * @param directory The resource directory. This directory should or
     *                  will contain the following input and output files:
     *      <ul>
     *        <li><code>resources*.properties</code> (mandatory input)</li>
     *        <li><code>ResourceKeys.class</code>    (optional  input)</li>
     *        <li><code>resources*.utf</code>                 (output)</li>
     *        <li><code>ResourceKeys.class</code>             (output)</li>
     *      </ul>
     *
     * @throws IOException if an input/output operation failed.
     */
    private ResourceCompiler(final File directory) throws IOException {
        try {
            String classname;
            classname = toRelative(new File(directory, CLASS_NAME));
            classname = classname.replace(File.separatorChar, '.');
            final Field[] fields = Class.forName(classname).getFields();
            System.out.print("Loading ");
            System.out.println(classname);
            /*
             * Copy all fields into {@link #allocatedIDs} map.
             */
            Field.setAccessible(fields, true);
            for (int i=fields.length; --i>=0;) {
                final Field field = fields[i];
                final String  key = field.getName();
                try {
                    final Object ID = field.get(null);
                    if (ID instanceof Integer) {
                        allocatedIDs.put((Integer)ID, key);
                    }
                } catch (IllegalAccessException exception) {
                    warning(toSourceFile(classname), key, "Access denied", exception);
                }
            }
        } catch (ClassNotFoundException exception) {
            /*
             * 'ResourceKeys.class' doesn't exist. This is okay
             * (probably normal). We will create 'ResourceKeys.java'
             * later using automatic key values.
             */
        }
    }

    /**
     * Scan the specified directory and all subdirectories for resources.
     *
     * @param  directory The root directory.
     * @throws IOException if an input/output operation failed.
     */
    private static void scanForResources(final File directory) throws IOException {
        ResourceCompiler compiler = null;
        final File[] content = directory.listFiles();
        for (int i=0; i<content.length; i++) {
            final File file = content[i];
            if (file.isDirectory()) {
                scanForResources(file);
                continue;
            }
            if (file.getName().endsWith(PROPERTIES_EXT)) {
                if (compiler==null) {
                    compiler = new ResourceCompiler(directory);
                }
                compiler.loadPropertyFile(file);
                String path = file.getPath();
                path = path.substring(0, path.length()-PROPERTIES_EXT.length())
                       + RESOURCES_EXT;
                compiler.writeUTFFile(new File(path));
            }
        }
        if (compiler!=null) {
            compiler.writeJavaSource(new File(directory, CLASS_NAME+".java"));
        }
    }

    /**
     * Load all properties from a <code>.properties</code> file. Resource
     * keys are checked for naming conventions (i.e. resources expecting some
     * arguments must have a key ending with "_$n" where "n" is the number of
     * arguments). This method transforms resource values into legal
     * {@link MessageFormat} patterns when necessary.
     *
     * @param  file Resource file to read.
     * @throws IOException if an input/output operation failed.
     */
    private void loadPropertyFile(final File file) throws IOException {
        final InputStream input=new FileInputStream(file);
        final Properties properties=new Properties();
        properties.load(input);
        input.close();
        resources.clear();
        for (final Iterator it=properties.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            final String key      = (String) entry.getKey();
            final String value    = (String) entry.getValue();
            /*
             * Check key and value validity.
             */
            if (key.trim().length()==0) {
                warning(file, key, "Empty key.", null);
                continue;
            }
            if (value.trim().length()==0) {
                warning(file, key, "Empty value.", null);
                continue;
            }
            /*
             * Check if the resource value is a legal MessageFormat pattern.
             */
            final MessageFormat message;
            try {
                message = new MessageFormat(toMessageFormatString(value));
            } catch (IllegalArgumentException exception) {
                warning(file, key, "Bad resource value", exception);
                continue;
            }
            /*
             * Check if the expected arguments count (according to naming
             * conventions) matches the arguments count found in the
             * MessageFormat pattern.
             */
            final int argumentCount;
            final int index = key.lastIndexOf(ARGUMENT_COUNT_PREFIX);
            if (index<0) {
                argumentCount = 0;
                resources.put(key, value);
            } else try {
                String suffix = key.substring(index+ARGUMENT_COUNT_PREFIX.length());
                argumentCount = Integer.parseInt(suffix);
                resources.put(key, message.toPattern());
            } catch (NumberFormatException exception) {
                warning(file, key, "Bad number in resource key", exception);
                continue;
            }
            final int expected=message.getFormats().length;
            if (argumentCount!=expected) {
                final String suffix = ARGUMENT_COUNT_PREFIX+expected;
                warning(file, key, "Key name should ends with \""+suffix+"\".", null);
                continue;
            }
        }
        /*
         * Finished loading properties. Now, check if some keys are missing.
         */
        if (!allocatedIDs.isEmpty()) {
            final Set missing = new HashSet(allocatedIDs.values());
            missing.removeAll(resources.keySet());
            final String filename = file.getName();
            if (filename.indexOf('_') == filename.lastIndexOf('_')) {
                for (final Iterator it=missing.iterator(); it.hasNext();) {
                    final String key = (String) it.next();
                    warning(file, key, "Key defined in previous languages "+
                                       "is missing in current one.", null);
                }
            }
            // Second check
            missing.clear();
            missing.addAll(resources.keySet());
            missing.removeAll(allocatedIDs.values());
            for (final Iterator it=missing.iterator(); it.hasNext();) {
                final String key = (String) it.next();
                warning(file, key, "Key was not defined in previous languages.", null);
            }
        }
        /*
         * Allocate an ID for each new key.
         */
        final String[] keys = (String[]) resources.keySet().toArray(new String[resources.size()]);
        Arrays.sort(keys, this);
        int freeID = 0;
        for (int i=0; i<keys.length; i++) {
            final String key = keys[i];
            if (!allocatedIDs.containsValue(key)) {
                Integer ID;
                while (allocatedIDs.containsKey(ID=new Integer(freeID++)));
                allocatedIDs.put(ID, key);
            }
        }
    }

    /**
     * Write UTF file. Method {@link #loadPropertyFile} should
     * be invoked beforehand to <code>writeUTFFile</code>.
     *
     * @param  file The destination file.
     * @throws IOException if an input/output operation failed.
     */
    private void writeUTFFile(final File file) throws IOException {
        final int count = allocatedIDs.isEmpty() ? 0 : ((Integer) Collections.max(allocatedIDs.keySet())).intValue()+1;
        final DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.writeInt(count);
        for (int i=0; i<count; i++) {
            final String value = (String) resources.get(allocatedIDs.get(new Integer(i)));
            out.writeUTF((value!=null) ? value : "");
        }
        out.close();
    }

    /**
     * Returns the source file for the specified class.
     */
    private static File toSourceFile(final String classname) {
        return new File(classname.replace('.','/')+".java");
    }

    /**
     * Returns the class name for the specified source file.
     * The returned class name is not to include the package name.
     */
    private static String toClassName(final File file) {
        String name = file.getName();
        final int index = name.lastIndexOf('.');
        if (index>=0) name = name.substring(0, index);
        return name;
    }

    /**
     * Make a file path relative to the classpath.  The file path may be
     * relative (to current <code>chdir</code>) or absolute. This method
     * finds the canonical form of <code>path</code> and compares it with
     * canonical forms of every path in the classpath.  If a classpath
     * matches the beginning of <code>path</code>, then the corresponding
     * part of <code>path</code> is removed.  If there is more than one
     * match, the one resulting in the shortest relative path is chosen.
     */
    private static String toRelative(final File path) throws IOException {
        String bestpath = null;
        final String  absolutePath = path.getCanonicalPath();
        final String fileSeparator = System.getProperty("file.separator", "/");
        final StringTokenizer tokr = new StringTokenizer(System.getProperty("java.class.path", "."),
                                                         System.getProperty("path.separator", ":"));
        while (tokr.hasMoreTokens()) {
            String classpath = new File(tokr.nextToken()).getCanonicalPath();
            if (!classpath.endsWith(fileSeparator)) classpath+=fileSeparator;
            if (absolutePath.startsWith(classpath)) {
                final String candidate = absolutePath.substring(classpath.length());
                if (bestpath==null || bestpath.length()>candidate.length()) {
                    // Choose the shortest path.
                    bestpath = candidate;
                }
            }
        }
        return (bestpath!=null) ? bestpath : path.getPath();
    }

    /**
     * Change a "normal" text string into a pattern compatible with
     * {@link MessageFormat}.
     * The main operation consists of changing ' for '', except for '{' and '}'
     * strings.
     */
    private static String toMessageFormatString(final String text) {
        int level =  0;
        int last  = -1;
        final StringBuffer buffer = new StringBuffer(text);
search: for (int i=0; i<buffer.length(); i++) { // Length of 'buffer' will vary.
            switch (buffer.charAt(i)) {
                /*
                 * Left and right braces take us up or down a level.  Quotes
                 * will only be doubled if we are at level 0.  If the brace
                 * is between quotes it will not be taken into account as it
                 * will have been skipped over during the previous pass through
                 * the loop.
                 */
                case '{' : level++; last=i; break;
                case '}' : level--; last=i; break;
                case '\'': {
                    /*
                     * If a brace ('{' or '}') is found between quotes,
                     * the entire block is ignored and we continue with the
                     * character following the closing quote.
                     */
                    if (i+2<buffer.length() && buffer.charAt(i+2)=='\'') {
                        switch (buffer.charAt(i+1)) {
                            case '{': i+=2; continue search;
                            case '}': i+=2; continue search;
                        }
                    }
                    if (level<=0) {
                        /*
                         * If we weren't between braces, we must double the
                         * quotes.
                         */
                        buffer.insert(i++, '\'');
                        continue search;
                    }
                    /*
                     * If we find ourselves between braces, we don't normally
                     * need to double our quotes.  However, the format
                     * {0,choice,...} is an exception.
                     */
                    if (last>=0 && buffer.charAt(last)=='{') {
                        int scan=last;
                        do if (scan>=i) continue search;
                        while (Character.isDigit(buffer.charAt(++scan)));
                        final String choice=",choice,";
                        final int end=scan+choice.length();
                        if (end<buffer.length() && buffer.substring(scan, end).equalsIgnoreCase(choice)) {
                            buffer.insert(i++, '\'');
                            continue search;
                        }
                    }
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Print a message to the error output stream {@link System#err}.
     *
     * @param file      File that produced the error, or <code>null</code> if none.
     * @param key       Resource key that produced the error, or <code>null</code> if none.
     * @param message   The message string.
     * @param exception An optional exception that is the cause of this warning.
     */
    private static void warning(final File file,      final String key,
                                final String message, final Exception exception) {
        System.out.flush();
        System.err.print("ERROR ");
        if (file!=null) {
            String filename = file.getPath();
            if (filename.endsWith(PROPERTIES_EXT)) {
                filename = filename.substring(0, filename.length()-PROPERTIES_EXT.length());
            }
            System.err.print('(');
            System.err.print(filename);
            System.err.print(')');
        }
        System.err.print(": ");
        if (key!=null) {
            System.err.print('"');
            System.err.print(key);
            System.err.print('"');
        }
        System.err.println();
        System.err.print(message);
        if (exception!=null) {
            System.err.print(": ");
            System.err.print(exception.getLocalizedMessage());
        }
        System.err.println();
        System.err.println();
        System.err.flush();
    }

    /**
     * Write <code>count</code> spaces to the <code>out</code> stream.
     * @throws IOException if an input/output operation failed.
     */
    private static void writeWhiteSpaces(final Writer out, int count) throws IOException {
        while (--count>=0) out.write(' ');
    }

    /**
     * Create a source file for resource keys.
     *
     * @param  file The destination file.
     * @throws IOException if an input/output operation failed.
     */
    private void writeJavaSource(final File file) throws IOException {
        final String packageName = toRelative(file.getParentFile()).replace(File.separatorChar, '.');
        final BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("/*\n"                                                             +
                  " * Geotools - OpenSource mapping toolkit\n"                       +
                  " * (C) 2003, Geotools Project Managment Committee (PMC)\n"        +
                  " *\n"                                                             +
                  " *     THIS IS AN AUTOMATICALLY GENERATED FILE. DO NOT EDIT!\n"   +
                  " *     Generated with: org.geotools.resources.ResourceCompiler\n" +
                  " */\n");
        out.write("package ");
        out.write(packageName);
        out.write(";\n\n\n");
        out.write("/**\n"                                                                  +
                  " * Resource keys. This interface is used when compiling sources, but\n" +
                  " * no dependencies to <code>ResourceKeys</code> should appear in any\n" +
                  " * resulting class files.  Since Java compiler inlines final integer\n" +
                  " * values, using long identifiers will not bloat constant pools of\n"   +
                  " * classes compiled against the interface, provided that no class\n"    +
                  " * implements this interface.\n"                                        +
                  " *\n"                                                                   +
                  " * @see org.geotools.resources.ResourceBundle\n"                        +
                  " * @see org.geotools.resources.ResourceCompiler\n"                      +
                  " */\n");
        out.write("public interface ");
        out.write(toClassName(file));
        out.write("\n{\n");
        final Map.Entry[] entries = (Map.Entry[]) allocatedIDs.entrySet().toArray(new Map.Entry[allocatedIDs.size()]);
        Arrays.sort(entries, this);
        int maxLength=0;
        for (int i=entries.length; --i>=0;) {
            final int length = ((String) entries[i].getValue()).length();
            if (length>maxLength) maxLength=length;
        }
        for (int i=0; i<entries.length; i++) {
            final String key = (String) entries[i].getValue();
            final String ID  = entries[i].getKey().toString();
            if (i!=0 && compare(entries[i-1], key)<-1) {
                out.write('\n');
            }
            writeWhiteSpaces(out, 4);
            out.write("public static final int ");
            out.write(key);
            writeWhiteSpaces(out, maxLength-key.length());
            out.write(" = ");
            writeWhiteSpaces(out, 5-ID.length());
            out.write(ID);
            out.write(";\n");
        }
        out.write("}\n");
        out.close();
    }

    /**
     * Compare two resource keys. Object <code>o1</code> and <code>o2</code>
     * are usually {@link String} objects representing resource keys (for
     * example, "<code>MISMATCHED_DIMENSION</code>").  This method compares
     * strings in the same manner as {@link String#compareTo}, except that
     * strings starting with one of the prefixes enumerated in {@link #ORDER}
     * will appear last in the sorted array.
     */
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Map.Entry) o1 = ((Map.Entry) o1).getValue();
        if (o2 instanceof Map.Entry) o2 = ((Map.Entry) o2).getValue();
        final String key1 = (String) o1;
        final String key2 = (String) o2;
        int i1=ORDER.length; while (--i1>=0) if (key1.startsWith(ORDER[i1])) break;
        int i2=ORDER.length; while (--i2>=0) if (key2.startsWith(ORDER[i2])) break;
        if (i1 < i2) return -2;
        if (i1 > i2) return +2;
        return XMath.sgn(key1.compareTo(key2));
    }

    /**
     * Run the resource compiler.
     *
     * @param  args Command-line arguments.
     * @throws IOException if an input/output operation failed.
     */
    public static void main(final String[] args) throws IOException {
        for (int i=0; i<args.length; i++) {
            scanForResources(new File(args[i]));
        }
    }
}
