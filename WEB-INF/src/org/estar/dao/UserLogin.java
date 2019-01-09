package org.estar.dao;

import java.security.NoSuchAlgorithmException;

import org.estar.util.HashUtil;

public class UserLogin {

	private String username, md5HexPassword;
	
	public UserLogin(String user, String password, boolean isMD5HexEncodedPassword) throws NoSuchAlgorithmException {
		this.username = user;
		if (isMD5HexEncodedPassword)
			this.md5HexPassword = password;
		else
			setMD5HexPassword(password);
	}

	public String getMD5HexPassword() {
		return md5HexPassword;
	}

	public void setMD5HexPassword(String password) throws NoSuchAlgorithmException {
		this.md5HexPassword = HashUtil.md5Hex(password);
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String user) {
		this.username = user;
	}
	
	public String toString() {
		return "[USER:" + username + ",PASSWORD:" +md5HexPassword + "]";
	}

	public boolean equals(UserLogin cfUserPasswordWrapper) {
		
		if (!this.getUserName().equals(cfUserPasswordWrapper.getUserName()))
			return false;
		
		//have to encode passwords and compare (cannot decode)
		String thisMD5Password = this.getMD5HexPassword();
		String cfMD5Password = cfUserPasswordWrapper.getMD5HexPassword();
		
		return thisMD5Password.equals(cfMD5Password);
	}
}
