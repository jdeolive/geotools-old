package org.geotools.se.v1_1.bindings;

import javax.xml.namespace.QName;

import org.geotools.se.v1_1.SE;
import org.geotools.xml.ParserDelegate;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class InlineContentParserDelegate extends DefaultHandler implements ParserDelegate {

    String encoding;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
    }
    
    public boolean canHandle(QName elementName) {
        return SE.InlineContent.equals(elementName);
    }

    public Object getParsedObject() {
        // TODO Auto-generated method stub
        return null;
    }

}
