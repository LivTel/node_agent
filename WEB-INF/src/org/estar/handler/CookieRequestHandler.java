package org.estar.handler;

import javax.servlet.http.HttpServletRequest;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.soap.SOAPException;
import org.estar.authentication.CookieAuthenticationService;
import org.estar.util.LoggerUtil;

public class CookieRequestHandler extends BasicHandler {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public void init() {
		traceLogger.log(5, CookieRequestHandler.class.getName(), ".init()");
	}
	
	public void invoke(MessageContext messageContext) throws AxisFault {
		traceLogger.log(5, CookieRequestHandler.class.getName(), ".invoke()");

		HttpServletRequest request = (HttpServletRequest)messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		
		String cookieHeader = request.getHeader("Cookie");
		
		traceLogger.log(5, CookieRequestHandler.class.getName(), "... request received from " + request.getRemoteAddr());
		
		boolean isSecure = request.isSecure();
		traceLogger.log(5, CookieRequestHandler.class.getName(), "... request.isSecure()= " + isSecure);
		
		//if not on SSL, check for Cookie Header
		if (isSecure) {
			traceLogger.log(5, CookieRequestHandler.class.getName(), "... ... login authenticated against certificate");
		} else {
			traceLogger.log(5, CookieRequestHandler.class.getName(), "... cookie header received= " + cookieHeader);
			traceLogger.log(5, CookieRequestHandler.class.getName(), "... ... authenticating");
			try {
				CookieAuthenticationService.authenticateCookieValue(cookieHeader);
				traceLogger.log(5, CookieRequestHandler.class.getName(), "... ... login authenticated");
			} catch (SOAPException e) {
				e.printStackTrace();
				throw new AxisFault(e.getMessage());
			}
		} 
		
		String service = messageContext.getTargetService();
		OperationDesc operation = messageContext.getOperation();
	
		traceLogger.log(5, CookieRequestHandler.class.getName(), "... invoking : " + operation.getName() + " on " + service);
	}
}
