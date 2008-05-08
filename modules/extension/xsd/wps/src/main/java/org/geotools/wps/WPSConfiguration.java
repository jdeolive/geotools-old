package org.geotools.wps;

import java.util.Map;

import net.opengis.wps.WpsFactory;

import org.geotools.ows.v1_1.OWSConfiguration;
import org.geotools.wps.bindings.LanguagesBinding;
import org.geotools.xml.ComplexEMFBinding;
import org.geotools.xml.Configuration;
import org.picocontainer.MutablePicoContainer;

/**
 * Parser configuration for the http://www.opengis.net/wps/1.0.0 schema.
 *
 * @generated
 */
public class WPSConfiguration extends Configuration {

    /**
     * Creates a new configuration.
     * 
     * @generated
     */     
    public WPSConfiguration() {
       super(WPS.getInstance());
   
       addDependency( new OWSConfiguration());
    }
    
    @Override
    protected void registerBindings(Map bindings) {
        
        bindings.put(WPS.ComplexDataCombinationsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ComplexDataCombinationsType));
        bindings.put(WPS.ComplexDataCombinationType,new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ComplexDataCombinationType));
        bindings.put(WPS.ComplexDataDescriptionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ComplexDataDescriptionType));
        bindings.put(WPS.ComplexDataType,new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ComplexDataType));
        bindings.put(WPS.CRSsType,new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.CRSsType));
        bindings.put(WPS.DataInputsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.DataInputsType));
        bindings.put(WPS.DataType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.DataType));
        bindings.put(WPS.DescriptionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.DescriptionType));
        bindings.put(WPS.DocumentOutputDefinitionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.DocumentOutputDefinitionType));
        bindings.put(WPS.InputDescriptionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.InputDescriptionType));
        bindings.put(WPS.InputReferenceType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.InputReferenceType));
        bindings.put(WPS.InputType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.InputType));
        bindings.put(WPS.LanguagesType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.LanguagesType));
        bindings.put(WPS.LiteralDataType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.LiteralDataType));
        bindings.put(WPS.LiteralInputType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.LiteralInputType));
        bindings.put(WPS.LiteralOutputType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.LiteralOutputType));
        bindings.put(WPS.OutputDataType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.OutputDataType));
        bindings.put(WPS.OutputDefinitionsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.OutputDefinitionsType));
        bindings.put(WPS.OutputDefinitionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.OutputDefinitionType));
        bindings.put(WPS.OutputDescriptionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.OutputDescriptionType));
        bindings.put(WPS.OutputReferenceType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.OutputReferenceType));
        bindings.put(WPS.ProcessBriefType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessBriefType));
        bindings.put(WPS.ProcessDescriptionType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessDescriptionType));
        bindings.put(WPS.ProcessFailedType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessFailedType));
        bindings.put(WPS.ProcessStartedType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessStartedType));
        bindings.put(WPS.RequestBaseType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.RequestBaseType));
        bindings.put(WPS.ResponseBaseType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ResponseBaseType));
        bindings.put(WPS.ResponseDocumentType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ResponseDocumentType));
        bindings.put(WPS.ResponseFormType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ResponseFormType));
        bindings.put(WPS.StatusType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.StatusType));
        bindings.put(WPS.SupportedComplexDataInputType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedComplexDataInputType));
        bindings.put(WPS.SupportedComplexDataType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedComplexDataType));
        bindings.put(WPS.SupportedCRSsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedCRSsType));
        bindings.put(WPS.SupportedUOMsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedUOMsType));
        bindings.put(WPS.UOMsType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.UOMsType));
        bindings.put(WPS.ValuesReferenceType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ValuesReferenceType));
        bindings.put(WPS.WPSCapabilitiesType, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.WPSCapabilitiesType));
        bindings.put(WPS._DescribeProcess, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._DescribeProcess));
        bindings.put(WPS._Execute, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._Execute));
        bindings.put(WPS._ExecuteResponse, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._ExecuteResponse));
        bindings.put(WPS._GetCapabilities, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._GetCapabilities));
        bindings.put(WPS._Languages, LanguagesBinding.class);
        bindings.put(WPS._ProcessDescriptions, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._ProcessDescriptions));
        bindings.put(WPS._ProcessOfferings, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._ProcessOfferings));
        bindings.put(WPS._WSDL, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS._WSDL));
        bindings.put(WPS.InputReferenceType_Header, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.InputReferenceType_Header));
        bindings.put(WPS.InputReferenceType_BodyReference, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.InputReferenceType_BodyReference));
        bindings.put(WPS.ProcessDescriptionType_DataInputs, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessDescriptionType_DataInputs));
        bindings.put(WPS.ProcessDescriptionType_ProcessOutputs, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.ProcessDescriptionType_ProcessOutputs));
        bindings.put(WPS.SupportedCRSsType_Default, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedCRSsType_Default));
        bindings.put(WPS.SupportedUOMsType_Default, new ComplexEMFBinding(WpsFactory.eINSTANCE,WPS.SupportedUOMsType_Default));
    }
    
    @Override
    protected void configureContext(MutablePicoContainer container) {
        container.registerComponentInstance(WpsFactory.eINSTANCE);
    }
} 