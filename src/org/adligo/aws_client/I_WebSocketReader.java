package org.adligo.aws_client;

import java.io.InputStream;

public interface I_WebSocketReader extends Runnable {
	public void setFrameHandler(I_WebSocketFrameHandler p);
	public I_WebSocketFrameHandler getFrameHandler();
	public void setInputStream(InputStream is);
	public InputStream getInputStream();
	/**
	 * this may pause reading for a while
	 * so that it may start up again later
	 * @param p
	 */
	public void setReading(boolean p);
	public boolean isReading();
	/**
	 * stop it for good
	 */
	public void shutdown();
}
