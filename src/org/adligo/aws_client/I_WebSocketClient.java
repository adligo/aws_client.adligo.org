package org.adligo.aws_client;

import java.util.List;

import org.adligo.i.util.client.I_Listener;

public interface I_WebSocketClient {

	/**
	 * Establishes the connection.
	 */
	public abstract void connect() throws java.io.IOException;

	/**
	 * Sends the specified string as a data frame.
	 * @param str The string to send.
	 * @throws java.io.IOException
	 */
	public abstract void send(String str) throws java.io.IOException;


	/**
	 * disconnects from the server
	 * any IOExceptions will be sent to debug
	 *  here (what could the client do with them anyway if it's closing)
	 *  close them again ????
	 */
	public abstract void disconnect();

	public abstract void addListener(I_Listener listener);

	public abstract void removeListener(I_Listener listener);

	public abstract List<I_Listener> getListeners();

}