package org.adligo.aws_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface I_IO {
	/**
	 * the input stream
	 * @return
	 */
	public InputStream getInputStream() throws IOException ;
	/**
	 * the output stream
	 * @return
	 */
	public OutputStream getOutputStream() throws IOException ;
	
	/**
	 * the port of the connection (usually 80 for http and 443 for ssl)
	 * @return
	 */
	public int getPort();
	
	/**
	 * closes the underlying java.net.Socket if thats what backs this
	 */
	public void close();
}
