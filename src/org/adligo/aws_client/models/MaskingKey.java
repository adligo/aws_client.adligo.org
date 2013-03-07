package org.adligo.aws_client.models;

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
}
