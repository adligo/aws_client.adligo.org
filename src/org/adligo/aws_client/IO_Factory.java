package org.adligo.aws_client;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.adligo.i.adi.shared.InvocationException;
import org.adligo.i.adig.shared.BaseGInvoker;
import org.adligo.i.adig.shared.I_GCheckedInvoker;

public class IO_Factory extends BaseGInvoker implements I_GCheckedInvoker<URI, I_IO>{

	public static final String UNSUPPORTED_SCHEME_2 = " must be one of (ws,wss)";
	public static final String UNSUPPORTED_SCHEME = "Unsupported scheme ";

	public IO_Factory() {
		super(URI.class, I_IO.class);
	}

	@Override
	public I_IO invoke(URI url) throws InvocationException {
		String scheme = url.getScheme();
		String host = url.getHost();

		int port = url.getPort();
		if (port == -1) {
			if (scheme.equals("wss")) {
				port = 443;
			} else if (scheme.equals("ws")) {
				port = 80;
			} else {
				throw new InvocationException(UNSUPPORTED_SCHEME + scheme + UNSUPPORTED_SCHEME_2);
			}
		}

		try {
			if (scheme.equals("wss")) {
				try {
					SocketFactory factory = SSLSocketFactory.getDefault();
					return new SocketIO(factory.createSocket(host, port));
				} catch (UnknownHostException x) {
					InvocationException ie = new InvocationException(x.getMessage());
					ie.initCause(x);
					throw ie;
				}
			 } else {
				 return new SocketIO(new Socket(host, port));
			 }
		} catch (IOException x) {
			InvocationException ie = new InvocationException(x.getMessage());
			ie.initCause(x);
			throw ie;	
		}
	}
	
}
