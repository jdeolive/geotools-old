/*
 * Created on Feb 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.demos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.validation.FeatureValidation;
import org.geotools.validation.IntegrityValidation;
import org.geotools.validation.PlugIn;
import org.geotools.validation.Validation;
import org.geotools.validation.ValidationProcessor;
import org.geotools.validation.ValidationResults;
import org.geotools.validation.dto.ArgumentDTO;
import org.geotools.validation.dto.PlugInDTO;
import org.geotools.validation.dto.TestDTO;
import org.geotools.validation.dto.TestSuiteDTO;
import org.geotools.validation.xml.ValidationException;
import org.geotools.validation.xml.XMLReader;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Validator purpose.
 * <p>
 * Description of Validator ...
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: Validator.java,v 1.2 2004/02/13 18:14:15 dmzwiers Exp $
 */
public class Validator {
	
	private static Properties dataStoreProp;
	private static Properties transProp;

	public static void main(String[] args) {
		loadProperties(args);
		Map ds = loadDataStores();
		ValidationProcessor v = loadTests();
		runTransactions(ds,v);
	}
	
	static class BatchValidationResults implements ValidationResults{
		Map errors = new HashMap();
		Validation v;

		public void error(Feature feature, String message){
			errors.put(feature,message);
		}
		public void warning(Feature feature, String message){
			errors.put(feature,message);
		}
		public void setValidation(Validation validation){
			v = validation;
		}
		public Map getErrors(){
			return errors;
		}
		public String toString(){
			String r = "";
			Iterator i = errors.keySet().iterator();
			while(i.hasNext()){
				Feature f = (Feature)i.next();
				String msg = (String)errors.get(f);
				r += f.getID()+" "+msg+"\n";
			}
			return r;
		}
	}
	
	private static void runTransactions(Map dsm, ValidationProcessor v){
		if(dsm == null || dsm.size() ==0){
			System.out.println("No Datastores were defined.");
			return;
		}
		if(v==null){
			System.err.println("An error occured: Cannot run without a ValidationProcessor.");
			return;
		}
		Iterator i = dsm.keySet().iterator();
		while(i.hasNext()){
			Map sources = new HashMap();
			String key = i.next().toString();
			DataStore ds = (DataStore)dsm.get(key);
			String t = transProp.getProperty(key+".Sources");
			String[] ss = t.split(",");
			for(int j=0;j<ss.length;j++){
				ss[j] = ss[j].trim();
				try{
					FeatureSource fs = ds.getFeatureSource(ss[j]);
					sources.put(ss,fs);
					BatchValidationResults vr = new BatchValidationResults();
					v.runFeatureTests(fs.getSchema(),fs.getFeatures().collection(),vr);
					System.out.println("Feature Test Results for "+key+":"+ss[j]);
					System.out.println(vr.toString());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			Envelope env = null;
			try{
				double minx = Double.parseDouble(transProp.getProperty("Bounds.minX"));
				double miny = Double.parseDouble(transProp.getProperty("Bounds.maxX"));
				double maxx = Double.parseDouble(transProp.getProperty("Bounds.maxX"));
				double maxy = Double.parseDouble(transProp.getProperty("Bounds.maxY"));
				env = new Envelope(minx,miny,maxx,maxy);
			}catch(Exception e){
				System.err.println("Env not specified.");
			}
			if(env == null)
				env = new Envelope(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

			try{
				BatchValidationResults vr = new BatchValidationResults();
				v.runIntegrityTests(sources,env,vr);
				System.out.println("Feature Integrety Test Results");
				System.out.println(vr.toString());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private static void loadProperties(String[] args){
		File dsProp = null;
		File trProp = null;
		
		dsProp = new File(ClassLoader.getSystemResource("org/geotools/demos/DataStore.properties").getFile());
		trProp = new File(ClassLoader.getSystemResource("org/geotools/demos/Transaction.properties").getFile());
		
		if(args.length>0){
			int i=0;
			while(i+1<args.length){
				String param = args[i++];
				String value = args[i++];
				if("-data".equals(param)){
					File tmp = null;
					tmp = new File(ClassLoader.getSystemResource(value).getFile());
					if(tmp!=null){
						dsProp=tmp;
					}else{
						System.err.println("Error: The data property file could not be found.");
						System.err.println("Data file:"+value);
						System.exit(1);
					}
				}else{
				if("-trans".equals(param)){
					File tmp = null;
					tmp = new File(ClassLoader.getSystemResource(value).getFile());
					if(tmp!=null){
						trProp=tmp;
					}else{
						System.err.println("Error: The transaction property file could not be found.");
						System.err.println("Data file:"+value);
						System.exit(1);
					}
				}else{
				if("-help".equals(param)){
					System.out.println("Batch Validator");
					System.out.println("Version 0.1");
					System.out.println("");
					System.out.println("Usage: java Validator [Options]");
					System.out.println("Options: -help -data -test -trans");
					System.out.println("");
					System.out.println("-data <filename>");
					System.out.println("Replaces the default datastore property file");
					System.out.println("");
					System.out.println("-trans <filename>");
					System.out.println("Replaces the default transaction property file");
					System.out.println("");
					System.out.println("-help");
					System.out.println("This screen.");
					System.exit(0);
				}else{
					System.err.println("Usage Error: use -help for usage");
					System.err.println("Invalid option:"+param);
					System.exit(1);
				}}}
			}
		}
		
		// the files should all contain the specified data, time to check for existance ...
		if(dsProp == null || (!dsProp.exists() || (!dsProp.isFile() || !dsProp.canRead()))){
			System.err.println("Error: The data property file had errors.");
			if(dsProp == null){
				System.err.println("Data file was null");
			}else{
			if(!dsProp.exists()){
				System.err.println("Data file was does not exist");
				System.err.println(dsProp.toString());
			}else{
			if(!dsProp.isFile()){
				System.err.println("Data path specified is not a file.");
				System.err.println(dsProp.toString());
			}else{
			if(!dsProp.canRead()){
				System.err.println("Data file cannot be read.");
				System.err.println(dsProp.toString());
			}else{
				System.err.println("Data file had an unknown error.");
				System.err.println(dsProp.toString());
			}}}}
			System.exit(1);
		}
		
		if(trProp == null || (!trProp.exists() || (!trProp.isFile() || !trProp.canRead()))){
			System.err.println("Error: The test property file had errors.");
			if(trProp == null){
				System.err.println("Transaction file was null");
			}else{
			if(!trProp.exists()){
				System.err.println("Transaction file was does not exist");
				System.err.println(trProp.toString());
			}else{
			if(!trProp.isFile()){
				System.err.println("Transaction path specified is not a file.");
				System.err.println(trProp.toString());
			}else{
			if(!trProp.canRead()){
				System.err.println("Transaction file cannot be read.");
				System.err.println(trProp.toString());
			}else{
				System.err.println("Transaction file had an unknown error.");
				System.err.println(trProp.toString());
			}}}}
			System.exit(1);
		}
		
		// the files are valid, time to load them
		dataStoreProp = new Properties();
		transProp = new Properties();
		
		try{
			dataStoreProp.load(new FileInputStream(dsProp));
		}catch(FileNotFoundException e){
			System.err.println("DataStore file was does not exist");
			System.err.println(dsProp.toString());
			e.printStackTrace();
			System.exit(1);
		}catch(IOException e){
			System.err.println("DataStore file had errors reading");
			System.err.println(dsProp.toString());
			e.printStackTrace();
			System.exit(1);
		}
		
		try{
			transProp.load(new FileInputStream(trProp));
		}catch(FileNotFoundException e){
			System.err.println("Transaction file was does not exist");
			System.err.println(trProp.toString());
			e.printStackTrace();
			System.exit(1);
		}catch(IOException e){
			System.err.println("Transaction file had errors reading");
			System.err.println(trProp.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private synchronized static Map loadDataStores(){
		String dsIds = dataStoreProp.getProperty("DataStoreIds");
		String[] ids = dsIds.split(",");
		Map result = new HashMap();
		int rcount = 0;
		// remove whitespace
		for(int i=0;i<ids.length;i++){
			ids[i] = ids[i].trim();
			Map m = new HashMap();
			String tmp = dataStoreProp.getProperty(ids[i]+".Params");
			String[] params = tmp.split(",");
			for(int j=0;j<params.length;j++){
				String[] vals = params[j].split("=");
				if(vals.length==2){
					m.put(vals[0].trim(),vals[1].trim());
				}else{
					System.err.println("DataStore "+ids[i]+" incured an error loading a parameter.");
					System.err.println("This DataStore may not be loaded correctly.");
					System.err.println("Parameters should be specified in a comma delimited list key=value");
					System.err.println("You specified:"+tmp);
				}
			}
			DataStore dataStore = null;
			try {
				dataStore = DataStoreFinder.getDataStore( m );
			} catch (Throwable ex) {
				ex.printStackTrace();
				System.err.println("DataStore "+ids[i]+" incured an error and will not be used.");
				
			}
			if(dataStore!=null){
				result.put(ids[i],dataStore);
			}else
				System.err.println("DataStore "+ids[i]+" incured an error and will not be used.");
		}
		
		return result;
	}
	
	private static ValidationProcessor loadTests(){
		File plugInDir = null;
		plugInDir = new File(transProp.getProperty("PlugInDir"));
		if(plugInDir == null){
			System.err.println("PlugIn Dir does not exist");
			System.exit(1);
		}
		if(!plugInDir.exists()){
			System.err.println("PlugIn Dir does not exist");
			System.err.println(plugInDir.toString());
			System.exit(1);
		}
		if(!plugInDir.isDirectory()){
			System.err.println("PlugIn Dir is not a directory");
			System.err.println(plugInDir.toString());
			System.exit(1);
		}
		if(!plugInDir.canRead()){
			System.err.println("PlugIn Dir cannot be read");
			System.err.println(plugInDir.toString());
			System.exit(1);
		}
		Map m = null;
		try{
			m = XMLReader.loadPlugIns(plugInDir);
		}catch(ValidationException e){
			System.err.println("PlugIn load had errors.");
			System.err.println(plugInDir.toString());
			e.printStackTrace();
			System.exit(1);
		}
		if(m==null){
			System.err.println("PlugIn load had errors.");
			System.err.println("No plugins were loaded");
			System.err.println(plugInDir.toString());
			System.exit(1);
		}
		if(m.size()==0){
			System.err.println("PlugIn load had errors.");
			System.err.println("No plugins were found");
			System.err.println(plugInDir.toString());
			System.exit(1);
		}
		

		File testSuite = null;
		testSuite = new File(transProp.getProperty("TestSuiteFile"));
		if(testSuite == null){
			System.err.println("TestSuite file does not exist");
			System.exit(1);
		}
		if(!testSuite.exists()){
			System.err.println("TestSuite file does not exist");
			System.err.println(testSuite.toString());
			System.exit(1);
		}
		if(!testSuite.isFile()){
			System.err.println("TestSuite file is not a file");
			System.err.println(testSuite.toString());
			System.exit(1);
		}
		if(!testSuite.canRead()){
			System.err.println("TestSuite file cannot be read");
			System.err.println(testSuite.toString());
			System.exit(1);
		}
		TestSuiteDTO dto = null;
		try{
			dto = XMLReader.readTestSuite(new FileReader(testSuite),m);
		}catch(FileNotFoundException e){
			System.err.println("TestSuite file was not found.");
			System.err.println(testSuite.toString());
			e.printStackTrace();
			System.exit(1);
		}catch(ValidationException e){
			System.err.println("TestSuite load had errors.");
			System.err.println(testSuite.toString());
			e.printStackTrace();
			System.exit(1);
		}
		Map mt = new HashMap(1);
		mt.put(dto.getName(),dto);
		
        // We need to make our own validator for batch
        // processing
        // (for starters it should use a custom FeatureResults
        //  that logs fail/warning information)
		BatchValidator gv = new BatchValidator(mt,m);
		return gv;
	}
}

/*
 * Created on Feb 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

class BatchValidator extends ValidationProcessor {

	/**
	 * BatchValidator constructor.
	 * <p>
	 * super();
	 * </p>
	 * 
	 */
	public BatchValidator() {
		super();
	}

	/**
	 * ValidationProcessor constructor.
	 * 
	 * <p>
	 * Builds a ValidationProcessor with the DTO provided.
	 * </p>
	 *
	 * @see load(Map,Map) 
	 * @param testSuites Map a map of names -> TestSuiteDTO objects
	 * @param plugIns Map a map of names -> PlugInDTO objects
	 */
	public BatchValidator(Map testSuites, Map plugIns) {
		super();
		load(testSuites,plugIns);
	}
	
	/**
	 * load purpose.
	 * <p>
	 * loads this instance data into this instance.
	 * </p>
	 * @param testSuites
	 * @param plugIns
	 */
	public void load(Map testSuites, Map plugIns){
		
		// step 1 make a list required plug-ins
		Set plugInNames = new HashSet();
		Iterator i = testSuites.keySet().iterator();

		while (i.hasNext()) {
			TestSuiteDTO dto = (TestSuiteDTO) testSuites.get(i.next());
			Iterator j = dto.getTests().keySet().iterator();
			while (j.hasNext()) {
				TestDTO tdto = (TestDTO) dto.getTests().get(j.next());
				plugInNames.add(tdto.getPlugIn().getName());
			}
		}

		i = plugIns.values().iterator();
		while(i.hasNext())
			errors.put(i.next(),Boolean.FALSE);
		
		// step 2 configure plug-ins with defaults
		Map defaultPlugIns = new HashMap(plugInNames.size());
		i = plugInNames.iterator();

		while (i.hasNext()) {
			String plugInName = (String) i.next();
			PlugInDTO dto = (PlugInDTO) plugIns.get(plugInName);
			Class plugInClass = null;

			try {
				plugInClass = Class.forName(dto.getClassName());
			} catch (ClassNotFoundException e) {
				//Error, using default.
				errors.put(dto,e);
				e.printStackTrace();
			}

			if (plugInClass == null) {
				plugInClass = Validation.class;
			}

			Map plugInArgs = dto.getArgs();

			if (plugInArgs == null) {
				plugInArgs = new HashMap();
			}

			try {
				PlugIn plugIn = new org.geotools.validation.PlugIn(plugInName,
						plugInClass, dto.getDescription(), plugInArgs);
				defaultPlugIns.put(plugInName, plugIn);
			} catch (ValidationException e) {
				e.printStackTrace();
				errors.put(dto,e);
				//error should log here
				continue;
			}
			errors.put(dto,Boolean.TRUE);
		}

		// step 3 configure plug-ins with tests + add to processor
		i = testSuites.keySet().iterator();

		while (i.hasNext()) {
			TestSuiteDTO tdto = (TestSuiteDTO) testSuites.get(i.next());
			Iterator j = tdto.getTests().keySet().iterator();

			while (j.hasNext()) {
				TestDTO dto = (TestDTO) tdto.getTests().get(j.next());

				// deal with test
				Map testArgs = dto.getArgs();

				if (testArgs == null) {
					testArgs = new HashMap();
				}else{
					Map m = new HashMap();
					Iterator k = testArgs.keySet().iterator();
					while(k.hasNext()){
						ArgumentDTO adto = (ArgumentDTO)testArgs.get(k.next());
						m.put(adto.getName(),adto.getValue());
					}
					testArgs = m;
				}

				try {
					PlugIn plugIn = (org.geotools.validation.PlugIn) defaultPlugIns
					.get(dto.getPlugIn().getName());
					Validation validation = plugIn.createValidation(dto.getName(),
							dto.getDescription(), testArgs);

					if (validation instanceof FeatureValidation) {
						addValidation((FeatureValidation) validation);
					}

					if (validation instanceof IntegrityValidation) {
						addValidation((IntegrityValidation) validation);
					}
				} catch (ValidationException e) {
					e.printStackTrace();
					errors.put(dto,e);
					//error should log here
					continue;
				}
				errors.put(dto,Boolean.TRUE);
			}
			errors.put(tdto,Boolean.TRUE);
		}
	}
}