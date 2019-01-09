package org.estar.handler;

import java.rmi.registry.LocateRegistry;

import javax.servlet.http.HttpServletRequest;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.estar.configuration.NodeAgentProperties;
import org.estar.messaging.RMITeaConnectionHandler;
import org.estar.util.LoggerUtil;

public class PingHandler extends BasicHandler {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	private static final String PING = "ping";
	
	public void init() {
		traceLogger.log(5, PingHandler.class.getName(), ".init()");
	}
	
	public void invoke(MessageContext messageContext) throws AxisFault {
		traceLogger.log(5, PingHandler.class.getName(), ".invoke()");

		HttpServletRequest request = (HttpServletRequest)messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		
		OperationDesc operationDesc = messageContext.getOperation();
		String operationName = operationDesc.getName();
		String service = messageContext.getTargetService();
		
		if (operationName.equalsIgnoreCase(PING)) {
			traceLogger.log(5, PingHandler.class.getName(), "received ping request on service " + service);
			traceLogger.log(5, PingHandler.class.getName(), "testing that rmi registry is up");
			testRegistry();
			traceLogger.log(5, PingHandler.class.getName(), "success, registry found");
			traceLogger.log(5, PingHandler.class.getName(), "sending ping to subsystems, and returning SOAPFault if anything is wrong");
			pingSubSystems();
			traceLogger.log(5, PingHandler.class.getName(), "ping sent successfully, no errors returned"); 
		} else {
			traceLogger.log(5, PingHandler.class.getName(), "request is not ping request");
		}
	}
	
	private void testRegistry() throws AxisFault {
		traceLogger.log(5, PingHandler.class.getName(), ".testRegistry()");
		
		String host = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.TEA_HOST_NAME_PROPERTY);
		String portStr = NodeAgentProperties.getInstance().getProperty(NodeAgentProperties.NODE_AGENT_PORT);
		int port = Integer.parseInt(portStr);
		
		try {
			traceLogger.log(5, PingHandler.class.getName(), "locating registry at: " + host + ":" + port);
			LocateRegistry.getRegistry(host, port); 
		} catch (Exception e) {
			errorLogger.log(5, PingHandler.class.getName(), "testRegistry() threw an exception");
			e.printStackTrace();
			AxisFault axisFault = getSOAPFault(e);
			errorLogger.log(5, PingHandler.class.getName(), "sending SOAP ERROR with message: '" + axisFault.getMessage() + "'");
			throw axisFault;
		}
		
	}
	
	private void pingSubSystems() throws AxisFault{
		traceLogger.log(5, PingHandler.class.getName(), ".pingSubSystems()");
		try {
			RMITeaConnectionHandler.getInstance().testThroughput();
		} catch (Exception e) {
			errorLogger.log(5, PingHandler.class.getName(), "testThroughput() on RMITeaConnectionHandler threw an exception");
			e.printStackTrace();
			AxisFault axisFault = getSOAPFault(e);
			errorLogger.log(5, PingHandler.class.getName(), "sending SOAP ERROR with message: '" + axisFault.getMessage() + "'");
			throw axisFault;
		}
	}
	
	private AxisFault getSOAPFault(Exception e) {
		String msg = "PING FAILED: " + e.getMessage();
		AxisFault axisFault = new AxisFault(e.getMessage());
		return axisFault;
	}
}
