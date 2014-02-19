package org.adligo.aws_client;
/**
 * Note this was originally written by 2009 Adam MacBeth
 * the original package was com.sixfire.websocket
 * 
 * Adligo did some design work to make the client async socket listeners
 * work 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.adligo.aws_client.models.MaskingKey;
import org.adligo.i.adi.shared.InvocationException;
import org.adligo.i.adig.shared.GRegistry;
import org.adligo.i.adig.shared.I_GCheckedInvoker;
import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.i.util.shared.I_Listener;
import org.adligo.jse.util.JSECommonInit;
import org.adligo.models.params.shared.EightBit;
import org.adligo.models.params.shared.I_XMLBuilder;
import org.adligo.models.params.shared.XMLBuilder;

/**
 * An implementation of a WebSocket protocol client.
 */
public class WebSocketClient implements I_WebSocketClient {
	public static final String WEB_SOCKET_CLIENT_IS_SHUTDOWN = "WebSocketClient is Shutdown.";
	public static final String THIS_WEB_SOCKET_CLIENT_HAS_BEEN_SHUTDOWN_AND_MAY_NO_LONGER_CONNECT = "This WebSocketClient has been shutdown and may no longer connect";
	public static final String WEB_SOCKET_HAS_DISCONNECTED = "WebSocket has disconnected.";
	public static final String HANDSHAKE_NOT_COMPLETE = "Handshake not complete";
	public static final String UNSUPPORTED_PROTOCOL = "Unsupported protocol: ";
	public static final String UNSUPPORTED_PROTOCOL_2 = " must be one of  (ws, wss) for instance ws://localhost:8080/somePath ";

	private static final Log log = LogFactory.getLog(WebSocketClient.class);
	private static final I_GCheckedInvoker<URI, I_IO> IO_FACTORY = GRegistry.getCheckedInvoker(
			AwsClientInvokerNames.IO_FACTORY, URI.class, I_IO.class);
	private static final String ZERO_PAD = "0000000000000000";
	private static volatile int threadId = 0;
	private boolean shutdown = false;
	
	/** The url. */
	private URI mUrl;
	private String cookie;
	
	/** The socket. */
	private I_IO mSocket;

	/** The socket mOutput stream. */
	private OutputStream mOutput;

	/** The external headers. */
	private HashMap<String, String> mHeaders = new HashMap<String, String>();
	/**
	 * if the socket has been disconnected
	 */
	private boolean open = false;
	

	private WebSocketMessageListeners listeners = new WebSocketMessageListeners();
	private DefaultMessageHandler messageHandler = new DefaultMessageHandler();
	private I_WebSocketReader wsReader;
	private Thread readerThread = null;
	
	static {
		//init the log factory if it hasn't been done
		JSECommonInit.callLogDebug("" + WebSocketClient.class);
		AwsRegistry.setUp();
	}
	
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
		
		wsReader = config.getReader();
		I_WebSocketFrameHandler framer = wsReader.getFrameHandler();
		framer.setMessageHandler(messageHandler);
		framer.setListeners(listeners);
		framer.setClient(this);
		
		readerThread = new Thread(wsReader);
		readerThread.setName("WebSocketReader-" + threadId++);
		
	}

	/* (non-Javadoc)
	 * @see org.adligo.aws_client.I_WebSocketClient#connect()
	 */
	@Override
	public void connect() throws java.io.IOException {
		if (shutdown) {
			throw new IOException(THIS_WEB_SOCKET_CLIENT_HAS_BEEN_SHUTDOWN_AND_MAY_NO_LONGER_CONNECT);
		}
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

		try {
			mSocket = IO_FACTORY.invoke(mUrl);
		} catch (InvocationException x) {
			//convert it back to a IOException so the client only has one exception 
			//type to deal with
			IOException toThrow = new IOException(x.getMessage());
			toThrow.initCause(x);
			throw toThrow;
		}
		int port = mSocket.getPort();
		if (port != 80) {
			host = host + ":" + port;
		}

		mOutput = mSocket.getOutputStream();
		StringBuffer extraHeaders = new StringBuffer();
		if (mHeaders != null) {
			for (Entry<String, String> entry : mHeaders.entrySet()) {
				String header = entry.getKey() + ": " + entry.getValue() + "\r\n";
				extraHeaders.append(header);				
			}
		}

		String key = genKey();
		
		String request = "GET "+path+" HTTP/1.1\r\n" +
		         	     "Upgrade: WebSocket\r\n" +
		         	     "Connection: Upgrade\r\n" +
		         	     "Host: "+host+"\r\n" +
		         	     "Sec-WebSocket-Version: 13\r\n" +
		         	     "Sec-WebSocket-Key: " + key + "\r\n" +
		         	     "Sec-WebSocket-Origin: "+origin+"\r\n" +
		         	     "Sec-WebSocket-Protocol: chat\r\n" +
		         	     "Origin: "+origin+"\r\n" +
		         	     extraHeaders.toString() +
		         	     "\r\n";
		mOutput.write(request.getBytes());
		mOutput.flush();

		InputStream input = mSocket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String header = reader.readLine();
		if (!header.equals("HTTP/1.1 101 Switching Protocols")) {
			throw new IOException("Invalid handshake response '" + header + 
					"' should be 'HTTP/1.1 101 Switching Protocols'" );
		}
		header = reader.readLine();
		while (header.contains("Set-Cookie: ")) {
			cookie = header.substring(12, header.length());
			header = reader.readLine();
		}
		if (!header.equals("Upgrade: WebSocket")) {
			throw new IOException("Invalid handshake response '" + header +
					"' should be 'Upgrade: WebSocket'");
		}

		header = reader.readLine();
		if (!header.equals("Connection: Upgrade")) {
			throw new IOException("Invalid handshake response '" +header +
					"' should be 'Connection: Upgrade'");
		}

		do {
			header = reader.readLine();
		} while (!header.equals(""));
		
		wsReader.setInputStream(input);
		open = true;
		
		wsReader.setReading(true);
		if (Thread.State.NEW == readerThread.getState()) {
			readerThread.start();
		}
	}

	


	/* (non-Javadoc)
	 * @see org.adligo.aws_client.I_WebSocketClient#send(java.lang.String)
	 */
	@Override
	public synchronized void send(String str) throws java.io.IOException {
		isAvailableToSend();
		if (log.isDebugEnabled()) {
			log.debug("sending string " + str + " which is " + str.length() + " characters.");
		}
		byte [] bytes = str.getBytes("UTF-8");
		if (log.isDebugEnabled()) {
			log.debug("sending string " + str + " which is " + bytes.length + " utf-8 bytes.");
		}
		writeBytes(bytes);
	}

	@Override
	public synchronized void send(byte [] bytes) throws java.io.IOException {
		isAvailableToSend();
		writeBytes(bytes);
	}

	private void isAvailableToSend() {
		if (shutdown) {
			throw new IllegalStateException(WEB_SOCKET_CLIENT_IS_SHUTDOWN);
		}
		if (!open) {
			throw new IllegalStateException(WEB_SOCKET_HAS_DISCONNECTED);
		}
	}
	
	private void writeBytes(byte[] bytes) throws IOException {
		//1000-0001 a text final frame
		mOutput.write(0x81);
		int len = bytes.length;
		if (len < 126) {
			EightBit eb = new EightBit(len);
			eb.setSlotZero(true);
			byte b = (byte) eb.unsigned();
			mOutput.write(b);
		} else if (len < 65536) {
			
			mOutput.write(0xFE);
			
			String binString = Integer.toBinaryString(len);
			if (binString.length() < 16) {
				String thisPad = ZERO_PAD.substring(0, 16 - binString.length());
				binString = thisPad + binString;
			}
			String first = binString.substring(0,8);
			byte b = (byte) new EightBit(first).unsigned();
			mOutput.write(b);
			
			String second = binString.substring(8,16);
			b = (byte) new EightBit(second).unsigned();
			mOutput.write(b);
			
		} else {
			throw new RuntimeException("todo text messages larger than 65536 bytes");
		}
		
		byte [] mask = MaskingKey.genMask();
		mOutput.write(mask);
		writeMaskedpayloadLength(bytes, mask);
		//mOutput.write(0xff);
		mOutput.flush();
	}


	/* (non-Javadoc)
	 * @see org.adligo.aws_client.I_WebSocketClient#disconnect()
	 */
	@Override
	public void disconnect() {
		wsReader.setReading(false);
		try {
			if (mOutput != null) {
				mOutput.close();
			}
		} catch (IOException x) {
			if (log.isDebugEnabled()) {
				log.debug(x.getMessage(), x);
			}
		}
		if (mSocket != null) {
			mSocket.close();
		}
		open = false;
	}
	
	/**
	 * code borrowed from jettys
	 * 
	 * WebSocketGeneratorRFC6455
	 * @param payloadLength
	 */
	private void writeMaskedpayloadLength(byte [] payload, byte [] mask) throws IOException {
		 int payloadLength = payload.length;
		 
		 byte [] trans = new byte[payloadLength];
		 int modulo = 0;
		 for (int i = 0; i < payloadLength; i++) {
			byte pay = payload[i]; 
			byte conv = (byte) (pay^mask[modulo++]); 
			trans[i] = conv;
			if (modulo >= 4) {
				modulo = 0;
			}
		 }
         mOutput.write(trans);
	}

	public void addListener(I_Listener listener) {
		listeners.addListener(listener);
	}

	public void removeListener(I_Listener listener) {
		listeners.removeListener(listener);
	}

	public List<I_Listener> getListeners() {
		return listeners.getListeners();
	}
	
	public static String genKey() {
		I_XMLBuilder builder = new XMLBuilder();
		byte [] sixteen = new byte[16];
		UUID uuid = UUID.randomUUID();
		String uuidString = uuid.toString();
		char [] chars = uuidString.toCharArray();
		int set = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '-') {
				//skip
			} else if (set >= 15){
				break;
			} else {
				sixteen[set++] = (byte) c;
			}
		}
		builder.appendBase64(sixteen);
		String key = builder.toXmlString();
		return key;
	}
	
	public boolean isOpen() {
		return open;
	}

	@Override
	public synchronized void shutdown() {
		shutdown = true;
		open = false;
		wsReader.shutdown();
		try {
			readerThread.join();
		} catch (InterruptedException x) {
			log.error(x.getMessage(), x);
		}
	}
}
