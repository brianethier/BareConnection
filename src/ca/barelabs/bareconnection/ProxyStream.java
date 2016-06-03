package ca.barelabs.bareconnection;

import java.io.OutputStream;

public class ProxyStream {

	private OutputStream outputStream;
	private ProxyStreamProperties properties;

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public ProxyStreamProperties getProperties() {
		return properties;
	}

	public void setProperties(ProxyStreamProperties properties) {
		this.properties = properties;
	}

}
