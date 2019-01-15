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

/**
 * Top level entry class for exposed web-service methods:
 * <ul>
 * <li>public String ping()
 * <li>public String handle_rtml(String rtmlDocumentString)
 * </ul>
 * And perhaps others!
 * Uses AXIS 1.4 and TOMCAT 5.5.
 */
public class NodeAgent {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	
	/*******************************************************/
	/**
	 * A string containing the version returned by the version method.
	 */
	private static final String VERSION = "1.2.0";
	/*******************************************************/
	
	
	//the constructor is run every time a method call is made on a NodeAgent
	public NodeAgent() { }
	
	//exposed interface methods **********
	/**
	 * ping web-service method entry point. 
	 * @return Returns a String, "ACK" if the NodeAgent is connected to the TEA,
	 *         and "NAK (not live)" if the connection to the TEA is not live.
	 * @see #traceLogger
	 * @see org.estar.configuration.NodeAgentProperties
	 * @see org.estar.configuration.NodeAgentProperties#IS_TEA_CONNECTED
	 * @see org.estar.configuration.NodeAgentProperties#TRUE
	 */
	public String ping() 
	{
		traceLogger.log(5, NodeAgent.class.getName(), ".ping() invoked");
		boolean isLive;
		String returnString;
		
		isLive = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if (isLive) 
		{
			returnString = "ACK";
		} 
		else 
		{
			returnString = "NAK (not live)";
		}
		traceLogger.log(5, NodeAgent.class.getName(), "... returned '" + returnString + "'");
		return returnString;
	}

	/**
	 * Version entry point?
	 * @return A string, the contents of the VERSION string.
	 * @see #VERSION
	 */
	public String getVersion() 
	{
		return VERSION;
	}

	/**
	 * Echo method, just returns the contents of the argument.
	 * @param message The message to return.
	 * @return The contents of the message parameter.
	 */
	public String echo(String message) 
	{
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
	/**
	 * Method to handle the handle_rtml web-service entry-point.
	 * @param rtmlDocumentString The RTML document to process, as a string.
	 * @return A string representation of the reply RTML document.
	 * @see #traceLogger
	 * @see #handle_rtml_document
	 * @see org.estar.rtml.RTMLDocument
	 * @see org.estar.util.RTMLUtil
	 * @see org.estar.util.RTMLUtil#getRTMLAsString
	 * @see org.estar.util.RTMLUtil#getLastResortErrorDocumentString
	 */
	public String handle_rtml(String rtmlDocumentString) 
	{
		traceLogger.log(5, NodeAgent.class.getName(), "invoked handle_rtml: " +rtmlDocumentString );
		traceLogger.log(5, NodeAgent.class.getName(), "initialising RTMLDocument" );
		RTMLDocument rtmlDocument = null;
		traceLogger.log(5, NodeAgent.class.getName(), "... initialised");
		try 
		{
			rtmlDocument = handle_rtml_document(rtmlDocumentString);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// turn returned RTMLDocument into a string
		try 
		{
			String rtmlReturnString = RTMLUtil.getRTMLAsString(rtmlDocument);
			traceLogger.log(5, NodeAgent.class.getName(), "... ... returning SYNCHRONOUS response to IA");
			traceLogger.log(5, NodeAgent.class.getName(), "payload= " + rtmlReturnString);
			return rtmlReturnString;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return RTMLUtil.getLastResortErrorDocumentString(e.toString());
		}
	}
	/***********************************************************************************************************************/
	
	//	/ exposed interface methods **********
	
	/**
	 * Method to log and RTML document to the errorLogger.
	 * @param errorDocument The error document to log.
	 * @see org.estar.util.RTMLUtil#getRTMLAsString
	 * @see #traceLogger
	 * @see #errorLogger
	 */
	private void logRTMLDocument(RTMLDocument errorDocument) 
	{
		String rtmlString = "";

		try 
		{
			rtmlString = RTMLUtil.getRTMLAsString(errorDocument);
		} 
		catch (Exception e) 
		{
			traceLogger.log(5, NodeAgent.class.getName(), "failed to log String version of RTMLDocument!");
			e.printStackTrace();
		}
		errorLogger.log(1, NodeAgent.class.getName(), rtmlString);
	}

	/**
	 * Method to process the RTML document.
	 * <ul>
	 * <li>A parser is created, initialised, and the input rtmlDocumentString parsed. If an error occurs
	 *     an error document is returned.
	 * <li>If the TEA is not connected, the document cannot be sent to it, so an error document is returned.
	 * <li>We rewrite the eSTAR project and user alias's to LT project and user ID's, using rewriteDocumentIfAliased.
	 *     If an error occurs an error document is returned.
	 * <li>If the document is a score request(isScoreRequest), we call the TEA's RMI method handleScore.
	 * <li>If the document is a request document (isRequest), we call the TEA's RMI method handleRequest.
	 * <li>If the document is an abort document (isAbort), we call the TEA's RMI method handleAbort.
	 * <li>If the document was not one of the above three types, we throw an exception.
	 * </ul>
	 * @param rtmlDocumentString A string represntation of the document to process.
	 * @return An instance of RTMLDocument containing the document object model of the 
	 *         reply document after processing.
	 * @see #traceLogger
	 * @see #errorLogger
	 * @see #logRTMLDocument
	 * @see org.estar.util.RTMLUtil#createErrorDocument
	 * @see org.estar.util.RTMLUtil#rewriteDocumentIfAliased
	 * @see org.estar.configuration.NodeAgentProperties#getInstance
	 * @see org.estar.configuration.NodeAgentProperties#getProperty
	 * @see org.estar.configuration.NodeAgentProperties#NODE_AGENT_NAME
	 * @see org.estar.messaging.RMITeaConnectionHandler
	 * @see org.estar.messaging.RMITeaConnectionHandler#getInstance
	 * @see org.estar.messaging.RMITeaConnectionHandler#handleScore
	 * @see org.estar.messaging.RMITeaConnectionHandler#handleRequest
	 * @see org.estar.messaging.RMITeaConnectionHandler#handleAbort
	 * @see org.estar.rtml.RTMLDocument
	 * @see org.estar.rtml.RTMLDocument#isScoreRequest
	 * @see org.estar.rtml.RTMLDocument#isRequest
	 * @see org.estar.rtml.RTMLDocument#isAbort
	 * @see org.estar.rtml.RTMLParser
	 * @see org.estar.rtml.RTMLParser#init
	 * @see org.estar.rtml.RTMLParser#parse
	 */
	private RTMLDocument handle_rtml_document(String rtmlDocumentString) 
	{
		traceLogger.log(5, NodeAgent.class.getName(), "handle_rtml_document(String) invoked");
		traceLogger.log(5, NodeAgent.class.getName(), "looking up node_agent name");
		String nodeAgentName = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_NAME);
		traceLogger.log(5, NodeAgent.class.getName(), "... found, name is :" +nodeAgentName );
		
		boolean isTeaConnected;
		String loggerMessage;
		
		//parse the String to extract the RTMLDocument
		RTMLDocument rtmlDocument = null;
		try 
		{
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
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			//create an RTML error document from scratch and return it
			errorLogger.log(1, NodeAgent.class.getName(), "... unable to parse received RTML document, returning RTML error document to client:");

			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
		// if the TEA is not connected log and return an error document to the client
		isTeaConnected = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE);
		if(!isTeaConnected) 
		{
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = false, not sending rtml onwards";
			traceLogger.log(5, NodeAgent.class.getName(), "... " +loggerMessage);
			traceLogger.log(5, NodeAgent.class.getName(), "... returning RTML error document");
			Exception e = new Exception(loggerMessage);
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		} 
		else 
		{
			loggerMessage = NodeAgentProperties.IS_TEA_CONNECTED  + " = true, sending rtml onwards";
			traceLogger.log(5, NodeAgent.class.getName(), "... " +loggerMessage);
		}
		
		//rewrite the estar project and user alias's to ngat project and user ID's
		try
		{
			traceLogger.log(5, NodeAgent.class.getName(), "... rewriting document if aliased");
			rtmlDocument = RTMLUtil.rewriteDocumentIfAliased(rtmlDocument, RTMLUtil.RTML_IDENT_SOURCE_ESTAR);
			traceLogger.log(5, NodeAgent.class.getName(), "... completed rewrite");
		} 
		catch (Exception e)
		{
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return errorDocument;
		}
		
		try 
		{
			//send the document onwards and get the response
			traceLogger.log(5, NodeAgent.class.getName(), "... ... sending document onwards to TEA (and awaiting response)");
			
			//******************* SEND TO TEA **********************
			RTMLDocument teaRTMLResponse;
			
			if (rtmlDocument.isScoreRequest()) 
			{
				traceLogger.log(5, NodeAgent.class.getName(), "... document type is score request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleScore(rtmlDocument);
			}
			else if (rtmlDocument.isRequest())
			{
				traceLogger.log(5, NodeAgent.class.getName(), "... document type is request");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleRequest(rtmlDocument);
			}
			else if (rtmlDocument.isAbort())
			{
				traceLogger.log(5, NodeAgent.class.getName(), "... document type is abort");
				teaRTMLResponse = RMITeaConnectionHandler.getInstance().handleAbort(rtmlDocument);
			}
			else
			{
				throw new Exception("unknown document request type");
			}

			// / ******************* SEND TO TEA ********************
			
			//return the rtml document
			return teaRTMLResponse;
		} 
		catch (Exception e) 
		{
			RTMLDocument errorDocument = RTMLUtil.createErrorDocument(e, nodeAgentName, rtmlDocumentString);
			logRTMLDocument(errorDocument);
			return  errorDocument;
		}
	}
}
