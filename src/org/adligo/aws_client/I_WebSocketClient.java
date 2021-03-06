package org.adligo.aws_client;

import java.util.List;

import org.adligo.i.util.shared.I_Listener;

import java.io.IOException;

public interface I_WebSocketClient {

	/**
	 * Establishes the connection.
	 */
	public abstract void connect() throws IOException;

	/**
	 * Sends the specified string as a data frame.
	 * @param str The string to send.
	 * @throws java.io.IOException
	 */
	public abstract void send(String str) throws IOException;
	public void send(byte [] bytes) throws IOException;
	
	/**
	 * disconnects from the server, in a way that may be reconnected later
	 * any IOExceptions will be sent to debug
	 *  here (what could the client do with them anyway if it's closing)
	 *  close them again ????
	 */
	public abstract void disconnect();
	/**
	 * shuts down the web socket client for good
	 * may not be reconnected after shutdown
	 */
	public abstract void shutdown();
	
	public abstract void addListener(I_Listener listener);

	public abstract void removeListener(I_Listener listener);

	public abstract List<I_Listener> getListeners();
	
	public boolean isOpen();

}