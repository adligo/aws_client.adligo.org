package org.adligo.aws_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;

public class SocketIO implements I_IO {
	private static final Log log = LogFactory.getLog(SocketIO.class);
	
	private Socket socket;
	
	public SocketIO(Socket p) {
		socket = p;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException{
		return socket.getOutputStream();
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return socket.getPort();
	}

	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException x) {
			if (log.isDebugEnabled()) {
				log.debug(x.getMessage(), x);
			}
		}
	}

}
