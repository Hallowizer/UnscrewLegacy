package com.hallowizer.unscrew.api;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TransformerException extends RuntimeException {
	private static final long serialVersionUID = -5187176269860701186L;
	
	public TransformerException(String name) {
		super(name);
	}
	
	public TransformerException(Throwable cause) {
		super(cause);
	}
	
	public TransformerException(String name, Throwable cause) {
		super(name, cause);
	}
}
