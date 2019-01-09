package org.estar.util;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;

import ngat.util.CommandParser;
import ngat.util.ConfigurationProperties;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.configuration.NodeAgentProperties;
import org.estar.rtml.RTMLContact;
import org.estar.rtml.RTMLCreate;
import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLException;
import org.estar.rtml.RTMLParser;
import org.estar.rtml.RTMLProject;
import org.estar.storage.PersistanceController;
import org.estar.storage.PersistentMap;

public class RTMLUtil {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public static final String RTML_IDENT_SOURCE_ESTAR = "estar";
	public static final String RTML_IDENT_SOURCE_NGAT   = "ngat";
	
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
	public static final String DOC_TYPE = "<!DOCTYPE RTML SYSTEM \"http://www.estar.org.uk/documents/rtml2.2.dtd\">";
	public static final String LAST_RESORT_RTML_PREFIX = "<RTML type=\"reject\" version=\"2.2\">";
	public static final String LAST_RESORT_RTML_POSTFIX = "</RTML> ";
	
	public static String getRTMLAsString(RTMLDocument document) throws Exception {
		
		RTMLCreate rtmlCreator = new RTMLCreate();
		rtmlCreator.create(document);
		return rtmlCreator.toXMLString();
	}
	
	/**
	 * use the project and user alias hash maps to look for alias entries
	 * if alias entries are found, rewrite the document.
	 * (currently rewrites project and/or user if either is aliased)
	 * @param rtmlDocument The document to rewrite (if alias entries exist for it's project name or user name)
	 * @param rtmlIdentitySource The name of the source for document project and usernames
	 * @return The re-written document (or the original document if no alias entry was found)
	 * @throws Exception
	 */
	public static RTMLDocument rewriteDocumentIfAliased(RTMLDocument rtmlDocument, String rtmlIdentitySource) throws Exception {
		 
		traceLogger.log(5, RTMLUtil.class.getName(),"... rewriteDocumentIfAliased(document) invoked");
		
		RTMLDocument rewrittenDocument = null;
		rewrittenDocument = (RTMLDocument)rtmlDocument.deepClone();
		
		String nodeAgentName = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_NAME);
		
		if (rewrittenDocument == null) {
			throw new Exception("Serverside error: failed to deep-clone document");
		}
		
		RTMLContact contact = rtmlDocument.getContact();
		RTMLProject project = rtmlDocument.getProject();
		if (contact == null) {
			throw new Exception("No Contact in received document");
		}
		
		if (project == null) {
			throw new Exception("No Project in received document");
		}
		
		if (contact.getUser() == null) {
			throw new Exception("No User in Contact in received document");
		}
		
		if (project.getProject() == null) {
			throw new Exception("No Project in Project in received document");
		}
		
		PersistentMap userAliasMapStore = PersistanceController.getInstance().getUserAliasMapStore();
		PersistentMap projectAliasMapStore = PersistanceController.getInstance().getProjectAliasMapStore();
		
		if (userAliasMapStore == null) {
			throw new Exception("Serverside error: user alias map store is null");
		}
		if (projectAliasMapStore == null) {
			throw new Exception("Serverside error: project alias map store is null");
		}
		
		String userAlias = null;
		String projectAlias = null;
		
		if (rtmlIdentitySource.equals(RTML_IDENT_SOURCE_ESTAR)) {
			if (userAliasMapStore.containsKey(contact.getUser())) {
				traceLogger.log(5, RTMLUtil.class.getName(),"... userAliasMapStore.containsKey(" + contact.getUser() + ") = true");
				userAlias = userAliasMapStore.getProperty(contact.getUser());
			} else {
				traceLogger.log(5, RTMLUtil.class.getName(),"... userAliasMapStore.containsKey(" + contact.getUser() + ") = false");
				traceLogger.log(5, RTMLUtil.class.getName(),userAliasMapStore.toString());
			}
			
			if (projectAliasMapStore.containsKey(project.getProject())) {
				traceLogger.log(5, RTMLUtil.class.getName(),"... projectAliasMapStore.containsKey(" + project.getProject() + ") = true");
				projectAlias = projectAliasMapStore.getProperty(project.getProject());
			} else {
				traceLogger.log(5, RTMLUtil.class.getName(),"... projectAliasMapStore.containsKey(" + project.getProject() + ") = false");
				traceLogger.log(5, RTMLUtil.class.getName(),projectAliasMapStore.toString());
			}
		} else if (rtmlIdentitySource.equals(RTML_IDENT_SOURCE_NGAT)) {
			if (userAliasMapStore.containsValue(contact.getUser())) {
				traceLogger.log(5, RTMLUtil.class.getName(),"... userAliasMapStore.containsValue(" + contact.getUser() + ") = true");
				userAlias = userAliasMapStore.getKey(contact.getUser());
			} else {
				traceLogger.log(5, RTMLUtil.class.getName(),"... userAliasMapStore.containsKey(" + contact.getUser() + ") = false");
				traceLogger.log(5, RTMLUtil.class.getName(),userAliasMapStore.toString());
			}
			
			if (projectAliasMapStore.containsValue(project.getProject())) {
				traceLogger.log(5, RTMLUtil.class.getName(),"... projectAliasMapStore.containsValue(" + project.getProject() + ") = true");
				projectAlias = projectAliasMapStore.getKey(project.getProject());
			} else {
				traceLogger.log(5, RTMLUtil.class.getName(),"... projectAliasMapStore.containsKey(" + project.getProject() + ") = false");
				traceLogger.log(5, RTMLUtil.class.getName(),projectAliasMapStore.toString());
			}
		} else {
			throw new Exception("Unknown RTML document identifiers source");
		}
		
		if (userAlias != null) {
			contact.setUser(userAlias);
			rewrittenDocument.setContact(contact);
			rewrittenDocument.addHistoryEntry(nodeAgentName, "urn:/node_agent", "rewritten contact to " + userAlias);
			traceLogger.log(5, RTMLUtil.class.getName(),"... rewritten contact as " + userAlias);
		}
		if (projectAlias != null) {
			project.setProject(projectAlias);
			rewrittenDocument.setProject(project);
			rewrittenDocument.addHistoryEntry(nodeAgentName, "urn:/node_agent", "rewritten project to " + projectAlias);
			traceLogger.log(5, RTMLUtil.class.getName(),"... rewritten project as " + projectAlias);
		}
		return rewrittenDocument;
	}
	
	
	/**
	 * attempts to create an RTML error document from the received rtmlDocumentString
	 * if it fails to do so, it creates an RTML error document from scratch
	 * if it fails to do that, it returns null
	 * @param exception
	 * @param nodeAgentName
	 * @param rtmlDocumentString
	 * @return
	 */
	public static RTMLDocument createErrorDocument(Exception exception, String nodeAgentName, String rtmlDocumentString) {
		
		traceLogger.log(5, RTMLUtil.class.getName(),"creating error document");
		RTMLDocument rtmlErrorDocument = null;
		try {
			RTMLParser parser = new RTMLParser();
			parser.init(true);
			
			//get the error document from the old document
			rtmlErrorDocument = parser.parse(rtmlDocumentString.trim());
			
			if (rtmlErrorDocument.isScoreRequest()) {
				rtmlErrorDocument.setScore(0.0);
			}
			
			rtmlErrorDocument.setReject();
			rtmlErrorDocument.setErrorString(exception.toString());
			
			rtmlErrorDocument.addHistoryError(nodeAgentName, "urn:/node_agent", exception.toString(), exception.getMessage());
			
			
			return rtmlErrorDocument;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			traceLogger.log(5, RTMLUtil.class.getName(),"failed to create error document from supplied document");
			e.printStackTrace();
		} 
		
		//not managed to make an error document yet, make the error document from scratch
		traceLogger.log(5, RTMLUtil.class.getName(),"trying to create error document from scratch");
		rtmlErrorDocument = new RTMLDocument();
		rtmlErrorDocument.setVersion(RTMLDocument.RTML_VERSION_22);
		rtmlErrorDocument.setReject();
		
		try {
			rtmlErrorDocument.setErrorString(exception.toString() + " : badly formatted rtml received");
			rtmlErrorDocument.addHistoryError(nodeAgentName, "urn:/node_agent", exception.toString(), "badly formatted rtml received");
			return rtmlErrorDocument;
		} catch (Exception e) {
			errorLogger.log(5, RTMLUtil.class.getName(), "failed to create error document from scratch!!");
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getLastResortErrorDocumentString(String errorMessage) {
		return 	XML_HEADER + 
					DOC_TYPE + 
					LAST_RESORT_RTML_PREFIX + 
					errorMessage + 
					LAST_RESORT_RTML_POSTFIX;
	}
	
	public static void testParseRTMLFile(String rtmlFilePath) {
		
		String rtmlDocumentString;
		try {
			if (!(new File(rtmlFilePath).exists())) {
				throw new RuntimeException("RTML file: " + rtmlFilePath + " does not exist");
			}
			rtmlDocumentString = FileUtil.getFileAsString(rtmlFilePath);
			RTMLParser parser = new RTMLParser();
			parser.init(true);
			RTMLDocument rtmlDocument = parser.parse(rtmlDocumentString.trim());
			
			System.out.println("Successfully parsed"); 
			System.out.println(rtmlDocument);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (RTMLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			System.out.println("Parse failed"); 
			e.printStackTrace();
		}
	}
	
	private static void showUsage() {
		System.out.println(RTMLUtil.class.getName() + " -rtml <rtml_file_path>");
	}
	
	public static void main (String args[]) {
	
		// Extract command line parameters.
		CommandParser cp = new CommandParser();
		try {
			cp.parse(args);
		} catch (ParseException px) {	    
			showUsage();
		}
		
		ConfigurationProperties config = cp.getMap();
		
		String rtmlFilePath = config.getProperty("rtml");
		testParseRTMLFile(rtmlFilePath);
	}
	
	/*
	public static void main (String a[]) {
		PersistentMap userAliasMapStore = PersistanceController.getInstance().getUserAliasMapStore();
		try {
			String rtmlDocumentString = FileUtil.getFileAsString("/home/nrc/workspace/estar_node_agent/example_rtml/rtml_ngat_request");
			RTMLParser parser = new RTMLParser();
			parser.init(true);
			RTMLDocument rtmlDocument = parser.parse(rtmlDocumentString.trim());
			rtmlDocument = RTMLUtil.rewriteDocumentIfAliased(rtmlDocument, RTMLUtil.RTML_IDENT_SOURCE_NGAT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RTMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
