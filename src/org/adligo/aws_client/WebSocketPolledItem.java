package org.adligo.aws_client;

/**
 * this is just to keep the poll method in the WebSocketClient package private
 * so it's not part of the WebSocketClient api
 * 
 * @author scott
 *
 */
class WebSocketPolledItem implements I_PolledItem {
	private WebSocketClient client;

	public WebSocketPolledItem(WebSocketClient p) {
		client = p;
	}
	
	@Override
	public void poll() {
		client.poll();
	}
	
}
