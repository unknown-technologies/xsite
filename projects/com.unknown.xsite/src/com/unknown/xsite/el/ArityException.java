package com.unknown.xsite.el;

@SuppressWarnings("serial")
public class ArityException extends ExecuteException {
	public ArityException(int expected, int actual) {
		super("invalid number of arguments: got " + actual + ", expected " + expected);
	}
}
