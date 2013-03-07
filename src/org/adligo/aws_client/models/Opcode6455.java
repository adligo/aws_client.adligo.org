package org.adligo.aws_client.models;

import org.adligo.models.params.client.EightBit;

public enum Opcode6455 {
	CONTINUATION(0), TEXT(1), BINARY(2), NON_CONTROL_3(3), NON_CONTROL_4(4)
	, NON_CONTROL_5(5), NON_CONTROL_6(6), NON_CONTROL_7(7), CLOSE(8), PING(9),
	PONG(10), CONTROL_11(11), CONTROL_12(12), CONTROL_13(13),
	CONTROL_14(14), CONTROL_15(15);
	private short num;
	private String onesAndZeros;
	
	private Opcode6455(int p) {
		num = (short) p;
		EightBit eb = new EightBit(num);
		onesAndZeros = eb.toOnesAndZeros().substring(4, 8);
	}
	
	public String getOnesAndZeros() {
		return onesAndZeros;
	}
	
	public short getNum() {
		return num;
	}
}
