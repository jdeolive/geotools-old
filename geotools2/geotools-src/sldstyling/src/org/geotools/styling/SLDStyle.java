/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.styling;

import org.geotools.filter.Expression;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;


/** A class to read and parse an SLD file based on verion 0.7.2 of the OGC
 * Styled Layer Descriptor Spec.
 * @author Ian Turton
 * @author Sean Geoghegan
 * @version $Id: SLDStyle.java,v 1.42 2004/03/28 04:08:50 jmacgill Exp $
 * @deprecated Replaced by SLDParser which has a more appropriate name
 */
public class SLDStyle extends SLDParser {
 // to make pmd to shut up
    
    /**
     * Create a Stylereader - use if you already have a dom to parse.
     *
     * @param factory The StyleFactory to use to build the style
     */
    public SLDStyle(StyleFactory factory) {
        super(factory);
    }
    
    /**
     * Creates a new instance of SLDStyler
     *
     * @param factory The StyleFactory to use to read the file
     * @param filename The file to be read.
     *
     * @throws java.io.FileNotFoundException - if the file is missing
     */
    public SLDStyle(StyleFactory factory, String filename)
    throws java.io.FileNotFoundException {
       super(factory, filename);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param f the File to be read
     *
     * @throws java.io.FileNotFoundException - if the file is missing
     */
    public SLDStyle(StyleFactory factory, File f)
    throws java.io.FileNotFoundException {
        super(factory, f);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param url the URL to be read.
     *
     * @throws java.io.IOException - if something goes wrong reading the file
     */
    public SLDStyle(StyleFactory factory, java.net.URL url)
    throws java.io.IOException {
       super(factory, url);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param s The inputstream to be read
     */
    public SLDStyle(StyleFactory factory, java.io.InputStream s) {
        super(factory,s);
    }

}
