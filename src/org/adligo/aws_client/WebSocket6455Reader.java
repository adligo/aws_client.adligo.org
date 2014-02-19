package org.adligo.aws_client;

import java.io.IOException;
import java.io.InputStream;

import org.adligo.aws_client.models.MaskingKey;
import org.adligo.aws_client.models.Opcode6455;
import org.adligo.aws_client.models.WebSocket6455FrameMutant;
import org.adligo.i.log.shared.Log;
import org.adligo.i.log.shared.LogFactory;
import org.adligo.models.params.shared.EightBit;

/**
 * This will read a the next frame from a InputStream
 * //TODO extract code from WebSocketClient
 * 
 * @author scott
 *
 */
public class WebSocket6455Reader implements Runnable, I_WebSocketReader {
	private static final Log log = LogFactory.getLog(WebSocket6455Reader.class);
	
	public static final int MAX_FRAME_SIZE = 65536;
	private InputStream input;
	private I_WebSocketFrameHandler handler = new WebSocket6455FrameHandler();
	
	
	private boolean reading = false;
	private boolean shutdown = false;
	private volatile boolean readingFrame = false;
	private volatile boolean readingMessage = false;
	
	public void run() {
		while (!shutdown) {
			if (reading) {
				try {
					readFrame();
				} catch (IOException x) {
					handler.onIoDisconnect();
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException x) {
					//do nothing;
				}
			}
		}
	}
		
	public void readFrame() throws IOException {
		WebSocket6455FrameMutant frame = readNextFrameHeaderThroughOpcode();
		if (frame == null) {
			reading = false;
			handler.onIoDisconnect();
		} else {
			if (!readFrameLengthMaskAndPayload(frame)) {
				reading = false;
				handler.onIoDisconnect();
			} else {
				handler.handle(frame);
			}
		}
	}
	
	private boolean readFrameLengthMaskAndPayload(WebSocket6455FrameMutant frame)
			throws IOException {
		byte [] bytes = new byte[1];
		
		if (input.read(bytes) == -1) {
			return false;
		} else {
			byte b = bytes[0];
			EightBit eb = new EightBit(b);
			frame.setMask(eb.getSlotZero());
			eb.setSlotZero(false);
			long payloadLength = eb.unsigned();
			
			if (payloadLength == 126) {
				payloadLength = readExtendedLength(2);
			} else if (payloadLength == 127) {
				payloadLength = readExtendedLength(8);
			}
			if (!readMask(frame)) {
				return false;
			}
			if (payloadLength <= MAX_FRAME_SIZE) {
				byte []  bs = new byte[(int) payloadLength];
				if (input.read(bs) == -1) {
					return false;
				} else {
					frame.setPayloadData(bs);
				}
			} else if (payloadLength > 65536) {
				sendDataToNeverNeverLand(payloadLength);
			}
		}
		return true;
	}
	/**
	 * Receives the next data frame.
	 * @return The received data. a byte[0] if there was a io issue
	 */
	private WebSocket6455FrameMutant readNextFrameHeaderThroughOpcode()  throws IOException {
		if (reading) {
			byte [] bytes = new byte[1];
			readingFrame = false;
			if (input.read(bytes) == -1) {
				return null;
			} else {
				readingFrame = true;
				WebSocket6455FrameMutant frame = new WebSocket6455FrameMutant();
				
				byte b = bytes[0];
				EightBit eb = new EightBit(b);
				frame.setFin(eb.getSlotZero());
				if (!frame.isFin()) {
					readingMessage = true;
				} else {
					readingMessage = false;
				}
				frame.setRsv1(eb.getSlotOne());
				frame.setRsv2(eb.getSlotTwo());
				frame.setRsv3(eb.getSlotThree());
				
				String ozs = eb.toOnesAndZeros();
				ozs = ozs.substring(4, 8);
				eb = new EightBit(ozs);
				
				int nbr = eb.unsigned();
				Opcode6455 opcode = Opcode6455.getForNumber(nbr);
				if (opcode != null) {
					frame.setOpcode(opcode);
				}
				return frame;
			}
		}
		return null;
	}


	/**
	 * the data was to large so just read it in
	 * to skip over it, not sure why jetty would be sending me
	 * frames this large, but I don't want em!
	 * 
	 * @param payloadLength
	 * @return
	 * @throws IOException
	 */
	private boolean sendDataToNeverNeverLand(long payloadLength)
			throws IOException {
		long read = 0;
		boolean readChunks = true;
		byte []  bs = new byte[(int) 65536];
		while (read < payloadLength) {
			if (input.read(bs) == -1) {
				return false;
			} else {
				if (readChunks) {
					read += 65536;
				}
				long left = payloadLength - read;
				if (left <= 65536) {
					bs = new byte[(int) left];
					readChunks = false;
					//one more time through loop
				}
			}
		}
		return true;
	}


	/**
	 * 
	 * @param size
	 * @return null on read error
	 * @throws IOException
	 */
	private Long readExtendedLength(int size) throws IOException {
		byte [] extendedLengthBytes = new byte[size];
		if (input.read(extendedLengthBytes) == -1) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				EightBit eb = new EightBit(extendedLengthBytes[i]);
				sb.append(eb.toOnesAndZeros());
			}
			String ozs = sb.toString();
			return Long.parseLong(ozs, size);
		}
	}

	
	/**
	 * @param frame
	 * @return false for a read error (aka read returns -1)
	 * @throws IOException
	 */
	private boolean readMask(WebSocket6455FrameMutant frame) throws IOException {
		if (frame.isMask()) {
			byte [] maskBytes = new byte[4];
			if (input.read(maskBytes) == -1) {
				return false;
			} else {
				MaskingKey key = new MaskingKey(maskBytes);
				frame.setMaskingKey(key);
			}
		}
		return true;
	}

	@Override
	public synchronized void setInputStream(InputStream is) {
		input = is;
	}

	@Override
	public InputStream getInputStream() {
		return input;
	}

	@Override
	public void setFrameHandler(I_WebSocketFrameHandler p) {
		handler = p;
	}

	@Override
	public I_WebSocketFrameHandler getFrameHandler() {
		return handler;
	}

	public synchronized boolean isReading() {
		return reading;
	}

	public synchronized void setReading(boolean p) {
		if (!p) {
			//hey stop reading
			while (readingMessage || readingFrame) {
				//but I'm in the middle of reading a message or a frame
				// so wait until message/frame is complete
				try {
					Thread.sleep(1000);
				} catch (InterruptedException x) {
					log.error(x.getMessage(), x);
				}
			}
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException x) {
				//note this is debug because there isnt' much you can do , close it again?
				if (log.isDebugEnabled()) {
					log.debug(x.getMessage(), x);
				}
			}
		}
		this.reading = p;
	}

	public synchronized boolean isShutdown() {
		return shutdown;
	}

	public synchronized void shutdown() {
		setReading(false);
		shutdown = true;
	}
}
