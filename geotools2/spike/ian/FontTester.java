/*
 * FontTester.java
 *
 * Created on 02 August 2002, 14:15
 */

package spike.ian;

import java.io.*;
import java.awt.*;
import javax.swing.*;
/**
 * A class to explore and test my understanding of true type fonts under java
 * @author  iant
 */
public class FontTester extends JFrame{
    String dataFolder = "";
    java.awt.Font font;
    /** Creates a new instance of FontTester */
    public FontTester() {
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
            setFont(font.deriveFont(20f)); 
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
        show();
    }
    
    public void paint(Graphics g){
        g.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ",25,50);
        g.drawString("abcdefghijklmnopqrstuvwxyz",25,100);
        g.drawString("0123456789 !\"£$%^&*() []{};:'@#~,</?|",25,150);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FontTester ft = new FontTester();
        if(!ft.loadFont("Mapdraw2.ttf")){
            System.out.println("Unable to load file");
            return;
        }
        ft.display();
    }
    
}
