package org.adligo.aws_client;
/**
 * Note this was originally written by 2009 Adam MacBeth
 * the original package was com.sixfire.websocket
 * 
 * Adligo did some design work to make the client async socket listeners
 * work 
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.adligo.i.log.client.Log;
import org.adligo.i.log.client.LogFactory;
import org.adligo.i.util.client.Event;
import org.adligo.i.util.client.I_Event;
import org.adligo.i.util.client.I_Listener;

/**
 * An implementation of a WebSocket protocol client.
 */
public class WebSocketClient {
	public static final String WEB_SOCKET_HAS_DISCONNECTED = "WebSocket has disconnected.";
	public static final String HANDSHAKE_NOT_COMPLETE = "Handshake not complete";
	public static final String UNSUPPORTED_PROTOCOL = "Unsupported protocol: ";
	public static final String UNSUPPORTED_PROTOCOL_2 = " must be one of  (ws, wss) for instance ws://localhost:8080/somePath ";

	private static final Log log = LogFactory.getLog(WebSocketClient.class);
	
	/** The url. */
	private URI mUrl;

	/** The socket. */
	private Socket mSocket;

	/** Whether the handshake is complete. */
	private boolean mHandshakeComplete;

	/** The socket input stream. */
	private InputStream mInput;

	/** The socket mOutput stream. */
	private OutputStream mOutput;

	/** The external headers. */
	private HashMap<String, String> mHeaders = new HashMap<String, String>();
	/**
	 * if the socket has been disconnected
	 */
	private boolean disconnected = false;
	/**
	 * the list of listeners that will receive data coming back from the server
	 */
	private volatile List<I_Listener> listeners = new ArrayList<I_Listener>();
	
	private WebSocketClientConfig.OUTPUT_FORMAT output_format = WebSocketClientConfig.OUTPUT_FORMAT.UTF8_STRING;

	private I_PollingDaemon daemon;
	
	private WebSocketPolledItem polledItem;
	/**
	 * Creates a new WebSocket targeting the specified URL.
	 * @param url The URL for the socket.
	 */
	public WebSocketClient(WebSocketClientConfig config) {
		URI url = config.getUrl();
		HashMap<String, String> headers = config.getHeaders();
		
		mUrl = url;
		String protocol = mUrl.getScheme();
		if (!protocol.equals("ws") && !protocol.equals("wss")) {
			throw new IllegalArgumentException(UNSUPPORTED_PROTOCOL + protocol
					+ UNSUPPORTED_PROTOCOL_2);
		}
		//defensive copy
		if (headers != null) {
			mHeaders.putAll(headers);
		}
		
		if (config.getOutputFormat() != null) {
			output_format = config.getOutputFormat();
		}
		
		polledItem = new WebSocketPolledItem(this);
		if (config.getDaemon() != null) {
			daemon = config.getDaemon();
		} 
		
	}

	/**
	 * Establishes the connection.
	 */
	public void connect() throws java.io.IOException {
		String host = mUrl.getHost();
		String path = mUrl.getPath();
		if (path.equals("")) {
			path = "/";
		}

		String query = mUrl.getQuery();
		if (query != null) {
			path = path + "?" + query;
		}

		String origin = "http://" + host;

		mSocket = createSocket();
		int port = mSocket.getPort();
		if (port != 80) {
			host = host + ":" + port;
		}

		mOutput = mSocket.getOutputStream();
		StringBuffer extraHeaders = new StringBuffer();
		if (mHeaders != null) {
			for (Entry<String, String> entry : mHeaders.entrySet()) {
				extraHeaders.append(entry.getKey() + ": " + entry.getValue() + "\r\n");				
			}
		}

		String request = "GET "+path+" HTTP/1.1\r\n" +
		         	     "Upgrade: WebSocket\r\n" +
		         	     "Connection: Upgrade\r\n" +
		         	     "Host: "+host+"\r\n" +
		         	     "Origin: "+origin+"\r\n" +
		         	     extraHeaders.toString() +
		         	     "\r\n";
		mOutput.write(request.getBytes());
		mOutput.flush();

		mInput = mSocket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(mInput));
		String header = reader.readLine();
		if (!header.equals("HTTP/1.1 101 Web Socket Protocol Handshake")) {
			throw new IOException("Invalid handshake response");
		}

		header = reader.readLine();
		if (!header.equals("Upgrade: WebSocket")) {
			throw new IOException("Invalid handshake response");
		}

		header = reader.readLine();
		if (!header.equals("Connection: Upgrade")) {
			throw new IOException("Invalid handshake response");
		}

		do {
			header = reader.readLine();
		} while (!header.equals(""));
		
		if (daemon == null) {
			DefaultPollingDaemon new_daemon = new DefaultPollingDaemon();
			new_daemon.addItem(polledItem);
			new_daemon.start();
			daemon = new_daemon;
		} else {
			daemon.addItem(polledItem);
		}

		mHandshakeComplete = true;
	}

	private Socket createSocket() throws java.io.IOException {
		String scheme = mUrl.getScheme();
		String host = mUrl.getHost();

		int port = mUrl.getPort();
		if (port == -1) {
			if (scheme.equals("wss")) {
				port = 443;
			} else if (scheme.equals("ws")) {
				port = 80;
			} else {
				throw new IllegalArgumentException("Unsupported scheme");
			}
		}

		if (scheme.equals("wss")) {
			SocketFactory factory = SSLSocketFactory.getDefault();
			return factory.createSocket(host, port);
		 } else {
			 return new Socket(host, port);
		 }
	}

	/**
	 * Sends the specified string as a data frame.
	 * @param str The string to send.
	 * @throws java.io.IOException
	 */
	public void send(String str) throws java.io.IOException {
		if (!mHandshakeComplete) {
			throw new IllegalStateException(HANDSHAKE_NOT_COMPLETE);
		}
		if (disconnected) {
			throw new IllegalStateException(WEB_SOCKET_HAS_DISCONNECTED);
		}

		mOutput.write(0x00);
		mOutput.write(str.getBytes("UTF-8"));
		mOutput.write(0xff);
		mOutput.flush();
	}

	/**
	 * Sends the specified string as a data frame.
	 * @param str The string to send.
	 * @throws java.io.IOException
	 */
	public void send(byte [] bytes) throws java.io.IOException {
		if (!mHandshakeComplete) {
			throw new IllegalStateException(HANDSHAKE_NOT_COMPLETE);
		}
		if (disconnected) {
			throw new IllegalStateException(WEB_SOCKET_HAS_DISCONNECTED);
		}
		/**
		 * note this 0000 0001 byte may be specific to Jetty
		 * I didn't check the wc3 recommendation for it
		 */
		mOutput.write(0x01);
		mOutput.write(bytes);
		mOutput.write(0xff);
		mOutput.flush();
	}
	/**
	 * Receives the next data frame.
	 * @return The received data. a byte[0] if there was a io issue
	 */
	private byte[] readNextBytes()  {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b = mInput.read();
			if ((b & 0x80) == 0x80) {
				// Skip data frame
				int len = 0;
				do {
					b = mInput.read() & 0x7f;
					len = len * 128 + b;
				} while ((b & 0x80) != 0x80);
	
				for (int i = 0; i < len; i++) {
					mInput.read();
				}
			}
	
			while (true) {
				b = mInput.read();
				if (b == 0xff) {
					break;
				}
				baos.write(b);		
			}
	
			return baos.toByteArray();
		} catch (IOException x) {
			log.error(x.getMessage(), x);
		}
		return new byte[0];
	}

	/**
	 * disconnects from the server
	 * any IOExceptions will be sent to debug
	 *  here (what could the client do with them anyway if it's closing)
	 */
	public void disconnect() {
		try {
			mInput.close();
		} catch (IOException x) {
			if (log.isDebugEnabled()) {
				log.debug(x.getMessage(), x);
			}
		}
		try {
			mOutput.close();
		} catch (IOException x) {
			if (log.isDebugEnabled()) {
				log.debug(x.getMessage(), x);
			}
		}
		try {	
			mSocket.close();
		} catch (IOException x) {
			if (log.isDebugEnabled()) {
				log.debug(x.getMessage(), x);
			}
		}
		if (daemon != null) {
			daemon.removeItem(polledItem);
		}
		disconnected = true;
	}
	
	public void addListener(I_Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(I_Listener listener) {
		listeners.remove(listener);
	}
	
	public List<I_Listener> getListeners() {
		//protect the listeners from mutation
		return Collections.unmodifiableList(listeners); 
	}
	
	void poll() {
		byte [] bytes = readNextBytes();
		if (bytes.length > 0) {
			Event e = new Event(this);
			Object data = bytes;
			if (output_format == WebSocketClientConfig.OUTPUT_FORMAT.UTF8_STRING) {
				try {
					data = new String(bytes, "UTF-8");
				} catch (UnsupportedEncodingException g) {
					e.setException(g);
				}
			}
			e.setValue(data);
			for (I_Listener listen: listeners) {
				listen.onEvent(new Event(e));
			}
		}
	}
}
