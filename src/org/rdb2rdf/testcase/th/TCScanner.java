package org.rdb2rdf.testcase.th;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.rdb2rdf.testcase.th.model.RDB2RDFTC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TCScanner {
	
	protected String tcFolder;
	
	protected String toolName;
	
	protected boolean implementsDM = false;
	
	protected boolean implementsR2RML = false;
	
	protected String manifestFile = "manifest.ttl";
	
	protected String dbFolderStartsWith = "D";	
	
	protected String tsManifestFile;

	protected static Options options;

    private static final Logger logger = LoggerFactory.getLogger(TCScanner.class);
    
    //protected String vocabularyFile = "rdb2rdf-test.ttl";
    protected String vocabularyPath = "model/rdb2rdf-test.ttl";
    
    
	
	public TCScanner() {

		
	}
	
	public void setTSManifestFile(String tsManifest) {
		this.tsManifestFile = tsManifest;
	}
	
	public String getTCFolder() {
		return tcFolder;
	}

	public void setTCFolder(String tcFolder) {
		this.tcFolder = tcFolder;
	}
	

	
	FilenameFilter manifestFileFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			String lowercaseName = name.toLowerCase();
			if (lowercaseName.contains("manifest.ttl")) {
				return true;
			} else {
				return false;
			}
		}
	};
	
	protected void processManifestFile(String dbPath, File manifestFile) {
		RDB2RDFTC rdb2rdfTC = new RDB2RDFTC(vocabularyPath); 
		rdb2rdfTC.setManifestFileName(manifestFile.getAbsolutePath());
		rdb2rdfTC.processDescription(dbPath,toolName,implementsDM,implementsR2RML);
	}
	
	protected void obtainBasicInfo() {
		
		RDB2RDFTC rdb2rdfTS = new RDB2RDFTC(vocabularyPath); 
		this.tcFolder = rdb2rdfTS.getPropertyValue(tsManifestFile,"http://purl.org/NET/rdb2rdf-test#TestSuite","http://purl.org/NET/rdb2rdf-test#workingDirectory");

		this.toolName = rdb2rdfTS.getPropertyValue(tsManifestFile,"http://usefulinc.com/ns/doap#Project","http://usefulinc.com/ns/doap#name");

		this.implementsDM = rdb2rdfTS.getPropertyValue(tsManifestFile,"http://usefulinc.com/ns/doap#Project","http://purl.org/NET/rdb2rdf-test#implementsDirectMapping").equalsIgnoreCase("true");
		
		this.implementsR2RML = rdb2rdfTS.getPropertyValue(tsManifestFile,"http://usefulinc.com/ns/doap#Project","http://purl.org/NET/rdb2rdf-test#implementsR2RML").equalsIgnoreCase("true");

	}
	
	
	protected void scanDetails() {
		DateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
		Date date = new Date();
		String dateString = dateFormat.format(date);

		dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
		dateString = dateFormat.format(date);

		dateFormat = new SimpleDateFormat("yyyy");
		dateString = dateFormat.format(date);



	}
	
	
	public void scan() {
		try {
			
			scanDetails();
			
			obtainBasicInfo();
			
			File folder = new File(tcFolder);
			File[] tcSubFolders = folder.listFiles();
			
			for (int i = 0; i < tcSubFolders.length; i++) {
			     if (tcSubFolders[i].isDirectory()) {
			         String databaseName = tcSubFolders[i].getName();
			         if (databaseName.startsWith(dbFolderStartsWith)) {
			        	 File [] mF = tcSubFolders[i].listFiles(manifestFileFilter);
			        	 if (mF != null || mF.length!=0)
			        		 processManifestFile(tcFolder + "/" + databaseName,mF[0]);
			         }
			     }
			}
			
	    } catch (Exception e) {
	    	System.out.println("Error while processing the directory " + tcFolder);
	    	System.exit(0);
	    	//e.printStackTrace();
	    }
		
		RDB2RDFTC.saveEarlModel();
	}	
	
	
	

	/**
	 * Shows the help to the console 
	 */	
    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rdb2rdf-th tsManifestFile", options, true);
    }	
	
	/**
	 * Create the options for the command line 
	 */	
	public static void createOptions() {
		options = new Options();
        options.addOption(new Option("h","help",true,"show help"));
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			createOptions();
			CommandLineParser parser = new PosixParser();
		    CommandLine cmd = null;
	        try {
	            cmd = parser.parse(options, args);
	        } catch (ParseException e) {
	            System.err.println(e.getMessage());
	            logger.debug("Exception ",e);
	            System.exit(-1);
	        }
			
	        if (cmd.hasOption("h")) {
	            printHelp();
	            System.exit(0);
	        }
	        if (cmd.getArgs().length != 1) {
	            printHelp();
	            logger.debug("Wrong number of parameters");
	            System.exit(-1);
	        }
	        
			TCScanner tcs = new TCScanner();

			tcs.setTSManifestFile(cmd.getArgs()[0]);
			
			//tcs.setVocabulary(cmd.getArgs()[1]);
			
			
			tcs.scan();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
