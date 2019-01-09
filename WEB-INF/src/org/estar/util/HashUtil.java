package org.estar.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class HashUtil {
	private static final String ALGORITHM = "MD5";
	
	public static String md5Hex(String data) throws NoSuchAlgorithmException {
		
		//convert String to char[] and invoke md5Hex(char[])
		char[] chars = data.toCharArray();
		return md5Hex(chars);
	}
	
	public static String md5Hex(char[] data) throws NoSuchAlgorithmException {
		
		//convert char[] to byte[] and invoke md5Hex(byte[])
		byte[] bytes = new byte[data.length];
		for (int i=0; i< data.length; i++) {
			bytes[i] = (byte)data[i];
		}
		return md5Hex(bytes);
	}
	
    public static String md5Hex(byte[] data) throws NoSuchAlgorithmException {
    	return new String(Hex.encodeHex(md5(data)));

    }
    
    public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        digest.reset();
        byte[] digestBytes = digest.digest(data);
        return digestBytes;
    }
}
