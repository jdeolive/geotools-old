package org.geotools.wfs.v2_0.bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import net.opengis.wfs20.QueryType;
import net.opengis.wfs20.Wfs20Factory;

import org.eclipse.emf.ecore.EObject;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.xml.ComplexEMFBinding;
import org.geotools.xs.bindings.XSQNameBinding;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

public class QueryTypeBinding extends ComplexEMFBinding {

    NamespaceContext namespaceContext;

    public QueryTypeBinding(NamespaceContext namespaceContext) {
        super(Wfs20Factory.eINSTANCE, WFS.QueryType);
        this.namespaceContext = namespaceContext;
    }
    
    @Override
    protected void setProperty(EObject eObject, String property, Object value, boolean lax) {
        super.setProperty(eObject, property, value, lax);
        if (!lax) {
            if ("typeNames".equalsIgnoreCase(property)) {
                QueryType q = (QueryType)eObject;
                
                //turn into list of qname
                List qNames = new ArrayList();
                for (Object s : q.getTypeNames()) {
                    try {
                        qNames.add(new XSQNameBinding(namespaceContext).parse(null, s));
                    } 
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                q.setTypeNames(qNames);
            }
            else if ("filter".equalsIgnoreCase(property)) {
                ((QueryType)eObject).setFilter((Filter)value);
            }
            else if ("propertyName".equalsIgnoreCase(property)) {
                super.setProperty(eObject, "abstractProjectionClause", value, lax);
            }
            else if ("sortBy".equalsIgnoreCase(property)) {
                SortBy[] sort = (SortBy[]) value;
                super.setProperty(eObject, "abstractSortingClause", 
                    new ArrayList(Arrays.asList(sort)), lax);
            }
        }
    }

}
