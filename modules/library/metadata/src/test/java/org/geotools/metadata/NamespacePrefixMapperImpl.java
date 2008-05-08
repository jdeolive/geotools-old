/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.metadata;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;


/**
 * A mapper between namespace prefixes and url they represent.
 * It is possible to specify a root namespace, which will be used if no namespace
 * is specified.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
    /**
     * If set, this namespace will be the root of the document with no prefix.
     */
    private String rootNamespace;

    /**
     * Builds a mapper of prefixes.
     *
     * @param rootNamespace The root namespace.
     */
    public NamespacePrefixMapperImpl(String rootNamespace) {
        super();
        this.rootNamespace = rootNamespace;
    }

    /**
     * Returns a preferred prefix for the given namespace URI.
     *
     * @param namespaceUri
     *      The namespace URI for which the prefix needs to be found.
     * @param suggestion
     *      The suggested prefix, returned if the given namespace is not recoginzed.
     * @param requirePrefix
     *      Ignored in this implementation.
     *
     * @return
     *      The prefix inferred from the namespace URI.
     */
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if (namespaceUri == null || namespaceUri.equals("")) {
            return "gmd";
        }
        if (namespaceUri.equalsIgnoreCase("http://www.isotc211.org/2005/gmd")){
            return "gmd";
        }
        if (namespaceUri.equalsIgnoreCase("http://www.isotc211.org/2005/gco")){
            return "gco";
        }
        if (namespaceUri.equalsIgnoreCase("http://www.w3.org/2001/XMLSchema-instance")) {
            return "xsi";
        }
        if (namespaceUri.equalsIgnoreCase("http://www.opengis.net/gml")) {
            return "gml";
        }
        return suggestion;
    }

    /**
     * Returns a list of namespace URIs that should be declared at the root element.
     * This implementation returns an empty list.
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] {};
    }
}
