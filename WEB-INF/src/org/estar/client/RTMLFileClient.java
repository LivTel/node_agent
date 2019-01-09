package org.estar.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.messaging.SoapCallFactory;
import org.estar.util.CookieUtils;
import org.estar.util.FileUtil;
import org.estar.util.LoggerUtil;

import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;

public class RTMLFileClient {
	
	private String authorizationCookieString;
	private URL soapServerUrl;
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public RTMLFileClient(String[] args) throws MalformedURLException {
		if (args.length != 4) {
			traceLogger.log(5, RTMLFileClient.class.getName(), "usage:" + this.getClass().getName() + " service_url  rtml_file_path");
			System.exit(0);
		}
		soapServerUrl 		= new URL(args[0]);
		String username 	= args[2];
		String password 	= args[3];
		
		this.authorizationCookieString = CookieUtils.makeAuthCookieString(username, password);
	}
	
	private Response callHandleRTML(String rtmlFilePath) throws SOAPException {
		
		//Build the params
		Vector params = new Vector();
		String rtmlDocumentString;
		try {
			if (!(new File(rtmlFilePath).exists())) {
				throw new RuntimeException("RTML file: " + rtmlFilePath + " does not exist");
			}
			rtmlDocumentString = FileUtil.getFileAsString(rtmlFilePath);
			params.addElement(new Parameter("rtmlDocument", String.class, rtmlDocumentString, null));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Call call;
		String methodName = "handle_rtml";
		call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, methodName,  params, SoapCallFactory.URN_NODE_AGENT);
		
		traceLogger.log(5, RTMLFileClient.class.getName(), "Target URL: " + soapServerUrl);
		traceLogger.log(5, RTMLFileClient.class.getName(), "Invoking call : " + call.toString());
		
		//Invoke the call.
		return call.invoke(soapServerUrl, "");
	}
	
	public static String handleResponse(Response response) {
        //Check the response.
		if (response != null) {
			if (!response.generatedFault())
			{
				traceLogger.log(5, RTMLFileClient.class.getName(), "\n\nResponse OK: ");
				String returnValue=response.getReturnValue().getValue().toString();
				System.out.println(returnValue);
				return returnValue;
			} else {
				Fault fault = response.getFault();
				
				traceLogger.log(5, RTMLFileClient.class.getName(), "Generated fault: ");
				System.out.println ("  Fault Code   = " + fault.getFaultCode());
				System.out.println ("  Fault   		= " + fault);
				return fault.toString();
			}
		}
		traceLogger.log(5, RTMLFileClient.class.getName(), "Received null response");
		return null;
	}
	
	public static void main(String[] args) {
		/* args:
		0. service_url  
		1. RTML file path 
		2. username
		3. password
		 */
		
		try {
			RTMLFileClient rtmlFileClient = new RTMLFileClient(args);
			String rtmlFilePath 	= args[1];
			
			Response response = rtmlFileClient.callHandleRTML(rtmlFilePath);				
			
			handleResponse(response);
			
			traceLogger.log(5, RTMLFileClient.class.getName(), "FINISHED");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
