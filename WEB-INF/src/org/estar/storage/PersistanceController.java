package org.estar.storage;

import java.io.IOException;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.estar.configuration.NodeAgentProperties;
import org.estar.util.LoggerUtil;

public class PersistanceController {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public static PersistanceController instance;
	private PersistentMap userAliasMapStore, projectAliasMapStore, passwordMapStore; 

	public static PersistanceController getInstance() {
		if (instance == null) {
			instance = new PersistanceController();
		}
		return instance;
	}
	
	private PersistanceController() {
		try {
			userAliasMapStore = new PersistentMap(NodeAgentProperties.USERALIAS_MAP_LOCATION);
			projectAliasMapStore = new PersistentMap(NodeAgentProperties.PROJECTALIAS_MAP_LOCATION);
			passwordMapStore = new PersistentMap(NodeAgentProperties.PASSWORD_MAP_LOCATION); 
		} catch (IOException e) {
			e.printStackTrace();
			errorLogger.log(1, PersistanceController.class.getName(), e);
		}
	}

	public PersistentMap getProjectAliasMapStore() {
		return projectAliasMapStore;
	}

	public void setProjectAliasMapStore(PersistentMap projectAliasMapStore) {
		this.projectAliasMapStore = projectAliasMapStore;
	}

	public PersistentMap getUserAliasMapStore() {
		return userAliasMapStore;
	}

	public void setUserAliasMapStore(PersistentMap userAliasMapStore) {
		this.userAliasMapStore = userAliasMapStore;
	}

	public PersistentMap getPasswordMapStore() {
		return passwordMapStore;
	}

	public void setPasswordMapStore(PersistentMap passwordMapStore) {
		this.passwordMapStore = passwordMapStore;
	}
}
