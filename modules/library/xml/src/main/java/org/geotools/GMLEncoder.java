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
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.WfsFactory;

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
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
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
    /** Version of encoder to use */
    public static enum Version {
        GML2, GML3, WFS1_0, WFS1_1
    }

    private Charset encoding = Charset.forName("UTF-8");

    private URL baseURL;

    /** GML Configuration to use */
    private Configuration gmlConfiguration;

    private String gmlNamespace;

    private String gmlLocation;

    /**
     * Schema or profile used to map between Java classes and XML elements.
     */
    private List<Schema> schemaList = new ArrayList<Schema>();

    String prefix = null;

    String namespace = null;

    private final Version version;

    private boolean legacy;

    GMLEncoder(Version version) {
        this.version = version;
        init();
    }

    /**
     * Engage legacy support for GML2.
     * <p>
     * The GML2 support for FeatureTransformer is much faster then that provided by the
     * GTXML parser/encoder. This speed is at the expense of getting the up front configuration
     * exactly correct (something you can only tell when parsing the produced result!). Setting
     * this value to false will use the same GMLConfiguration employed when parsing and has less
     * risk of producing invalid content.
     * @param legacy
     */
    public void setLegacy( boolean legacy ){
        this.legacy = legacy;
    }
    /**
     * Set the target namespace for the encoding.
     * 
     * @param prefix
     * @param namespace
     */
    public void setNamespace(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    /**
     * Set the encoding to use.
     * 
     * @param encoding
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     * Base URL to use when encoding
     */
    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Set up out of the box configuration for GML encoding.
     * <ul>
     * <li>GML2</li>
     * <li>GML3</li>
     * </ul>
     * The following are not avialable yet:
     * <ul>
     * <li>gml3.2 - not yet available</li>
     * <li>wfs1.0 - not yet available</li>
     * <li>wfs1.1 - not yet available</li>
     * </ul>
     * 
     * @param version
     */
    protected void init() {
        List<Schema> schemas = new ArrayList<Schema>();
        schemas.add(new XSSchema().profile()); // encoding of common java types
        Schema hack = new SchemaImpl(XS.NAMESPACE);

        AttributeTypeBuilder builder = new AttributeTypeBuilder();
        builder.setName("date");
        builder.setBinding(Date.class);
        hack.put(new NameImpl(XS.DATETIME), builder.buildType());

        schemas.add(hack);
        
        // GML 2
        //
        if (Version.GML2 == version) {
            gmlNamespace = org.geotools.gml2.GML.NAMESPACE;
            gmlLocation = "gml/2.1.2/feature.xsd";
            gmlConfiguration = new org.geotools.gml2.GMLConfiguration();
            schemas.add(new org.geotools.gml2.GMLSchema().profile());
        }
        if (Version.WFS1_0 == version) {
            gmlNamespace = org.geotools.gml2.GML.NAMESPACE;
            gmlLocation = "gml/2.1.2/feature.xsd";
            gmlConfiguration = new org.geotools.wfs.v1_0.WFSConfiguration();
            
            schemas.add(new org.geotools.gml2.GMLSchema().profile());
            
        }
        // GML 3
        //
        if (Version.GML3 == version) {
            gmlNamespace = org.geotools.gml3.GML.NAMESPACE;
            gmlLocation = "gml/3.1.1/base/gml.xsd";
            gmlConfiguration = new org.geotools.gml3.GMLConfiguration();
            schemas.add(new org.geotools.gml3.GMLSchema().profile());
        }
        if (Version.WFS1_1 == version) {
            gmlNamespace = org.geotools.gml3.GML.NAMESPACE;
            gmlLocation = "gml/3.1.1/base/gml.xsd";
            gmlConfiguration = new org.geotools.wfs.v1_1.WFSConfiguration();
            
            schemas.add(new org.geotools.gml3.GMLSchema().profile());
        }
        schemaList = schemas;
    }

    private Entry<Name, AttributeType> searchSchemas(Class<?> binding) {
        // sort by isAssignable so we get the most specific match possible
        //
        Comparator<Entry<Name, AttributeType>> sort = new Comparator<Entry<Name, AttributeType>>() {
            public int compare(Entry<Name, AttributeType> o1, Entry<Name, AttributeType> o2) {
                Class<?> binding1 = o1.getValue().getBinding();
                Class<?> binding2 = o2.getValue().getBinding();
                if (binding1.equals(binding2)) {
                    return 0;
                }
                if (binding1.isAssignableFrom(binding2)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        List<Entry<Name, AttributeType>> match = new ArrayList<Entry<Name, AttributeType>>();

        // process the listed profiles recording all available matches
        for (Schema profile : schemaList) {
            for (Entry<Name, AttributeType> entry : profile.entrySet()) {
                AttributeType type = entry.getValue();
                if (type.getBinding().isAssignableFrom(binding)) {
                    match.add(entry);
                }
            }
        }
        Collections.sort(match, sort);

        Iterator<Entry<Name, AttributeType>> iter = match.iterator();
        if (iter.hasNext()) {
            Entry<Name, AttributeType> entry = iter.next();
            return entry;
        } else {
            return null; // no binding found that matches
        }
    }

    @SuppressWarnings("unchecked")
    public void encode( OutputStream out, SimpleFeatureCollection collection ) throws IOException {
        
        if( version == Version.GML2){
            if( legacy ){
                encodeLegacyGML2(out,collection);
            }
            else {
                throw new IllegalStateException("Cannot encode a feature collection using GML2 (only WFS)");
            }
        }
        if( version == Version.WFS1_0 ){
            Encoder e = new Encoder( new org.geotools.wfs.v1_0.WFSConfiguration() );
            e.getNamespaces().declarePrefix( prefix, namespace );
            e.setIndenting(true);
            
            FeatureCollectionType featureCollectionType = WfsFactory.eINSTANCE.createFeatureCollectionType();
            featureCollectionType.getFeature().add(collection);
            
            e.encode( featureCollectionType, org.geotools.wfs.WFS.FeatureCollection, out );
        }
        if( version == Version.WFS1_1 ){
            Encoder e = new Encoder( new org.geotools.wfs.v1_1.WFSConfiguration() );
            e.getNamespaces().declarePrefix( prefix, namespace );
            e.setIndenting(true);
            
            FeatureCollectionType featureCollectionType = WfsFactory.eINSTANCE.createFeatureCollectionType();
            featureCollectionType.getFeature().add(collection);
            
            e.encode( featureCollectionType, org.geotools.wfs.WFS.FeatureCollection, out );
        }
    }

    private void encodeLegacyGML2(OutputStream out, SimpleFeatureCollection collection) throws IOException {
        final SimpleFeatureType TYPE = collection.getSchema();
        
        FeatureTransformer transform = new FeatureTransformer();
        transform.setIndentation(4);
        transform.setGmlPrefixing(true);
        
        if( prefix != null && namespace != null ){
            transform.getFeatureTypeNamespaces().declareDefaultNamespace(prefix, namespace );
            transform.addSchemaLocation(prefix,namespace);
            //transform.getFeatureTypeNamespaces().declareDefaultNamespace("", namespace );
        }
        
        if( TYPE.getName().getNamespaceURI() != null && TYPE.getUserData().get("prefix") != null){
            String typeNamespace = TYPE.getName().getNamespaceURI();
            String typePrefix = (String) TYPE.getUserData().get("prefix");
            
            transform.getFeatureTypeNamespaces().declareNamespace(TYPE, typePrefix, typeNamespace );
        }
        else if ( prefix != null && namespace != null ){
            // ignore namespace URI in feature type
            transform.getFeatureTypeNamespaces().declareNamespace(TYPE, prefix, namespace);
        }
        else {
            // hopefully that works out for you then
        }
        
        // we probably need to do a wfs feaure collection here?
        transform.setCollectionPrefix(null);
        transform.setCollectionNamespace(null);
        
        // other configuration
        transform.setCollectionBounding(true);
        transform.setEncoding(encoding);
        
        // configure additional feature namespace lookup
        transform.getFeatureNamespaces(); 
        
        String srsName = CRS.toSRS( TYPE.getCoordinateReferenceSystem() );
        if( srsName != null ){
            transform.setSrsName(srsName);
        }
        
        try {
            transform.transform( collection, out );
        } catch (TransformerException e) {
            throw (IOException) new IOException("Failed to encode feature collection:"+e).initCause(e);
        }
    }

    /**
     * Encode the provided SimpleFeatureType into an XSD file, using a target namespace
     * <p>
     * When encoding the simpleFeatureType:
     * <ul>
     * <li>target prefix/namespace can be provided by prefix and namespace parameters. This is the
     * default for the entire XSD document.</li>
     * <li>simpleFeatureType.geName().getNamespaceURI() is used for the simpleFeatureType itself,
     * providing simpleFeatureType.getUserData().get("prefix") is defined</li>
     * </ul>
     * 
     * @param simpleFeatureType
     *            To be encoded as an XSD document
     * @param prefix
     *            Prefix to use for for target namespace
     * @param namespace
     *            Target namespace
     */
    public void encode(OutputStream out, SimpleFeatureType simpleFeatureType) throws IOException {
        XSDSchema xsd = xsd(simpleFeatureType);
        
        XSDResourceImpl.serialize(out, xsd.getElement(), encoding.name());
    }

    @SuppressWarnings("unchecked")
    protected XSDSchema xsd(SimpleFeatureType simpleFeatureType) throws IOException {
        XSDFactory factory = XSDFactory.eINSTANCE;
        XSDSchema xsd = factory.createXSDSchema();

        xsd.setSchemaForSchemaQNamePrefix("xsd");
        xsd.getQNamePrefixToNamespaceMap().put("xsd", XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
        xsd.setElementFormDefault(XSDForm.get(XSDForm.QUALIFIED));

        if (baseURL == null) {
            throw new IllegalStateException("Please setBaseURL prior to encoding");
        }

        if (prefix != null || namespace != null) {
            xsd.setTargetNamespace(namespace);
            xsd.getQNamePrefixToNamespaceMap().put(prefix, namespace);
        }

        if (simpleFeatureType.getName().getNamespaceURI() != null
                && simpleFeatureType.getUserData().get("prefix") != null) {
            String providedNamespace = simpleFeatureType.getName().getNamespaceURI();
            String providedPrefix = (String) simpleFeatureType.getUserData().get("prefix");
            xsd.getQNamePrefixToNamespaceMap().put(providedPrefix, providedNamespace);
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

        XSDComplexTypeDefinition featureType = xsd(xsd, simpleFeatureType, ABSTRACT_FEATURE_TYPE);

        // package up and add to xsd
        element.setTypeDefinition(featureType);
        xsd.getContents().add(element);
        xsd.updateElement();
        return xsd;
    }

    
    /**
     * Build the XSD definition for the provided type.
     * <p>
     * The generated definition is recorded in the XSDSchema prior to being returned.
     * 
     * @param xsd
     *            The XSDSchema being worked on
     * @param type
     *            ComplexType to capture as an encoding, usually a SimpleFeatureType
     * @param L_TYPE
     *            definition to use as the base type, or null
     * @return XSDComplexTypeDefinition generated for the provided type
     */
    @SuppressWarnings("unchecked")
    protected XSDComplexTypeDefinition xsd(XSDSchema xsd, ComplexType type,
            final XSDComplexTypeDefinition BASE_TYPE) {
        XSDFactory factory = XSDFactory.eINSTANCE;

        XSDComplexTypeDefinition definition = factory.createXSDComplexTypeDefinition();
        definition.setName(type.getName().getLocalPart());
        definition.setDerivationMethod(XSDDerivationMethod.EXTENSION_LITERAL);

        if (BASE_TYPE != null) {
            definition.setBaseTypeDefinition(BASE_TYPE);
        }
        List<String> skip = Collections.emptyList();
        if ("AbstractFeatureType".equals(BASE_TYPE.getName())) {
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
                            xsd(xsd, complexType, null);
                        }
                    } else {
                        Class<?> binding = attributeType.getBinding();
                        Entry<Name, AttributeType> entry = searchSchemas(binding);
                        if (entry == null) {
                            throw new IllegalStateException("No type for " + attribute.getName()
                                    + " (" + binding.getName() + ")");
                        }
                        name = entry.getKey();
                    }
                }

                XSDTypeDefinition attributeDefinition = xsd.resolveTypeDefinition(name
                        .getNamespaceURI(), name.getLocalPart());
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
        xsd.getContents().add(definition);

        return definition;
    }
}
