package org.estar.messaging;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.Fault;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.estar.rtml.RTMLDocument;
import org.estar.rtml.RTMLIntelligentAgent;
import org.estar.rtml.RTMLParser;
import org.estar.storage.PersistanceController;
import org.estar.storage.PersistentMap;
import org.estar.util.CookieUtils;
import org.estar.util.LoggerUtil;
import org.estar.util.RTMLUtil;

/**
 * This class services RTML updates back to Intelligent Agents.
 * It receives messages from the DefaultNodeAgentAsynchronousResponseHandler.handleAsyncResponse(rtmlDocument)
 * method and from the RTML received, it finds which IA (from the Hash lookup table) the rtml is destined for
 * and makes a SOAP call on that IA with the RTML.
 * If the RTML is a 'completed' document and the send to the IA went okay, 
 *   then the entry for this RTML document is wiped from the Hash table. 
 */
public class IAAsynchronousMessagingClient {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public IAAsynchronousMessagingClient() {}
	
	/**
	 * 
	 * @param rtmlDocument
	 * @throws Exception
	 */
	public void sendRTMLUpdateToIA(RTMLDocument rtmlDocument) throws Exception {
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "sendRTMLUpdateToIA()");
	
		//get the address of the agent to send the document to
		RTMLIntelligentAgent returnAgent =rtmlDocument.getIntelligentAgent();
	
		//unmarshal a String version of the document
		String rtmlDocumentString = RTMLUtil.getRTMLAsString(rtmlDocument);
		
		//get the username for the call from the RTML
		String username = rtmlDocument.getContact().getUser();
		
		//lookup the password for the user from the passwordMapStore
		PersistentMap passwordMapStore = PersistanceController.getInstance().getPasswordMapStore();
		String password = passwordMapStore.getProperty(username);
		
		//build the Cookie header value from the user/password details
		String cookieAuthString = CookieUtils.makeAuthCookieString(username, password); 
		
		//send rtmlDocumentString to host and port of the returnAgent
		if (rtmlDocument.getVersion().equals(RTMLDocument.RTML_VERSION_22)) {
			String host = returnAgent.getHostname();
			int port = returnAgent.getPort();
			traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... host=" + host + ", port=" + port + ", username=" + username);
			sendRTMLUpdateToIA(cookieAuthString, rtmlDocumentString, host, port);
		} else {
			String returnURL = returnAgent.getUri();
			sendRTMLUpdateToIA(cookieAuthString, rtmlDocumentString, new URL(returnURL));
		}
	}
	
	/**
	 * 
	 * @param rtmlDocumentString
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	private void sendRTMLUpdateToIA(String cookieAuthString, String rtmlDocumentString, String host, int port) throws Exception {
		URL destinationURL = new URL("http://" +host +":" +port);
		sendRTMLUpdateToIA(cookieAuthString, rtmlDocumentString, destinationURL);
	}

	/**
	 * 
	 * @param rtmlDocumentString
	 * @param destinationURL
	 * @throws Exception
	 */
	private void sendRTMLUpdateToIA(String cookieAuthString, String rtmlDocumentString, URL destinationURL) throws Exception {
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... sendRTMLUpdateToIA: " + destinationURL);
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "payload= " + rtmlDocumentString);
		
		//build the Call
		Call call;
		
		//put the RTML document in the parameter of the call
		Vector params = new Vector();
		params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
		
		call = SoapCallFactory.buildCookieAuthenticatedCall(cookieAuthString, SoapCallFactory.METHOD_NAME_HANDLE_RTML, params, SoapCallFactory.URN_USER_AGENT);

		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... Invoking " + SoapCallFactory.METHOD_NAME_HANDLE_RTML + " on " + destinationURL);
		
		//******************* SEND TO IA **********************
		Response response = call.invoke(destinationURL, "");
		// / ***************** SEND TO IA *********************
		
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... response=" + response);
		handleResponse(response);	
	}
	
	/*
	 * JUST FOR DEBUG
	 */
	public void sendEchoToIA(String rtmlDocumentString, URL destinationURL) throws Exception {
		
		Vector params = new Vector();
		params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
		
		String cookieAuthString = CookieUtils.makeAuthCookieString("agent", "InterProcessCommunication"); 
		Call call = SoapCallFactory.buildCookieAuthenticatedCall(cookieAuthString, SoapCallFactory.METHOD_NAME_ECHO, params, SoapCallFactory.URN_USER_AGENT);

		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... ... Invoking " + SoapCallFactory.METHOD_NAME_ECHO + " on " + destinationURL);
		
		//Invoke the call.
		Response response = call.invoke(destinationURL, "");
		
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... response=" + response);
		handleResponse(response);	
	}
	
	/*
	 * JUST FOR DEBUG
	 */
	public void sendPingToIA(String rtmlDocumentString, URL destinationURL) throws Exception {
		
		String cookieAuthString = CookieUtils.makeAuthCookieString("agent", "InterProcessCommunication"); 
		Call call = SoapCallFactory.buildCookieAuthenticatedCall(cookieAuthString, SoapCallFactory.METHOD_NAME_PING, null, SoapCallFactory.URN_USER_AGENT);

		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... ... Invoking " + SoapCallFactory.METHOD_NAME_PING + " on " + destinationURL);
		
		//Invoke the call.
		Response response = call.invoke(destinationURL, "");
		
		traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... response=" + response);
		handleResponse(response);	
	}
	
	/**
	 * 
	 * @param response
	 * @throws Exception
	 */
	private void handleResponse(Response response) throws Exception {
		
		//Check the response.
		if (!response.generatedFault())
		{
			traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "Response OK: ");
			traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "... " + response.getReturnValue());
			return;
		}
		else
		{
			Fault fault = response.getFault();
			Exception e = new Exception("Received SOAP Fault:" +fault + ", fault code=" + fault.getFaultCode());
			
			errorLogger.log(1, IAAsynchronousMessagingClient.class.getName(), "Generated fault: ");
			errorLogger.log(1, IAAsynchronousMessagingClient.class.getName(), "  Fault Code   = " + fault.getFaultCode());
			errorLogger.log(1, IAAsynchronousMessagingClient.class.getName(), "  Fault   		   = " + fault);
			throw e;
		}
	}
	
	/**
	 * used to test asychronous callback mechanism
	 * takes a single argument representing the file containing the RTML call
	 * Uses the intelligent agent represented in the RTML and invokes the RTML on it.
	 */
	public static void main(String a[]) {
		
		//load an RTML document from a file
		//using the sendRTMLUpdateToIA method, send the document to that location
		
		if (a.length != 3) {
			throw new RuntimeException("Usage: " + IAAsynchronousMessagingClient.class.getName() + " <RTML_FILE_PATH> <SERVICE_URL> <handle_rtml | echo | ping>");
		}
		
		String rtmlFilePath = a[0]; 
		String serviceUrlString = a[1];
		String rpcMethodNameString = a[2];
		String rtmlDocumentString = "";
		
		IAAsynchronousMessagingClient intelligentAgentMessagingClient = null;
		URL serviceURL = null;
		
		//***** EXTRACT THE ARGUMENTS *****
		try {
			if (!(new File(rtmlFilePath).exists())) {
				throw new RuntimeException("test RTML file: " + rtmlFilePath + " does not exist");
			}
			
			//extract the string representing the RTML Document
			rtmlDocumentString = org.estar.util.FileUtil.getFileAsString(rtmlFilePath);
			
			//get the URL from the main()
			serviceURL = new URL(serviceUrlString);
			
			//instantiate IAAsynchronousMessagingClient
			intelligentAgentMessagingClient = new IAAsynchronousMessagingClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//***** INVOKE THE CALL *****
		//invoke the call
		try {
			if (rpcMethodNameString.equals("handle_rtml")) {
				RTMLParser parser = new RTMLParser();
				parser.init(true);
				RTMLDocument rtmlDocument = parser.parse(rtmlDocumentString.trim());
				intelligentAgentMessagingClient.sendRTMLUpdateToIA(rtmlDocument);
				
			} else if (rpcMethodNameString.equals("ping")) {
				intelligentAgentMessagingClient.sendPingToIA(rtmlDocumentString, serviceURL); 
			
			} else if (rpcMethodNameString.equals("echo")) {
				intelligentAgentMessagingClient.sendEchoToIA(rtmlDocumentString, serviceURL);
			
			}
			intelligentAgentMessagingClient.sendEchoToIA(rtmlDocumentString, serviceURL);
			traceLogger.log(5, IAAsynchronousMessagingClient.class.getName(), "completed");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
