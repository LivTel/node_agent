package org.estar.authentication;

import java.io.StringWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Response;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.RPCRouter;
import org.apache.soap.server.ServiceManager;
import org.apache.soap.server.http.ServerHTTPUtils;
import org.estar.util.LoggerUtil;

/** 
 * Description: This class must be specified as user defined provider type 
 * for all Java services that need to be deployed on Apache SOAP with Cookie 
 * authentication.
 * 
 * Instead of using the default Java provider, use this one to provide a basic 
 * authentication scheme and check against authorization rules. 
 * The class org.apache.soap.providers.TemplateProvider gives valuable 
 * info on how to implement a custom provider.
 * @author Neil Clay
*/ 

public class CookieAuthenticationProvider implements org.apache.soap.util.Provider { 
	
	protected DeploymentDescriptor 	dd; 
	protected Envelope             			envelope; 
	protected Call                 				call; 
	protected String               				methodName; 
	protected String               				targetObjectURI; 
	protected HttpServlet          			servlet; 
	protected HttpSession          			session; 	
	protected Object               			targetObject; 
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public void locate(DeploymentDescriptor dd, Envelope env, Call call, String methodName, String targetObjectURI, SOAPContext reqContext) throws SOAPException { 
		
		traceLogger.log(5, CookieAuthenticationProvider.class.getName(), 
				"locate(" + dd + ", " + env + ", " + call + ", " + methodName + ", " +targetObjectURI + ", " + reqContext + ") invoked");
		
		HttpServlet servlet =(HttpServlet) reqContext.getProperty( Constants.BAG_HTTPSERVLET ); 
		HttpSession session =(HttpSession) reqContext.getProperty( Constants.BAG_HTTPSESSION ); 
		HttpServletRequest rq =(HttpServletRequest) reqContext.getProperty(Constants.BAG_HTTPSERVLETREQUEST); 
		
		//Authorization *****************************
		
			//debug, print values of all headers and attributes
			Enumeration attributeNames = rq.getAttributeNames();

		 	traceLogger.log(5, CookieAuthenticationProvider.class.getName(), "Showing attribute names and values:");
		 	
		 	while(attributeNames.hasMoreElements()) {
		 		String attributeName =(String)attributeNames.nextElement();
		 		traceLogger.log(5, CookieAuthenticationProvider.class.getName(), attributeName + " : " + rq.getAttribute(attributeName));
		 	}
		 	Enumeration headerNames = rq.getHeaderNames();
		 	traceLogger.log(5, CookieAuthenticationProvider.class.getName(), "Showing header names and values:");
		 	while(headerNames.hasMoreElements()) {
		 		String headerName =(String)headerNames.nextElement();
		 		traceLogger.log(5, CookieAuthenticationProvider.class.getName(), headerName + " : " + rq.getHeader(headerName));
		 	}
		 	
			//extract the cookie value from the HttpHeader and authenticate it
		    String cookieString = rq.getHeader("Cookie");
		    traceLogger.log(5, CookieAuthenticationProvider.class.getName(), "cookieString = " + cookieString);
		    
		    //throws SOAPException if not authenticated
	    	try {
				CookieAuthenticationService.authenticateCookieValue(cookieString);
			} catch(SOAPException e) {
				errorLogger.log(1, CookieAuthenticationProvider.class.getName(), "Cannot authenticate : " +cookieString);
				throw e;
			}
	    
	    //authenticated
	    
		traceLogger.log(5, CookieAuthenticationProvider.class.getName(), "authenticated");
		
	    // /Authorization *****************************
	    
		this.dd              		= dd; 
		this.envelope        		= env; 
		this.call            			= call; 
		this.methodName      = methodName; 
		this.targetObjectURI 	= targetObjectURI; 
		this.servlet         		= servlet; 
		this.session         		= session; 
		
		ServletConfig  config  = null; 
		ServletContext context = null; 
		
		if( servlet != null ) config  = servlet.getServletConfig(); 
		if( config != null ) context = config.getServletContext(); 
		
		ServiceManager serviceManager = ServerHTTPUtils.getServiceManagerFromContext(context); 
		
		//Default processing for 'java' and 'script' providers 
		//call on a valid method name? 
		if(!RPCRouter.validCall(dd, call)) { 
			throw new SOAPException(Constants.FAULT_CODE_SERVER,  "Method '" + call.getMethodName() +   "' is not supported."); 
		}
		
		//locates target object, e.g. : org.estar.NodeReplicator and puts reference to it in targetObject
		targetObject = ServerHTTPUtils.getTargetObject(serviceManager, dd, targetObjectURI, servlet, session, reqContext, context);
	}; 
	
	
	public void invoke(SOAPContext reqContext, SOAPContext resContext) throws SOAPException { 
		traceLogger.log(1, CookieAuthenticationProvider.class.getName(), "invoke(" + reqContext + ", " + resContext + ") invoked");
		
		try { 
			Response resp = RPCRouter.invoke( dd, call, targetObject,  reqContext, resContext ); 
			Envelope env = resp.buildEnvelope(); 
			StringWriter  sw = new StringWriter(); 
			env.marshall(sw, call.getSOAPMappingRegistry(), resContext); 
			resContext.setRootPart( sw.toString(), Constants.HEADERVAL_CONTENT_TYPE_UTF8); 
		} catch( Exception e ) {
			errorLogger.log(1, CookieAuthenticationProvider.class.getName(), e);
			if( e instanceof SOAPException ) 
				throw(SOAPException) e; 
			throw new SOAPException( Constants.FAULT_CODE_SERVER, e.toString()); 
		}
	}
	
} 