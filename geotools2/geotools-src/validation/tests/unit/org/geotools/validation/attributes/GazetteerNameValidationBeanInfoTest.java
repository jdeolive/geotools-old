/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.attributes;

import junit.framework.TestCase;
import java.beans.*;
import java.net.*;
/**
 * GazetteerNameValidationBeanInfoTest purpose.
 * <p>
 * Description of GazetteerNameValidationBeanInfoTest ...
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @version $Id: GazetteerNameValidationBeanInfoTest.java,v 1.1 2004/04/29 21:57:32 sploreg Exp $
 */
public class GazetteerNameValidationBeanInfoTest extends TestCase {

	public GazetteerNameValidationBeanInfoTest(){super("");}
	public GazetteerNameValidationBeanInfoTest(String s){super(s);}
	/*
	 * Class to test for PropertyDescriptor[] getPropertyDescriptors()
	 */
	public void testGetPropertyDescriptors() {
		try{
			GazetteerNameValidation gnv = new GazetteerNameValidation();
			gnv.setName("test");
			gnv.setGazetteer(new URL("http://http://hydra/time/"));
			BeanInfo bi = Introspector.getBeanInfo(gnv.getClass());
			PropertyDescriptor[] pd = bi.getPropertyDescriptors();
			PropertyDescriptor url, name;url = name = null;
			for(int i=0;i<pd.length;i++){
				if("name".equals(pd[i].getName())){
					name = pd[i];
				}
				if("gazetteer".equals(pd[i].getName())){
					url = pd[i];
				}
			}
			assertNotNull(url);
			assertNotNull(name);
			assertTrue("test".equals(name.getReadMethod().invoke(gnv,null)));
			assertTrue((new URL("http://http://hydra/time/")).equals(url.getReadMethod().invoke(gnv,null)));
		}catch(Exception e){
			e.printStackTrace();
			fail(e.toString());
		}
	}

}
