package org.adligo.aws_client;

import java.net.URI;
import java.util.HashMap;

public class WebSocketClientConfig {
	public enum OUTPUT_FORMAT{ BYTES, UTF8_STRING};
	
	private URI url;
	private HashMap<String, String> headers;
	private OUTPUT_FORMAT outputFormat;
	/**
	 * allows the client to pass in a shared polling daemon
	 * (back ground thread which the client must start)
	 */
	private I_PollingDaemon daemon;
	
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
	public I_PollingDaemon getDaemon() {
		return daemon;
	}
	public void setDaemon(I_PollingDaemon daemon) {
		this.daemon = daemon;
	}
	
}
