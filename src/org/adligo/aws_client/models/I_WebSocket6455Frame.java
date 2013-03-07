package org.adligo.aws_client.models;

public interface I_WebSocket6455Frame extends I_WebSocketFrame {

	public abstract boolean isFin();

	public abstract boolean isRsv1();

	public abstract boolean isRsv2();

	public abstract boolean isRsv3();

	public abstract Opcode6455 getOpcode();

	public abstract boolean isMask();

	public abstract byte[] getPayloadData();

	public abstract int getPayloadSize();

	public abstract MaskingKey getMaskingKey();

}