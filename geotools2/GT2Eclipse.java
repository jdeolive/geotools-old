/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Quick utility that will update you Eclipse .project and .classpath file
 * to reflect the last build.
 * <p>
 * This utility will process <code>buildReport.txt</code> to determine
 * the build order (and to ensure that you actually try building with
 * maven first).</p>
 * <p>
 * For each <i>project</i> in <code>buildReport.txt</code>:</p>
 * <ul>
 * <li>geotools-src/<i>project</i>/project.xml will be processed:
 *     <ul>
 *     <li>dependencies will be added to MAVEN_REPORT int .classpath
 *         </li>
 *     <li><code>src</code> will be added as a folder compiling to
 *         <code>target/classes</code>
 *         </li>
 *     <li><code>tests</code> will be added as a folder compiling to
 *         <code>target/test_classes</code>
 *         </li>
 *     </ul>
 *     </li>
 * </ul>
 * Example:
 * <code><pre>
 * maven build
 * java org.geotools2.build.GT2Eclipse
 * </pre></code>
 * <p>
 * If you are running this from eclipse remember to hit refresh afterwords.</p>
 * 
 * @see org.geotools2.build
 * @author jgarnett, Refractions Reasearch Inc.
 * @version CVS Version
 */
public class GT2Eclipse
{
    /**
     * List of demos based on geotools-demos
     */ 
    public static Set demos(String path){
        Set set = new TreeSet();        
        File geotools2demos = new File( path, "geotools-demos" );
        String dir[] = geotools2demos.list();
        String src;
        for( int i=0; i<dir.length; i++ ){
            src = dir[i];
            if( src.equals("CVS") ) continue;
            if(! set.contains( src ) ){
                System.out.println("target "+src+" was not built by maven");                
            }
            System.out.println("demo "+src+" not built by maven");
            set.add( src );
            
        }        
        return set;
    }
    
/**
 * List of targets build by the last <code>maven build</code> command
 */ 
public static Set targets(String path){
    try
    {
        BufferedReader buildReport = new BufferedReader(
            new FileReader( new File( path, "buildReport.txt") )
        );
        Set set = new TreeSet();
        buildReport.readLine(); // skip first line
        String line;
        while( (line = buildReport.readLine()) != null ){
            line = line.substring( line.indexOf(':')+1 );
            line = line.substring( 0, line.indexOf(' ') );
            // a few corrections
            if( line.equals( "oracle-spatial") ) line = "oraclespatial";
            if( line.equals( "java2drendering") ) line = "Java2DRendering";
            if( line.equals( "wmsserver")) continue; 
            System.out.println("target "+line );            
            set.add( line );
        }        
        File geotools2src = new File( path, "geotools-src" );
        String dir[] = geotools2src.list();
        String src;
        for( int i=0; i<dir.length; i++ ){
            src = dir[i];
            if( src.equals("CVS") ) continue;
            if(! set.contains( src ) ){
                System.out.println("target "+src+" was not built by maven");
                continue;
            }
            System.out.println("target arc not build by maven");
            set.add( src );
        }
        // a few corrections
        //
        set.add( "postgis" );
        set.add( "j2se-demos" );        
        
        return set;
    }
    catch (FileNotFoundException e)
    {
        e.printStackTrace();
        System.err.println(
            "Please run maven.build from your geotools2 directory." +
            "This will generate the file buildReports.txt required"
        );
        return null;
    } catch (IOException e){
        e.printStackTrace();
        System.err.println(
            "Problem understanding the file buildReports.txt"
        );
        return null;                
    }         
}
static public Set dependencies( String path, Set targets ){
    Set depends = new TreeSet();
    // read file
    /*
    for( Iterator i=targets.iterator(); i.hasNext();){
        depends.addAll( process( path, "geotools-src/"+i.next()+"/project.xml" )); 
    }
    */
    // This did not work so lets try another way
    
    // grab everything in the repository
    File repository = new File( System.getProperty( "user.home" )+"/.maven/repository" );
    System.out.println("Repository "+repository +" -- "+repository.exists());
    String content[] = repository.list();
    if( content != null )
    {
        for( int i=0; i<content.length; i++){
            System.out.println("processing "+content[i]+"...");
            File dir = new File( repository, content[i]+"/jars" );
            String jars[] = dir.list(new FilenameFilter(){
                public boolean accept(File dir, String name)
                {
                    if( name.indexOf("SNAPSHOT") != -1 ) return false;
                    if( name.endsWith("-0.1.jar")) return false;
                    if( name.endsWith(".md5") ) return false;
                    return true;
                }             
            });
            Map map = new HashMap();
            for( int j=0; j<jars.length;j++){
                String jar = jars[j];
                int split = jar.lastIndexOf('-');
                if( split == -1){
                    System.out.println( "  added "+jars[j] );                
                    depends.add( content[i]+"/jars/"+ jar );                    
                    continue;
                }
                String id = jar.substring(0, split );
                String version = jar.substring(split+1, jar.length()-4 );                
                System.out.println( "version: "+version+" jar:"+id );
                
                if( map.containsKey( id )){
                    // take the latest version
                    String v = (String) map.get( id );                    
                    System.out.println(
                        "compare "+id+" version "+version+" against "+v + " - "+
                        version.compareToIgnoreCase( v )                    
                    );
                    if( version.compareToIgnoreCase( v ) < 0 ){
                        continue;
                    }
                    System.out.println( "  update version to "+jars[j] );
                    map.remove( id );                                            
                }
                map.put( id, version );                                                        
            }
            
            for( Iterator e=map.entrySet().iterator();e.hasNext();){
                Map.Entry entry = (Map.Entry) e.next();
                depends.add( content[i]+"/jars/"+entry.getKey()+"-"+entry.getValue()+".jar" );                                            
            }
        }
    }
               
    return depends;
}

/** grabs first tag on line*/
static public String tag( String line ){
    int start = line.indexOf('>');
    int end = line.indexOf('<', start ); 
    return line.substring( start+1, end );
}
static public List process( String path, String target )
{
    List list = new LinkedList();        
    try
    {
        BufferedReader project = new BufferedReader(
            new FileReader( new File( path, target ) )
        );
        
        String line;
        
        line = project.readLine(); // first line 
        // skip till dependencies
        for ( ;  line != null; line = project.readLine() ){
            line = line.trim();
            if( line.equals( "<dependencies>" ) ) break;                        
        }                     
        for( ; line != null; line = project.readLine() ){
            line = line.trim();
            if( line.equals( "</dependencies>" ) ) break;
            
            if( line.equals( "<dependency>" ) ){
                String id = null;
                String version = null;
                
                for(; line != null; line = project.readLine() ){
                    line = line.trim();
                    if( line.equals( "</dependency>" ) ) break;
                    if( line.startsWith("<id>")) id = tag( line );
                    if( line.startsWith("<version>")) version = tag( line );
                    if( line.startsWith("<artifactId>")) id = tag( line );
                }                
                if( version.equals("0.1") || version.indexOf("SNAPSHOT") != -1 ){
                    break; // intra module dependency
                }
                else {
                    // JTS/jars/JTS-1.3.jar
                    list.add( id + "/jars/"+id+"-"+version+".jar" );
                }
            }
        }
        return list;
    }
    catch (FileNotFoundException e)
    {
        e.printStackTrace();
        System.err.println(
            "Please run maven.build from your geotools2 directory." +
            "This will generate the file buildReports.txt required"
        );
    } catch (IOException e){
        e.printStackTrace();
        System.err.println(
            "Problem understanding the file buildReports.txt"
        );
    }
    return list;
}
public static void entry( PrintStream classpath, String project, String path, String target ){
    if( new File( project, path+"/"+target+"/src").exists() ){        
        classpath.println("    <classpathentry kind=\"src\"" );
        classpath.println("        output=\""+path+"/"+target+"/target/classes\"" );
        classpath.println("        path=\""+path+"/"+target+"/src\"/>" );
    }
    if( new File( project, path+"/"+target+"/tests/unit").exists() ){
        classpath.print("      <classpathentry");
        
        if( target.equals("defaultcore") )
        classpath.print(" excluding=\"**/DummyFeatureType.java\"");
        
        if( target.equals("map") )
                    classpath.print(" excluding=\"**/BoundingBoxImplTest.java\"");
                    
        classpath.println(" kind=\"src\"");
        classpath.println("        output=\""+path+"/"+target+"/target/test-classes\"");
        classpath.println("        path=\""+path+"/"+target+"/tests/unit\"/>" );
    }
}
public static void main( String args[] ) {
    String dir = null;
    if( args.length == 0 ){
        dir = "../geotools2";
    }
    else if( args[0].equals("-h") ||
        args[0].equals("-help")){
        System.out.println("use: java GT2Eclipse geotools2directory");
        System.out.println();
        System.out.println("Please run maven build prior to using this utility");        
        System.exit(0);        
    }
    else {
        dir = args[0];
    }
    
    PrintStream classpath = System.out;
    PrintStream project = System.out;
    try
    {
		File file = new File( dir, ".classpath");
		classpath = new PrintStream( new FileOutputStream( file, false));            
		System.out.println( "Writing .classpath to:"+file );

		file = new File( dir, ".project");
		project = new PrintStream( new FileOutputStream( file, false));            
    }    
    catch (FileNotFoundException e)
    {
        e.printStackTrace();
        System.out.println("Could not create files at:"+dir );
        System.exit(1);
    }
    
    classpath.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    classpath.println("<classpath>");
    
    String target;
    Set targets = targets( dir );
	for( int t=1; t<args.length; t++)
	{
		targets.add( args[t]);
	}
    
    for( Iterator i=targets.iterator(); i.hasNext();){
        entry( classpath, dir, "geotools-src", (String) i.next() );    
    }
    
    Set demos = demos( dir );
    for( Iterator i=demos.iterator(); i.hasNext();){
        entry( classpath, dir, "geotools-demos", (String) i.next() );    
    }
    classpath.println("");    
    //classpath.println("    <classpathentry kind=\"var\" path=\"JRE_LIB\" sourcepath=\"JRE_SRC\"/>" );
    for( Iterator i=dependencies( dir, targets ).iterator(); i.hasNext();){
        String jar = (String) i.next();
        classpath.println("    <classpathentry kind=\"var\" path=\"MAVEN_REPO/"+jar+"\"/>" );                
    }
    classpath.println("    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>");
    classpath.println("    <classpathentry kind=\"output\" path=\"geotools-src\"/>" );        
    classpath.println("</classpath>");
    classpath.close();
    
    project.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    project.println("<projectDescription>");
    project.println("  <name>gtbuild</name>");
    project.println("  <comment>Welcome to the homepage of the GeoTool2 build process In the left side bar you should see a list of active modules, visit each for more details on the status of each module.</comment>");
    project.println("  <projects>");
    project.println("  </projects>");
    project.println("  <buildSpec>");
    project.println("    <buildCommand>");
    project.println("      <name>org.eclipse.jdt.core.javabuilder</name>");
    project.println("      <arguments>");
    project.println("      </arguments>");
    project.println("    </buildCommand>");
    project.println("  </buildSpec>");
    project.println("  <natures>");
    project.println("    <nature>org.eclipse.jdt.core.javanature</nature>");
    project.println("  </natures>");
    project.println("</projectDescription>");
}

}
