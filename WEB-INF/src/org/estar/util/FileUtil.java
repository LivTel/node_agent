package org.estar.util;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {
	public static String getFileAsString(String filePath) throws IOException  {
		
		String fileAsString = " ";
	    FileInputStream fstream = new FileInputStream(filePath);
	    DataInputStream in = new DataInputStream(fstream);

	    while (in.available() !=0) {
	    	char charFound = (char)in.read();
	    	fileAsString += charFound;
	    }

	    in.close();
	    
	    return fileAsString;
	}
}
