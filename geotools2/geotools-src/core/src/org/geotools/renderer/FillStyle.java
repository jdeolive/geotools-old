/*
 * FillStyle.java
 *
 * Created on 16 January 2002, 14:29
 */

/**
 *
 * @author  ian
 * @version 
 */
public class FillStyle {
    int r;
    int g;
    int b;
    /** Creates new FillStyle */
    public FillStyle() {
    }
    
    /** Getter for property r.
     * @return Value of property r.
     */
    public int getR() {
        return r;
    }    

    /** Setter for property r.
     * @param r New value of property r.
     */
    public void setR(int r) {
        this.r = r;
    }
    
    /** Getter for property g.
     * @return Value of property g.
     */
    public int getG() {
        return g;
    }
    
    /** Setter for property g.
     * @param g New value of property g.
     */
    public void setG(int g) {
        this.g = g;
    }
    
    /** Getter for property b.
     * @return Value of property b.
     */
    public int getB() {
        return b;
    }
    
    /** Setter for property b.
     * @param b New value of property b.
     */
    public void setB(int b) {
        this.b = b;
    }
    public int getRGB(){
        return b+g*256+r*65536; // check this
    }
}
