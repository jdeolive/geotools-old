/*
 * Created on Jan 21, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.attributes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
/**
 * GazetteerNameValidationTest purpose.
 * <p>
 * Description of GazetteerNameValidationTest ...
 * </p>
 *  
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: GazetteerNameValidationTest.java,v 1.1 2004/02/17 17:19:15 dmzwiers Exp $
 */
public class GazetteerNameValidationTest extends TestCase {

	public GazetteerNameValidationTest(){super("");}
	public GazetteerNameValidationTest(String s){super(s);}
	
	public void testValidate() {
		class testFeature implements Feature{
			Map attrs = new HashMap();
			public FeatureCollection getParent(){return null;}
			public void setParent(FeatureCollection collection){}
			public FeatureType getFeatureType(){return null;}
			public String getID(){return "";}
			public Object[] getAttributes(Object[] attributes){return attrs.entrySet().toArray();}
			//	used
			public Object getAttribute(String xPath){return attrs.get(xPath);}
			public Object getAttribute(int index){return attrs.entrySet().toArray()[index];}
			public void setAttribute(int position, Object val) throws IllegalAttributeException, ArrayIndexOutOfBoundsException{}
			public int getNumberOfAttributes(){return attrs.size();}
			public void setAttributes(Object[] attributes) throws IllegalAttributeException{}
			//	used
			public void setAttribute(String xPath, Object attribute)throws IllegalAttributeException{attrs.put(xPath,attribute);}
			public Geometry getDefaultGeometry(){return null;}
			public void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException{}
			public Envelope getBounds(){return null;}
		}
		Feature f = new testFeature();
		try{f.setAttribute("CityName","Vancouver");}catch(Exception e){}
		GazetteerNameValidation gnv = new GazetteerNameValidation();
		gnv.setAttributeName("CityName");
		try{gnv.setGazetteer(new URL("http://cgdi-dev.geoconnections.org/cgi-bin/prototypes/cgdigaz/cgdigaz.cgi?version=1.0&request=GetPlacenameGeometry&wildcards=false&geomtype=bbox"));}catch(Exception e){}
//		ValidationResults results = new RoadValidationResults();
//		if(!gnv.validate(f,null,results)){
//			fail("Did not validate.");
//		}
		
	}
	
	public void testURLConnection(){
		String place = "Vancouver";
		try{
			URL gazetteerURL = new URL("http://cgdi-dev.geoconnections.org/cgi-bin/prototypes/cgdigaz/cgdigaz.cgi?version=1.0&request=GetPlacenameGeometry&wildcards=false&geomtype=bbox&placename="+place);
			HttpURLConnection gazetteerConnection = (HttpURLConnection)gazetteerURL.openConnection();
			if(!("OK".equals(gazetteerConnection.getResponseMessage())))
				throw new Exception("An error occured creating the connection to the Gazetteer.");
			InputStream gazetteerInputStream = gazetteerConnection.getInputStream();
			InputStreamReader gazetteerInputStreamReader = new InputStreamReader(gazetteerInputStream);
			BufferedReader gazetteerBufferedReader = new BufferedReader(gazetteerInputStreamReader);
			

			InputSource gazetteerInputSource = new InputSource(gazetteerBufferedReader);
			DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
			dfactory.setNamespaceAware(true);

			// TODO turn on validation
			dfactory.setValidating(false);
			dfactory.setIgnoringComments(true);
			dfactory.setCoalescing(true);
			dfactory.setIgnoringElementContentWhitespace(true);

			Document serviceDoc = dfactory.newDocumentBuilder().parse(gazetteerInputSource);
			Element elem = serviceDoc.getDocumentElement();
			
			// elem == featureCollection at this point
			
			elem = getChildElement(elem,"queryInfo");
			if(elem==null)
				throw new NullPointerException("Invalid DOM tree returned by gazetteer.");

			// this number is the number of instances found.
			int number = Integer.parseInt(getChildText(elem,"numberOfResults"));
			
			assertTrue("Error - Vancouver not found.", number>0);
		}catch(Exception e){
			e.printStackTrace();
			fail(e.toString());
		}
	}


	/**
	 * getChildElement purpose.
	 * 
	 * <p>
	 * Used to help with XML manipulations. Returns the first child element of
	 * the specified name.
	 * </p>
	 *
	 * @param root The root element to look for children in.
	 * @param name The name of the child element to look for.
	 *
	 * @return The child element found, null if not found.
	 *
	 * @see getChildElement(Element,String,boolean)
	 */
	public static Element getChildElement(Element root, String name) {
		Node child = root.getFirstChild();

		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equals(child.getNodeName())) {
					return (Element) child;
				}
			}
			child = child.getNextSibling();
		}
		return null;
	}

	/**
	 * getChildText purpose.
	 * 
	 * <p>
	 * Used to help with XML manipulations. Returns the first child text value
	 * of the specified element name.
	 * </p>
	 *
	 * @param root The root element to look for children in.
	 * @param childName The name of the attribute to look for.
	 *
	 * @return The value if the child was found, the null otherwise.
	 */
	public static String getChildText(Element root, String childName) {
		Element elem = getChildElement(root, childName);
		if (elem != null) {
			Node child;
			NodeList childs = elem.getChildNodes();
			int nChilds = childs.getLength();
			for (int i = 0; i < nChilds; i++) {
				child = childs.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) {
					return child.getNodeValue();
				}
			}
		}
		return null;
	}
}
