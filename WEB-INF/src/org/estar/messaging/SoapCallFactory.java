package org.estar.messaging;

import java.util.Vector;

import org.estar.authentication.SOAPHTTPCookieAuthenticatedConnection;

import org.apache.soap.Constants;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.StringDeserializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.xml.QName;

public class SoapCallFactory {

	public static final String METHOD_NAME_HANDLE_RTML = "handle_rtml";
	public static final String METHOD_NAME_PING = "ping";
	public static final String METHOD_NAME_ECHO = "echo";
	
	public static final String URN_NODE_AGENT =  "urn:/node_agent";
	public static final String URN_USER_AGENT =  "urn:/user_agent";
	
	private static final String NAME_SPACE_URI = "http://schemas.xmlsoap.org/soap/encoding/";
	
	
	public static Call buildCookieAuthenticatedCall(String cookieAuthString, String methodName, Vector params, String urn) {
		
		Call call = new Call();
		
		//using cookie authentication for return call	
		SOAPHTTPCookieAuthenticatedConnection soapHTTPConnection = new SOAPHTTPCookieAuthenticatedConnection();
		soapHTTPConnection.setAuthorizationCookieValue(cookieAuthString);

		call = buildCall(methodName, params, urn);
		
		//set the transport on the call
		call.setSOAPTransport(soapHTTPConnection);
		
		return call;
	}
	
	
	//just for writeup
	public static Call buildCookieAuthenticatedCall2(String cookieAuthString, String methodName, Vector params, String urn) {
		
		Call call = new Call();
		
		//using cookie authentication for return call	
		
		SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();

		call = buildCall(methodName, params, urn);
		
		//set the transport on the call
		call.setSOAPTransport(soapHTTPConnection);
		
		return call;
	}
	// / just for writeup
	
	
	
	private static Call buildCall(String methodName, Vector params, String urn) {
		
		Call call = new Call();
		
		SOAPMappingRegistry smr = new SOAPMappingRegistry();
	    StringDeserializer sd = new StringDeserializer();
	    
	    smr.mapTypes(Constants.NS_URI_SOAP_ENC, new QName(NAME_SPACE_URI, "string"), null, null, sd);
	    call.setSOAPMappingRegistry(smr);	
		
	    call.setTargetObjectURI(urn);
		call.setMethodName(methodName);
		call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
		
		if (params != null) {
			call.setParams(params);
		}
		
		return call;
	}
}
