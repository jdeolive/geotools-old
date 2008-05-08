/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.complex.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDAttributeUseCategory;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.data.feature.adapter.ISOAttributeTypeAdapter;
import org.geotools.data.feature.adapter.ISOFeatureTypeAdapter;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GML;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.gml3.GMLSchema;
import org.geotools.gml3.smil.SMIL20;
import org.geotools.gml3.smil.SMIL20LANG;
import org.geotools.gml3.smil.SMIL20LANGSchema;
import org.geotools.gml3.smil.SMIL20Schema;
import org.geotools.xlink.XLINK;
import org.geotools.xml.Binding;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.geotools.xs.XS;
import org.geotools.xs.XSSchema;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class EmfAppSchemaReader {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(EmfAppSchemaReader.class.getPackage()
            .getName());

    /**
     * Caches the GML 3.1.1 types and its dependencies
     */
    private static Map FOUNDATION_TYPES = new HashMap();

    /**
     * Contains all the AttributeDescriptors and AttributeTypes defined in the
     * application schema and its imports
     */
    private Map registry;

    private TypeFactory typeFactory;

    private SchemaIndex appSchemaIndex;

    private EmfAppSchemaReader() {
        typeFactory = new TypeFactoryImpl();
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public Map getTypeRegistry() {
        return new HashMap(this.registry);
    }

    /**
     * 
     * @param configuration
     *            configuration object used to access the XSDSchema to parse.
     *            This configuration object might contain {@link Binding}s
     * @throws IOException
     */
    public void parse(Configuration configuration) throws IOException {
        //find out the schemas involved in the app schema configuration
        appSchemaIndex = Schemas.findSchemas(configuration);

        //set up the type registry
        registry = new HashMap();
        
        //register the "fundation" gml types already bound to geotools AttributeTypes
        if (EmfAppSchemaReader.FOUNDATION_TYPES.isEmpty()) {
            createFoundationTypes();
        } 
        registry.putAll(EmfAppSchemaReader.FOUNDATION_TYPES);

        //with the application schemas...
        XSDSchema[] appSchemas = appSchemaIndex.getSchemas();
        Map schemas = new HashMap();
        for (Iterator it = Arrays.asList(appSchemas).iterator(); it.hasNext();) {
            XSDSchema schema = (XSDSchema) it.next();
            schemas.put(schema.getTargetNamespace(), schema);
        }
       
        //establish a preferred parsing order so there are the less proxies possible
        String []preferredOrder = {XS.NAMESPACE, XLINK.NAMESPACE, SMIL20.NAMESPACE, SMIL20LANG.NAMESPACE, GML.NAMESPACE};
        List schemaList = new ArrayList(appSchemas.length);
        for(int i  = 0; i < preferredOrder.length; i++){
            String targetNamespace = preferredOrder[i];
            XSDSchema schema = (XSDSchema) schemas.get(targetNamespace);
            if(schema != null){
                schemaList.add(schema);
                schemas.remove(targetNamespace);
            }
        }
        schemaList.addAll(schemas.values());

        //and import them all
        for (Iterator it = schemaList.iterator(); it.hasNext();) {
            XSDSchema schema = (XSDSchema) it.next();
            importSchema(schema);
        }
    }

    public void parse(final URL location) throws IOException {

        String nameSpace = findSchemaNamespace(location);

        String schemaLocation = location.toExternalForm();
        Configuration configuration = new ApplicationSchemaConfiguration(nameSpace, schemaLocation);

        parse(configuration);
    }

    private String findSchemaNamespace(URL location) throws IOException {
        String targetNamespace = null;
        // parse some of the instance document to find out the
        // schema location
        InputStream input = location.openStream();

        // create stream parser
        XmlPullParser parser = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            // parse root element
            parser = factory.newPullParser();
            parser.setInput(input, "UTF-8");
            parser.nextTag();

            // look for schema location
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                if ("targetNamespace".equals(parser.getAttributeName(i))) {
                    targetNamespace = parser.getAttributeValue(i);
                    break;
                }
            }
            // reset input stream
            parser.setInput(null);
        } catch (XmlPullParserException e) {
            String msg = "Cannot find target namespace for schema document " + location;
            throw (RuntimeException) new RuntimeException(msg).initCause(e);
        } finally {
            input.close();
        }
        if (targetNamespace == null) {
            throw new IllegalArgumentException(
                    "Input document does not specifies a targetNamespace");
        }
        return targetNamespace;
    }

    private void createFoundationTypes() {
        synchronized (EmfAppSchemaReader.FOUNDATION_TYPES) {
            if (!EmfAppSchemaReader.FOUNDATION_TYPES.isEmpty()) {
                return;
            }
            Schema schema;
            schema = new XSSchema();
            importSchema(schema);

            schema = new SMIL20Schema();
            importSchema(schema);

            schema = new SMIL20LANGSchema();
            importSchema(schema);

            schema = new GMLSchema();
            importSchema(schema);

            LOGGER.info("Creating GMLConfiguration to get the prebuilt gml schemas from");
            GMLConfiguration configuration = new GMLConfiguration();
            LOGGER.info("Aquiring prebuilt gml schema and its dependencies");
            SchemaIndex index = Schemas.findSchemas(configuration);
            XSDSchema[] schemas = index.getSchemas();

            LOGGER.info("Importing GML schema and dependencies");
            for (int i = 0; i < schemas.length; i++) {
                XSDSchema xsdSchema = schemas[i];
                String targetNamespace = xsdSchema.getTargetNamespace();
                if (XS.NAMESPACE.equals(targetNamespace)) {
                    LOGGER.finest("Ignoring XS schema parsing");
                    continue;
                }
                importSchema(xsdSchema);
            }

            EmfAppSchemaReader.FOUNDATION_TYPES.putAll(registry);
            registry.clear();
        }
    }

    private void importSchema(XSDSchema xsdSchema) {
        String targetNamespace = xsdSchema.getTargetNamespace();
        LOGGER.fine("Importing schema " + targetNamespace);

        List typeDefinitions = xsdSchema.getTypeDefinitions();
        LOGGER.finer("Importing " + targetNamespace + " type definitions");
        importXsdTypeDefinitions(typeDefinitions);

        List elementDeclarations = xsdSchema.getElementDeclarations();
        LOGGER.finer("Importing " + targetNamespace + " element definitions");
        importElementDeclarations(elementDeclarations);
    }

    private void importElementDeclarations(List elementDeclarations) {
        XSDElementDeclaration elemDecl;
        for (Iterator it = elementDeclarations.iterator(); it.hasNext();) {
            elemDecl = (XSDElementDeclaration) it.next();
            LOGGER.finest("Creating attribute descriptor for " + elemDecl.getQName());
            AttributeDescriptor descriptor;
            try {
                descriptor = createAttributeDescriptor(null, elemDecl);
                LOGGER.finest("Registering attribute descriptor " + descriptor.getName());
                register(descriptor);
            } catch (NoSuchElementException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }
    }

    private void register(AttributeDescriptor descriptor) {
        Name name = descriptor.getName();
        registry.put(name, descriptor);
    }

    private void register(AttributeType type) {
        TypeName name = type.getName();
        Object old = registry.put(name, type);
        if (old != null) {
            LOGGER.fine(type.getName() + " replaced by new value.");
        }
    }

    private AttributeDescriptor createAttributeDescriptor(final XSDComplexTypeDefinition container,
            final XSDElementDeclaration elemDecl) {
        String targetNamespace = elemDecl.getTargetNamespace();
        String name = elemDecl.getName();
        Name elemName = Types.attributeName(targetNamespace, name);

        AttributeType type;
        try {
            type = getTypeOf(elemDecl);
        } catch (NoSuchElementException e) {
            String msg = "Type not found for " + elemName + " at type container "
                    + container.getTargetNamespace() + "#" + container.getName() + " at "
                    + container.getSchema().getSchemaLocation();
            NoSuchElementException nse = new NoSuchElementException(msg);
            nse.initCause(e);
            throw nse;
        }
        int minOccurs = container == null ? 0 : Schemas.getMinOccurs(container, elemDecl);
        int maxOccurs = container == null ? Integer.MAX_VALUE : Schemas.getMaxOccurs(container,
                elemDecl);
        boolean nillable = elemDecl.isNillable();

        if (maxOccurs == -1) {
            // this happens when maxOccurs is set to "unbounded"
            maxOccurs = Integer.MAX_VALUE;
        }
        AttributeDescriptor descriptor = typeFactory.createAttributeDescriptor(type, elemName,
                minOccurs, maxOccurs, nillable);

        descriptor.putUserData(XSDElementDeclaration.class, elemDecl);

        return descriptor;
    }

    /**
     * If the type of elemDecl is annonymous creates a new type with the same
     * name than the atrribute and returns it. If it is not anonymous, looks it
     * up on the registry and in case the type does not exists in the registry
     * uses a proxy.
     * 
     * @param elemDecl
     * @return
     */
    private AttributeType getTypeOf(XSDElementDeclaration elemDecl) {
        boolean hasToBeRegistered = false;
        XSDTypeDefinition typeDefinition;

        // TODO REVISIT, I'm not sure this is the way to find out if the
        // element's type is defined in line (an thus no need to register it
        // as a global type)
        if(elemDecl.isElementDeclarationReference()){
            elemDecl = elemDecl.getResolvedElementDeclaration();
        }
        typeDefinition = elemDecl.getAnonymousTypeDefinition();
        if (typeDefinition == null) {
            hasToBeRegistered = true;
            typeDefinition = elemDecl.getTypeDefinition();
        }

        if (typeDefinition == null) {
            throw new NoSuchElementException("The element declaration "
                    + elemDecl.getTargetNamespace() + "#" + elemDecl.getName()
                    + " has a null type definition, can't continue, fix it on the schema");
        }

        AttributeType type;
        if (hasToBeRegistered) {
            String targetNamespace = typeDefinition.getTargetNamespace();
            String name = typeDefinition.getName();
            TypeName typeName = Types.typeName(targetNamespace, name);
            type = getType(typeName);
            if (type == null) {
                if (null == typeDefinition.getSimpleType()
                        && typeDefinition instanceof XSDComplexTypeDefinition) {
                    type = new ComplexTypeProxy(typeName, this.registry);
                } else {
                    type = new AttributeTypeProxy(typeName, this.registry);
                }

                /*
                 * type = createType(typeDefinition); register(type);
                 */
            }
        } else {
            String name = elemDecl.getName();
            String targetNamespace = elemDecl.getTargetNamespace();
            TypeName overrideName = Types.typeName(targetNamespace, name);
            type = createType(overrideName, typeDefinition);
        }
        return type;
    }

    private AttributeType createType(XSDTypeDefinition typeDefinition) {
        String targetNamespace = typeDefinition.getTargetNamespace();
        String name = typeDefinition.getName();
        TypeName typeName = Types.typeName(targetNamespace, name);
        return createType(typeName, typeDefinition);
    }

    /**
     * Creates an {@link AttributeType} that matches the xsd type definition as
     * much as possible.
     * <p>
     * The original type definition given by the {@link XSDTypeDefinition} is
     * kept as AttributeType's metadata stored as a "user data" property using
     * <code>XSDTypeDefinition.class</code> as key.
     * </p>
     * <p>
     * If it is a complex attribute, it will contain all the properties declared
     * in the <code>typeDefinition</code>, as well as all the properties
     * declared in its super types.
     * </p>
     * TODO: handle the case where the extension mechanism is restriction.
     * 
     * @param assignedName
     * @param typeDefinition
     * @return
     */
    private AttributeType createType(final TypeName assignedName,
            final XSDTypeDefinition typeDefinition) {

        AttributeType attType;

        final XSDTypeDefinition baseType = typeDefinition.getBaseType();

        AttributeType superType = null;
        if (baseType != null) {
            String targetNamespace = baseType.getTargetNamespace();
            String name = baseType.getName();
            superType = getType(targetNamespace, name);
            if (superType == null) {
                superType = createType(baseType);
                register(superType);
            }
        }

        // if typeDefinition.getSimpleType() != null it means it is a complex
        // xsd type
        // with a simple content model, and has some xml attributes declared,
        // hence the
        // xsd complex type definition, as simple xsd types can't have
        // attributes
        XSDSimpleTypeDefinition simpleType = typeDefinition.getSimpleType();

        if (simpleType == null && typeDefinition instanceof XSDComplexTypeDefinition) {
            XSDComplexTypeDefinition complexTypeDef;
            complexTypeDef = (XSDComplexTypeDefinition) typeDefinition;
            boolean includeParents = true;
            List children;
            children = Schemas.getChildElementDeclarations(typeDefinition, includeParents);

            final Collection schema = new ArrayList(children.size());

            XSDElementDeclaration childDecl;
            AttributeDescriptor descriptor;
            for (Iterator it = children.iterator(); it.hasNext();) {
                childDecl = (XSDElementDeclaration) it.next();
                try {
                    descriptor = createAttributeDescriptor(complexTypeDef, childDecl);
                    schema.add(descriptor);
                } catch (NoSuchElementException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                }
            }

            attType = createType(assignedName, schema, typeDefinition, superType);

        } else {
            Class binding = String.class;
            boolean isIdentifiable = false;
            boolean isAbstract = false;
            Set restrictions = Collections.EMPTY_SET;
            InternationalString description = null;
            attType = typeFactory.createAttributeType(assignedName, binding, isIdentifiable,
                    isAbstract, restrictions, superType, description);
        }

        attType.putUserData(XSDTypeDefinition.class, typeDefinition);
        return attType;
    }

    private AttributeType createType(TypeName assignedName, Collection schema,
            XSDTypeDefinition typeDefinition, AttributeType superType) {

        AttributeType abstractFType = getType(GML.NAMESPACE, GML.AbstractFeatureType.getLocalPart());
        assert abstractFType != null;

        boolean isFeatureType = isDerivedFrom(typeDefinition, abstractFType.getName());
        boolean isSimpleContent = isSimpleContent(schema);

        boolean isAbstract = false;// TODO
        Set restrictions = Collections.EMPTY_SET;
        InternationalString description = null; // TODO

        AttributeType type;
        if (isFeatureType) {
            if (isSimpleContent) {
                SimpleTypeFactory fac = new SimpleTypeFactoryImpl();
                // let the factory decide
                CoordinateReferenceSystem crs = null;
                // let the factory decide
                AttributeDescriptor defaultGeometry = null;
                type = fac.createFeatureType(assignedName, schema, defaultGeometry, crs,
                        isAbstract, restrictions, superType, description);
            } else {
                type = typeFactory.createFeatureType(assignedName, schema, null, null, isAbstract,
                        restrictions, superType, description);

            }
        } else {
            boolean isIdentifiable = isIdentifiable((XSDComplexTypeDefinition) typeDefinition);
            type = typeFactory.createComplexType(assignedName, schema, isIdentifiable, isAbstract,
                    restrictions, superType, description);
        }
        return type;
    }

    /**
     * Determines if elements of the given complex type definition are required
     * to have an identifier by looking for a child element of
     * <code>typeDefinition</code> of the form
     * <code>&lt;xs:attribute ref=&quot;gml:id&quot; use=&quot;required&quot; /&gt;</code>
     * 
     * @param typeDefinition
     * @return
     */
    private boolean isIdentifiable(XSDComplexTypeDefinition typeDefinition) {
        List attributeUses = typeDefinition.getAttributeUses();

        final String idAttName = GML.id.getLocalPart();

        for (Iterator it = attributeUses.iterator(); it.hasNext();) {
            XSDAttributeUse use = (XSDAttributeUse) it.next();
            XSDAttributeUseCategory useCategory = use.getUse();

            XSDAttributeDeclaration idAtt = use.getAttributeDeclaration();

            String targetNamespace = idAtt.getTargetNamespace();
            String name = idAtt.getName();
            if (GML.NAMESPACE.equals(targetNamespace) && idAttName.equals(name)) {
                if (XSDAttributeUseCategory.REQUIRED_LITERAL.equals(useCategory)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if all the AttributeDescriptors contained in
     * <code>schema</code> are of a simple type and no one has maxOccurs > 1.
     * <p>
     * Note this method ignores the attributes from the GML namespace
     * </p>
     * 
     * @param schema
     * @return
     */
    private boolean isSimpleContent(Collection schema) {
        AttributeDescriptor descriptor;
        for (Iterator it = schema.iterator(); it.hasNext();) {
            descriptor = (AttributeDescriptor) it.next();
            if(GML.NAMESPACE.equals(descriptor.getName().getNamespaceURI())){
                continue;
            }
            if (descriptor.getMaxOccurs() > 1) {
                return false;
            }
            if (descriptor.getType() instanceof ComplexType) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if <code>typeDefinition</code> is derived
     * from a type named <code>superTypeName</code>
     * 
     * @param typeDefinition
     * @param superTypeName
     * @return
     */
    private boolean isDerivedFrom(XSDTypeDefinition typeDefinition, final TypeName superTypeName) {

        XSDTypeDefinition baseType;
        final String superNS = superTypeName.getNamespaceURI();
        final String superName = superTypeName.getLocalPart();

        String targetNamespace;
        String name;
        while ((baseType = typeDefinition.getBaseType()) != null) {
            targetNamespace = baseType.getTargetNamespace();
            name = baseType.getName();
            if (XS.NAMESPACE.equals(targetNamespace) && XS.ANYTYPE.getLocalPart().equals(name)) {
                return false;
            }
            if (superNS.equals(targetNamespace) && superName.equals(name)) {
                return true;
            }
            typeDefinition = baseType;
        }
        return false;
    }

    private AttributeType getType(String namespace, String name) {
        TypeName typeName = Types.typeName(namespace, name);
        return getType(typeName);
    }

    private AttributeType getType(TypeName typeName) {
        AttributeType type = (AttributeType) registry.get(typeName);
        return type;
    }

    private void importXsdTypeDefinitions(List typeDefinitions) {
        XSDTypeDefinition typeDef;
        AttributeType attType;
        for (Iterator it = typeDefinitions.iterator(); it.hasNext();) {
            typeDef = (XSDTypeDefinition) it.next();
            String targetNamespace = typeDef.getTargetNamespace();
            String name = typeDef.getName();

            attType = getType(targetNamespace, name);
            if (attType == null) {
                LOGGER.finest("Creating attribute type " + typeDef.getQName());
                attType = createType(typeDef);
                LOGGER.finest("Registering attribute type " + attType.getName());
                register(attType);
            } else {
                // LOGGER.finer("Ignoring type " +
                // typeDef.getQName()
                // + " as it already exists in the registry");
            }
        }
        LOGGER.finer("--- type definitions imported successfully ---");
    }

    private void importSchema(Schema schema) {
        for (Iterator it = schema.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            Name key = (Name) entry.getKey();
            Object value = entry.getValue();
            if (registry.containsKey(key)) {
                LOGGER.finer("Ignoring " + key + " as it already exists. type "
                        + value.getClass().getName());
            } else {
                LOGGER.finer("Importing " + key + " of type " + value.getClass().getName());
                if (value instanceof AttributeType) {
                    AttributeType type = (AttributeType) value;
                    register(type);
                } else if (value instanceof AttributeDescriptor) {
                    AttributeDescriptor descriptor = (AttributeDescriptor) value;
                    register(descriptor);
                } else if (value instanceof org.geotools.feature.AttributeType) {
                    org.geotools.feature.AttributeType gtType;
                    gtType = (org.geotools.feature.AttributeType) value;
                    String nsUri = schema.namespace().getURI();
                    AttributeType isoType = ISOAttributeTypeAdapter.adapter(nsUri, gtType);
                    register(isoType);
                } else if (value instanceof org.geotools.feature.FeatureType) {
                    org.geotools.feature.FeatureType gtType;
                    gtType = (org.geotools.feature.FeatureType) value;
                    FeatureType isoType = new ISOFeatureTypeAdapter(gtType);
                    register(isoType);
                }
            }
        }
        LOGGER.fine("Schema " + schema.namespace().getURI() + " imported successfully");
    }

    public static EmfAppSchemaReader newInstance() {
        return new EmfAppSchemaReader();
    }

}
