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
package org.geotools.xml.transform;

import java.io.StringWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;


/**
 * TransformerBase provides support for writing Object->XML encoders. The basic
 * pattern for useage is to extend TransformerBase and implement the
 * createTranslator(ContentHandler) method. This is easiest done by extending
 * the inner class TranslatorSupport. A Translator uses a ContentHandler to
 * issue SAX events to a javax.xml.transform.Transformer. If possible, make
 * the translator public so it can be used by others as well.
 *
 * @author Ian Schneider
 */
public abstract class TransformerBase {
    private int indentation = -1;
    private boolean xmlDecl = false;
    private boolean nsDecl = true;

    /**
     * Create a Translator to issue SAXEvents to a ContentHandler.
     */
    public abstract Translator createTranslator(ContentHandler handler);

    /**
     * Create a Transformer which is initialized with the settings of this
     * TransformerBase.
     */
    public Transformer createTransformer() throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        if (indentation > -1) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                indentation + "");
        } else {
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        }

        if (xmlDecl) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } else {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        }

        return transformer;
    }

    /**
     * Perform the XML encoding on the given object to the given OutputStream.
     * Calls transform(Object,StreamResult);
     */
    public void transform(Object object, java.io.OutputStream out)
        throws TransformerException {
        transform(object, new StreamResult(out));
    }

    /**
     * Perform the XML encoding on the given object to the given Writer. Calls
     * transform(Object,StreamResult);
     */
    public void transform(Object object, java.io.Writer out)
        throws TransformerException {
        transform(object, new StreamResult(out));
    }

    /**
     * Perform the XML encoding on the given object to the given OutputStream.
     * Calls createTransformer(),createXMLReader() and
     * Transformer.transform().
     */
    public void transform(Object object, StreamResult result)
        throws TransformerException {
        Transformer t = createTransformer();
        Source xmlSource = new SAXSource(createXMLReader(object),
                new InputSource());

        try {
            t.transform(xmlSource, result);
        } catch (RuntimeException re) {
            throw new TransformerException("Encoding Error", re);
        }
    }
    
    /**
     * Perform the XML encoding of the given object into an internal buffer and
     * return the resulting String. Calls transform(Object,Writer). <em>It 
     * should be noted the most efficient mechanism of encoding is using the
     * OutputStream or Writer methods</em>
     */
    public String transform(Object object) throws TransformerException {
        StringWriter sw = new StringWriter();
        transform(object,sw);
        return sw.getBuffer().toString();
    }

    /**
     * Create an XMLReader to use in the transformation.
     */
    public XMLReader createXMLReader(Object object) {
        return new XMLReaderSupport(this,object);
    }

    /**
     * Get the number of spaces to indent the output xml.  Defaults to -1.
     *
     * @return The number of spaces to indent, or -1, to disable.
     */
    public int getIndentation() {
        return indentation;
    }

    /**
     * Set the number of spaces to indent the output xml. Default to -1.
     *
     * @param amt The number of spaces to indent if > 0, otherwise disable.
     */
    public void setIndentation(int amt) {
        indentation = amt;
    }

    /**
     * Will this transformation omit the standard XML declaration. <b>Defaults
     * to false</b>
     *
     * @return true if the XML declaration will be omitted, false otherwise.
     */
    public boolean isOmitXMLDeclaration() {
        return xmlDecl;
    }

    /**
     * Set this transformer to omit/include the XML declaration. <b>Defaults to
     * false</b>
     *
     * @param xmlDecl Omit/include the XML declaration.
     */
    public void setOmitXMLDeclaration(boolean xmlDecl) {
        this.xmlDecl = xmlDecl;
    }

    /**
     * Should this transformer declare namespace prefixes in the first element
     * it outputs? Defaults to true.
     *
     * @return true if namespaces will be declared, false otherwise
     */
    public boolean isNamespaceDeclartionEnabled() {
        return nsDecl;
    }

    /**
     * Enable declaration of namespace prefixes in the first element. Defaults
     * to true;
     *
     * @param enabled Enable namespace declaration.
     */
    public void setNamespaceDeclarationEnabled(boolean enabled) {
        nsDecl = enabled;
    }
    

    /**
     * Filter output from a ContentHandler and insert Namespace declarations in
     * the first element.
     */
    private static class ContentHandlerFilter implements ContentHandler {
        private final ContentHandler original;
        private AttributesImpl namespaceDecls;

        public ContentHandlerFilter(ContentHandler original,
            AttributesImpl nsDecls) {
            this.original = original;
            this.namespaceDecls = nsDecls;
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            original.characters(ch, start, length);
        }

        public void endDocument() throws SAXException {
            original.endDocument();
        }

        public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
            original.endElement(namespaceURI, localName, qName);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            original.endPrefixMapping(prefix);
        }

        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
            original.ignorableWhitespace(ch, start, length);
        }

        public void processingInstruction(String target, String data)
            throws SAXException {
            original.processingInstruction(target, data);
        }

        public void setDocumentLocator(org.xml.sax.Locator locator) {
            original.setDocumentLocator(locator);
        }

        public void skippedEntity(String name) throws SAXException {
            original.skippedEntity(name);
        }

        public void startDocument() throws SAXException {
            original.startDocument();
        }

        public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
            if (namespaceDecls != null) {
                for (int i = 0, ii = atts.getLength(); i < ii; i++) {
                    namespaceDecls.addAttribute(null, null, atts.getQName(i),
                        atts.getType(i), atts.getValue(i));
                }
                
                    
                atts = namespaceDecls;
                namespaceDecls = null;
            }
            if (namespaceURI == null)
                namespaceURI = "";
            if (localName == null)
                localName = "";
            original.startElement(namespaceURI, localName, qName, atts);
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
            original.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * Support for writing Translators.
     */
    protected abstract static class TranslatorSupport implements Translator {
        protected final ContentHandler contentHandler;
        private String prefix;
        private String namespace;
        protected final Attributes NULL_ATTS = new AttributesImpl();
        protected NamespaceSupport nsSupport = new NamespaceSupport();
        protected SchemaLocationSupport schemaLocation;

        public TranslatorSupport(ContentHandler contentHandler, String prefix,
            String nsURI) {
            this.contentHandler = contentHandler;
            this.prefix = prefix;
            this.namespace = nsURI;
            if (prefix != null && nsURI != null)
                nsSupport.declarePrefix(prefix, nsURI);
        }

        public TranslatorSupport(ContentHandler contentHandler, String prefix,
            String nsURI, SchemaLocationSupport schemaLocation) {
            this(contentHandler, prefix, nsURI);
            this.schemaLocation = schemaLocation;
        }
        
        /**
         * Utility method to copy namespace declarations from "sub" translators
         * into this ns support...
         */
        protected void addNamespaceDeclarations(TranslatorSupport trans) {
            NamespaceSupport additional = trans.getNamespaceSupport();
            java.util.Enumeration declared = additional.getDeclaredPrefixes();
            while (declared.hasMoreElements()) {
                String prefix = declared.nextElement().toString();
                nsSupport.declarePrefix(prefix, additional.getURI(prefix));
            }
        }

        protected void element(String element, String content) {
            element(element, content, NULL_ATTS);
        }

        protected void element(String element, String content, Attributes atts) {
            start(element, atts);

            if (content != null) {
                chars(content);
            }

            end(element);
        }

        protected void start(String element) {
            start(element, NULL_ATTS);
        }

        protected void start(String element, Attributes atts) {
            try {
                String el = (prefix == null) ? element : (prefix + ":"
                    + element);
                contentHandler.startElement("", "", el, atts);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        protected void chars(String text) {
            try {
                char[] ch = text.toCharArray();
                contentHandler.characters(ch, 0, ch.length);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        protected void end(String element) {
            try {
                String el = (prefix == null) ? element : (prefix + ":"
                    + element);
                contentHandler.endElement("", "", el);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }

        public String getDefaultNamespace() {
            return namespace;
        }

        public String getDefaultPrefix() {
            return prefix;
        }

        public NamespaceSupport getNamespaceSupport() {
            return nsSupport;
        }

        public SchemaLocationSupport getSchemaLocationSupport() {
            return schemaLocation;
        }
    }

    /**
     * Adds support for schemaLocations.
     *
     * @task REVISIT: consider combining this with NamespaceSupport,  as it
     *       would make sense to just add a location to your nsURI.  Or at
     *       least tie them more closely, so that you can only add a
     *       SchemaLocation if the namespace actually exists.
     */
    public static class SchemaLocationSupport {
        private Map locations = new HashMap();

        public void setLocation(String nsURI, String uri) {
            locations.put(nsURI, uri);
        }

        public String getSchemaLocation() {
            return getSchemaLocation(locations.keySet());
        }

        public String getSchemaLocation(String nsURI) {
            String uri = (String) locations.get(nsURI);

            if (uri == null) {
                return "";
            } else {
                return nsURI + " " + uri;
            }
        }

        public String getSchemaLocation(Set namespaces) {
            StringBuffer location = new StringBuffer();

            for (Iterator it = namespaces.iterator(); it.hasNext();) {
                location.append(getSchemaLocation((String) it.next()));

                if (it.hasNext()) {
                    location.append(" "); //\n?  Pretty printing?
                }
            }

            return location.toString();
        }
    }

    /**
     * Support for the setup of an XMLReader for use in a transformation.
     */
    protected static class XMLReaderSupport extends XMLFilterImpl {
        TransformerBase base;
        Object object;

        public XMLReaderSupport(TransformerBase transfomerBase, Object object) {
            this.base = transfomerBase;
            this.object = object;
        }

        public void parse(InputSource in) throws SAXException {
            ContentHandler handler = getContentHandler();

            Translator translator;

            if (base.isNamespaceDeclartionEnabled()) {
                AttributesImpl atts = new AttributesImpl();
                ContentHandlerFilter filter = new ContentHandlerFilter(handler,
                        atts);
                translator = base.createTranslator(filter);
                atts.addAttribute(null, null,
                    "xmlns:" + translator.getDefaultPrefix(), null,
                    translator.getDefaultNamespace());

                NamespaceSupport ns = translator.getNamespaceSupport();
                java.util.Enumeration e = ns.getPrefixes();

                //TODO: only add schema locations for namespaces that are
                //actually here, or some sort of better checking.  
                //Set namespaces = new HashSet();
                while (e.hasMoreElements()) {
                    String prefix = e.nextElement().toString();

                    if (prefix.equals("xml")) {
                        continue;
                    }

                    String xmlns = "xmlns:" + prefix;

                    if (atts.getValue(xmlns) == null) {
                        atts.addAttribute(null, null, xmlns, null,
                            ns.getURI(prefix));

                        //namespaces.add(ns.getURI(prefix));
                    }
                }

                String defaultNS = ns.getURI("");

                if ((defaultNS != null) && (atts.getValue("xmlns:") == null)) {
                    atts.addAttribute(null, null, "xmlns:", null, defaultNS);

                    //namespaces.add(defaultNS);
                }

                SchemaLocationSupport schemaLocSup = translator
                    .getSchemaLocationSupport();

                if ((schemaLocSup != null)
                        && !schemaLocSup.getSchemaLocation().equals("")) {
                    atts.addAttribute(null, null, "xmlns:xsi", null,
                        "http://www.w3.org/2001/XMLSchema-instance");
                    atts.addAttribute(null, null, "xsi:schemaLocation", null,
                        schemaLocSup.getSchemaLocation());
                }
            } else {
                translator = base.createTranslator(handler);
            }

            handler.startDocument();
            translator.encode(object);
            handler.endDocument();
        }
    }
}
