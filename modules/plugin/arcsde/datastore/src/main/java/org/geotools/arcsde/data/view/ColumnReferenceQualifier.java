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
package org.geotools.arcsde.data.view;

import java.util.Map;

import org.geotools.arcsde.pool.Session;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.ColumnIndex;
import net.sf.jsqlparser.statement.select.ColumnReference;
import net.sf.jsqlparser.statement.select.ColumnReferenceVisitor;

import com.esri.sde.sdk.client.SeConnection;

/**
 * Qualifies a column reference (aliased) the ArcSDE "table.user." prefix as
 * required by the ArcSDE java api to not get confused when using joined tables.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ColumnReferenceQualifier.java 18656 2006-03-14 18:05:11Z
 *          groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/view/ColumnReferenceQualifier.java $
 * @since 2.3.x
 */
public class ColumnReferenceQualifier implements ColumnReferenceVisitor {
    /** DOCUMENT ME! */
    private ColumnReference qualifiedReference;

    /** DOCUMENT ME! */
    private Session session;

    private Map tableAliases;

    /**
     * Creates a new ColumnReferenceQualifier object.
     * 
     * @param session
     *            DOCUMENT ME!
     */
    private ColumnReferenceQualifier(Session session, Map tableAliases) {
        this.session = session;
        this.tableAliases = tableAliases;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param session
     *            DOCUMENT ME!
     * @param colRef
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static ColumnReference qualify(Session session, Map tableAliases,
            ColumnReference colRef) {
        if (colRef == null) {
            return null;
        }

        ColumnReferenceQualifier qualifier = new ColumnReferenceQualifier(session, tableAliases);
        colRef.accept(qualifier);

        return qualifier.qualifiedReference;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param columnIndex
     *            DOCUMENT ME!
     */
    public void visit(ColumnIndex columnIndex) {
        qualifiedReference = columnIndex;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param column
     *            DOCUMENT ME!
     */
    public void visit(Column column) {
        this.qualifiedReference = ColumnQualifier.qualify(session, tableAliases, column);
    }
}
