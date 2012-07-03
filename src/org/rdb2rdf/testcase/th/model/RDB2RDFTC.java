package org.rdb2rdf.testcase.th.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openjena.riot.Lang;
import org.rdb2rdf.testcase.th.Comparator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class RDB2RDFTC {

	protected String manifestFileName;
	protected Model model;
	protected OntModel oModel;
	
	protected static Model earlModelDM = null;
	protected static Model earlModelR2RML = null;
	
	protected static String toolName = null;
	
	protected static String nsBase = "http://mappingpedia.org/rdb2rdf/";
	protected static String doapURI = "http://usefulinc.com/ns/doap#";
	protected static String earlURI = "http://www.w3.org/ns/earl#";
	protected static String dcURI = "http://purl.org/dc/elements/1.1/";
	protected static String rdb2rdftestURI = "http://purl.org/NET/rdb2rdf-test#";

	
	protected static String tcNSBase = "http://www.w3.org/2001/sw/rdb2rdf/test-cases/#";
	
	protected static Resource myTH;
	protected static Resource myTool;
	
	protected static Resource softwareResource;
	protected static Resource projectResouce;
	protected static Resource dbmsResource;
	protected static Resource homepageResource;
	
	protected static Resource developerResource;
	protected static Resource mboxResource;
	
	protected static Resource earlPass;
	protected static Resource earlFail;
		
	
	
	protected String vocabularyFile = "rdb2rdf-test.ttl";
	
	protected String NS = "http://purl.org/NET/rdb2rdf-test#";
	
	protected static final String databaseClass = "DataBase";
	protected static final String r2rmlClass = "R2RML";
	protected static final String directMappingClass = "DirectMapping";
	protected static final String testSuiteClass = "TestSuite";
	
	protected String manifest = "manifest.ttl";
	
	protected String currentDir = "";
	
	
	public RDB2RDFTC(String vocabulary) {
		vocabularyFile = vocabulary;
		loadVocabulary();
	}
	
	public void loadVocabulary() {
		oModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, null);
		
		InputStream in = FileManager.get().open( vocabularyFile );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + vocabularyFile + " not found");
		}
		oModel.read(in, "", "TURTLE");
	}
	
	public String getManifestFileName() {
		return manifestFileName;
	}


	public void setManifestFileName(String manifestFileName) {
		int index = manifestFileName.lastIndexOf(File.separator);
		currentDir = manifestFileName.substring(0, index);
		this.manifestFileName = manifestFileName;
	}


	
	protected String getToolOutputFileName(String defaultOutputFileName,String toolName) {
		String toolOutputFileName = "";
		int index = defaultOutputFileName.lastIndexOf(".");
		toolOutputFileName = defaultOutputFileName.substring(0,index) + "-" + toolName + "." + defaultOutputFileName.substring(index+1);
		return toolOutputFileName;
	}
	
	protected void createAssertion(Model earlModel, boolean passed, String identifier) {
		Resource assertion = earlModel.createResource();
		Resource assertionClass = earlModel.createResource(earlURI + "Assertion");
		assertion.addProperty(RDF.type, assertionClass);
		
		Property assertedBy = earlModel.createProperty(earlURI + "assertedBy" );
		assertion.addProperty(assertedBy, myTH);
		Property subject = earlModel.createProperty(earlURI + "subject" );
		assertion.addProperty(subject, myTool);
		
		Resource resultResource = earlModel.createResource();
		Resource resultClass = earlModel.createResource(earlURI + "TestResult");
		resultResource.addProperty(RDF.type,resultClass );
		
		Property dateP = earlModel.createProperty(dcURI + "date" );
		
		Date date = new Date();
		DateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String dateString = dateFormat.format(date);
		resultResource.addLiteral(dateP,earlModel.createTypedLiteral(dateString,XSDDatatype.XSDdateTime));
		
		Property outcome = earlModel.createProperty(earlURI + "outcome" );
		
		if (passed)
			resultResource.addProperty(outcome,earlPass);			
		else
			resultResource.addProperty(outcome,earlFail);

		
		Property result = earlModel.createProperty(earlURI + "result" );
		assertion.addProperty(result, resultResource);

		Resource tc = earlModel.createResource(tcNSBase+identifier);
		
		Property earlTest = earlModel.createProperty(earlURI + "test" );
		assertion.addProperty(earlTest, tc);
		
		Property dbmsProp = earlModel.createProperty(rdb2rdftestURI + "dbms");
		assertion.addProperty(dbmsProp, dbmsResource);

	}
	
	protected void processDirectGraph(String toolName) {
		String defaultOutputFileName = "",toolOutputFileName = "";
		try {
		OntClass dmClass = oModel.getOntClass(NS + directMappingClass);
		ExtendedIterator instances = dmClass.listInstances();
		List dgList = new ArrayList();
		int i = 0;
		if (instances.hasNext()) {
	
			Individual ind = (Individual) instances.next();
			String property = "";
			OntProperty iProperty;
			RDFNode rdfNode; 

			property = NS + "output";
			iProperty = oModel.getOntProperty(property);
			rdfNode = ind.getPropertyValue(iProperty);
	
			defaultOutputFileName = rdfNode.toString();
			toolOutputFileName = getToolOutputFileName(defaultOutputFileName,toolName);
			
			//Compare the content of the two files
			Comparator comparator = new Comparator(currentDir + defaultOutputFileName, currentDir + toolOutputFileName);
	
			property = dcURI + "identifier";
			iProperty = oModel.getOntProperty(property);
			rdfNode = ind.getPropertyValue(iProperty);
			String id = rdfNode.toString();
			
			if (comparator.modelsAreEquivalent())
				createAssertion(earlModelDM,true,id);
			else
				createAssertion(earlModelDM,false,id);			
		}
	  }
	  catch (Exception ex) {
	    	System.out.println("Error while processing the file " + currentDir + toolOutputFileName);
	    	//System.exit(0);
	  }
		
	}

	protected void processR2RML(String toolName)  {
		String defaultOutputFileName = "",toolOutputFileName = "";
		try {
		OntClass r2rClass = oModel.getOntClass(NS + r2rmlClass);
		ExtendedIterator instances = r2rClass.listInstances();
		
		List r2rmlList = new ArrayList();
		int i = 0;
		while (instances.hasNext()) {
			Individual ind = (Individual) instances.next();
			String property = "";  
			OntProperty iProperty;
			RDFNode rdfNode; 
			property = NS +"hasExpectedOutput";
			iProperty = oModel.getOntProperty(property);
			rdfNode = ind.getPropertyValue(iProperty);
			
			if (rdfNode.toString().contains("true")) { //if there is expected output
				property = NS + "output";
				iProperty = oModel.getOntProperty(property);
				rdfNode = ind.getPropertyValue(iProperty);
				defaultOutputFileName = rdfNode.toString();
				toolOutputFileName = getToolOutputFileName(defaultOutputFileName,toolName);

				Comparator comparator = new Comparator(currentDir + defaultOutputFileName, currentDir + toolOutputFileName,Lang.NQUADS);
				property = dcURI + "identifier";
				iProperty = oModel.getOntProperty(property);
				rdfNode = ind.getPropertyValue(iProperty);
				String id = rdfNode.toString();
				
				if (comparator.datasetsAreEquivalent())
					createAssertion(earlModelR2RML,true,id);
				else
					createAssertion(earlModelR2RML,false,id);			
			}
			else { //if there is no expected output
				property = dcURI + "identifier";
				iProperty = oModel.getOntProperty(property);
				rdfNode = ind.getPropertyValue(iProperty);
				String id = rdfNode.toString();
				String fileName = currentDir+ "mapped" + id.charAt(id.length()-1) +"-" + toolName + ".nq";
				
				File f = new File(fileName);
				if (f.exists())			//if there is a file generated from the tool
					createAssertion(earlModelR2RML,false,id);
				else
					createAssertion(earlModelR2RML,true,id);
			}
		}
	  }
	  catch (Exception ex) {
	    	System.out.println("Error while processing the file " + currentDir + toolOutputFileName);
	    	//System.exit(0);
	  }
		
	}
	
	
	protected void processTCs(String toolName,boolean implementsDM, boolean implementsR2RML) throws Exception {
		this.toolName = toolName;
		if (implementsDM)
			processDirectGraph(toolName);
		if (implementsR2RML)
			processR2RML(toolName);
	}
	
	
	public String getPropertyValue(String file, String clazz, String predicate) {
		String value = "";
		try {
			 // create an empty model	
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
	
			// use the FileManager to find the input file
			InputStream in = FileManager.get().open( file );
			if (in == null) {
			    throw new IllegalArgumentException(
			                                 "File: " + manifestFileName + " not found");
			}
	
			model.read(in, "", "TURTLE");
	
			oModel.add(model);

			OntClass tsClass = oModel.getOntClass(clazz);
			ExtendedIterator instances = tsClass.listInstances();

			if (instances.hasNext()) {
				Individual ind = (Individual) instances.next();
				Property prop = oModel.getProperty(predicate);
				RDFNode rdfNode = ind.getPropertyValue(prop);
				
				if (rdfNode == null) {
					System.out.println("You should provide the DBMS information, e.g., <myProject> rdb2rdftest:dbms  r2rml:Oracle  " +
									   "\nPlease check http://www.w3.org/2001/sw/rdb2rdf/wiki/Submitting_Test_Results");
					System.exit(0);
				}
				if (rdfNode.isLiteral())
					value = rdfNode.asLiteral().getString();
				else
					value = rdfNode.asResource().getURI();
			}
		}
		catch (Exception ex) {
			//ex.printStackTrace();
			System.out.println("Error reading the file "+file +", class " + clazz + ", predicate "+ predicate);
			System.exit(0);
		}
					
		return value;
	}
	
	protected Model checkEARLModel(Model earlModel, String toolName, String dbms, String homepage, String programmingLanguage, String developer, String developerName, String developerEmail, boolean implementsDM, boolean implementsR2RML) {
		if (earlModel==null) {
			earlModel = ModelFactory.createDefaultModel();
			
			earlPass = earlModel.createResource(earlURI + "pass");
			earlFail = earlModel.createResource(earlURI + "fail");

			softwareResource = earlModel.createResource(earlURI+"Software");
			projectResouce = earlModel.createResource(doapURI+ "Project");
									
			myTool = earlModel.createResource(nsBase+"myProject"+"/"+toolName);
			myTool.addProperty(RDF.type, projectResouce);
			
			Property doapName = earlModel.createProperty(doapURI + "name" );
			myTool.addProperty(doapName, toolName);
			
			Property doapPLanguage = earlModel.createProperty(doapURI + "programming-language" );
			myTool.addProperty(doapPLanguage, programmingLanguage);


			developerResource = earlModel.createResource(developer);
			mboxResource = earlModel.createResource(developerEmail);
			
			Property doapDeveloper = earlModel.createProperty(NS + "developer" );
			myTool.addProperty(doapDeveloper,developerResource);
			
			Property foafName = earlModel.createProperty("http://xmlns.com/foaf/0.1/name");
			developerResource.addProperty(foafName, developerName);
			
			Property pmbox = earlModel.createProperty("http://xmlns.com/foaf/0.1/mbox");
			mboxResource = earlModel.createProperty(developerEmail);
			
			developerResource.addProperty(pmbox, mboxResource);
			
			
			Property implDM = earlModel.createProperty(NS + "implementsDirectMapping" );
			//myTool.addProperty(implDM, new Boolean(implementsDM).toString());			
			myTool.addLiteral(implDM,earlModel.createTypedLiteral(new Boolean(implementsDM).toString(),XSDDatatype.XSDboolean));
			Property implR2RML = earlModel.createProperty(NS + "implementsR2RML" );
			//myTool.addProperty(implR2RML, new Boolean(implementsR2RML).toString());
			myTool.addLiteral(implR2RML,earlModel.createTypedLiteral(new Boolean(implementsR2RML).toString(),XSDDatatype.XSDboolean));

			myTH = earlModel.createResource(nsBase+"myTestHarness");
			myTH.addProperty(RDF.type, softwareResource);
			
			Property doapHomePage = earlModel.createProperty(doapURI + "homepage");
			
			homepageResource = earlModel.createResource(homepage);
			myTool.addProperty(doapHomePage, homepageResource);
			
						
			dbmsResource = earlModel.createResource(dbms);
			
		}
		return earlModel;
		
	}

	public void processDescription(String dbPath, String toolName, String dbms, String homepage, String programmingLanguage, String developer, String developerName, String developerEmail, boolean implementsDM, boolean implementsR2RML) {
		try {
			currentDir =  dbPath +"/";
			// create an empty model	
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
	
			// use the FileManager to find the input file
			InputStream in = FileManager.get().open( manifestFileName );
			if (in == null) {
			    throw new IllegalArgumentException(
			                                 "File: " + manifestFileName + " not found");
			}
	
			model.read(in, "", "TTL");
	
			oModel.add(model);
			
			earlModelDM = checkEARLModel(earlModelDM,toolName, dbms, homepage, programmingLanguage, developer,developerName,developerEmail, implementsDM,  implementsR2RML);
			earlModelR2RML = checkEARLModel(earlModelR2RML,toolName, dbms, homepage, programmingLanguage, developer,developerName,developerEmail, implementsDM,  implementsR2RML);
			
			processTCs(toolName,implementsDM,implementsR2RML);
			
			oModel.remove(model);
			
		}
		catch (Exception ex) {
			//ex.printStackTrace();
			System.out.println("Error while processing the TS");
			System.exit(0);
		}
		
	}
	
	public static void saveEarlModel () {
		saveEarlModel(earlModelDM, toolName, "dm");
		saveEarlModel(earlModelR2RML, toolName, "r2rml");
	}
	
	protected static void saveEarlModel(Model earlModel, String tool, String implement) {
		try {
			  FileOutputStream fout=new FileOutputStream("earl-"+ tool +"-" + implement+ ".ttl");
			  earlModel.write(fout,"TTL");
		}
		catch(IOException e) {
			  System.out.println("Exception caught"+e.getMessage());
			  //System.exit(0);
		}
	}
	
}
