/*
 * MIFMIDFile.java
 *
 * Created on January 1, 2002, 5:24 PM
 */

package org.geotools.mapinfo;
import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.layer.*;
/**
 * The class object stores all the information read from the mif and mid file by constructing a MIFMIDReader
 * object.
 *
 * MapInfo data is in two files — the graphics reside in a .MIF file and attribute data is
 * contained in a .MID file. The MIF file has two areas — the file header area and the data section.
 * Information on how to create MapInfo tables is in the header; the graphical object
 * definitions are in the data section.
 * MIF File Header
 * This is a description of MIF file header with optional information in square brackets.
 * VERSION n
 * Charset ”characterSetName”
 * [ DELIMITER ”<c>” ]
 * [ UNIQUE n,n.. ]
 * [ INDEX n,n.. ]
 * [ COORDSYS...]
 * [ TRANSFORM...]
 * COLUMNS n
 * <name> <type>
 * <name> <type>
 *  .
 *  .
 * DATA
 *
 *
 *
 * In this class object the above described information will be all stored in a string[] variable, call getMIFFileHeader() to get it.
 *
 * MIF Data Section
 * The data section of the MIF file follows the header and must be introduced with
 * DATA on a single line:
 * DATA
 * The data section of the MIF file can have any number of graphical primitives, one for
 * each graphic object. MapInfo matches up entries in the MIF and MID files, associating
 * the first object in the MIF file with the first row in the MID file, the second object in the
 * MIF file with the second row in the MID file, and so on.
 * When there is no graphic object corresponding to a particular row in the MID file, a
 * “blank” object (NONE) must be written as a place holder in the corresponding place
 * in the MIF file.
 *
 * The variables to store those data are layers, PointLayer,LineLayer,PolygonLayer and MultiLayer to mix them together.
 * also four GeoData[] variable stores the attribute data for those layers respectively. The class doesn't provide Theme
 * variable, since it only can take one GeoData, not all( GeoData[]). Also an index of geoData and point,line poly GeoData
 * string "point", "line", "poly". it means that if string[0] is "point", geoData[0] is connected to pointGeoData[0],
 * both of them are in the same order . it aslo is an index of each layer objects and the total objects which might useful for some programs
 * using a string array which contain "point","poly","none","line", if layerIndex[0] is "line", it means id = (0+1)=1 object is a line object.
 * Furthermore, shading description text in the MIF file , i just read those line as strings for dealing with them when need.
 * if object[0], "0" get from the total ojects id substract 1, the object[0] will contain a string
 * if there is no shading sentence for the object, the string will be "none"
 *
 * @see MIFMIDReader
 * @author Jianhui Jin
 * @version 1.0
 */
public class MIFMIDFile extends java.lang.Object {
    // storing the mif file header that could be utilized later,if necessary.
    private  String[] mifFileHeader;
    
    // all the layer together
    private  MultiLayer multiLayer;
    
    //point layer
    private  PointLayer pointLayer;
    
    //line layer
    private  LineLayer lineLayer;
    
    //polygon Layer
    private  PolygonLayer polyLayer;
    
    // storing the all kinds of the mid file data
    private  GeoData[] multiGeoData;
    
    // point layer geodata
    private  GeoData[] pointGeoData;
    
    // line layer geodata
    private  GeoData[] lineGeoData;
    
    // polygon layer geodata
    private  GeoData[] polyGeoData;
    
    //point count
    private int pointCount=0;
    //line count
    private int lineCount=0;
    //polygon count
    private int polyCount=0;
    // total geographical objects count
    private int totalCount=0;
    
    // index of geoData and point,line poly GeoData
    // string "point", "line", "poly"
    // it means that if string[0] is "point", geoData[0] is connected to pointGeoData[0],
    // both of them are in the same order
    // it aslo is an index of each layer objects and the total objects which might useful for some programs
    // using a string array which contain "point","poly","none","line",
    //if layerIndex[0] is "line", it means id = (0+1)=1 object is a line object
    private String[] geoDataIndex;
    
    // shading description strings in the mid file , i just read those line as strings for dealt with these late
    // if object[0], "0" get from the total ojects id substract 1, the object[0] will contain a string
    // if there is no shading sentence for the object, the string will be "none"
    private String[] shadingStrings;
    
    
    
    
    
    /** Creates new MIFMIDFile */
    public MIFMIDFile() {
        
    }
    
    
    
    /** get MIF file Header storing the mif file header that could be utilized later,if necessary.*/
    public String[] getMIFFileHeader(){
        return mifFileHeader;
    }
    
    /** get PointLayer if it exists, otherwise return null */
    public PointLayer getPointLayer() {
        if (pointCount==0)
            return null;
        else return pointLayer;
    }
    
    
    /** get lineLayer if it exists, otherwise return null */
    public LineLayer getLineLayer() {
        if (lineCount==0)
            return null;
        else return lineLayer;
    }
    
    
    
    /** get PolygonLayer if it exists, otherwise return null */
    public PolygonLayer getPolygonLayer() {
        if (polyCount==0)
            return null;
        else return polyLayer;
    }
    
    /** get MultiLayer if it exists, otherwise return null */
    public MultiLayer getMultiLayer() {
        if (totalCount==0)
            return null;
        else return multiLayer;
    }
    
    /** index of geoData and point,line poly GeoData
     * string "point", "line", "poly"
     * it means that if string[0] is "point", geoData[0] is connect to pointGeoData[0],
     * both of them are in the same order
     * it aslo is an index of each layer objects and the total objects which might useful for some programs
     * using a string array which contain "point","poly","none","line",
     * if layerIndex[0] is "line", it means id = (0+1)=1 object is a line object
     */
    public String[] getGeoDataIndex(){
        return geoDataIndex;
    }
    
    
    /** get total geodata in the mid file */
    public GeoData[] getMultiGeoData(){
        if (totalCount==0)
            return null;
        else return multiGeoData;
    }
    
    /** get Point geodata in the mid file */
    public GeoData[] getPointGeoData(){
        if (pointCount==0)
            return null;
        else return pointGeoData;
    }
    
    
    /** get Line geodata in the mid file */
    public GeoData[] getLineGeoData(){
        if (lineCount==0)
            return null;
        else return lineGeoData;
    }
    
    
    /** get Polygon geodata in the mid file */
    public GeoData[] getPolygonGeoData(){
        if (polyCount==0)
            return null;
        else return polyGeoData;
    }
    
    /** get shading sentences in the mid file
     * shading description strings in the mid file , i just read those line as strings for dealt with these late
     * if object[0], "0" get from the total ojects id substract 1, the object[0] will contain a string
     * if there is no shading sentence for the object, the string will be "none"
     */
    public String[] getShadingStrings(){
        return shadingStrings;
    }
    
    
    
    // since the object of the class is for storing the file information of mif and mid file
    // that must be set up by mifReader class object in the same package ,
    // those set methods could be package access level.
    
    
    void setShadingStrings(String[] obj){
        shadingStrings=obj;
    }
    void setGeoDataIndex(String[] s){
        geoDataIndex=s;
        
    }
    boolean setMIFFileHeader(String[] s){
        if(s==null)
            return false;
        else{
            mifFileHeader=s;
            return true;
        }
    }
    
    
    boolean setPointLayerAndPointCount(int count,PointLayer l){
        pointCount=count;
        if(count==0)
            return false;
        else {
            pointLayer=l;
            return true;
        }
    }
    
    boolean setLineLayerAndLineCount(int count,LineLayer l){
        lineCount=count;
        if(count==0)
            return false;
        else {
            lineLayer=l;
            return true;
        }
    }
    
    
    boolean setPolygonLayerAndPolygonCount(int count,PolygonLayer l){
        polyCount=count;
        if(count==0)
            return false;
        else {
            polyLayer=l;
            return true;
        }
    }
    
    
    boolean setMultiLayerAndTotalCount(int count,MultiLayer l){
        totalCount=count;
        if(count==0)
            return false;
        else {
            multiLayer=l;
            return true;
        }
    }
    
    
    boolean setMultiGeoData(GeoData[] geodata){
        if(geodata[0].getSize()==0)
            return false;
        else{
            multiGeoData=geodata;
            return true;
        }
    }
    
    boolean setPointGeoData(GeoData[] geodata){
        if(geodata[0].getSize()==0)
            return false;
        else{
            pointGeoData=geodata;
            return true;
        }
        
    }
    
    boolean setLineGeoData(GeoData[] geodata){
        if(geodata[0].getSize()==0)
            return false;
        else{
            lineGeoData=geodata;
            return true;
        }
    }
    
    boolean setPolygonGeoData(GeoData[] geodata){
        if(geodata[0].getSize()==0)
            return false;
        else{
            polyGeoData=geodata;
            return true;
        }
    }
}
