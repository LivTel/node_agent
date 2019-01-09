package org.estar.client;

import java.net.URL;
import java.util.Vector;

import org.estar.messaging.SoapCallFactory;
import org.estar.util.CookieUtils;

import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;

public class AvailabilityClient {

	private static final String DEFAULT_KEYSTORE_LOCATION = "/home/nrc/.keystore";//"/home/nrc/.dud_keystore"; //
	private static final String SSL_TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore";
	
	private static final String NO_AUTH_SWITCH = "-none";
	private static final String COOKIE_SWITCH = "-cookie";
	private static final String CERTIFICATE_SWITCH = "-cert";
	private static final String USING_AXIS_SWITCH = "-axis";
	private static final String USING_TOMCAT_SWITCH = "-tomcat";
	
	private String authorizationCookieString;
	
	private static URL soapServerUrl;
	private boolean authIsCertificate = false;
	private boolean authIsCookie = false;
	private boolean authIsNone = false;
	
	private boolean usingTomcat = false;
	private boolean usingAxis = false;
	private String urn = "";
	
	public AvailabilityClient(String[] args) {
		try {
			handleArgs(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private Response callGetAvailabilityPrediction() throws SOAPException {
		//Build the params
		Vector params = null;
		
		Call call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, "getAvailabilityPrediction",  params, SoapCallFactory.URN_NODE_AGENT);
		System.out.println("Target URL: " + soapServerUrl);
		System.out.println("Invoking call : " + call.toString());
		
		//Invoke the call.
		return call.invoke(soapServerUrl, "");
	}
	
	private Response callSetAvailabilityPrediction(long startTime, long endTime, double prediction) throws SOAPException {
		//Build the params
		Vector params = new Vector();
		params.addElement(new Parameter("startTime", Long.class, startTime, null));
		params.addElement(new Parameter("endTime", Long.class, endTime, null));
		params.addElement(new Parameter("prediction", Double.class, prediction, null));
		
		Call call = SoapCallFactory.buildCookieAuthenticatedCall(authorizationCookieString, "setAvailabilityPrediction",  params, SoapCallFactory.URN_NODE_AGENT);
		System.out.println("Target URL: " + soapServerUrl);
		System.out.println("Invoking call : " + call.toString());
		
		//Invoke the call.
		return call.invoke(soapServerUrl, "");
	}
	
	public static String handleResponse(Response response) {
        //Check the response.
		if (!response.generatedFault())
		{
			System.out.println("Response OK: ");
			String returnValue=response.getReturnValue().getValue().toString();
			System.out.println("... " + returnValue);
			return returnValue;
		}
		else
		{
			Fault fault = response.getFault();
			
			System.err.println("Generated fault: ");
			System.out.println ("  Fault Code   = " + fault.getFaultCode());
			System.out.println ("  Fault   		= " + fault);
			return fault.toString();
		}
	}
	

	private void handleArgs(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Incorrect arguments, Usage:");
			System.err.println("  java " + AvailabilityClient.class.getName()  + " <SOAP-Server-URL>");
		}
		
		soapServerUrl 	= new URL(args[0]);
		
		//summarize usage
		System.out.println("Using SOAP Server URL : " + soapServerUrl);
		
		authorizationCookieString = CookieUtils.makeAuthCookieString("kdh1", "EXOfollowup");
	}
	
	/**
	 * Node testing main for testing the service
	 * @param args The arguments: <service_url> <-none/-cookie/-cert> <-axis/-tomcat> <urn> <ping | echo | handle_rtml>>
	 */
	public static void main(String[] args) {
		AvailabilityClient nodeTester	= new AvailabilityClient(args);

		Response response = null;
		try {
			response = nodeTester.callGetAvailabilityPrediction();				
			handleResponse(response);
			
			System.out.println("... finished");
		} catch (SOAPException e) {
			e.printStackTrace();
			System.err.println("Caught SOAPException (" +
				e.getFaultCode() + "): " +  
				e.getMessage());
		}
	}
}