package org.estar.authentication;

import java.security.NoSuchAlgorithmException;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.Constants;
import org.apache.soap.SOAPException;
import org.estar.dao.UserLogin;
import org.estar.storage.PersistanceController;
import org.estar.storage.PersistentMap;
import org.estar.util.CookieUtils;
import org.estar.util.LoggerUtil;

public class CookieAuthenticationService {
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	//authenticate Cookie String
	public static void authenticateCookieValue(String cookieString) throws SOAPException {
		
		traceLogger.log(5, CookieAuthenticationService.class.getName(), "authenticateCookieValue(" + cookieString + ") invoked");
		
		if (cookieString == null) {
			try {
				throw new SOAPException(Constants.FAULT_CODE_PROTOCOL,"Authentication failed due to null value in cookie");
			} catch (SOAPException e) {
				errorLogger.log(1, CookieAuthenticationService.class.getName(), e);
				throw e;
			}
		}
		
		UserLogin userSupplied;
		try {
			userSupplied = CookieUtils.decodeAuthCookieString(cookieString);
			authenticateUser(userSupplied);
		} catch (SOAPException e) {
			errorLogger.log(1, CookieAuthenticationService.class.getName(), e);
			throw e;
		}
	}
	
	//authenticate password against username, if failed throw SOAPException
	private static void authenticateUser(UserLogin userSupplied) throws SOAPException {
		traceLogger.log(5, CookieAuthenticationService.class.getName(), "... ... authenticateUser(" + userSupplied + ") invoked");
		
		//look up user from database, using username of supplied user
		UserLogin userLookedUp;

		try {
			userLookedUp = lookupUserLogin(userSupplied.getUserName());
			if (userLookedUp == null) {
				throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "Authentication failed due to unrecognised user: " + userSupplied.getUserName());
			}
		} catch (NoSuchAlgorithmException e) {
			throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "Unrecognised hashing algorithm");
		}
		
		traceLogger.log(5, CookieAuthenticationService.class.getName(), "... ... successfully found user with username: " + userSupplied.getUserName() + ", now checking supplied password");
		
		//Checks to see if the user object supplied and a user object found from that login are the same
		//if they are then it's valid login
		
		//NB: equals() has been overridden in UserPasswordWrapper
		//it checks the usernames and MD5Hex versions of the passwords
		
		if (userSupplied.equals(userLookedUp)) {
			traceLogger.log(5, CookieAuthenticationService.class.getName(), "... ... authenticated login (" + userSupplied + " == " +userLookedUp + ")");
			return;
		} else {
			throw new SOAPException(Constants.FAULT_CODE_PROTOCOL,"Authentication failed, cannot authenticate password of user " + userSupplied.getUserName());
		}
	}
	
	//this coulld be altered so that it looks up the user from a database, given a username
	private static UserLogin lookupUserLogin(String username) throws NoSuchAlgorithmException {
		//read the password from the mapping
		PersistentMap passwordMapStore = PersistanceController.getInstance().getPasswordMapStore();
		String password = passwordMapStore.getProperty(username);
		
		if (password == null) {
			return null;
		}
		
		//return the looked up user as a UserLogin object
		return new UserLogin(username, password, false);
	}
	
	public static void main(String a[]) {
		//test of authentication using data supplied by Perl client
		String cookieValue = CookieUtils.COOKIE_STRING_START + "%2561%2567%2565%256e%2574%253a%253a%2539%2538%2563%2536%2566%2532%2566%2563%2531%2566%2531%2534%2539%2566%2562%2532%2564%2561%2538%2564%2538%2533%2534%2532%2561%2561%2537%2538%2564%2531%2532%2535";
		 
		try {
			CookieAuthenticationService.authenticateCookieValue(cookieValue);
			System.out.println("successfully authenticated");
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}
}
