package spike.ian;

/*
 * Font2DCanvas.java
 *
 * Created on 20 August 2002, 16:47
 */
/*
 * @(#)Font2DTest.java	1.1 99/11/19
 *
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.applet.Applet;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ResourceBundle;
import java.util.Locale;
import java.io.*;

import com.sun.image.codec.jpeg.*;

/**
 * Font2DTest.  
 *
 * @version @(#)Font2DTest.java	1.1 99/11/19
 * @author Chris Campbell
 */

public class Font2DCanvas extends Canvas implements Printable {
    Font font;
    int charHeight;
    int charWidth;
    int charBase;
    int drawMethod;
    int displayType;
    int pageImgHeight;
    int pageHeight;
    Object antialias;
    Object fracMetrics;
    BufferedImage offscr;
    boolean hasChanged;
    Vector dispText;
    Vector userText;
    Vector resourceText;
    

    static final int DRAW_CHARS           = 0;
    static final int DRAW_STRING_STRING   = 1;
    static final int DRAW_STRING_ITERATOR = 2;
    static final int DRAW_GLYPH_VECTOR    = 3;
    static final int DRAW_BYTES           = 4;
    static final int DRAW_TEXT_LAYOUT     = 5;

    static final int DISPLAY_RANGE     = 0;
    static final int DISPLAY_TEXT      = 1;
    static final int DISPLAY_GLYPHS    = 2;
    static final int DISPLAY_RESOURCES = 3;

    public Font2DCanvas(Font font, int base) {
        //this.font2dtest = f2dt;
        this.font     = font;
        charWidth     = 1;
        charHeight    = 1;
        pageImgHeight = 0;
        pageHeight    = 0;
        charBase      = base;
        hasChanged    = true;
        antialias     = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        fracMetrics   = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        dispText      = new Vector();
        dispText.add("This is Java 2D!");
        userText      = dispText;
        drawMethod    = DRAW_CHARS;
        displayType   = DISPLAY_RANGE;
        setupResourceStrings();
        repaint();
    }

    public void setBase(int base) {
        charBase = base;
        hasChanged = true;
        repaint();
    }

    public void setFont(Font font, String transform) {
        this.font = font;
        if (transform.equals("None") == false) {
            setFontTransform(transform);
        }
        hasChanged = true;
        resizeCanvas(false);
        repaint();
    }

    public void setFontTransform(String transform) {
        AffineTransform at = new AffineTransform();

        if (transform.equals("Translate")) {
            at.translate(10, 10);
        } else if (transform.equals("Rotate")) {
            at.rotate(Math.PI / 6);
        } else if (transform.equals("Scale")) {
            at.scale(2, 2);
        } else if (transform.equals("Shear")) {
            at.shear(.4, 0);
        }

        font = font.deriveFont(at);
    }

    public void setMethod(String method) {
        if (method.equals("drawChars()")) {
            drawMethod = DRAW_CHARS;
        } else if (method.equals("drawString(String)")) {
            drawMethod = DRAW_STRING_STRING;
        } else if (method.equals("drawString(Iterator)")) {
            drawMethod = DRAW_STRING_ITERATOR;
        } else if (method.equals("drawGlyphVector()")) {
            drawMethod = DRAW_GLYPH_VECTOR;
        } else if (method.equals("drawBytes()")) {
            drawMethod = DRAW_BYTES;
        } else if (method.equals("TextLayout.draw()")) {
            drawMethod = DRAW_TEXT_LAYOUT;
        }

        hasChanged = true;
        repaint();
    }

    public void setDisplayType(String type) {
        if (type.equals("Unicode Range")) {
            displayType = DISPLAY_RANGE;
        } else if (type.equals("User Text")) {
            dispText = userText;
            displayType = DISPLAY_TEXT;
        } else if (type.equals("All Glyphs")) {
            displayType = DISPLAY_GLYPHS;
        } else if (type.equals("Resource Text")) {
            displayType = DISPLAY_TEXT;
            userText = dispText;
            dispText = resourceText;
        }

        hasChanged = true;
        repaint();
    }
          
    public void setDisplayText(Vector textVector) {
        userText = textVector;
        dispText = userText;

        if (displayType == DISPLAY_TEXT) {
            hasChanged = true;
            repaint();
        }
    }

    protected void setupResourceStrings() {
        ResourceBundle rb;
        String holdString;
        String filename = "./resources/resource.data";
        resourceText = new Vector();

        File f = new File(filename);
        if (!f.exists()) {
            resourceText.add("Valid resource.data file needed for resource text");
            return;
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            while ((holdString = in.readLine()) != null) {
                if (holdString.length() >= 5) {
                    rb = ResourceBundle.getBundle("resources.TextResources", 
                                     new Locale(holdString.substring(0, 2),
                                                holdString.substring(3, 5)));
                    resourceText.add(rb.getString("string"));
                }
            }
            in.close();
        } catch (java.io.IOException ioe) {
            resourceText.add("Error reading resource text from file: " + 
                                 filename);
        } catch (java.util.MissingResourceException mre) {
            resourceText.add("Missing resource files... (*.properties files)");
        }
    }

    public void setAntialiasing(boolean aa) {
        antialias = (aa ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON :
                          RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        hasChanged = true;
        repaint();
    }

    public void setFractionalMetrics(boolean fm) {
        fracMetrics = (fm ? RenderingHints.VALUE_FRACTIONALMETRICS_ON :
                            RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        hasChanged = true;
        repaint();
    }

    public void resizeCanvas(boolean printing) {
        Graphics2D g2d = (Graphics2D)getGraphics();
        if (g2d != null) {
            int canvasWidth = 0;
            int canvasHeight = 0;

            g2d.setFont(font);
            RenderingHints hints = new RenderingHints(
                      RenderingHints.KEY_TEXT_ANTIALIASING, antialias);
            hints.put(RenderingHints.KEY_FRACTIONALMETRICS, fracMetrics);
            g2d.setRenderingHints(hints);
            FontRenderContext frc = g2d.getFontRenderContext();
            Rectangle2D rect = g2d.getFont().getMaxCharBounds(frc);
            if (displayType == DISPLAY_RANGE) {
                charWidth = (int)rect.getWidth() + 4;
                charHeight = (int)rect.getHeight() + 3;
                canvasWidth = (charWidth * 21) + 10;
                canvasHeight = (charHeight * 16) + getParentHeight();
            } else if (displayType == DISPLAY_TEXT) {
                int maxLineWidth = 1;
                String holdString;
                charWidth = (int)rect.getWidth();
                charHeight = (int)rect.getHeight() + 2;
                for (int i = 0; i < dispText.size(); i++) {
                    holdString = (String)dispText.elementAt(i);
                    if (holdString.length() > maxLineWidth) {
                        maxLineWidth = holdString.length();
                    }
                }
                canvasWidth = charWidth * maxLineWidth;
                canvasHeight = (charHeight * dispText.size()) + getParentHeight();
            } else {
                int numGlyphs = g2d.getFont().getNumGlyphs();
                charWidth = (int)rect.getWidth() + 4;
                charHeight = (int)rect.getHeight() + 3;
                canvasWidth = (charWidth * 21) + 10;
                canvasHeight = (charHeight * ((numGlyphs / 16) + 1)) + getParentHeight();
            }
        
            if (!printing) {
                setSize(canvasWidth, canvasHeight);
                resizeParent();
            }
        }
    }

    public void resizeParent() {
        Component c = getParent();
        if (c instanceof ScrollPane) {
            c.validate();
        }
    }

    public int getParentHeight() {
        Component c = getParent();
        if (c != null) {
            return c.getHeight();
        } else {
            return 0;
        }
    }

    public void writeSymbolImage(String filename) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            BufferedImage image = (BufferedImage)createImage(getWidth(), 
                                                             getHeight());
            Graphics g = image.getGraphics();
            paint(g);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam jep = encoder.getDefaultJPEGEncodeParam(image);
            jep.setQuality(1.0f, false);
            encoder.setJPEGEncodeParam(jep);
            encoder.encode(image);
            g.dispose();
            out.close();
        } catch (java.io.FileNotFoundException fnfe) {
            System.err.println("File not found: " + filename);
        } catch (java.io.IOException ioe) {
            System.err.println("Could not write file: " + filename);
        }
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if ((offscr == null) || (hasChanged == true)) {
            hasChanged = false;
            resizeCanvas(false);
            Dimension d = getSize();
            offscr = (BufferedImage)createImage(d.width, d.height);     
            Graphics2D offg2d = offscr.createGraphics();
            offg2d.setColor(getBackground());
            offg2d.fill(new Rectangle(0, 0, d.width, d.height));
            offg2d.setFont(font);
            offg2d.setColor(Color.black);
            RenderingHints hints = new RenderingHints(
                      RenderingHints.KEY_TEXT_ANTIALIASING, antialias);
            hints.put(RenderingHints.KEY_FRACTIONALMETRICS, fracMetrics);
            offg2d.setRenderingHints(hints);

            if (displayType == DISPLAY_RANGE) {
                paintRangeOffscreen(offg2d, 0, false);
            } else if (displayType == DISPLAY_TEXT) {
                paintTextOffscreen(offg2d, 0, false);
            } else {
                paintGlyphsOffscreen(offg2d, 0, false);
            }
        }
        g2d.drawImage(offscr, 0, 0, null);
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        Graphics2D g2d = (Graphics2D)g;

        resizeCanvas(true);

        pageImgHeight = (int)pageFormat.getImageableHeight();
        pageHeight = (int)pageFormat.getHeight();

        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setFont(font);
        g2d.setColor(Color.black);
        RenderingHints hints = new RenderingHints(
                  RenderingHints.KEY_TEXT_ANTIALIASING, antialias);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, fracMetrics);
        g2d.setRenderingHints(hints);

        if (displayType == DISPLAY_RANGE) {
            return paintRangeOffscreen(g2d, pageIndex, true);
        } else if (displayType == DISPLAY_TEXT) {
            return paintTextOffscreen(g2d, pageIndex, true);
        } else {
            return paintGlyphsOffscreen(g2d, pageIndex, true);
        }
    }

    public int paintRangeOffscreen(Graphics2D g2d, 
                                   int pageIndex, boolean printing) {
        FontRenderContext frc = g2d.getFontRenderContext();
        Font labelFont = new Font("Monospaced", Font.PLAIN, 12);
        char[] carray = new char[1];
        int c = charBase;
        int x = 0;
        int y = 0;
        int yp = charHeight;
        int yh = getHeaderLineHeight(g2d) * 4;
        int pitop = (pageImgHeight - yh - charHeight) * pageIndex;
        int pibot = (pageImgHeight - yh - charHeight) * (pageIndex + 1);
        boolean modified = false;

        g2d.setFont(labelFont);
        int startx = (int)g2d.getFont().getStringBounds("0000", frc).getWidth() 
                         + charWidth;
        g2d.setFont(font);

        for (int v = 0; v < 16; v++) {
            if (printing) {
                if (yp >= pibot) {
                    return Printable.PAGE_EXISTS; 
                } else if (yp < pitop) {
                    yp += charHeight;
                    c += 16;
                    continue;
                } else {
                    if (!modified) {
                        paintHeader(g2d, pageIndex);
                        y = yh + charHeight;
                    } else {
                        y += charHeight;
                    }
                    modified = true;
                }
            } else {
                y = yp;
            }

            g2d.setFont(labelFont);
            g2d.drawString(Integer.toHexString(c), 10, y);
            g2d.setFont(font);
            x = startx;
            
            for (int h = 0; h < 16; h++) {
                carray[0] = (char)c++;
                if (g2d.getFont().canDisplay(carray[0]) || printing) {
                    paintString(g2d, new String(carray, 0, 1), x, y);
                } else {
                    g2d.setColor(Color.red);
                    paintString(g2d, new String(carray, 0, 1), x, y);
                    g2d.setColor(Color.black);
                } 
                x += charWidth;
            }
            yp += charHeight;
        }

        if (modified) {
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    public int paintTextOffscreen(Graphics2D g2d, 
                                   int pageIndex, boolean printing) {
        String holdString;
        int y = 0;
        int yp = charHeight;
        int yh = getHeaderLineHeight(g2d) * 4;
        int pitop = (pageImgHeight - yh - charHeight) * pageIndex;
        int pibot = (pageImgHeight - yh - charHeight) * (pageIndex + 1);
        boolean modified = false;

        for (int i = 0; i < dispText.size(); i++) {
            if (printing) {
                if (yp >= pibot) {
                    return Printable.PAGE_EXISTS; 
                } else if (yp < pitop) {
                    yp += charHeight;
                    continue;
                } else {
                    if (!modified) {
                        paintHeader(g2d, pageIndex);
                        y = yh + charHeight;
                    } else {
                        y += charHeight;
                    }
                    modified = true;
                }
            } else {
                y = yp;
            }

            holdString = (String)dispText.elementAt(i);
            paintString(g2d, holdString, charWidth, y);
            yp += charHeight;
        }

        if (modified) {
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    public int paintGlyphsOffscreen(Graphics2D g2d, 
                                    int pageIndex, boolean printing) {
        FontRenderContext frc = g2d.getFontRenderContext();
        Font g2dFont = g2d.getFont();
        int[] glyphIndices = new int[1];
        int numGlyphs = g2d.getFont().getNumGlyphs();
        int numRows = (((numGlyphs % 16) == 0) ? (numGlyphs / 16) : 
                                                 (numGlyphs / 16) + 1);
        int x = 0;
        int y = 0;
        int yp = charHeight;
        int yh = getHeaderLineHeight(g2d) * 4;
        int pitop = (pageImgHeight - yh - charHeight) * pageIndex;
        int pibot = (pageImgHeight - yh - charHeight) * (pageIndex + 1);
        Font labelFont = new Font("Monospaced", Font.PLAIN, 12);
        GlyphVector gv;
        boolean modified = false;

        g2d.setFont(labelFont);
        int startx = (int)g2d.getFont().getStringBounds("0000", frc).getWidth() 
                         + charWidth;
        g2d.setFont(g2dFont);

        for (int h = 0; h < numRows; h++) {
            if (printing) {
                if (yp >= pibot) {
                    return Printable.PAGE_EXISTS; 
                } else if (yp < pitop) {
                    yp += charHeight;
                    continue;
                } else {
                    if (!modified) {
                        paintHeader(g2d, pageIndex);
                        y = yh + charHeight;
                    } else {
                        y += charHeight;
                    }
                    modified = true;
                }
            } else {
                y = yp;
            }

            g2d.setFont(labelFont);
            g2d.drawString(Integer.toHexString(h * 16), 10, y);
            g2d.setFont(g2dFont);
            x = startx;

            for (int v = 0; v < 16; v++) {
                if (numGlyphs > ((h * 16) + v)) {
                    glyphIndices[0] = (h * 16) + v;
                    gv = g2dFont.createGlyphVector(frc, glyphIndices);
                    g2d.drawGlyphVector(gv, x, y);
                } else {
                    g2d.setColor(Color.red);
                    glyphIndices[0] = g2dFont.getMissingGlyphCode();
                    gv = g2dFont.createGlyphVector(frc, glyphIndices);
                    g2d.drawGlyphVector(gv, x, y);
                    g2d.setColor(Color.black);
                }
                x += charWidth;
            }
            yp += charHeight;
        }    
        
        if (modified) {
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    protected int getHeaderLineHeight(Graphics2D g2d) {
        Font currentFont = g2d.getFont();

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D rect = g2d.getFont().getMaxCharBounds(frc);  

        g2d.setFont(currentFont);

        return (int)rect.getHeight() + 2; 
    }

    public int paintHeader(Graphics2D g2d, int pageIndex) {
        String str;
        Font currentFont = g2d.getFont();
        
        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D rect = g2d.getFont().getMaxCharBounds(frc);
        int h = getHeaderLineHeight(g2d);

        

        str = "Font: " + font.getName();                
         g2d.drawString(str, 5, h);

        
        str = "Page: " + (pageIndex + 1);
        g2d.drawString(str, 5, h * 4);

        g2d.setFont(currentFont);

        return (h * 4);
    }

    public void paintString(Graphics2D g2d, String str, int x, int y) {
        int len = str.length();
        FontRenderContext frc = g2d.getFontRenderContext();

        switch (drawMethod) {
        case DRAW_GLYPH_VECTOR:
            GlyphVector gv = g2d.getFont().createGlyphVector(frc, str);
            g2d.drawGlyphVector(gv, (float)x, (float)y);
            break;
        case DRAW_STRING_STRING:
            g2d.drawString(str, x, y);
            break;
        case DRAW_STRING_ITERATOR:
            AttributedString as = new AttributedString(str);
            as.addAttribute(TextAttribute.FONT, g2d.getFont());
            AttributedCharacterIterator aci = as.getIterator();
            g2d.drawString(aci, x, y);
            break;
        case DRAW_CHARS:
            char[] carray = new char[1024];
            if (len > 0) {
                str.getChars(0, len, carray, 0);
                g2d.drawChars(carray, 0, len, x, y);
            }
            break;
        case DRAW_BYTES:
            byte[] barray;
            if (len > 0) {
                barray = str.getBytes();
                g2d.drawBytes(barray, 0, len, x, y);
            }
            break;
        case DRAW_TEXT_LAYOUT:
            TextLayout tl = new TextLayout(str, g2d.getFont(), frc);
            tl.draw(g2d, x, y);
            break;
        default:
            break;
        }
    }
}
