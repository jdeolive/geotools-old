/*
 * FontTester.java
 *
 * Created on 02 August 2002, 14:15
 */

package spike.ian;

import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
/**
 * A class to explore and test my understanding of true type fonts under java
 * @author  iant
 */
public class FontTester extends JFrame{
    String dataFolder = "";
    java.awt.Font font;
    Font2DCanvas f2dc = new Font2DCanvas(font,0);
    /** Creates a new instance of FontTester */
    public FontTester() {
        f2dc.setMethod("drawGlyphVector()");
        f2dc.setDisplayType("All Glyphs");
        f2dc.setFractionalMetrics(true);
        f2dc.setAntialiasing(true);
        
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            if(dataFolder == null){
                dataFolder = "spike/ian/";
            } else {
                dataFolder+="/tests/unit/testData/";
            }
        }
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    
    public boolean loadFont(String name){
        try{
            // create input stream
            System.out.println("TEMP : " + System.getProperty("java.io.tmpdir"));

            File file = new File(System.getProperty("user.dir"),dataFolder+name);
            System.out.println("about to load "+file.toString());
            
            FileInputStream is = new FileInputStream(file);
            font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,is);
            f2dc.setFont(font.deriveFont(20f)); 
            
        } catch (IOException e){
            System.out.println("IO error " + e.toString());
            return false;
        } catch (FontFormatException fe){
            System.out.println("problem loading font " + fe.toString());
        } 
        return true;
    }
    
    public void display(){
        this.setSize(400,150);
        
        this.getContentPane().add(f2dc);
        show();
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FontTester ft = new FontTester();
        if(!ft.loadFont("geography.ttf")){
            System.out.println("Unable to load file");
            return;
        }
        ft.display();
    }
    
    
}
