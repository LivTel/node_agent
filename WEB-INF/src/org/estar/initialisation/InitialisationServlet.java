package org.estar.initialisation;

import javax.servlet.http.HttpServlet;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.configuration.NodeAgentProperties;
import org.estar.messaging.RMITeaConnectionHandler;
import org.estar.util.LoggerUtil;

public class InitialisationServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static Logger traceLogger, errorLogger;
	
	public InitialisationServlet() {
		LoggerUtil.setUpLoggers();
		
		traceLogger  = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
		errorLogger  = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
		
	}
	
	//run initialise methods on 
	//ServerProperties, RTMLHashStorage and TeaConnectionHandler
	//in order to initialise the services on web-server startup
	public void init() throws javax.servlet.ServletException {
		
		traceLogger.log(5, InitialisationServlet.class.getName(), ".init()");

		//if isLive property is set, create the connection to the TEA
		try {
			NodeAgentProperties nodeAgentProperties = NodeAgentProperties.getInstance();
			if (nodeAgentProperties != null) {
				if (!nodeAgentProperties.getProperty(NodeAgentProperties.IS_TEA_CONNECTED).equalsIgnoreCase(NodeAgentProperties.TRUE))
					traceLogger.log(1, InitialisationServlet.class.getName(), 
							"... " + NodeAgentProperties.IS_TEA_CONNECTED + " = false, will not connect RMITeaConnectionHandler on request receipt");
				else {
					traceLogger.log(5, InitialisationServlet.class.getName(), 
							"..." + NodeAgentProperties.IS_TEA_CONNECTED + " = true");
					traceLogger.log(5, InitialisationServlet.class.getName(), 
							"... loading RMITeaConnectionHandler instance");
					RMITeaConnectionHandler.getInstance();
				}
			} else {
				errorLogger.log(1, InitialisationServlet.class.getName(), 
							"... nodeAgentProperties = null!");
			}
			//debug, print the classpath
			//DebugUtil.debugShowClasspath();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void destroy() {
		try {
			traceLogger.log(5, InitialisationServlet.class.getName(), ".destroy()");
			RMITeaConnectionHandler.getInstance().destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
