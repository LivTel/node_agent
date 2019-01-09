package org.estar.util;

import java.io.IOException;

import ngat.util.logging.BogstanLogFormatter;
import ngat.util.logging.ConsoleLogHandler;
import ngat.util.logging.DatagramLogHandler;
import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;
import ngat.util.logging.Logging;

import org.estar.configuration.NodeAgentProperties;

public class LoggerUtil {

	public static final String TRACE_LOGGER_NAME = "TRACE";

	public static final String ERROR_LOGGER_NAME = "ERROR";

	public static void setUpLoggers() {

		NodeAgentProperties nodeAgentProperties = NodeAgentProperties
				.getInstance();

		ConsoleLogHandler console = new ConsoleLogHandler(
				new BogstanLogFormatter());
		console.setLogLevel(Logger.ALL);

		String useGlsString = nodeAgentProperties
				.getProperty(NodeAgentProperties.GLS_USE);
		boolean useGls = useGlsString.equalsIgnoreCase("true");

		Logger traceLogger = LogManager.getLogger(TRACE_LOGGER_NAME);
		traceLogger.setLogLevel(Logger.ALL);
		traceLogger.addHandler(console);
		
		Logger errorLogger = LogManager.getLogger(ERROR_LOGGER_NAME);
		errorLogger.setLogLevel(Logger.ALL);
		errorLogger.addHandler(console);

		if (useGls) {
			String glsHost = nodeAgentProperties
					.getProperty(NodeAgentProperties.GLS_HOST);
			String gslChannelError = nodeAgentProperties
					.getProperty(NodeAgentProperties.GLS_CHANNEL_ERROR);
			String glsChannelTrace = nodeAgentProperties
					.getProperty(NodeAgentProperties.GLS_CHANNEL_TRACE);

			int gslPort;
			try {
				gslPort = new Integer(nodeAgentProperties
						.getProperty(NodeAgentProperties.GLS_PORT)).intValue();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}

			System.out.println("Starting GLS logging with parameters: host="
					+ glsHost + ", port=" + gslPort + ", channelError="
					+ gslChannelError + ", channelTrace=" + glsChannelTrace);

			traceLogger.setChannelID(glsChannelTrace); // set the ChannelID so
														// GLS knows how to
														// process this log
														// stream
			try {
				DatagramLogHandler datagramLogHandler = new DatagramLogHandler(
						glsHost, gslPort);
				datagramLogHandler.setLogLevel(Logging.LOG_LEVEL_PRATTLING);
				traceLogger.addHandler(datagramLogHandler);
			} catch (IOException e) {
				e.printStackTrace();
			}

			errorLogger.setChannelID(gslChannelError); // set the ChannelID so
														// GLS knows how to
														// process this log
														// stream
			try {
				DatagramLogHandler datagramLogHandler = new DatagramLogHandler(
						glsHost, gslPort);
				datagramLogHandler.setLogLevel(Logging.LOG_LEVEL_CHATTY);
				errorLogger.addHandler(datagramLogHandler); // send logs to GLS
															// server
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		errorLogger.log(5, LoggerUtil.class.getName(),
					"errorLogger initialised");
		
		traceLogger.log(5, LoggerUtil.class.getName(),
					"traceLogger initialised");
	}
}
