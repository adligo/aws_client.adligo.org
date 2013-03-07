package org.adligo.aws_client.models;

public interface I_WebSocketFrame {
	/**
	 * note some web socket version do masking and other things
	 * to the payload data, this method returns the 
	 * data as a end user would expect it (unmasked or as sent by the sender)
	 * @return
	 */
	public byte[] getCleanPayloadData();
	/**
	 * turn the data in the frame
	 * into a send-able array of bytes
	 * for submission through a OutputStream to a Server
	 * (ie might do the masking)
	 * @return
	 */
	public byte [] toSendableBytes();
}
