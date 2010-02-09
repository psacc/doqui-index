package it.doqui.index.ecmengine.mtom.server;

public class SystemException extends Exception {

	private static final long serialVersionUID = 0x4555d28716aceac7L;

	public SystemException() {
	}

	public SystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemException(String message) {
		super(message);
	}

	public SystemException(Throwable cause) {
		super(cause);
	}
}
