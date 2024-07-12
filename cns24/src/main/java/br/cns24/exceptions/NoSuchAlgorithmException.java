package br.cns24.exceptions;

public class NoSuchAlgorithmException extends RuntimeException {
	private static final long serialVersionUID = -7622879292661019088L;

	public NoSuchAlgorithmException() {
		super();
	}

	public NoSuchAlgorithmException(String str, Throwable t) {
		super(str, t);
	}

	public NoSuchAlgorithmException(String str) {
		super(str);
	}

	public NoSuchAlgorithmException(Throwable t) {
		super(t);
	}

}
