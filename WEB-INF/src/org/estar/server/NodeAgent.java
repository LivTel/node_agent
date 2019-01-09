package org.estar.server;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.configuration.NodeAgentProperties;
import org.estar.messaging.RMITeaConnectionHandler;
import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLParser;
import org.estar.tea.TelescopeAvailability;
import org.estar.util.LoggerUtil;
import org.estar.util.RTMLUtil;

/*
 * uses AXIS 1.4 and TOMCAT 5.5
 */
public class NodeAgent {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	
	/*******************************************************/
	private static final String VERSION = "1.2.0";
	/*******************************************************/
	
	
	//the constructor is run every time a method call is made on a NodeAgent
	public NodeAgent() { }
	
	//exposed interface methods **********
	
	public String ping() { 
		traceLogger.log(5, NodeAgent.class.getName(), ".ping() invoked");
		boolean isLive;
		String returnString;
		
		isLive = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if (isLive) {
			returnString = "ACK";
		} else {
			returnString = "NAK (not live)";
		}
		
		traceLogger.log(5, NodeAgent.class.getName(), "... returned '" + returnString + "'");
		
		return returnString;
	}
	
	public String getVersion() {
		return VERSION;
	}
	
	public String echo(String message) {
		return message;
	}
	
	/**
	 * 
	 * @param rtmlDocumentString
	 * @param waitTimeString NB - a String, for SOAP transport simplicity
	 * @return
	 */
	public String testUpdateCallback(String rtmlDocumentString, String waitTimeString) {
		traceLogger.log(5, NodeAgent.class.getName(), "testUpdateCallback(" +rtmlDocumentString + "," + waitTimeString + ") invoked");
		
		boolean isTeaConnected;
		String loggerMessage;
		
		long waitTime = Long.parseLong(waitTimeString);
		
		//parse the String to extract the RTMLDocument
		RTMLDocument rtmlDocument = null;
		try {
			RTMLParser parser = new RTMLParser();
			parser.init(true);
			rtmlDocument = parser.parse(rtmlDocumentString.trim());
		} catch (Exception e1) {
			e1.printStackTrace();
			String message = "unable to parse received RTML document, got exception: " + e1.getMessage();
			
			//create an RTML error document from scratch and return it
			
			errorLogger.log(1, NodeAgent.class.getName(), "... " + message);
			
			return message;
		}
		
		isTeaConnected = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		
		if (isTeaConnected) {
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = true, sending rtml onwards";
			traceLogger.log(5, NodeAgent.class.getName(), "... " +loggerMessage);
			try {
				//send the document onwards and get the response
				traceLogger.log(5, NodeAgent.class.getName(), "... ... sending document onwards to TEA (and awaiting response)");
				
				//******************* SEND TO TEA **********************
				RMITeaConnectionHandler.getInstance().testUpdateCallback(rtmlDocument, waitTime);
				// / ******************* SEND TO TEA ********************
				
				String msg = "testUpdateCallback(" + rtmlDocument + ", " + waitTime + ") invoked";
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			} 
		} else {
			String msg = NodeAgentProperties.IS_TEA_CONNECTED  + " = false, not invoking testUpdateCallback";
			return msg;
		}
	}
	
	public void setAvailabilityPrediction(long startTime, long endTime, double prediction) {
		traceLogger.log(5, NodeAgent.class.getName(), "setAvailabilityPrediction(" + startTime + "," + endTime  + "," + prediction +" )  +  invoked");
		try {
			RMITeaConnectionHandler.getInstance().setAvailabilityPrediction(startTime, endTime, prediction);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public TelescopeAvailability getAvailabilityPrediction(long startTime, long endTime) {
		traceLogger.log(5, NodeAgent.class.getName(), "getAvailability(" + startTime + "," + endTime  + ") invoked");
		TelescopeAvailability availability;
		try {
			availability = RMITeaConnectionHandler.getInstance().getAvailabilityPrediction();
			return availability;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	/***********************************************************************************************************************/
	
	public String handle_rtml(String rtmlDocumentString) {
		traceLogger.log(5, NodeAgent.class.getName(), "invoked handle_rtml: " +rtmlDocumentString );
		traceLogger.log(5, NodeAgent.class.getName(), "initialising RTMLDocument" );
		RTMLDocument rtmlDocument = null;
		traceLogger.log(5, NodeAgent.class.getName(), "... initialised");
		try {
			rtmlDocument = handle_rtml_document(rtmlDocumentString);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			String rtmlReturnString = RTMLUtil.getRTMLAsString(rtmlDocument);
			traceLogger.log(5, NodeAgent.class.getName(), "... ... returning SYNCHRONOUS response to IA");
			traceLogger.log(5, NodeAgent.class.getName(), "payload= " + rtmlReturnString);
			return rtmlReturnString;
			
		} catch (Exception e) {
			e.printStackTrace();
			return RTMLUtil.getLastResortErrorDocumentString(e.toString());
		}
	}
	/***********************************************************************************************************************/
	
	//	/ exposed interface methods **********
	
	
	private void logRTMLDocument(RTMLDocument errorDocument) {
		String rtmlString = "";
		try {
			rtmlString = RTMLUtil.getRTMLAsString(errorDocument);
		} catch (Exception e) {
			traceLogger.log(5, NodeAgent.class.getName(), "failed to log String version of RTMLDocument!");
			e.printStackTrace();
		}
		errorLogger.log(1, NodeAgent.class.getName(), rtmlString);
	}
	
	private RTMLDocument handle_rtml_document(String rtmlDocumentString) {
		
		traceLogger.log(5, NodeAgent.class.getName(), "handle_rtml_document(String) invoked");
		traceLogger.log(5, NodeAgent.class.getName(), "looking up node_agent name");
		String nodeAgentName = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_NAME);
		traceLogger.log(5, NodeAgent.class.getName(), "... found, name is :" +nodeAgentName );
		
		boolean isTeaConnected;
		String loggerMessage;
		
		//parse the String to extract the RTMLDocument
		RTMLDocument rtmlDocument = null;
		try {
			traceLogger.log(5, NodeAgent.class.getName(), "... testing errorLogger, the word 'TESTED' should follow this line");
			errorLogger.log(1, NodeAgent.class.getName(), "... 'TESTED'");
			traceLogger.log(5, NodeAgent.class.getName(), "... instantiating Parser");
			RTMLParser parser = new RTMLParser();
			traceLogger.log(5, NodeAgent.class.getName(), "... successful");
			traceLogger.log(5, NodeAgent.class.getName(), "... initialising Parser");
			parser.init(true);
			traceLogger.log(5, NodeAgent.class.getName(), "... successful");
			traceLogger.log(5, NodeAgent.class.getName(), "... parsing document String");
			rtmlDocument = parser.parse(rtmlDocumentString.trim());
			traceLogger.log(5, NodeAgent.class.getName(), "... parse successful");
		} catch (Exception e) {
			e.printStackTrace();
			//create an RTML error document from scratch and return it
			errorLogger.log(1, NodeAgent.class.getName(), "... unable to parse received RTML document, returning RTML error document to client:");
			
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
		
		isTeaConnected = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if(!isTeaConnected) {
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = false, not sending rtml onwards";
			traceLogger.log(5, NodeAgent.class.getName(), "... " +loggerMessage);
			traceLogger.log(5, NodeAgent.class.getName(), "... returning RTML error document");
			Exception e = new Exception(loggerMessage);
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		} else {
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = true, sending rtml onwards";
			traceLogger.log(5, NodeAgent.class.getName(), "... " +loggerMessage);
		}
		
		//rewrite the estar project and user alias's to ngat project and user ID's
		try {
			traceLogger.log(5, NodeAgent.class.getName(), "... rewriting document if aliased");
			rtmlDocument = RTMLUtil.rewriteDocumentIfAliased(rtmlDocument, RTMLUtil.RTML_IDENT_SOURCE_ESTAR);
			traceLogger.log(5, NodeAgent.class.getName(), "... completed rewrite");
		} catch (Exception e) {
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return errorDocument;
		}
		
		try {
			//send the document onwards and get the response
			traceLogger.log(5, NodeAgent.class.getName(), "... ... sending document onwards to TEA (and awaiting response)");
			
			//******************* SEND TO TEA **********************
			RTMLDocument teaRTMLResponse;
			
			if (rtmlDocument.isScoreRequest()) {
				traceLogger.log(5, NodeAgent.class.getName(), "... document type is score request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleScore(rtmlDocument);
			} else if (rtmlDocument.isRequest()) {
				traceLogger.log(5, NodeAgent.class.getName(), "... document type is request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleRequest(rtmlDocument);
			} else {
				throw new Exception("unknown document request type");
			}

			// / ******************* SEND TO TEA ********************
			
			//return the rtml document
			return teaRTMLResponse;
		} catch (Exception e) {
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
	}
}
