/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.mapinfo;
import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.layer.*;

/**
 * The class object stores all the information read from the mif and mid file
 * by constructing a MIFMIDReader object.
 *
 * MapInfo data is in two files — the graphics reside in a .MIF file and
 * attribute data is contained in a .MID file. The MIF file has two areas
 * — the file header area and the data section.
 * Information on how to create MapInfo tables is in the header; the graphical
 * object definitions are in the data section.
 *
 * MIF File Header.<br>
 * This is a description of the MIF file header format with optional
 * information in square brackets.<br>
 * VERSION n<br>
 * Charset ”characterSetName”<br>
 * [ DELIMITER ”<c>” ]<br>
 * [ UNIQUE n,n.. ]<br>
 * [ INDEX n,n.. ]<br>
 * [ COORDSYS...]<br>
 * [ TRANSFORM...]<br>
 * COLUMNS n<br>
 * &lt;name&gt; &lt;type&gt;<br>
 * &lt;name&gt; &lt;type&gt;<br>
 *  .<br>
 *  .<br>
 * DATA<br>
 *
 * In this class object the information above will all be stored
 * in a string[] variable. Call getMIFFileHeader() to get it.
 *
 * MIF Data Section.<br>
 * The data section of the MIF file follows the header and must be introduced
 * with DATA on a single line:<br>
 * DATA<br>
 * The data section of the MIF file can have any number of graphical
 * primitives, one for each graphic object. MapInfo matches up entries in the
 * MIF and MID files, associating the first object in the MIF file with the
 * first row in the MID file, the second object in the MIF file with the second
 * row in the MID file, and so on.  When there is no graphic object
 * corresponding to a particular row in the MID file, a “blank” object (NONE)
 * must be written as a place holder in the corresponding place in the MIF
 * file.
 *
 * @see MIFMIDReader
 * @version $Id: MIFMIDFile.java,v 1.2 2002/07/15 17:42:14 loxnard Exp $
 * @author Jianhui Jin
 */
public class MIFMIDFile extends java.lang.Object {
    // Store the MIF file header. Could be utilized later, if necessary.
    private  String[] mifFileHeader;
    
    // All the layers together
    private  MultiLayer multiLayer;
    
    //Point layer
    private  PointLayer pointLayer;
    
    //Line layer
    private  LineLayer lineLayer;
    
    //Polygon layer
    private  PolygonLayer polyLayer;
    
    // Store all types of MID file data
    private  GeoData[] multiGeoData;
    
    // Point layer geodata
    private  GeoData[] pointGeoData;
    
    // line layer geodata
    private  GeoData[] lineGeoData;
    
    // Polygon layer geodata
    private  GeoData[] polyGeoData;
    
    // Point count
    private int pointCount = 0;
    // Line count
    private int lineCount = 0;
    // Polygon count
    private int polyCount = 0;
    // Total geographical objects count
    private int totalCount = 0;
    
    /**
     * Index of geoData and point,line poly GeoData
     * string "point", "line", "poly".
     * It means that if string[0] is "point", geoData[0] is connected to
     * pointGeoData[0], both of them are in the same order.
     * It is also an index of each layer object and the total objects which
     * might be useful for some programs using a string array which contains
     * "point","poly","none","line".
     * If layerIndex[0] is "line", it means id = (0+1)=1 object is a line
     * object private String[] geoDataIndex;
     * Shading description strings in the mid file, I just read those lines
     * as strings for dealt with these late.
     * If object[0], "0" get from the total ojects id subtract 1,
     * the object[0] will contain a string.
     * If there is no shading sentence for the object, the string will be
     * "none" private String[] shadingStrings;
     * Creates new MIFMIDFile.
     */
    public MIFMIDFile() {
        
    }
    
    /**
     * Gets MIF file Header, storing the MIF file header that could be
     * utilized later, if necessary.
     */
    public String[] getMIFFileHeader(){
        return mifFileHeader;
    }
    
    /**
     * Gets PointLayer if it exists, otherwise returns null.
     */
    public PointLayer getPointLayer() {
        if (pointCount == 0)
            return null;
        else return pointLayer;
    }
    
    /**
     * Gets lineLayer if it exists, otherwise returns null.
     */
    public LineLayer getLineLayer() {
        if (lineCount == 0)
            return null;
        else return lineLayer;
    }
     
    /** get PolygonLayer if it exists, otherwise return null */
    public PolygonLayer getPolygonLayer() {
        if (polyCount==0)
            return null;
        else return polyLayer;
    }
    
    /**
     * Gets MultiLayer if it exists, otherwise returns null.
     */
    public MultiLayer getMultiLayer() {
        if (totalCount == 0)
            return null;
        else return multiLayer;
    }
    
    /**
     * Index of geoData and point,line poly GeoData
     * string "point", "line", "poly".
     * It means that if string[0] is "point", geoData[0]
     * is connected to pointGeoData[0],
     * both of them are in the same order.
     * It is also an index of each layer object and the total
     * objects which might be useful for some programs
     * using a string array which contain "point","poly","none","line",
     * if layerIndex[0] is "line", it means id = (0+1)=1 object is a line
     * object.
     */
    public String[] getGeoDataIndex(){
        return geoDataIndex;
    }
    
    
    /**
     * Gets total GeoData in the MID file.
     */
    public GeoData[] getMultiGeoData(){
        if (totalCount == 0)
            return null;
        else return multiGeoData;
    }
    
    /**
     * Gets Point GeoData in the MID file.
     */
    public GeoData[] getPointGeoData(){
        if (pointCount == 0)
            return null;
        else return pointGeoData;
    }
        
    /**
     * Gets Line GeoData in the MID file.
     */
    public GeoData[] getLineGeoData(){
        if (lineCount == 0)
            return null;
        else return lineGeoData;
    }
        
    /**
     * Gets Polygon GeoData in the MID file.
     */
    public GeoData[] getPolygonGeoData(){
        if (polyCount == 0)
            return null;
        else return polyGeoData;
    }
    
    /**
     * Gets shading sentences in the MID file.
     * Shading description strings in the mid file, i just read those lines as
     * strings for dealing with these late.
     * If object[0], "0" get from the total ojects id subtract 1, the object[0]
     * will contain a string. If there is no shading sentence for the object,
     * the string will be "none".
     */
    public String[] getShadingStrings(){
        return shadingStrings;
    }
    
    
    
    // Since the object of the class is for storing the file information of MIF
    // and MID files, that must be set up by mifReader class object in the same
    // package, those set methods could be package access level.
    
    
    void setShadingStrings(String[] obj){
        shadingStrings = obj;
    }
    void setGeoDataIndex(String[] s){
        geoDataIndex = s;
    }
    
    boolean setMIFFileHeader(String[] s){
        if(s == null) {
            return false;
        }
        else {
            mifFileHeader = s;
            return true;
        }
    }
        
    boolean setPointLayerAndPointCount(int count, PointLayer l){
        pointCount = count;
        if(count == 0) {
            return false;
        }
        else {
            pointLayer = l;
            return true;
        }
    }
    
    boolean setLineLayerAndLineCount(int count, LineLayer l){
        lineCount = count;
        if(count == 0) {
            return false;
        }
        else {
            lineLayer = l;
            return true;
        }
    }
        
    boolean setPolygonLayerAndPolygonCount(int count, PolygonLayer l){
        polyCount = count;
        if(count == 0) {
            return false;
        }
        else {
            polyLayer = l;
            return true;
        }
    }
        
    boolean setMultiLayerAndTotalCount(int count, MultiLayer l){
        totalCount = count;
        if(count == 0) {
            return false;
        }
        else {
            multiLayer = l;
            return true;
        }
    }
        
    boolean setMultiGeoData(GeoData[] geodata){
        if(geodata[0].getSize() == 0) {
            return false;
        }
        else{
            multiGeoData = geodata;
            return true;
        }
    }
    
    boolean setPointGeoData(GeoData[] geodata){
        if(geodata[0].getSize() == 0) {            
            return false;
        }
        else{
            pointGeoData = geodata;
            return true;
        }
        
    }
    
    boolean setLineGeoData(GeoData[] geodata){
        if(geodata[0].getSize() == 0) {
            return false;
        }
        else{
            lineGeoData = geodata;
            return true;
        }
    }
    
    boolean setPolygonGeoData(GeoData[] geodata){
        if(geodata[0].getSize() == 0) {
            return false;
        }
        else{
            polyGeoData = geodata;
            return true;
        }
    }
}
