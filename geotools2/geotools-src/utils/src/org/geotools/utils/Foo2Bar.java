/*
 * Foo2Bar.java
 *
 * Created on 12 November 2002, 22:17
 */

package org.geotools.utils;

import org.apache.commons.cli.*;

//import org.geotools.shapefile.Shapefile;

/**
 *
 * @author  James
 */
public class Foo2Bar {
    
    /** Creates a new instance of Foo2Bar */
    public Foo2Bar() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Option help = new Option("help", "show this message");
        Option height = OptionBuilder.withArgName( "height" )
                                .hasArg()
                                .withDescription(  "hight of output image in pixels" )
                                .create( "h" );

        
        Options options = new Options();
        options.addOption(help);
        options.addOption(height);
        
        CommandLineParser parser = new PosixParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            //if(line.hasOption("help")){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "gml2img", options );
            //}
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        
        
        
        //System.out.println("Foo is now Bar");
        
    }
    
}
