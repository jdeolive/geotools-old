package org.geotools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDDerivationMethod;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDForm;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.geotools.feature.NameImpl;
import org.geotools.feature.Schema;
import org.geotools.feature.simple.SimpleSchema;
import org.geotools.xml.Configuration;
import org.geotools.xs.XS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * UtilityClass for encoding GML content.
 * <p>
 * This utility class uses a range of GeoTools technologies as required; if you would like finer
 * grain control over the encoding process please review the source code of this class and take your
 * own measures.
 * <p>
 */
public class GMLEncoder {
    private OutputStream out;

    private Charset encoding = Charset.forName("UTF-8");

    private URL baseURL;

    private String prefix;

    private String namespace;

    /** GML Configuration to use */
    private Configuration gmlConfiguration;

    private String gmlNamespace;

    private String gmlLocation;

    /**
     * Schema or profile used to map between Java classes
     * and XML elements.
     */
    private List<Schema> schemaList = new ArrayList<Schema>();

    GMLEncoder(OutputStream out) {
        this.out = out;
    }

    /** Base URL to use when encoding */
    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public void setNameSpace(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public void setGML(String version) {
        if ("gml2".equals(version)) {
            gmlNamespace = org.geotools.gml2.GML.NAMESPACE;
            gmlLocation = "gml/2.1.2/feature.xsd";
            gmlConfiguration = new org.geotools.gml2.GMLConfiguration();
            schemaList.clear();
            // populate with schema / profiles
        }
        if ("gml3".equals(version)) {
            gmlNamespace = org.geotools.gml3.GML.NAMESPACE;
            gmlLocation = "gml/3.1.1/base/gml.xsd";
            gmlConfiguration = new org.geotools.gml3.GMLConfiguration();
            schemaList.clear();
            // populate with schema / profiles
        }
        // if( "3.2".equals( version )){
        // gmlConfiguration = org.geotools.gml3.v3_2.GMLConfiguration;
        // }
    }

    private Name findName(Class binding) {
        // need to figure out schema / profiles here
        return null;
    }
    
    /**
     * Encode the provided SimpleFeatureType into an XSD file.
     * 
     * @param output
     * @param schema
     */
    @SuppressWarnings("unchecked")
    public void encode(SimpleFeatureType schema) throws IOException {
        XSDFactory factory = XSDFactory.eINSTANCE;
        XSDSchema xsd = factory.createXSDSchema();

        xsd.setSchemaForSchemaQNamePrefix("xsd");
        xsd.getQNamePrefixToNamespaceMap().put("xsd", XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
        xsd.setElementFormDefault(XSDForm.get(XSDForm.QUALIFIED));

        if (baseURL == null) {
            throw new IllegalStateException("Please setBaseURL prior to encoding");
        }

        if (prefix != null && namespace != null) {
            xsd.setTargetNamespace(namespace);
            xsd.getQNamePrefixToNamespaceMap().put(prefix, namespace);
        }

        if (schema.getName().getNamespaceURI() != null) {
            String namespace = schema.getName().getNamespaceURI();
            String prefix = (String) schema.getUserData().get("prefix");
            xsd.getQNamePrefixToNamespaceMap().put(prefix, namespace);
        }

        if (schema.getUserData().get("schemaURI") != null) {
            throw new IllegalArgumentException("Unable to support app-schema supplied types");
        }

        // import GML import
        XSDImport gml = factory.createXSDImport();
        gml.setNamespace(gmlNamespace);
        gml.setSchemaLocation(baseURL.toString() + "/" + gmlLocation);
        gml.setResolvedSchema(gmlConfiguration.getXSD().getSchema());
        xsd.getContents().add(gml);

        xsd.getQNamePrefixToNamespaceMap().put("gml", gmlNamespace);
        xsd.getQNamePrefixToNamespaceMap().put("gml", "http://www.opengis.net/gml");

        XSDElementDeclaration element = factory.createXSDElementDeclaration();
        element.setName(schema.getTypeName());

        XSDElementDeclaration _FEATURE = xsd.resolveElementDeclaration(gmlNamespace, "_Feature");
        element.setSubstitutionGroupAffiliation(_FEATURE);

        XSDComplexTypeDefinition featureType = factory.createXSDComplexTypeDefinition();
        featureType.setName(schema.getTypeName());
        featureType.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

        XSDComplexTypeDefinition ABSTRACT_FEATURE_TYPE = xsd.resolveComplexTypeDefinition(
                gmlNamespace, "AbstractFeatureType");
        featureType.setBaseTypeDefinition(ABSTRACT_FEATURE_TYPE);

        // should look at ABSTRACT_FEATURE_TYPE to determine contents to skip
        List<String> skip = Arrays.asList(new String[] { "nounds", "description", "boundedBy" });

        // attributes
        XSDModelGroup attributes = factory.createXSDModelGroup();
        attributes.setCompositor(XSDCompositor.SEQUENCE_LITERAL);

        Name anyName = new NameImpl(XS.NAMESPACE, XS.ANYTYPE.getLocalPart());

        for (PropertyDescriptor descriptor : schema.getDescriptors()) {

            if (descriptor instanceof AttributeDescriptor) {
                AttributeDescriptor attributeDescriptor = (AttributeDescriptor) descriptor;

                if (skip.contains(attributeDescriptor.getLocalName())) {
                    continue;
                }

                XSDElementDeclaration attribute = factory.createXSDElementDeclaration();
                attribute.setName(attributeDescriptor.getLocalName());
                attribute.setNillable(attributeDescriptor.isNillable());

                Name name = attributeDescriptor.getType().getName();

                // return the first match.
                if (!anyName.equals(name)) {
                    AttributeType attributeType = attributeDescriptor.getType();

                    if (attributeType instanceof ComplexType) {
                        // any complex contents must resolve (we cannot encode against
                        // an abstract type for example)
                        if (xsd.resolveTypeDefinition(name.getNamespaceURI(), name.getLocalPart()) == null) {
                            // xsd((ComplexType) attributeType, xsd, factory );
                        }
                    } else {
                        Class binding = attributeType.getBinding();
                        name = findName(binding);
                        if (name == null) {
                            throw new IllegalStateException("No type for " + attribute.getName()
                                    + " (" + binding.getName() + ")");
                        }
                    }
                }

                XSDTypeDefinition type = xsd.resolveTypeDefinition(name.getNamespaceURI(),
                        name.getLocalPart());
                element.setTypeDefinition(type);

                XSDParticle particle = factory.createXSDParticle();
                particle.setMinOccurs(attributeDescriptor.getMinOccurs());
                particle.setMaxOccurs(attributeDescriptor.getMaxOccurs());
                particle.setContent(element);
                attributes.getContents().add(particle);
            }
        }

        // set up fatureType with attributes
        XSDParticle contents = factory.createXSDParticle();
        contents.setContent(attributes);

        featureType.setContent(contents);

        // package up and add to xsd
        element.setTypeDefinition(featureType);
        xsd.getContents().add(element);
        xsd.updateElement();

        XSDResourceImpl.serialize(out, xsd.getElement(), encoding.name());
    }
}
