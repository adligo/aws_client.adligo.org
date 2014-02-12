package org.adligo.aws_client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adligo.i.util.shared.Event;
import org.adligo.i.util.shared.I_Listener;

class WebSocketMessageListeners {
	/**
	 * the list of listeners that will receive data coming back from the server
	 */
	private volatile List<I_Listener> listeners = new ArrayList<I_Listener>();
	
	
	void sendEvent(Event p) {
		for (I_Listener listener: listeners) {
			listener.onEvent(p);
		}
	}
	
	
	public void addListener(I_Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(I_Listener listener) {
		listeners.remove(listener);
	}
	
	public  List<I_Listener> getListeners() {
		//protect the listeners from mutation
		return Collections.unmodifiableList(listeners); 
	}
}
