package org.adligo.aws_client;

import org.adligo.i.util.shared.Event;

public class DefaultMessageHandler implements I_WebSocketMessageHandler {
	private WebSocketMessageListeners listeners;

	@Override
	public void onMessage(String p) {
		Event e = new Event();
		e.setValue(p);
		listeners.sendEvent(e);
	}

	@Override
	public void onMessage(byte[] p) {
		Event e = new Event();
		e.setValue(p);
		listeners.sendEvent(e);
	}
	
}
