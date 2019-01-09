package org.estar.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.mail.MessagingException;

import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.Base64;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.transport.SOAPTransport;
import org.apache.soap.transport.TransportMessage;
import org.apache.soap.util.net.HTTPUtils;

public class SOAPHTTPCookieAuthenticatedConnection implements SOAPTransport {
	private BufferedReader responseReader;
	private Hashtable responseHeaders;
	private SOAPContext responseSOAPContext;
	
	private String httpProxyHost;
	private int    httpProxyPort = 80;
	private int    timeout;
	private String userName;
	private String password;
	private String authCookieValue;
	private String proxyUserName;
	private String proxyPassword;
	private boolean maintainSession = true;
	private String cookieHeader;
	private String cookieHeader2;
	private int    outputBufferSize = HTTPUtils.DEFAULT_OUTPUT_BUFFER_SIZE;
	private Boolean tcpNoDelay = null;
	
	/**
	 * Set HTTP proxy host.
	 *
	 * @param host the HTTP proxy host or null if no proxy.
	 */
	public void setProxyHost (String host) {
		httpProxyHost = host;
	}
	
	/**
	 * Set HTTP proxy port.
	 *
	 * @param host the HTTP proxy port.
	 */
	public void setProxyPort (int port) {
		httpProxyPort = port;
	}
	
	/**
	 * Get HTTP proxy host.
	 *
	 * @return the HTTP proxy host or null if no proxy.
	 */
	public String getProxyHost () {
		return httpProxyHost;
	}
	
	/**
	 * Get HTTP proxy port.
	 *
	 * @return the HTTP proxy port. Invalid if getProxyHost() returns null.
	 */
	public int getProxyPort () {
		return httpProxyPort;
	}
	
	/**
	 * Set the username for HTTP Basic authentication.
	 */
	public void setUserName (String userName) {
		this.userName = userName;
	}
	
	/**
	 * Set the password for HTTP Basic authentication.
	 */
	public void setPassword (String password) {
		this.password = password;
	}
	
	
	//New code for authentication via Cookies
	/**
	 * Set the cookie to be used for authentication
	 */
	public void setAuthorizationCookieValue (String authCookieValue) {
		this.authCookieValue = authCookieValue;
	}
	
	/**
	 * Set the username for HTTP proxy basic authentication.
	 */
	public void setProxyUserName (String userName) {
		this.proxyUserName = userName;
	}
	
	/**
	 * Set the password for HTTP proxy basic authentication.
	 */
	public void setProxyPassword (String password) {
		this.proxyPassword = password;
	}
	
	private static String encodeAuth(String userName, String password) throws SOAPException {
		try {
			return Base64.encode((userName + ":" + password).getBytes("8859_1"));
		} catch (UnsupportedEncodingException e) {
			throw new SOAPException (Constants.FAULT_CODE_CLIENT, e.getMessage(), e);
		}
	}
	
	/**
	 * Indicate whether to maintain HTTP sessions.
	 */
	public void setMaintainSession (boolean maintainSession) {
		this.maintainSession = maintainSession;
		if (maintainSession == false) {
			cookieHeader = null;
			cookieHeader2 = null;
		}
	}
	
	/**
	 * Return session maintanence status.
	 */
	public boolean getMaintainSession (){
		return maintainSession;
	}
	
	/**
	 * Set the HTTP read timeout.
	 *
	 * @param timeout the amount of time, in ms, to block on reading data.
	 *                A zero value indicates an infinite timeout.
	 */
	public void setTimeout (int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Get the HTTP read timeout.
	 *
	 * @return the amount of time, in ms, to block on reading data.
	 */
	public int getTimeout () {
		return timeout;
	}
	
	/**
	 * Sets the output buffer size (in bytes).
	 *
	 * @param sz The output buffer size (in bytes).
	 */
	public void setOutputBufferSize(int sz) {
		outputBufferSize = sz;
	}
	
	/**
	 * Gets the output buffer size (in bytes).
	 *
	 * @return The output buffer size (in bytes).
	 */
	public int getOutputBufferSize() {
		return outputBufferSize;
	}
	
	/**
	 * Get the TCPNoDelay setting.
	 *
	 * @return the TCPNoDelay setting.
	 */
	public Boolean getTcpNoDelay() {
		return tcpNoDelay;
	}
	
	/**
	 * Set the TCPNoDelay setting.  Setting this to true will disable
	 * Nagle's algorithm for TCP.  The default is false.
	 */
	public void setTcpNoDelay(Boolean nodelay) {
		tcpNoDelay = nodelay;
	}
	
	/**
	 * This method is used to request that an envelope be posted to the
	 * given URL. The response (if any) must be gotten by calling the
	 * receive() function.
	 *
	 * @param sendTo the URL to send the envelope to
	 * @param action the SOAPAction header field value
	 * @param headers any other header fields to go to as protocol headers
	 * @param env the envelope to send
	 * @param smr the XML<->Java type mapping registry (passed on)
	 * @param ctx the request SOAPContext
	 *
	 * @exception SOAPException with appropriate reason code if problem
	 */
	public void send (URL sendTo, String action, Hashtable headers, Envelope env, SOAPMappingRegistry smr, SOAPContext ctx) throws SOAPException {
		try {
			String payload = null;
			if (env != null) {
				StringWriter payloadSW = new StringWriter ();
				env.marshall (payloadSW, smr, ctx);
				payload = payloadSW.toString ();
			}
			
			if (headers == null) {
				headers = new Hashtable ();
			}
			if (maintainSession) {
				// if there is saved cookie headers, put them in to the request
				if (cookieHeader2 != null) { // RFC 2965 header
					headers.put ("Cookie2", cookieHeader2);
				} 
				if (cookieHeader != null) { // RFC 2109 header
					headers.put ("Cookie", cookieHeader);
				}
			}
			
			headers.put (Constants.HEADER_SOAP_ACTION, 
					(action != null) ? ('\"' + action + '\"') : "");
			if (userName != null) {
				// add the Authorization header for Basic authentication
				headers.put (Constants.HEADER_AUTHORIZATION,
						"Basic " + encodeAuth(userName, password));
			}
			if (this. authCookieValue != null) {
				// add the Authorization header for Basic authentication
				headers.put ("Cookie",
						this.authCookieValue);
			}
			if (proxyUserName != null) {
				// add the Proxy-Authorization header for proxy authentication
				headers.put (Constants.HEADER_PROXY_AUTHORIZATION,
						"Basic " + encodeAuth(proxyUserName, proxyPassword));
			}
			
			TransportMessage response;
			try
			{
				TransportMessage msg = new TransportMessage(payload, ctx, headers);
				msg.save();
				response = HTTPUtils.post (sendTo, msg,
						timeout, httpProxyHost, httpProxyPort,
						outputBufferSize, tcpNoDelay);
			} catch (MessagingException me) {
				throw new IOException ("Failed to encode mime multipart: " + me);
			} catch (UnsupportedEncodingException uee) {
				throw new IOException ("Failed to encode mime multipart: " + uee);
			}
			
			Reader envReader = response.getEnvelopeReader();
			if (envReader != null)
				responseReader = new BufferedReader(envReader);
			else
				responseReader = null;
			responseSOAPContext = response.getSOAPContext();
			responseHeaders = response.getHeaders();
			if (maintainSession) {
				// look for Set-Cookie2 and Set-Cookie headers and save them after
				// stripping everything after the ';' (i.e., ignore all cookie attrs).
				// Only update my state iff the header is there .. otherwise
				// leave the current 
				// Note: Header is case-insensitive
				String hdr;
				
				hdr = getHeaderValue (responseHeaders, "Set-Cookie2");
				
				if (hdr != null) {
					cookieHeader2 = hdr;
					int index = cookieHeader2.indexOf (';');
					if (index != -1) {
						cookieHeader2 = cookieHeader2.substring (0, index);
					}
				}
				
				hdr = getHeaderValue (responseHeaders, "Set-Cookie");
				
				if (hdr != null) {
					cookieHeader = hdr;
					int index = cookieHeader.indexOf (';');
					if (index != -1) {
						cookieHeader = cookieHeader.substring (0, index);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw new SOAPException (Constants.FAULT_CODE_CLIENT, e.getMessage(), e);
		} catch (MessagingException e) {
			throw new SOAPException (Constants.FAULT_CODE_CLIENT, e.getMessage(), e);
		} catch (IOException e) {
			throw new SOAPException (Constants.FAULT_CODE_CLIENT, e.getMessage(), e);
		}
	}
	
	/**
	 * Return a buffered reader to receive back the response to whatever
	 * was sent to whatever.
	 *
	 * @return a reader to read the results from or null if that's not
	 *         possible.
	 */
	public BufferedReader receive () {
		return responseReader;
	}
	
	/**
	 * Return access to headers generated by the protocol.
	 *
	 * @return a hashtable containing all the headers
	 */
	public Hashtable getHeaders () {
		return responseHeaders;
	}
	
	/**
	 * Return the SOAPContext associated with the response.
	 *
	 * @return response SOAPContext
	 */
	public SOAPContext getResponseSOAPContext () {
		return responseSOAPContext;
	}
	
	/**
	 * Obtain a header value from the table using a case insensitive search.
	 *
	 * @param headers a colletion of headers from the http response
	 * @param headerName the name of the header to find
	 * @return the header value or null if not found
	 */
	private static String getHeaderValue (Hashtable headers, String headerName) {
		for (Enumeration e = headers.keys (); e.hasMoreElements ();) {
			String key = (String) e.nextElement();
			
			if (key.equalsIgnoreCase (headerName)) {
				return (String) headers.get(key);
			}
		}
		
		return null;
	}
}
