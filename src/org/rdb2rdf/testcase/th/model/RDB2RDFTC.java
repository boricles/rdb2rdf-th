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
	
	protected static Model earlModel = null;
	protected static String nsBase = "http://mappingpedia.org/rdb2rdf/";
	protected static String doapURI = "http://usefulinc.com/ns/doap#";
	protected static String earlURI = "http://www.w3.org/ns/earl#";
	protected static String dcURI = "http://purl.org/dc/elements/1.1/";
	
	protected static String tcNSBase = "http://www.w3.org/2001/sw/rdb2rdf/test-cases/#";
	
	protected static Resource myTH;
	protected static Resource myTool;
	
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
	
	protected void createAssertion(boolean passed, String identifier) {
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
		DateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = dateFormat.format(date);
		resultResource.addLiteral(dateP,earlModel.createTypedLiteral(dateString,XSDDatatype.XSDdate));
		
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

	}
	
	protected void processDirectGraph(String toolName) throws Exception {
		OntClass dmClass = oModel.getOntClass(NS + directMappingClass);
		ExtendedIterator instances = dmClass.listInstances();
		List dgList = new ArrayList();
		int i = 0;
		if (instances.hasNext()) {
	
			Individual ind = (Individual) instances.next();
			String property = "", defaultOutputFileName = "", toolOutputFileName = "";
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
				createAssertion(true,id);
			else
				createAssertion(false,id);			
		}
	}

	protected void processR2RML(String toolName) throws Exception {
		OntClass r2rClass = oModel.getOntClass(NS + r2rmlClass);
		ExtendedIterator instances = r2rClass.listInstances();
		
		List r2rmlList = new ArrayList();
		int i = 0;
		while (instances.hasNext()) {
			Individual ind = (Individual) instances.next();
			String property = "", defaultOutputFileName = "", toolOutputFileName = "";
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
				//System.out.println(defaultOutputFileName + "\t" + toolOutputFileName);
				//Compare the content of the two files
			}
			else { //if there is no expected output
				
			}

			

		}
		
	}
	
	
	protected void processTCs(String toolName,boolean implementsDM, boolean implementsR2RML) throws Exception {
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
	
			model.read(in, "", "TTL");
	
			oModel.add(model);

			OntClass tsClass = oModel.getOntClass(clazz);
			ExtendedIterator instances = tsClass.listInstances();

			if (instances.hasNext()) {
				Individual ind = (Individual) instances.next();
				Property prop = oModel.getProperty(predicate);
				RDFNode rdfNode = ind.getPropertyValue(prop);
				value = rdfNode.asLiteral().getString();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
					
		return value;
	}
	
	protected void checkEARLModel(String toolName,boolean implementsDM, boolean implementsR2RML) {
		if (earlModel==null) {
			earlModel = ModelFactory.createDefaultModel();
			
			earlPass = earlModel.createResource(earlURI + "pass");
			earlFail = earlModel.createResource(earlURI + "fail");

			
			myTool = earlModel.createResource(nsBase+"myProject"+"/"+toolName);
			myTool.addProperty(RDF.type, doapURI + "Project");
			
			Property doapName = earlModel.createProperty(doapURI + "name" );
			myTool.addProperty(doapName, toolName);
			
			Property implDM = earlModel.createProperty(NS + "implementsDirectMapping" );
			myTool.addProperty(implDM, new Boolean(implementsDM).toString());

			Property implR2RML = earlModel.createProperty(NS + "implementsR2RML" );
			myTool.addProperty(implR2RML, new Boolean(implementsR2RML).toString());
			
			myTH = earlModel.createResource(nsBase+"myTestHarness");
			myTH.addProperty(RDF.type, earlURI + "Software");
			
		}
		
	}

	public void processDescription(String dbPath, String toolName, boolean implementsDM, boolean implementsR2RML) {
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
			
			checkEARLModel(toolName, implementsDM,  implementsR2RML);
			
			processTCs(toolName,implementsDM,implementsR2RML);
			
			oModel.remove(model);
			
		}
		catch (Exception ex) {
			System.out.println("Error while processing the TS");
			System.exit(0);
		}
		
	}
	
	public static void saveEarlModel() {
		try {
			  FileOutputStream fout=new FileOutputStream("earl.ttl");
			  earlModel.write(fout,"TTL");
		}
		catch(IOException e) {
			  System.out.println("Exception caught"+e.getMessage());
		}
	}
	
}
