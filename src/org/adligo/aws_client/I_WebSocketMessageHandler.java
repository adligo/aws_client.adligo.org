package org.adligo.aws_client;

public interface I_WebSocketMessageHandler {
	public void onMessage(String p);
	public void onMessage(byte [] p);
}
