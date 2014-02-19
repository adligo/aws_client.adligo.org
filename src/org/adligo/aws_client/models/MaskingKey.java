package org.adligo.aws_client.models;

import java.util.UUID;

import org.adligo.models.params.shared.I_XMLBuilder;
import org.adligo.models.params.shared.XMLBuilder;

public class MaskingKey {
	private byte[] bytes = new byte[4];
	
	public MaskingKey(byte [] p_bytes) {
		for (int i = 0; i < 4; i++) {
			bytes[i] = p_bytes[i];
		}
	}
	
	public byte getByte(int p) {
		if (p >= 4 || p < 0) {
			throw new RuntimeException("getByte must take a int between 0 and 3");
		}
		return bytes[p];
	}
	

	/**
	 * 
	 * @return
	 */
	public static byte[] genMask() {
		double d = Math.random();
		String dS = "" + d;
		dS = dS.substring(3, dS.length());
		char [] chars = dS.toCharArray();
		byte [] toRet = new byte[4];
		toRet[0] = (byte) chars[0];
		toRet[0] = (byte) chars[1];
		toRet[0] = (byte) chars[2];
		toRet[0] = (byte) chars[3];
		return toRet;
	}
	
}
