package org.estar.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.estar.messaging.SoapCallFactory;
import org.estar.rtml.RTMLDocument;
import org.estar.util.FileUtil;
import org.estar.util.LoggerUtil;
import org.estar.util.RTMLUtil;

public class NodeClient {

	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);

	public static final String PING = "ping";
	public static final String ECHO = "echo";
	public static final String HANDLE_RTML = "handle_rtml";
	public static final String GET_VERSION = "getVersion";
	public static final String TEST_UPDATE_CALLBACK = "testUpdateCallback";
	
	private URL url;
	private String urn;
	private String authorizationCookieString;

	public NodeClient(URL url, String urn, String authorizationCookieString) throws MalformedURLException {
		this.url = url;
		this.urn = urn;
		this.authorizationCookieString = authorizationCookieString;
	}
	
		/**
	 * Testing method, to ping service
	 * @return The resonse of the ping method call
	 * @throws SOAPException
	 */
	public Response callPing() throws SOAPException {
		
		Call call;
		String methodName = "ping";
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  null, SoapCallFactory.URN_NODE_AGENT);
		
		traceLogger.log(5, NodeClient.class.getName(), "Target URL: " + url);
		traceLogger.log(5, NodeClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call
		return call.invoke(url, "");
	}
	
	/**
	 * Testing method, to get the Version String of the NodeAgent
	 * @return The Version of the NodeAgent
	 * @throws SOAPException
	 */
	public Response callGetVersion() throws SOAPException {
		Call call;
		
		String methodName = "getVersion";
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  null, SoapCallFactory.URN_NODE_AGENT);
		
		
		traceLogger.log(5, NodeClient.class.getName(), "Target URL: " + url);
		traceLogger.log(5, NodeClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call
		return call.invoke(url, "");
	}
	
	public Response callHandleRTML(RTMLDocument document) throws SOAPException {
		
		//Build the params
		Vector params = new Vector();
		
		try {
			String rtmlDocumentString = RTMLUtil.getRTMLAsString(document);
			params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
		} catch (Exception e) {
			e.printStackTrace();
			throw new SOAPException("RTMLCreate", e.getMessage());
		}
		
		Call call;
		String methodName = "handle_rtml";
		
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  params, SoapCallFactory.URN_NODE_AGENT);
		
		traceLogger.log(5, NodeClient.class.getName(), "Target URL: " + url);
		traceLogger.log(5, NodeClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call.
		return call.invoke(url, "");
	}
	
	public Response callHandleRTML(String rtmlFilePath) throws SOAPException {
		
		//Build the params
		Vector params = new Vector();
		String rtmlDocumentString;
		try {
			if (!(new File(rtmlFilePath).exists())) {
				throw new RuntimeException("test RTML file: " + rtmlFilePath + " does not exist");
			}
			rtmlDocumentString = FileUtil.getFileAsString(rtmlFilePath);
			params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Call call;
		String methodName = "handle_rtml";
		
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  params, SoapCallFactory.URN_NODE_AGENT);
		
		traceLogger.log(5, NodeClient.class.getName(), "Target URL: " + url);
		traceLogger.log(5, NodeClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call.
		Response response = call.invoke(url, "");
		return response;
	}
	
	/**
	 * Used to test the full RTML lifecycle, including asychronous update back to IA
	 * @param rtmlFilePath Path to RTML document, the IA details of which pertain to a listening IA.
	 * @return Response to method invocation
	 * @throws SOAPException
	 */
	public Response callTestUpdateCallback(String rtmlFilePath, long waitTime) throws SOAPException {
		
		//Build the params
		Vector params = new Vector();
		String rtmlDocumentString;
		String waitTimeString = String.valueOf(waitTime);
		try {
			if (!(new File(rtmlFilePath).exists())) {
				throw new RuntimeException("test RTML file: " + rtmlFilePath + " does not exist");
			}
			rtmlDocumentString = FileUtil.getFileAsString(rtmlFilePath);
			params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
			params.addElement(new Parameter("waitTime", String.class, waitTimeString, null));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Call call;
		String methodName = "testUpdateCallback";
		
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  params, SoapCallFactory.URN_NODE_AGENT);
		
		traceLogger.log(5, NodeClient.class.getName(), "Target URL: " + url);
		traceLogger.log(5, NodeClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call.
		return call.invoke(url, "");
	}
	
	public String handleResponse(Response response) {
		//Check the response.
		if (!response.generatedFault())
		{
			traceLogger.log(5, NodeClient.class.getName(), "Response OK: ");
			Parameter returnValue = response.getReturnValue();
			String returnValueAsString;
			if (returnValue != null) {
				returnValueAsString = returnValue.getValue().toString();
			} else {
				returnValueAsString = "null";
			}
			traceLogger.log(5, NodeClient.class.getName(), "... " + returnValueAsString);
			return returnValueAsString;
		} else {
			Fault fault = response.getFault();
			
			errorLogger.log(1, NodeClient.class.getName(), "Generated fault: ");
			errorLogger.log(1, NodeClient.class.getName(), "  Fault Code   = " + fault.getFaultCode());
			errorLogger.log(1, NodeClient.class.getName(), "  Fault   		= " + fault);
			return fault.toString();
		}
	}
}
