package org.adligo.aws_client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.adligo.aws_client.models.I_WebSocket6455Frame;
import org.adligo.aws_client.models.I_WebSocketFrame;
import org.adligo.aws_client.models.Opcode6455;
import org.adligo.aws_client.models.WebSocketConnectionStates;
import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.Event;

public class WebSocket6455FrameHandler implements I_WebSocketFrameHandler {
	private static final Log log = LogFactory.getLog(WebSocket6455FrameHandler.class);
	/**
	 * 1Mb default
	 */
	private int bufferSize = 2^20;
	private int messageBytesSoFar = 0;
	private ByteBuffer bb = ByteBuffer.allocate(bufferSize);
	
	private I_WebSocketMessageHandler messageHandler;
	private WebSocketMessageListeners listeners;
	private WebSocketClient client;
	
	@Override
	public void handle(I_WebSocketFrame frame) {
		I_WebSocket6455Frame iFrame = (I_WebSocket6455Frame) frame;
		Opcode6455 opcode = iFrame.getOpcode();
		
		if (opcode.isControl()) {
			switch (opcode) {
				case CLOSE:
					listeners.sendEvent(
							createEvent(WebSocketConnectionStates.CONNECTION_CLOSED));
					break;
				default:
					listeners.sendEvent(
							createEvent(WebSocketConnectionStates.CONNECTION_OPEN));
			}
		} else {
			byte [] bytes = null;
			if (iFrame.isMask()) {
				bytes = iFrame.getCleanPayloadData();
			} else {
				bytes = iFrame.getPayloadData();
			}
			bb.put(bytes, messageBytesSoFar, bytes.length);
			if (iFrame.isFin()) {
				byte [] bbFin = bb.array();
				if (opcode == Opcode6455.TEXT) {
					try {
						String data = new String(bbFin, "UTF-8");
						listeners.sendEvent(createEvent(data));
					} catch (UnsupportedEncodingException e) {
						log.error(e.getMessage(), e);
					}
				} else if (opcode == Opcode6455.BINARY) {
					listeners.sendEvent(createEvent(bbFin));
				}
			}
		}
		if (iFrame.isFin()) {
			bb.clear();
			messageBytesSoFar = 0;
		}
	}


	@Override
	public void onIoDisconnect() {
		listeners.sendEvent(
				createEvent(WebSocketConnectionStates.CONNECTION_CLOSED));
		client.disconnect();
	}

	private Event createEvent(Object data) {
		Event e = new Event(client);
		e.setValue(data);
		return e;
	}

	public I_WebSocketMessageHandler getMessageHandler() {
		return messageHandler;
	}


	public void setMessageHandler(I_WebSocketMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}


	public WebSocketClient getClient() {
		return client;
	}


	public void setClient(WebSocketClient client) {
		this.client = client;
	}


	public WebSocketMessageListeners getListeners() {
		return listeners;
	}


	public void setListeners(WebSocketMessageListeners listeners) {
		this.listeners = listeners;
	}
	
	
}
