package edu.msu.frib.daolog.util;

import java.util.Base64;
import java.util.Base64.Encoder;

public class Basic {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String filter = "*hgj*";
//		System.out.println(filter.replaceAll("\\*", ""));
		
		String one = "carrivea:password1";
		
		Encoder encoder = Base64.getUrlEncoder();
		
		String oneEncoded = encoder.encodeToString(one.getBytes());
		System.out.println("output: " + oneEncoded);
	}

}
