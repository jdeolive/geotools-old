/*
 * Proj.java
 *
 * Created on February 19, 2002, 4:19 PM
 */

package org.geotools.proj4j;
import java.io.*;
import java.util.Vector;
/**
 *
 * @author  jamesm
 */
public class Proj {
    private Projection pj;
    boolean reversein = false;	/* != 0 reverse input arguments */
    boolean reverseout = false;	/* != 0 reverse output arguments */
    boolean bin_in = false;	/* != 0 then binary input */
    boolean bin_out = false;	/* != 0 then binary output */
    boolean echoin = false;	/* echo input data to output line */
    String tag = "#";	/* beginning of line tag character */
    boolean inverse = false;	/* != 0 then inverse projection */
    boolean prescale = false;	/* != 0 apply cartesian scale factor */
    boolean dofactors = false;	/* determine scale factors */
    boolean veryVerby = false; /* very verbose mode */
    boolean postscale = false;
    String cheby_str;		/* string controlling Chebychev evaluation */
    String oform=null;	/* output format for x-y or decimal degrees */
    String oterr = "*\t*";	/* output line for unprojectable input */
    
    Vector pargs = new Vector();
    
    public void usage(){
        System.out.println("PROJ4J Rel: 0.0.1 February 2002 port of PROJ 4.4.5");
        System.out.println("usage: [ -beEfiIlormsStTvVwW [args] ] [ +opts[=arg] ]");
    }
        
    
    /** Creates a new instance of Proj */
    public Proj(String args[]) {
        if(args.length==0){
            usage();
            System.exit(0);
        }
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("-")){
                switch(args[i].charAt(1)){
                    case 'b':
                        bin_in=bin_out=true;
                        continue;
                    case 'i':
                        bin_in=true;
                        continue;
                    case 'o':
                        bin_out=true;
                        continue;
                    case 'I': /* alt. method to spec inverse */
                        inverse = true;
                        continue;
                    case 'E': /* echo ascii input to ascii output */
                        echoin = true;
                        continue;
                    case 'V': /* very verbose processing mode */
                        veryVerby = true;
                        //mon = 1;
                    case 'S': /* compute scale factors */
                        dofactors = true;
                        continue;
                    case 't': /* set col. one char */
                        if(i+1<args.length){
                            tag = args[++i];
                        }
                        else System.err.println("missing -t col. 1 tag");
                        continue;
                    case 'l': /* list projections, ellipses or units */
                        System.err.println("listing of available projections,units and ellips not yet supported");
                        continue;
                    case 'e': /* error line alternative */
                        if(i+1<args.length){
                            oterr = args[++i];
                        }
                        else System.err.println("missing -e col. 1 tag");
                        continue;
                    case 'T': /* generate Chebyshev coefficients */
                        if(i+1<args.length){
                            cheby_str = args[++i];
                        }
                        else System.err.println("missing -T col. 1 tag");
                        continue;
                    case 'm': /* cartesian multiplier */
                        System.err.println("cartesian multiplyer not yet implemented");
                        continue;
                    case 'W': /* specify seconds precision */
                    case 'w': /* -W for constant field width */
                        System.err.println("precision specification not yet implemented");
                        continue;
                    case 'f': /* alternate output format degrees or xy */
                        System.err.println("alternate output formats not yet implemented");
                        continue;
                    case 'r': /* reverse input */
                        reversein = true;
                        continue;
                    case 's': /* reverse output */
                        reverseout = true;
                        continue;
                    default:
                        System.err.println("unknown argument "+args[i]);
                        break;
                }                
            }
            else if(args[i].startsWith("+")){
                pargs.addElement(args[i].substring(1));
            }
        }
        //build array of arguments
        String params[] = new String[pargs.size()];
        for(int i=0;i<pargs.size();i++){
            params[i] = (String)pargs.elementAt(i);
        }
        try{
            pj = ProjectionFactory.createProjection(params);
        }
        catch(ProjectionException pe){
            System.err.println("projection initialization failure\n"+pe);
            System.exit(0);
        }
        if( pj.isLatLong() )
        {
            System.err.println("+proj=latlong unsuitable for use with proj program.");
            System.exit( 0 );
        }
        //need to deal with input files as well as stin
        try{
            process(System.in,System.out);
        }
        catch(IOException ie){
            System.err.println("Error reading from input\n"+ie);
        }
    }
    
    public void process(InputStream in,OutputStream out) throws IOException{
        BufferedReader read = new BufferedReader(new InputStreamReader(in));
        PrintWriter write = new PrintWriter(new OutputStreamWriter(out));
        String input;
        XY xy;
        while(true){
            input = read.readLine();
            if(input.startsWith(this.tag))continue; 
            
            try{
                xy = pj.forward(new LP(input));
                write.write(""+xy.x+"\t"+xy.y+"\n");
                write.flush();
            }
            catch(ProjectionException pe){
                write.write(this.oterr+"\n");
                write.flush();
            }
                
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException{
        new Proj(args);
    }
    
}
