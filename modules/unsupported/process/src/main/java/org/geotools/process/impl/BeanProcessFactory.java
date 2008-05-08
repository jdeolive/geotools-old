package org.geotools.process.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.geotools.process.Parameter;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

public abstract class BeanProcessFactory implements ProcessFactory {
    
    public Process create() {
        return new SimpleProcess(this){
            public void process() throws Exception {
                BeanProcessFactory.this.process( input, result );
            }
        };
    }

    public InternationalString getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public Map<String, Parameter< ? >> getParameterInfo() {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo( getInputBean() );
        } catch (IntrospectionException e) {
            return null;
        }
        Map<String,Parameter<?>> parameterInfo = new HashMap<String, Parameter<?>>();
        
        for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ){
            Method getter = descriptor.getReadMethod();
            if( getter == null ) continue;
            
            Parameter<?> parameter = new Parameter(descriptor.getName(), descriptor.getPropertyType(),
                    Text.text(descriptor.getDisplayName()) );
            
            parameterInfo.put( descriptor.getName(), parameter );            
        }
        return parameterInfo;
    }

    public Map<String, Parameter< ? >> getResultInfo() {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo( getResultBean() );
        } catch (IntrospectionException e) {
            return null;
        }
        Map<String,Parameter<?>> parameterInfo = new HashMap<String, Parameter<?>>();
        
        for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ){
            Method setter = descriptor.getWriteMethod();
            if( setter == null ) continue;
            
            Parameter<?> parameter = new Parameter(descriptor.getName(), descriptor.getPropertyType(),
                    Text.text(descriptor.getDisplayName()) );
            
            parameterInfo.put( descriptor.getName(), parameter );            
        }
        return parameterInfo;
    }
    public Map<String, Parameter< ? >> getResultInfo( Map<String, Object> parameters )
            throws IllegalArgumentException {
        return null;
    }

    public InternationalString getTitle() {
        return Text.text( getClass().getSimpleName() );
    }

    protected void process( Map<String, Object> inputMap, Map<String, Object> resultMap ) throws Exception {
        BeanInfo inputInfo = Introspector.getBeanInfo( getInputBean() );
        Object inputBean = inputInfo.getBeanDescriptor().getBeanClass().getConstructor( new Class[0]);
        // should use commons beans here ....
        configure( inputInfo, inputMap, inputBean );
        
        
        Object resultBean = process( inputBean );        
        BeanInfo resultInfo = Introspector.getBeanInfo( getResultBean(), Object.class );
        
        results( resultMap, resultBean, resultInfo );        
    }
    
    private void results( Map<String, Object> resultMap, Object bean, BeanInfo info ) {
        for( PropertyDescriptor property : info.getPropertyDescriptors() ){
            if( resultMap.containsKey( property.getName() )){
                Method setter = property.getReadMethod();
                try {
                    Object value = setter.invoke( bean );
                    resultMap.put( property.getName(), value );
                } catch (Exception e) {
                    // ignore for right now .. TODO WARNING
                }
            }
        }
    }

    private void configure( BeanInfo info, Map<String, Object> inputMap, Object bean ) {
        for( PropertyDescriptor property : info.getPropertyDescriptors() ){
            if( inputMap.containsKey( property.getName() )){
                Method setter = property.getWriteMethod();
                try {
                    setter.invoke( bean, inputMap.get( property.getName() ));
                } catch (Exception e) {
                    // ignore for right now .. TODO WARNING
                }
            }
        }
    }

    /** 
     * Please return us an instanceof the bean you expect for input.
     * <p>
     * We will generate the correct process api input parameters to reflect
     * your choice.
     * @return bean used for input
     */
    protected abstract Class<?> getInputBean();
    protected abstract Class<?> getResultBean();
    protected Object process( Object input ) {
        return null; // please implement your process here
    }
}
