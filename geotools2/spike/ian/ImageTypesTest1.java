/*
 * ImageTypesTest.java
 *
 * Created on 09 October 2002, 17:07
 */

package spike.ian;

import javax.imageio.ImageIO;

/**
 * A simple test program to see what image types can be read and writen on your machine using the JAI 
 * @author  iant
 */
public class ImageTypesTest1 {
    
    /** Creates a new instance of ImageTypesTest */
    public ImageTypesTest1() {
    }
    
    public static void main(String args[]){
        String[] types = ImageIO.getWriterMIMETypes();
        if(types.length > 0){
            System.out.println("You can write the following formats:");
        }else{
            System.out.println("You have no writers registered, that's odd!");
        }
        for(int i=0;i<types.length;i++){
            System.out.println(types[i]);
        }
        types = ImageIO.getReaderMIMETypes();
        if(types.length > 0){
            System.out.println("You can read the following formats:");
        }else{
            System.out.println("You have no readers registered, that's odd!");
        }
        for(int i=0;i<types.length;i++){
            System.out.println(types[i]);
        }
    }
}
