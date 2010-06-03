package org.geotools;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Map.Entry;

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
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleSchema;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.gml2.GMLSchema;
import org.geotools.xml.Configuration;
import org.geotools.xs.XS;
import org.geotools.xs.XSSchema;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.Schema;

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
    
    /**
     * Set up out of the box configuration for GML encoding.
     * <ul>
     * <li>gml2</li>
     * <li>gml3</li>
     * <li>gml3.2 - not yet available</li>
     * <li>wfs1.0 - not yet available</li>
     * <li>wfs1.1 - not yet available</li>
     * </ul>
     * @param version
     */
    public void setGML(String version) {
        List<Schema> schemas = new ArrayList<Schema>();
        schemas.add( new XSSchema().profile() ); // encoding of common java types
        Schema hack = new SchemaImpl(XS.NAMESPACE);
        
        AttributeTypeBuilder builder = new AttributeTypeBuilder();
        builder.setName("date");
        builder.setBinding( Date.class );
        hack.put( new NameImpl( XS.DATETIME), builder.buildType() );
        
        schemas.add( hack );

        if ("gml2".equals(version)) {
            gmlNamespace = org.geotools.gml2.GML.NAMESPACE;
            gmlLocation = "gml/2.1.2/feature.xsd";
            gmlConfiguration = new org.geotools.gml2.GMLConfiguration();
            schemas.add( new org.geotools.gml2.GMLSchema().profile() );
        }
        if ("gml3".equals(version)) {
            gmlNamespace = org.geotools.gml3.GML.NAMESPACE;
            gmlLocation = "gml/3.1.1/base/gml.xsd";
            gmlConfiguration = new org.geotools.gml3.GMLConfiguration();
            schemas.add( new org.geotools.gml3.GMLSchema().profile() );
        }
        if("wfs1.0".equals(version)){
            throw new UnsupportedOperationException("wfs1.0 bindings not yet sorted out");
        }
        if("wfs1.1".equals(version)){
            throw new UnsupportedOperationException("wfs1.0 bindings not yet sorted out");
        }
        schemaList = schemas;
    }

    private Entry<Name,AttributeType> searchSchemas(Class<?> binding) {
        // sort by isAssignable so we get the most specific match possible
        //
        Comparator<Entry<Name, AttributeType>> sort = new Comparator<Entry<Name, AttributeType>>() {
            public int compare(Entry<Name, AttributeType> o1, Entry<Name, AttributeType> o2) {
                Class<?> binding1 = o1.getValue().getBinding();
                Class<?> binding2 = o2.getValue().getBinding();
                if( binding1.equals(binding2)){
                    return 0;
                }
                if( binding1.isAssignableFrom(binding2)){
                    return 1;
                }
                else {
                    return 0;
                }
            }
        };
        List<Entry<Name, AttributeType>> match = new ArrayList<Entry<Name, AttributeType>>();
        
        // process the listed profiles recording all available matches
        for( Schema profile : schemaList ){
            for( Entry<Name, AttributeType> entry : profile.entrySet() ){
                AttributeType type = entry.getValue();
                if( type.getBinding().isAssignableFrom( binding )){
                    match.add( entry );
                }
            }
        }
        Collections.sort(match, sort );
        
        Iterator<Entry<Name, AttributeType>> iter = match.iterator();
        if( iter.hasNext() ){
            Entry<Name, AttributeType> entry = iter.next();
            return entry;
        }
        else {
            return null; // no binding found that matches
        }
    }
    
    /**
     * Encode the provided SimpleFeatureType into an XSD file.
     * 
     * @param output
     * @param simpleFeatureType
     */
    @SuppressWarnings("unchecked")
    public void encode(SimpleFeatureType simpleFeatureType) throws IOException {
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

        if (simpleFeatureType.getName().getNamespaceURI() != null) {
            String namespace = simpleFeatureType.getName().getNamespaceURI();
            String prefix = (String) simpleFeatureType.getUserData().get("prefix");
            xsd.getQNamePrefixToNamespaceMap().put(prefix, namespace);
        }

        if (simpleFeatureType.getUserData().get("schemaURI") != null) {
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
        element.setName(simpleFeatureType.getTypeName());

        XSDElementDeclaration _FEATURE = xsd.resolveElementDeclaration(gmlNamespace, "_Feature");
        element.setSubstitutionGroupAffiliation(_FEATURE);

        XSDComplexTypeDefinition ABSTRACT_FEATURE_TYPE = xsd.resolveComplexTypeDefinition(
                gmlNamespace, "AbstractFeatureType");
        
        XSDComplexTypeDefinition featureType = xsd( xsd, simpleFeatureType, ABSTRACT_FEATURE_TYPE );
        
        // package up and add to xsd
        element.setTypeDefinition(featureType);
        xsd.getContents().add(element);
        xsd.updateElement();

        XSDResourceImpl.serialize(out, xsd.getElement(), encoding.name());
    }
    
    /**
     * Build the XSD definition for the provided type.
     * <p>
     * The generated definition is recorded in the XSDSchema prior to being returned.
     * 
     * @param xsd The XSDSchema being worked on
     * @param type ComplexType to capture as an encoding, usually a SimpleFeatureType
     * @param BASE_TYPE definition to use as the base type, or null
     * @return XSDComplexTypeDefinition generated for the provided type
     */
    @SuppressWarnings("unchecked")
    protected XSDComplexTypeDefinition xsd( XSDSchema xsd, ComplexType type, final XSDComplexTypeDefinition BASE_TYPE ){
        XSDFactory factory = XSDFactory.eINSTANCE;
        
        XSDComplexTypeDefinition definition = factory.createXSDComplexTypeDefinition();
        definition.setName( type.getName().getLocalPart() );
        definition.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

        if( BASE_TYPE != null ){
            definition.setBaseTypeDefinition(BASE_TYPE);
        }
        List<String> skip = Collections.emptyList();
        if( "AbstractFeatureType".equals( BASE_TYPE.getName() ) ){
            // should look at ABSTRACT_FEATURE_TYPE to determine contents to skip
            skip = Arrays.asList(new String[] { "nounds", "description", "boundedBy" });
        }
        
        // attributes
        XSDModelGroup attributes = factory.createXSDModelGroup();
        attributes.setCompositor(XSDCompositor.SEQUENCE_LITERAL);

        Name anyName = new NameImpl(XS.NAMESPACE, XS.ANYTYPE.getLocalPart());

        for (PropertyDescriptor descriptor : type.getDescriptors()) {

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
                        ComplexType complexType = (ComplexType) attributeType;
                        // any complex contents must resolve (we cannot encode against
                        // an abstract type for example)
                        if (xsd.resolveTypeDefinition(name.getNamespaceURI(), name.getLocalPart()) == null) {
                            // not yet added; better add it into the mix
                            xsd( xsd, complexType, null );
                        }
                    } else {
                        Class<?> binding = attributeType.getBinding();
                        Entry<Name, AttributeType> entry = searchSchemas( binding );
                        if( entry == null ){
                            throw new IllegalStateException("No type for " + attribute.getName()
                                    + " (" + binding.getName() + ")");
                        }
                        name = entry.getKey();
                    }
                }

                XSDTypeDefinition attributeDefinition = xsd.resolveTypeDefinition(name.getNamespaceURI(), name.getLocalPart());
                attribute.setTypeDefinition(attributeDefinition);

                XSDParticle particle = factory.createXSDParticle();
                particle.setMinOccurs(attributeDescriptor.getMinOccurs());
                particle.setMaxOccurs(attributeDescriptor.getMaxOccurs());
                particle.setContent(attribute);
                attributes.getContents().add(particle);
            }
        }

        // set up fatureType with attributes
        XSDParticle contents = factory.createXSDParticle();
        contents.setContent(attributes);
        
        definition.setContent(contents);
        xsd.getContents().add( definition );
        
        return definition;
    }
}
