package org.estar.storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.util.LoggerUtil;

/**
 * Class to store alias mappings on disc
 * @author nrc
 *
 */
public class PersistentMap extends Properties{
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public PersistentMap(String fileName) throws IOException {
		
		traceLogger.log(5, PersistentMap.class.getName(), "Loading alias store: " + fileName);
		Properties properties = new Properties();
		FileInputStream in = new FileInputStream(fileName);
		properties.load(in);
		this.putAll(properties);
		traceLogger.log(5, PersistentMap.class.getName(), "... " + this);
	}
	
	public String getKey(String value) {
		Enumeration keysE = this.keys();
		while (keysE.hasMoreElements()) {
			String key = (String)keysE.nextElement();
			if (this.getProperty(key).equals(value)) {
				return key;
			}
		}
		return null;
	}
	
	public String toString() {
		String s = this.getClass().getName() + "[";
		Enumeration keysE = this.keys();
		boolean hasElements = false;
		while (keysE.hasMoreElements()) {
			String key = (String)keysE.nextElement();
			String value = this.getProperty(key);
			s += key + "=" + value + ";";
			hasElements = true;
		}
		if (hasElements) {
			s = s.substring(0, s.length() -1);
		}
		s += "]";
		return s;
	}
}
