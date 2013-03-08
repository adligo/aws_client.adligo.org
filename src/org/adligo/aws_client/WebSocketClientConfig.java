package org.adligo.aws_client;

import java.net.URI;
import java.util.HashMap;

import org.adligo.aws_client.models.WebSocketProtocol;

public class WebSocketClientConfig {
	public enum OUTPUT_FORMAT{ BYTES, UTF8_STRING};
	
	private URI url;
	private HashMap<String, String> headers;
	private OUTPUT_FORMAT outputFormat;
	private WebSocketProtocol protocol = WebSocketProtocol.RFC6544;
	private I_WebSocketReader reader = null;
	
	public WebSocketClientConfig(WebSocketProtocol p) {
		setProtocol(p);
	}
	
	public URI getUrl() {
		return url;
	}
	public void setUrl(URI url) {
		this.url = url;
	}
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	public OUTPUT_FORMAT getOutputFormat() {
		return outputFormat;
	}
	public void setOutputFormat(OUTPUT_FORMAT outputFormat) {
		this.outputFormat = outputFormat;
	}
	public I_WebSocketReader getReader() {
		return reader;
	}
	public void setReader(I_WebSocketReader reader) {
		this.reader = reader;
	}
	public WebSocketProtocol getProtocol() {
		return protocol;
	}
	
	public void setProtocol(WebSocketProtocol p) {
		protocol = p;
		switch (protocol) {
			default:
				reader = new WebSocket6455Reader();
		}
	}
	
}
