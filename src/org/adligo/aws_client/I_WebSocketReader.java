package org.adligo.aws_client;

import java.io.InputStream;

public interface I_WebSocketReader extends Runnable {
	public void setFrameHandler(I_WebSocketFrameHandler p);
	public I_WebSocketFrameHandler getFrameHandler();
	public void setInputStream(InputStream is);
	public InputStream getInputStream();
}
