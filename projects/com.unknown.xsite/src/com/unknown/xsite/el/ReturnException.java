package com.unknown.xsite.el;

@SuppressWarnings("serial")
public class ReturnException extends RuntimeException {
	private final Value value;

	public ReturnException(Value value) {
		this.value = value;
	}

	public Value getValue() {
		return value;
	}
}
