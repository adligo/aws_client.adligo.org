package org.adligo.aws_client;

import org.adligo.aws_client.models.I_WebSocketFrame;

/**
 * classes that implement this should simply bridge
 * frames to messages aka events in this WebSocketClient api
 * 
 * @author scott
 *
 */
public interface I_WebSocketFrameHandler {
	public void handle(I_WebSocketFrame frame);
	public void onIoDisconnect();
	public void setMessageHandler(I_WebSocketMessageHandler p);
	public I_WebSocketMessageHandler getMessageHandler();
	public WebSocketClient getClient() ;
	/**
	 * used for the source of the events
	 * @param client
	 */
	public void setClient(WebSocketClient client);

	public WebSocketMessageListeners getListeners();
	public void setListeners(WebSocketMessageListeners listeners);
	
}
