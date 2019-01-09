package org.estar.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import ngat.util.CommandParser;
import ngat.util.ConfigurationProperties;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Response;
import org.estar.util.CookieUtils;
import org.estar.util.LoggerUtil;

public class NodeTester  {
	
	/*
	private static final String DEFAULT_KEYSTORE_LOCATION = "/home/nrc/.keystore";//"/home/nrc/.dud_keystore"; //
	private static final String SSL_TRUSTSTORE_PROPERTY = "javax.net.ssl.trustStore";
	private static final String SSL_TRUSTSTORE_PROPERTY_PASSWORD = "javax.net.ssl.trustStorePassword";
	*/
	
	private String authorizationCookieString, urn, method, rtmlFilePath;
	private long waitTime;
	private static URL url;
	
	public NodeTester(String[] args) throws MalformedURLException {
		
		// Extract command line parameters.
		CommandParser cp = new CommandParser();
		try {
			cp.parse(args);
		} catch (ParseException px) {	    
			showUsage();
		}
		
		ConfigurationProperties config = cp.getMap();
		
		url 	= new URL(config.getProperty("url"));
		urn 	= config.getProperty("urn");
		method 		= config.getProperty("method");
		rtmlFilePath	= config.getProperty("rtml");
		
		try {
			waitTime = config.getLongValue("wait");
		} catch (ParseException e1) {
		}
		
		String username = config.getProperty("username");
		String password = config.getProperty("password");
		this.authorizationCookieString = CookieUtils.makeAuthCookieString(username, password);
	}
	
	public NodeTester(String url, String urn, String method, String rtmlFilePath, String username, String password, long waitTime) throws MalformedURLException {
		this.url 	= new URL(url);
		this.urn = urn;
		this.method = method;
		this.rtmlFilePath = rtmlFilePath;
		this.waitTime = waitTime;
		
		this.authorizationCookieString = CookieUtils.makeAuthCookieString(username, password);
	}
	
	public void invokeArgs() {
		
		long waitTime = 0;
		
		System.out.println(NodeTester.class.getName() + " Starting.");
		System.out.println(NodeTester.class.getName() + " Invocation:");
		System.out.println(NodeTester.class.getName() + " ... url=" + url);
		System.out.println(NodeTester.class.getName() + " ... urn=" + urn);
		System.out.println(NodeTester.class.getName() + " ... method=" + method);
		System.out.println(NodeTester.class.getName() + " ... rtml=" + rtmlFilePath);
		System.out.println(NodeTester.class.getName() + " ... wait=" + waitTime + "mS");
		System.out.println(NodeTester.class.getName() + " ... auth cookie=" + authorizationCookieString);
		
		Response response = null;
		try {
			NodeClient nodeClient	= new NodeClient(url, urn, authorizationCookieString);
			
			System.out.println(NodeTester.class.getName() + " Calling method '" + method + "'");
			
			if (method.equals(NodeClient.PING)) {
				response = nodeClient.callPing();				
			} else if (method.equals(NodeClient.ECHO)) {
				response = nodeClient.callPing();
			} else if (method.equals(NodeClient.HANDLE_RTML)) {
				response = nodeClient.callHandleRTML(rtmlFilePath);				
			} else if (method.equals(NodeClient.GET_VERSION)) {
				response = nodeClient.callGetVersion();				
			} else if (method.equals(NodeClient.TEST_UPDATE_CALLBACK)) {
				response = nodeClient.callTestUpdateCallback(rtmlFilePath, waitTime);		
			} 
			
			String responseString = nodeClient.handleResponse(response);
			
			System.out.println(NodeTester.class.getName() + " ... " + responseString);
			System.out.println(NodeTester.class.getName() + " ... finished");
			
			if (responseString.equals("ACK")) {
				System.exit(0);
			} else {
				System.exit(-9);
			}
		} catch (SOAPException e) {
			e.printStackTrace();
			System.out.println(NodeTester.class.getName() + " Caught SOAPException (" +
					e.getFaultCode() + "): " +  
					e.getMessage());
			System.exit(-9);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-9);
		}
	}
	
	private static void showUsage() {
		String m = NodeTester.class.getName() + "-url <url> -urn <urn> -method <testUpdateCallback | handle_rtml | getVersion | ping> -rtml <path_to_rtml_file> -wait <callback_wait_time_mS> ";
	}
	
	/**
	 * Node testing main for testing the service
	 * @param args The arguments: -url <url> -urn <urn> -method <testUpdateCallback | handle_rtml | getVersion | ping> -rtml <path_to_rtml_file> -wait <callback_wait_time_mS>
	 */
	public static void main(String[] args) {
		
		NodeTester nodeTester;
		try {
			nodeTester = new NodeTester(args);
			nodeTester.invokeArgs();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}