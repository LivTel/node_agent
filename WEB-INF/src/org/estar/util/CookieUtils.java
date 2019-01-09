package org.estar.util;

import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import ngat.util.logging.LogManager;
import ngat.util.logging.Logger;

import org.apache.soap.Constants;
import org.apache.soap.SOAPException;
import org.estar.dao.UserLogin;

public class CookieUtils {

	private static final String DELIMITER = "%25";
	public static final String COLON_DELIMITER = "::";
	public static final String COOKIE_STRING_START = "user=";
	
	static Logger traceLogger = LogManager.getLogger(LoggerUtil.TRACE_LOGGER_NAME);
	static Logger errorLogger = LogManager.getLogger(LoggerUtil.ERROR_LOGGER_NAME);
	
	public static String makeAuthCookieString(String user, String password) {
		
		traceLogger.log(5, CookieUtils.class.getName(),"... makeAuthCookieString(" + user + "," + password + ")");
		
		String resultCookie = "";
		String cookie = "";
		try{
			cookie = user  + COLON_DELIMITER  + HashUtil.md5Hex(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		for (int i=0; i< cookie.length(); i++ ) {
			int ord = cookie.charAt(i);
			String ordAsString = "" + Integer.toHexString(ord);
			if (ordAsString.length() == 1) {
				ordAsString = DELIMITER +"0" + ordAsString;
			} else {
				ordAsString = DELIMITER  +ordAsString;
			}
			resultCookie += ordAsString;
		}
		
		String cookieAuthValue = COOKIE_STRING_START  +resultCookie;
		traceLogger.log(5, CookieUtils.class.getName(),"... ... created: [" +cookieAuthValue + "]");  
		return cookieAuthValue;
	}
	
	//server side
	public static UserLogin decodeAuthCookieString(String cookieValue) throws SOAPException {
		
		String delimitedString = cookieValue.replaceAll(DELIMITER, ",");
		StringTokenizer st = new StringTokenizer(delimitedString, ",");
		
		String decoded = "";
		while (st.hasMoreTokens()) {
			String numAsHex = st.nextToken();

			if (!numAsHex.equals(COOKIE_STRING_START)) {
				int numAsDec;
				try {
					numAsDec = Integer.parseInt(numAsHex, 16);
					char character = (char)numAsDec;
					decoded += character;
				} catch (NumberFormatException e) {
					throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "NumberFormatException thrown in attempting to convert Hex #" + numAsHex + " to decimal");
				}
			}
		}
		String user = getUser(decoded);
		String md5HexPassword = getMD5HexEncodedPassword(decoded);
		
		try {
			return new UserLogin(user, md5HexPassword, true);
		} catch (NoSuchAlgorithmException e) {
			throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "Unrecognised hashing algorithm");
		}
	}
	
	private static String getUser(String decodedCookieValue) throws SOAPException {
		int posOfColonDelimiter = decodedCookieValue.indexOf(COLON_DELIMITER);
		if (posOfColonDelimiter == -1)
			throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "Unrecognised user/password format");
		
		return decodedCookieValue.substring(0, posOfColonDelimiter);
	}
	
	private static String getMD5HexEncodedPassword(String decodedCookieValue) throws SOAPException {
		int posOfColonDelimiter = decodedCookieValue.indexOf(COLON_DELIMITER);
		if (posOfColonDelimiter == -1)
			throw new SOAPException(Constants.FAULT_CODE_PROTOCOL, "Unrecognised user/password format");
		
		return decodedCookieValue.substring(posOfColonDelimiter +COLON_DELIMITER.length(), decodedCookieValue.length());
	}
	
	public static void main(String args[]) {
		
		//String cookieValueGenerated = makeAuthCookieString("Dominik.Martin", "RoboM1nd");
		String cookieValueGenerated = makeAuthCookieString("Dominik.Martin", "NwJE8zSm");
		
		/*
		// lines from logs.
		2015-03-17 T 22:33:11.383 UTC : org.estar.handler.CookieRequestHandler : - : ... cookie header received= user=%2550%2541%2554%2554%252f%2544%256f%256d%2569%256e%2569%256b%252e%254d%2561%2572%2574%2569%256e%253a%253a%2565%2538%2533%2538%2566%2539%2536%2532
2566%2535%2539%2534%2532%2539%2535%2534%2536%2533%2535%2564%2537%2563%2561%2532%2531%2565%2530%2566%2539%2533%2533%2530

2015-03-17 T 22:37:21.153 UTC : org.estar.handler.CookieRequestHandler : - : ... cookie header received= user=%2544%256f%256d%2569%256e%2569%256b%252e%254d%2561%2572%2574%2569%256e%253a%253a%2565%2538%2533%2538%2566%2539%2536%2532%2566%2535%2539%2534%2532
2539%2535%2534%2536%2533%2535%2564%2537%2563%2561%2532%2531%2565%2530%2566%2539%2533%2533%2530

		 */
		String cookieValueSupplied1 = "user=%2550%2541%2554%2554%252f%2544%256f%256d%2569%256e%2569%256b%252e%254d%2561%2572%2574%2569%256e%253a%253a%2565%2538%2533%2538%2566%2539%2536%25322566%2535%2539%2534%2532%2539%2535%2534%2536%2533%2535%2564%2537%2563%2561%2532%2531%2565%2530%2566%2539%2533%2533%2530";
		String cookieValueSupplied2 = "user=%2544%256f%256d%2569%256e%2569%256b%252e%254d%2561%2572%2574%2569%256e%253a%253a%2565%2538%2533%2538%2566%2539%2536%2532%2566%2535%2539%2534%25322539%2535%2534%2536%2533%2535%2564%2537%2563%2561%2532%2531%2565%2530%2566%2539%2533%2533%2530";
		
		System.err.println("cookie value gen0= " +cookieValueGenerated);
		System.err.println("cookie value sup1= " +cookieValueSupplied1);
		System.err.println("cookie value sup2= " +cookieValueSupplied2);
		
		boolean same = cookieValueSupplied1.trim().equals(cookieValueGenerated.trim()) || cookieValueSupplied2.trim().equals(cookieValueGenerated.trim());
		
		System.err.println("Same String = "+ same); 
		
		/*
		UserLogin loginSupplied = null;
		try {
			loginSupplied = decodeAuthCookieString(cookieValueSupplied);
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println(loginSupplied);
		*/
	}
}
