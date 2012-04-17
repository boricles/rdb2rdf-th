package org.rdb2rdf.testcase.th;

import java.io.InputStream;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.lib.DatasetLib;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.lib.SinkNull;
import org.openjena.atlas.lib.SinkWrapper;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.RiotReader;
import org.openjena.riot.RiotWriter;
import org.openjena.riot.lang.LangRIOT;
import org.openjena.riot.lang.SinkToGraph;
import org.openjena.riot.lang.SinkTriplesToGraph;

public class Comparator {
	
	protected Model sourceModel;
	protected Model targetModel;

	protected DatasetGraph sourceDataSet;
	protected DatasetGraph targetDataSet;
	
	protected String sourceFileName;
	protected String targetFileName;	
	
	public Comparator() {
	}
	
	public Comparator(String sourceFileName, String targetFileName) {
		this(sourceFileName,targetFileName,Lang.NTRIPLES);
	}
	

	public Comparator(String sourceFileName, String targetFileName, Lang lang) {
		this.sourceFileName = sourceFileName;
		this.targetFileName = targetFileName;
		if ( lang == Lang.NTRIPLES) {		
			sourceModel = loadModel(sourceFileName,sourceModel);
			targetModel = loadModel(targetFileName,targetModel);
		}
		else {
			sourceDataSet = loadDataSet(sourceFileName,sourceDataSet);
			targetDataSet = loadDataSet(targetFileName,targetDataSet);
		}
	}

	
	
	protected DatasetGraph loadDataSet(String fileName, DatasetGraph datasetGraph) {
		return RiotLoader.load(fileName, Lang.NQUADS);
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
			System.out.println("Error loading file " + fileName);
			//ex.printStackTrace();
			System.exit(0);
		}
		return model;
	}
	
	public boolean modelsAreEquivalent() {
		return sourceModel.isIsomorphicWith(targetModel);
	}
	
	public boolean datasetsAreEquivalent() {
		Iterator<Node> iterSource = sourceDataSet.listGraphNodes();
		Iterator<Node> iterTarget = targetDataSet.listGraphNodes();
		
		if (sourceDataSet.isEmpty() != targetDataSet.isEmpty() )   //if one is empty and the other does not -> they are different
			return false;
		
		if (sourceDataSet.isEmpty() && targetDataSet.isEmpty() ) { //load as models, because they don't have graphs
			sourceModel = loadModel(sourceFileName,sourceModel);
			targetModel = loadModel(targetFileName,targetModel);
			return modelsAreEquivalent();
		}
			
		//if they have graphs
		for (;iterSource.hasNext();) {
			Node node = iterSource.next();
			Graph sourceGraph = sourceDataSet.getGraph(node);
			Graph targetGraph = targetDataSet.getGraph(node);
			if (!sourceGraph.isIsomorphicWith(targetGraph))
				return false;
			
		}
		return true;
	}

}
