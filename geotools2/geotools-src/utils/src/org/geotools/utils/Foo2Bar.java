/*
 * Foo2Bar.java
 *
 * Created on 12 November 2002, 22:17
 */
package org.geotools.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

//import org.geotools.shapefile.Shapefile;

/**
 *
 * @author  James
 */
public class Foo2Bar
{
    /** Creates a new instance of Foo2Bar
     * Creates a new instance of Foo2BarCreates a new instance of Foo2BarCreates a new instance of Foo2BarCreates a new instance of Foo2BarCreates a new instance of Foo2BarCreates a new instance of Foo2Bar
     */
    public Foo2Bar()
    {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Option help = new Option("help", "show this message");
        Option height = OptionBuilder.withArgName("height").hasArg()
                                     .withDescription("height of output image in pixels")
                                     .create("h");

        Option width = OptionBuilder.withArgName("width").hasArg()
                                    .withDescription("height of output image in pixels")
                                    .create("w");

        Option file = OptionBuilder.withArgName("file").hasArg()
                                   .withDescription("The GML file to convert")
                                   .create("f");

        Option format = OptionBuilder.withArgName("format").hasArg()
                                     .withDescription("The output format")
                                     .create("o");

        Options options = new Options();

        options.addOption(help);
        options.addOption(height);
        options.addOption(width);
        options.addOption(file);
        options.addOption(format);
        
        CommandLineParser parser = new PosixParser();

        try
        {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            //if(line.hasOption("help")){
            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp("gml2img", options);

            //}
        }
        catch (ParseException exp)
        {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }

        //System.out.println("Foo is now Bar");
    }
}