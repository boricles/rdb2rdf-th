package org.rdb2rdf.testcase.th;

import java.io.InputStream;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class Comparator {
	
	protected Model sourceModel;
	protected Model targetModel;
	
	public Comparator() {
		
	}
	
	public Comparator(String sourceFileName, String targetFileName) {
		sourceModel = loadModel(sourceFileName,sourceModel);
		targetModel = loadModel(targetFileName,targetModel);
	}
	
	protected Model loadModel(String fileName, Model model) {
		try {
			 // create an empty model	
			model = ModelFactory.createDefaultModel();
	
			// use the FileManager to find the input file
			InputStream in = FileManager.get().open( fileName );
			if (in == null) {
			    throw new IllegalArgumentException(
			                                 "File: " + fileName + " not found");
			}
	
			model.read(in, "", "TTL");
			}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return model;
	}
	
	public boolean modelsAreEquivalent() {
		return sourceModel.isIsomorphicWith(targetModel);
	}

}
