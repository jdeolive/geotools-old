/**
 * MIFMIDReader.java
 *
 * Created on December 15, 2001, 11:32 PM
 **/

package org.geotools.mapinfo;
import java.net.*;
import java.io.*;
import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.layer.*;
import uk.ac.leeds.ccg.geotools.geodata.*;
import java.util.*;

/**
 * As part of the geotool package, MIFMIDReader could read
 * mapinfo interchange files (MIF,MID) as geotools objects.
 * construct a MIFMIDFile class object in which the geographical information are stored.
 * it is quite simple to use the class. only three public method can be called in it.
 * Two of them are construct methods used to create a MIFMIDReader object
 * by using a File object or a URL object respectively.
 * Another one is getMIFMIDFile() which is the only method can called in this class
 * it will return an MIFMIDFile object.
 *
 * if the read method of some object type in mif file have not been developped
 * in the current version of the class, it will throw MIFObjectTypeException
 * @see MIFObjectTypeException
 * @see MIFMIDFile
 * @author  Jianhui Jin
 * @version 1.0
 **/
public class MIFMIDReader extends java.lang.Object {
    private URL mifUrl=null; // file url
    private File mifFileName=null;// file name
    private BufferedReader reader=null;
    private int DATATYPE=0;// storing the data type ids for the mif file
    private MultiLayer multiLayer;// combine all the possible layer together since mif contain all possible geographical objects.points lines polygons
    private PointLayer pointLayer=new PointLayer(); // point layer for point objects
    private LineLayer lineLayer=new LineLayer();// line layer for line,pline objects
    private PolygonLayer polyLayer=new PolygonLayer();// polygons layer for region,rect objects
    //private Hashtable table1=new Hashtable();
    private String delimiter = null;// delimiter for database
    private int colNumber=0;// column number of database
    private String[] colName;// column name of the current geodata
    private byte[] colType;// two possible '0' for double and '1' for string for the type of column in database
    private SimpleGeoData[] geoData; // contain all the attribut data
    private SimpleGeoData[] pointGeoData;// contain attribute data for point layer
    private SimpleGeoData[] lineGeoData;// contain attribute data for line layer
    private SimpleGeoData[] polyGeoData;// contain attribute data for polygon layer
    private int id=0,polyId=0,lineId=0,pointId=0,noneId=0;// count the number of geographical objects
    private String fileLine=null;// the line in mif file currently has been readed in the reader. only read once throught out all the method, continues read thought out the file
    private String midFileLine=null;// the line in mid file currently has been readed in the reader. only read once throught out all the method, continues read thought out the file
    private BufferedReader midReader;// bufferedReader object for mid file
    private Object[][] midData;// mid file data in array
    private Vector objectDbaseIndex= new Vector(1,1);//  index of geotools geographical objects' id and mif geographical objects' id
    private Vector layerVector = new Vector(1,1);// index of geotools geographical objects' id starting from 0 and each layer name string
    private MIFMIDFile mifmidFile; // MIFMIDFile object contain all the information read
    private int objectNumber=0;// mif geographical objects number
    private String shadingString=new String();// storing the currectly read shadingStrings
    private Hashtable shadingStringsTable=new Hashtable(); // keep all shading Strings, late convert to shadingStr.
    private String[] shadingStr;// finally storing all shadingString
    private int shadingNumber=0;// count the shadingStrings in the mif file
    private String[] mifFileHeader; // mif file header from top to "Data" or "data" line
    
    
    /** Creates new MIFMIDReader by using URL as file source
     * if the read method of some object type in mif file have not been developped
     * in the current version of the class, it will throw MIFObjectTypeException
     */
    public MIFMIDReader(URL url) throws MIFObjectTypeException, FileNotFoundException, IOException {
        String urlFile=url.toString();
       
        if(urlFile.endsWith("d"))
            url= new URL(urlFile.substring(0,urlFile.length()-1)+"f");
        else if(urlFile.endsWith("D"))
            url= new URL(urlFile.substring(0,urlFile.length()-1)+"F");
        this.mifUrl=url; // assign url to the parameter read() need
        
        read(); // read all data
        
        setMIFMIDFile(); // assign data to MIFMIDFile object
        
        
        
    }
    
    /** Creates new MIFMIDReader by using URL as file source
     * if the read method of some object type in mif file have not been developped
     * in the current version of the class, it will throw MIFObjectTypeException
     */
    public MIFMIDReader(File fName) throws MIFObjectTypeException, FileNotFoundException, IOException {
        String fn=fName.toString();
        if(fn.endsWith("d"))
            fName= new File(fn.substring(0,fn.length()-1)+"f");
        else if(fn.endsWith("D"))
            fName= new File(fn.substring(0,fn.length()-1)+"F");
        
        this.mifFileName=fName;// assign fName to the parameter read() need
        
        read();// read all data
        
        setMIFMIDFile();// assign data to MIFMIDFile object
        
    }
    
    
    // create and set the MIFMIDFile Object
    private void setMIFMIDFile(){
        multiLayer=new MultiLayer(); // construct multiLayer;
        
        if(pointId>0) multiLayer.addLayer(pointLayer);// if point object number greater than 0, add it to multiLayer
        if(lineId>0)  multiLayer.addLayer(lineLayer);//if line object number greater than 0, add it to multiLayer
        if(polyId>0)  multiLayer.addLayer(polyLayer);//if polygon object number greater than 0, add it to multiLayer
        
        mifmidFile=new MIFMIDFile(); // construct MIFMIDFile object
        
        // the below code basicly assign all the data to mifmidfile object calling related methods
        mifmidFile.setMIFFileHeader(mifFileHeader);
        
        mifmidFile.setLineGeoData(lineGeoData);
        mifmidFile.setLineLayerAndLineCount(lineId,lineLayer);
        
        mifmidFile.setMultiGeoData(geoData);
        mifmidFile.setMultiLayerAndTotalCount(id,multiLayer);
        
        mifmidFile.setPointGeoData(pointGeoData);
        mifmidFile.setPointLayerAndPointCount(pointId,pointLayer);
        
        mifmidFile.setPolygonGeoData(polyGeoData);
        mifmidFile.setPolygonLayerAndPolygonCount(polyId,polyLayer);
        
        // convert Vector layerVector to String[] one by one and get rid of "none" object
        int id=0;
        Object[] temp=layerVector.toArray();
        Vector newlayerVector=new Vector(1,1);
        for(int i=0;i<temp.length;i++){
            if(temp[i].toString().equalsIgnoreCase("none"))
                continue;
            
            newlayerVector.add(temp[i].toString());
        }
        temp =newlayerVector.toArray();
        String[] strings=new String[newlayerVector.toArray().length];
        for(int i=0;i<temp.length;i++)
        {
            strings[i]=(String) temp[i];
        }
        
        mifmidFile.setGeoDataIndex(strings);
        
        
        constructShadingStrings(); // convert Vector to String[]
        mifmidFile.setShadingStrings(shadingStr);
        
        
    }
     /**
      * call the method, get the object of MIFMIDFile class storing all the informaion
      * in the mif and mid file.
      */
    public MIFMIDFile getMIFMIDFile(){
        return mifmidFile;
    }
    // change as private
    
    // get bufferedreader object for reading mif file
    private BufferedReader getReader() throws FileNotFoundException, IOException{
        BufferedReader reader=null;
       
            if(mifUrl!=null)
                reader=new BufferedReader(new InputStreamReader(mifUrl.openStream()));
            if(mifFileName!=null)
                reader=new BufferedReader(new FileReader(mifFileName));
        
        
        return reader;
    }
    
    // do all the reading job
    private void read() throws MIFObjectTypeException,FileNotFoundException,IOException {
        
            reader=getReader(); // get bufferedreader for reading mif file
            headerReader(); // read mif file header
            
            do{
                
                StringTokenizer lineSt=new StringTokenizer(fileLine); // construct stringtokenizer for each line
                typeChooser(lineSt.nextToken());// choose object type so called data type here
                // choose related object read method and call it
                switch(DATATYPE){
                    case  0 : pointLayer.addGeoPoint(pointReader()); //read point
                    break;
                    
                    case  1 : lineLayer.addGeoLine(lineReader());// read line
                    break;
                    
                    case  2 : lineLayer.addGeoLine(
                    plineReader(
                    Integer.valueOf(lineSt.nextToken()).intValue())); // read mulitiline
                    break;
                    
                    case  3 : GeoPolygon[] poly=regionReader();
                    for(int i=0;i<poly.length;i++)
                        polyLayer.addGeoPolygon(poly[i]); // read region or polygons
                    break;
                    
                    case  4 : polyLayer.addGeoPolygon(rectReader());// read rect
                    break;
                    
                    case  5 : id++;
                    noneId++;
                    objectNumber++;
                    objectDbaseIndex.add(new Integer(objectNumber));
                    layerVector.add("none"); // read "none" object and count every thing need
                    break;
                    
                    case  6 : shadingReader(); // read the one shading text line
                    break;
                }
                
                if(DATATYPE!=6&&objectNumber>1) assignShadingStrings(); // construct a string storing all shading strings for one object
            }
            while((fileLine=reader.readLine())!=null);
            midReader();
            assignShadingStrings();//construct possible last one,since the loop stop.
        
        
        
    }
    
    // read mif file header part
    private void headerReader() throws IOException{
        Vector headerString= new Vector();// contain the file header line strings and will be convected
        // into the String[] late in the method
        
        while(!(fileLine=reader.readLine()).substring(0,4).equalsIgnoreCase("Data"))
        {
            headerString.add(fileLine);// vector add each fileline
            StringTokenizer st=new StringTokenizer(fileLine);
            String fileItem=st.nextToken();// get the start string
            // get delimiter from header for reading atribute data in mid file
            if (fileItem.equalsIgnoreCase("delimiter"))
            {
                String s= st.nextToken();
                delimiter=s.substring(1,s.length()-1);
            }
            // get column number ,name and type
            // the code will convert types of char,logical and date to string
            // types of interger smallint,decilmal,float to Double since the requirement of geodat class
            // since there are only two possibe datta type can be stored in the GeoData object, mid file data have to convert into those two types.
            if (fileItem.equalsIgnoreCase("columns"))
            {
                colNumber= Integer.valueOf(st.nextToken()).intValue(); // total how many columns in mid file
                colType=new byte[colNumber]; // for keeping the converted data types for each columns
                colName=new String[colNumber]; // columns' names
                for(int i=0;i<colNumber;i++)
                {
                    fileLine=reader.readLine(); // get this line
                    headerString.add(fileLine);// vector add each header fileline
                    StringTokenizer sto=new StringTokenizer(fileLine);
                    colName[i]=sto.nextToken();// the start string is column name
                    String s=sto.nextToken(); // second is the column type
                    if(s.startsWith("Char")|s.startsWith("char")|s.startsWith("Logical")|s.startsWith("logical")|s.startsWith("Date")|s.startsWith("date"))
                        colType[i]=1; // convert all the data type which are able be convert into string, remember it in the byte[]
                    else colType[i]=0; // the rest , all will be converted as Double.
                    
                    
                }
            }
        }
        
        // convert Vector to String one by one
        Object[] headerStr=headerString.toArray();
        mifFileHeader=new String[headerStr.length];
        for(int i=0;i<headerStr.length;i++)
        {
            mifFileHeader[i]=(String) headerStr[i];
            
        }
        
        while((fileLine=reader.readLine()).length()<2);// skin the possible empty line
        // System.out.println(delimiter);
        
        //     return new StringTokenizer(fileLine).nextToken();
        
        
    }
    
    // judge the data type of the string representing from the read() method
    // used in read() to choose the right method to read the right data type
    private void typeChooser(String typeStr) throws MIFObjectTypeException {
        if(typeStr.equalsIgnoreCase("point"))
            DATATYPE=0;
        else if(typeStr.equalsIgnoreCase("line"))
            DATATYPE=1;
        else if(typeStr.equalsIgnoreCase("pline"))
            DATATYPE=2;
        else if(typeStr.equalsIgnoreCase("region"))
            DATATYPE=3;
        else if(typeStr.equalsIgnoreCase("rect"))
            DATATYPE=4;
        else if(typeStr.equalsIgnoreCase("none"))
            DATATYPE=5;
        else if(typeStr.equalsIgnoreCase("multipoint")|typeStr.equalsIgnoreCase("roundrect")
        |typeStr.equalsIgnoreCase("arc")|typeStr.equalsIgnoreCase("text")
        |typeStr.equalsIgnoreCase("ellipse")
        |typeStr.equalsIgnoreCase("collection"))
            
            throw new MIFObjectTypeException(typeStr);
        // those types have not been dealed with in the class , will throw the exception
        else DATATYPE=6; // the rest text line must be shading texts.
    }
    
    
    // read each shading sentence and add to the last one, combined as one line
    private void shadingReader(){
        shadingString = shadingString+" "+fileLine;
        
    }
    // after the shading string for one object read, put it in the the table
    private void assignShadingStrings(){
        shadingNumber++;//
        if(shadingString.equalsIgnoreCase("")) shadingString="none";
        shadingStringsTable.put(new Integer(shadingNumber),shadingString);
        shadingString=new String(); // new one for next object.
    }
    // convect table to string[] one by one
    private void constructShadingStrings(){
        Object[] indexObject=objectDbaseIndex.toArray();
        
        shadingStr=new String[id-noneId];
        for(int i=0;i<shadingStr.length;i++)
        {
            shadingStr[i]=(String) shadingStringsTable.get((Integer) indexObject[i]);
            //System.out.println(shadingStr[i]);
        }
    }
    
    
    // open mid file, get Bufferreader
    private void getMidReader() throws FileNotFoundException, IOException{
        
        if(mifFileName==null)
        {
            String urlstr=mifUrl.toString();
            midReader= new BufferedReader(new InputStreamReader(new URL(urlstr.substring(0,urlstr.length()-3)+"mid").openStream()));
        }
        else midReader= new BufferedReader(new FileReader(mifFileName.toString().substring(0,mifFileName.toString().length()-3)+"mid"));
        
    }
    
    // read all the data in mid file
    private void midReader() throws IOException{
        int c=0; // each character read once, one by one, since the structure of mid file quite complicated, not suitable for using stringtokenizer
        StringBuffer s=null; // storing each character and construct individual strings
        geoData=new SimpleGeoData[colNumber]; // all geodata array, one element for one column designed by james
        lineGeoData=new SimpleGeoData[colNumber];// line geodata array, one element for one column designed by james
        pointGeoData=new SimpleGeoData[colNumber];// point geodata array, one element for one column designed by james
        polyGeoData=new SimpleGeoData[colNumber]; // polygon geodata array, one element for one column designed by james
        getMidReader(); // construct bufferedreader
        midData= new Object[objectNumber][colNumber]; // storing the separated strings in the following codes
        
        // read mid file and store it in midData
        for(int i=0;i<objectNumber;i++)
        {
            for(int j=0;j<colNumber;j++)
            {
                int commainmiddle=0;//for check the pair of " " in case comma " , "  might exist in the middle of string item
                s=new StringBuffer();
                while( (((c = midReader.read())!=-1)&&(c!=',')&&(c!='\n'))|commainmiddle%2==1) // read character one by one until the end of each data item
                {
                    if(c=='"')
                        commainmiddle++; // count character "
                    
                    if((c=='"')|(c=='\r'))
                        continue;
                    s.append((char) c); // joint each character except " and \r
                }
                if(s.length()==0)
                    midData[i][j]="empty";
                else midData[i][j]=s.toString(); // construct string , if is empty , assig "empty"
                
                
            }
            
        }
        
        
        // construct GeoData[] in the following loop.
        // each geodata contain data in the middata array from top to bottom, then left to right.
        for(int i=0;i<colNumber;i++)
        {
            Hashtable table = new Hashtable();
            Hashtable pointtable = new Hashtable();
            Hashtable linetable = new Hashtable();
            Hashtable polytable = new Hashtable();
            Hashtable individualtable=null;// used as a pointer connecting pointtable linetable and polytable when need.
            int idAll=0,idPoint=0,idLine=0,idPoly=0; // data id
            int idLink=0;// used as a pointer connecting the id above
            
            
            for(int j=0;j<id;j++)
            {
                Object data= midData[((Integer) objectDbaseIndex.elementAt(j)).intValue()-1][i]; // since the mif object might contain one or more geotools object
                // using the objectDbaseIndex to point out
                // using layerVector to decide link individualtable to which table among pointtable,linetable,polytable
                if(layerVector.elementAt(j).equals("point"))
                {individualtable=pointtable;idAll++;idPoint++;idLink=idPoint;}
                
                else if (layerVector.elementAt(j).equals("line"))
                {individualtable=linetable;idAll++;idLine++;idLink=idLine;}
                
                
                else if (layerVector.elementAt(j).equals("poly"))
                {individualtable=polytable;idAll++;idPoly++;idLink=idPoly;}
                
                else if (layerVector.elementAt(j).equals("none"))
                    continue; // get rid of the attribut data for none object
                
                // use colType[] elements to decide the data type putted in each table
                if(colType[i]==0&&data=="empty")
                { table.put(new Integer(idAll),new Double(0.00));
                individualtable.put(new Integer(idLink),new Double(0.00));
                }
                
                else if (colType[i]==0)
                {     table.put(new Integer(idAll),new Double((String) data));
                individualtable.put(new Integer(idLink),new Double((String) data));
                }
                else if(colType[i]==1&&data=="empty")
                {
                    table.put(new Integer(idAll),"empty");
                    individualtable.put(new Integer(idLink),"empty");
                }
                else    {
                    table.put(new Integer(idAll),(String) data);
                    individualtable.put(new Integer(idLink),(String) data);
                }
            }
            
            // construct all the geoData[]
            geoData[i]=new SimpleGeoData(table);
            geoData[i].setName(colName[i]);
            if(colType[i]==1) geoData[i].setDataType(GeoData.CHARACTER);
            
            
            
            
            pointGeoData[i]=new SimpleGeoData(pointtable);
            pointGeoData[i].setName(colName[i]);
            if(colType[i]==1) pointGeoData[i].setDataType(GeoData.CHARACTER);
            
            
            
            lineGeoData[i]=new SimpleGeoData(linetable);
            lineGeoData[i].setName(colName[i]);
            if(colType[i]==1) lineGeoData[i].setDataType(GeoData.CHARACTER);
            
            
            
            polyGeoData[i]=new SimpleGeoData(polytable);
            polyGeoData[i].setName(colName[i]);
            if(colType[i]==1) polyGeoData[i].setDataType(GeoData.CHARACTER);
        }
        
        midReader.close();
    }
    
    // read point object
    private GeoPoint pointReader(){
        pointId++;//geotools point count
        id++;//total count
        layerVector.add("point");//index of total id and individual id, for example pointId, which one is which one
        objectNumber++; // mif object id
        objectDbaseIndex.add(new Integer(objectNumber));//index of geotools object and mif objects
        StringTokenizer st=new StringTokenizer(fileLine);
        st.nextToken();//first string is a string "Point", skin it
        // second is x position data, third is y position data
        return new GeoPoint(id,Double.valueOf(st.nextToken()).doubleValue(),
        Double.valueOf(st.nextToken()).doubleValue());
    }
    
    //read rectangle type
    private GeoPolygon rectReader(){
        polyId++;// geotoosl polygon count
        id++;//total count
        layerVector.add("poly");//index of total id and individual id, for example pointId, which one is which one
        objectNumber++; // mif object id
        objectDbaseIndex.add(new Integer(objectNumber));//index of geotools object and mif objects
        StringTokenizer st=new StringTokenizer(fileLine);
        st.nextToken();// skin the string "rect"
        GeoPoint[] point=new GeoPoint[4];// rectangle has four points consisting of one polygon
        point[0]=new GeoPoint(Double.valueOf(st.nextToken()).doubleValue(),
        Double.valueOf(st.nextToken()).doubleValue()); //left-up point
        
        point[2]=new GeoPoint(Double.valueOf(st.nextToken()).doubleValue(),
        Double.valueOf(st.nextToken()).doubleValue()); //right-bottom point
        point[1]=new GeoPoint(point[2].getX(),point[0].getY()); //right-up point
        point[3]=new GeoPoint(point[0].getX(),point[2].getY()); //left-bottom point
        return new GeoPolygon(id,point);//construct GeoPolygon object
    }
    
    // read line type
    private GeoLine lineReader(){
        lineId++;// geotools line count
        id++;// total count
        layerVector.add("line");//index of total id and individual id, for example pointId, which one is which one
        objectNumber++;// mif object id
        objectDbaseIndex.add(new Integer(objectNumber));//index of geotools object and mif objects
        StringTokenizer st=new StringTokenizer(fileLine);
        st.nextToken();// skin the string "line"
        GeoPoint[] point=new GeoPoint[2];// two point to one line
        point[0]=new GeoPoint(Double.valueOf(st.nextToken()).doubleValue(),
        Double.valueOf(st.nextToken()).doubleValue());
        point[1]=new GeoPoint(Double.valueOf(st.nextToken()).doubleValue(),
        Double.valueOf(st.nextToken()).doubleValue());
        
        return new GeoLine(id,point);
    }
    
    
    
    // poly line type,take lineNum as parameter to decide how many points in theline
    private GeoLine plineReader(int lineNum) throws IOException{
        objectNumber++;// mif object id
        GeoPoint[] point=new GeoPoint[lineNum];// many points
        
        for(int i=0;i<lineNum;i++)
        {
            layerVector.add("line");////index of total id and individual id, for example pointId, which one is which one
            objectDbaseIndex.add(new Integer(objectNumber));//index of geotools object and mif objects
            lineId++;// geotools line count
            id++;// total count
            fileLine=reader.readLine();
            StringTokenizer st=new StringTokenizer(fileLine);
            point[i]=new GeoPoint(Double.valueOf(st.nextToken()).doubleValue(),
            Double.valueOf(st.nextToken()).doubleValue());
        }
        
        return new GeoLine(id,point);
    }
    
    // region type
    private GeoPolygon[] regionReader() throws IOException{
        
        int polyNumber=0,pointNumber=0;// how many polygon in the region and how many points for each polygon
        GeoPolygon[] poly;// total polygons
        
        StringTokenizer polyNumtoken= new StringTokenizer(fileLine);
        polyNumtoken.nextToken();// how many polygons
        polyNumber=Integer.valueOf(polyNumtoken.nextToken()).intValue();
        poly=new GeoPolygon[polyNumber];
        objectNumber++;// mif object count
        
        // the loop read each polygon
        for(int i=0;i<polyNumber;i++)
        {
            pointNumber=Integer.valueOf(new StringTokenizer(reader.readLine()).nextToken()).intValue();// how many point in this polygon
            GeoPoint[] point=new GeoPoint[pointNumber];// point array for this polygon
            polyId++;// polygon count
            id++;// total count
            
            layerVector.add("poly");//index of total id and individual id, for example pointId, which one is which one
            objectDbaseIndex.add(new Integer(objectNumber));//index of geotools object and mif objects
            
            // loop take each point position data
            for(int j=0;j<pointNumber;j++)
            {
                fileLine=reader.readLine();
                StringTokenizer pointToken=new StringTokenizer(fileLine);
                point[j]=new GeoPoint(Double.valueOf(pointToken.nextToken()).doubleValue(),
                Double.valueOf(pointToken.nextToken()).doubleValue());
            }
            
            poly[i]=new GeoPolygon(id,point);
        }
        return poly;
        
    }
}
