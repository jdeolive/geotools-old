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
package org.geotools.data.arcsde;

import java.util.logging.Logger;

import org.geotools.filter.Filter;
import org.geotools.filter.FilterHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * stolen from filter module tests. Uses SAX to extact a GetFeature query from
 * and incoming GetFeature request XML stream.
 *
 * <p>
 * Note that this Handler extension ignores Filters completely and must be
 * chained as a parent to the PredicateFilter method in order to recognize
 * them. If it is not chained, it will still generate valid queries, but with
 * no filtering whatsoever.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 * @version 0.9 beta, 11/01/01
 */
public class TestFilterHandler implements ContentHandler, FilterHandler
{
    /** Standard logging class */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    /** Local tracking methods to deal with incoming XML stream */
    /** Tracks tag we are currently inside */
    private Filter filter = null;

    /**
     * Creates a new TestFilterHandler object.
     */
    public TestFilterHandler()
    {
    }

    /**
     * Start of SAX Content Handler Methods most of these are unused at the
     * moment no namespace awareness, yet
     *
     * @param locator DOCUMENT ME!
     */
    /**
     * Notes the document locator.
     *
     * @param locator DOCUMENT ME!
     */
    public void setDocumentLocator(Locator locator)
    {
    }

    /**
     * Notes the start of the document.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startDocument() throws SAXException
    {
        //_log.info("start of document");
    }

    /**
     * Notes the start of the document.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endDocument() throws SAXException
    {
        //_log.info( "at end of document");
    }

    /**
     * Notes processing instructions.
     *
     * @param target DOCUMENT ME!
     * @param data DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void processingInstruction(String target, String data)
        throws SAXException
    {
    }

    /**
     * Notes start of prefix mappings.
     *
     * @param prefix DOCUMENT ME!
     * @param uri DOCUMENT ME!
     */
    public void startPrefixMapping(String prefix, String uri)
    {
    }

    /**
     * Notes end of prefix mappings.
     *
     * @param prefix DOCUMENT ME!
     */
    public void endPrefixMapping(String prefix)
    {
    }

    /**
     * Notes the start of the element and sets type names and query attributes.
     *
     * @param namespaceURI URI for namespace appended to element.
     * @param localName Local name of element.
     * @param rawName Raw name of element.
     * @param atts Element attributes.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startElement(String namespaceURI, String localName,
        String rawName, Attributes atts) throws SAXException
    {
    }

    /**
     * Notes the end of the element exists query or bounding box.
     *
     * @param namespaceURI URI for namespace appended to element.
     * @param localName Local name of element.
     * @param rawName Raw name of element.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endElement(String namespaceURI, String localName, String rawName)
        throws SAXException
    {
    }

    /**
     * Checks if inside parsed element and adds its contents to the appropriate
     * variable.
     *
     * @param ch URI for namespace appended to element.
     * @param start Local name of element.
     * @param length Raw name of element.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException
    {
    }

    /**
     * Notes ignorable whitespace.
     *
     * @param ch URI for namespace appended to element.
     * @param start Local name of element.
     * @param length Raw name of element.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException
    {
    }

    /**
     * Notes skipped entity.
     *
     * @param name Name of skipped entity.
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void skippedEntity(String name) throws SAXException
    {
    }

    /**
     * Gets filter.
     *
     * @param filter (OGC WFS) Filter from (SAX) filter..
     */
    public void filter(Filter filter)
    {
        LOGGER.finer("found filter: " + filter.toString());

        this.filter = filter;
    }

    /**
     * Gets filter.
     *
     * @return (OGC WFS) Filter from (SAX) filter..
     */
    public Filter getFilter()
    {
        return this.filter;
    }
}
